package com.whv.util;


import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by gonglongmin on 2018/11/19.
 */
public class CrackWhv {

    private static Logger LOGGER = Logger.getLogger(CrackWhv.class);

    public static final ThreadLocal<Map<String, String>> tokenThreadLocal = new ThreadLocal<Map<String, String>>() {{
        set(new HashMap<>());
    }};

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment";
    private static final ArrayBlockingQueue<LoginAccount> queue = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws Exception {
        List<ApplicantInfo> applicants = loadApplicant("./applicants.txt");

        Map<String, List<List<ApplicantInfo>>> groupedList = groupApplicantInfoList(applicants);

        loadLoginAccounts("./login_account.txt");

        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        forkJoinPool.submit(() -> groupedList.keySet().parallelStream().forEach(location -> {
            ForkJoinPool listPool = new ForkJoinPool(4);
            try {
                listPool.submit(() -> {
                    groupedList.get(location).parallelStream().forEach(applicantInfoList -> {
                        LoginAccount loginAccount = queue.peek();

                        WebClient webClient = new WebClient(BrowserVersion.CHROME);
                        AtomicInteger currentStep = new AtomicInteger(0);
                        HtmlPage currentResponse = null;
                        String token = null;
                        while (currentStep.get() != 6) {
                            try {
                                if (currentStep.get() == 0) {
                                    // 模拟一个浏览器
                                    // 设置webClient的相关参数
                                    webClient.setCssErrorHandler(new SilentCssErrorHandler());
                                    //设置ajax
                                    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                                    //设置支持js
                                    webClient.getOptions().setJavaScriptEnabled(true);

                                    webClient.getOptions().setPopupBlockerEnabled(true);
                                    //CSS渲染禁止
                                    webClient.getOptions().setCssEnabled(false);
                                    //超时时间
                                    webClient.getOptions().setTimeout(3600 * 1000);
                                    //设置js抛出异常:false
                                    webClient.getOptions().setThrowExceptionOnScriptError(false);
                                    //允许重定向
                                    webClient.getOptions().setRedirectEnabled(true);
                                    //允许cookie
                                    webClient.getCookieManager().setCookiesEnabled(true);

                                    webClient.setConfirmHandler((page, string) -> true);

                                    final List collectedAlerts = new ArrayList();
                                    webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

                                    HtmlPage htmlPage = webClient.getPage(LOGIN_URL);
                                    token = ((HtmlHiddenInput) htmlPage.getElementById("__RequestVerificationToken")).getValueAttribute();
                                    currentResponse = htmlPage;
                                    currentStep.set(1);
                                }
                                if (currentStep.get() == 1) {
                                    HtmlPage afterLogin = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), loginAccount);
                                    String content = afterLogin.asText();
                                    while (content.contains("Your account has been locked, please login after 2 minutes")) {
                                        LOGGER.error("Sleep 2 mins, account is locked.");
                                        Thread.sleep(2 * 60 * 1001); // sleep 2mins
                                        afterLogin = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), loginAccount);
                                        content = afterLogin.asText();
                                    }
                                    currentStep.set(2);
                                    currentResponse = afterLogin;
                                }
                                if (currentStep.get() == 2) {
                                    HtmlPage afterSelectCenter = ScheduleAppointment.submitSelectCenter(currentResponse, webClient, location.toLowerCase()); //TODO location needs to be provided.
                                    currentStep.set(3);
                                    currentResponse = afterSelectCenter;
                                }
                                if (currentStep.get() == 3) {
                                    Iterator<ApplicantInfo> iterator = applicantInfoList.iterator();
                                    while (iterator.hasNext()) {
                                        ApplicantInfo currentApplicationInfo = iterator.next();
                                        LOGGER.info("Adding applicant [ " + currentApplicationInfo.getFirstName() + " ] ...");
                                        HtmlPage afterAddApplicant = ScheduleAppointment.submitAddApplicant(currentResponse, webClient, currentApplicationInfo);
                                        while (afterAddApplicant.getByXPath("//input[@class='submitbtn']") == null) {
                                            LOGGER.info("Retry to add applicant [ " + currentApplicationInfo.getFirstName() + " ] ...");
                                            ScheduleAppointment.submitAddApplicant(currentResponse, webClient, currentApplicationInfo);
                                        }
                                        currentResponse = afterAddApplicant;
                                    }
                                    currentStep.set(4);
                                }
                                if (currentStep.get() == 4) {
                                    // This step can pass afterSelectCenter or afterAddApplicant
                                    HtmlPage afterSubmitApplicantList = ScheduleAppointment.submitApplicantList(currentResponse, webClient);
                                    currentStep.set(5);
                                    currentResponse = afterSubmitApplicantList;

                                }
                                if (currentStep.get() == 5) {
                                    // Press after select available day.
                                    HtmlPage afterSubmitApplicantList = ScheduleAppointment.submitFinalCalendar(currentResponse, webClient);
                                    currentStep.set(6);
                                    currentResponse = afterSubmitApplicantList;

                                }

                            } catch (Exception e) {
                                HtmlAnchor logout = (HtmlAnchor) (currentResponse.getElementById("logoutForm")).getElementsByTagName("a").get(0);
                                try {
                                    LOGGER.error(e);
                                    LOGGER.error("Logout...");
                                    logout.click();
                                } catch (Exception logoutException) {
                                    LOGGER.error(logoutException);
                                }
                            }
                        }
                    });
                }).get();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        })).get();
    }

    private static Map<String, List<List<ApplicantInfo>>> groupApplicantInfoList(List<ApplicantInfo> applicantInfoList) {
        Map<String, List<List<ApplicantInfo>>> finalMap = new HashMap<>();
        Map<String, List<ApplicantInfo>> groupedByLocation = applicantInfoList.parallelStream().collect(Collectors.groupingBy(ApplicantInfo::getLocation));
        groupedByLocation.forEach((location, list) -> {
            List<List<ApplicantInfo>> groupedList = new ArrayList<>();
            for (int i = 0; i < applicantInfoList.size(); i = i + 5) {
                groupedList.add(applicantInfoList.subList(i, i + 5 > applicantInfoList.size() ? applicantInfoList.size() : i + 5));
            }
            finalMap.put(location, groupedList);
        });

        return finalMap;
    }

    private static List<ApplicantInfo> loadApplicant(String path) {
        List<ApplicantInfo> applicantInfoList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] columns = line.trim().split(",");
                    ApplicantInfo applicantInfo = new ApplicantInfo();
                    applicantInfo.setFirstName(columns[0].trim());
                    applicantInfo.setLastName(columns[1].trim());
                    applicantInfo.setPassportNumber(columns[2].trim());
                    applicantInfo.setDateOfBirth(columns[3].trim());
                    applicantInfo.setPassportExpiryDate(columns[4].trim());
                    applicantInfo.setGenderId(columns[5].trim().equalsIgnoreCase("female") ? "2" : "1");
                    applicantInfo.setDialCode(columns[6].trim());
                    applicantInfo.setMobile(columns[7].trim());
                    applicantInfo.setEmailId(columns[8].trim());
                    applicantInfo.setLocation(columns[9].trim());
                    applicantInfo.setDay(columns[10].split("/")[0]);
                    applicantInfo.setMonth(columns[10].split("/")[1]);
                    applicantInfo.setYear(columns[10].split("/")[2]);
                    applicantInfoList.add(applicantInfo);
                }
            }

        } catch (IOException e) {
            LOGGER.error(e);
        }

        return applicantInfoList;

    }


    private static void loadLoginAccounts(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] accountInfos = line.split(",");
                LoginAccount loginAccount = new LoginAccount();
                loginAccount.setName(accountInfos[0].trim());
                loginAccount.setPassword(accountInfos[1].trim());
                queue.put(loginAccount);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }


    static class ApplicantInfo {
        private String firstName;
        private String lastName;
        private String passportNumber;
        private String dateOfBirth;
        private String passportExpiryDate;
        private String genderId;
        private String dialCode;
        private String mobile;
        private String emailId;
        private String location;
        private String day;
        private String month;
        private String year;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPassportNumber() {
            return passportNumber;
        }

        public void setPassportNumber(String passportNumber) {
            this.passportNumber = passportNumber;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getPassportExpiryDate() {
            return passportExpiryDate;
        }

        public void setPassportExpiryDate(String passportExpiryDate) {
            this.passportExpiryDate = passportExpiryDate;
        }

        public String getGenderId() {
            return genderId;
        }

        public void setGenderId(String genderId) {
            this.genderId = genderId;
        }

        public String getDialCode() {
            return dialCode;
        }

        public void setDialCode(String dialCode) {
            this.dialCode = dialCode;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmailId() {
            return emailId;
        }

        public void setEmailId(String emailId) {
            this.emailId = emailId;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }

    static class LoginAccount {

        private String name;
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
