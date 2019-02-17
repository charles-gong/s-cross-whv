package com.whv.util;


import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.whv.util.ScheduleAppointment.TIME_OUT;

/**
 * Created by gonglongmin on 2018/11/19.
 */
public class CrackWhv {

    public static final Logger LOGGER = Logger.getLogger(CrackWhv.class);

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment";

    public static final Map<String, WebResponse> jsResponseMap = new ConcurrentHashMap<>();

    private static final Map<String, Integer> locationMapping = new HashMap<String, Integer>() {{
        put("guangzhou", 161);
        put("beijing", 160);
        put("chengdu", 163);
        put("shanghai", 162);
    }};

    private static final Map<String, Integer> visaCategoryMapping = new HashMap<String, Integer>() {{
        put("BE", 419);
        put("GV", 418);
        put("WHV", 416);
    }};


    private static final ArrayBlockingQueue<LoginAccount> queue = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws Exception {
        String applicantInfoPath = "";
        String loginAccountInfoPath = "";
        Integer threads = null;
        AtomicInteger stoppedAt = new AtomicInteger(0);
        if (args.length == 0) {
            threads = 1;
            stoppedAt.set(7);
        } else {
            applicantInfoPath = args[0];
            loginAccountInfoPath = args[1];
            threads = Integer.valueOf(args[2]);
        }

        loadJsCache();

        List<ApplicantInfo> applicants = loadApplicant(applicantInfoPath);

        loadLoginAccounts(loginAccountInfoPath);

        ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
        forkJoinPool.submit(() -> applicants.parallelStream().forEach(applicant -> {
            LoginAccount loginAccount = queue.peek();
            StringBuffer path = new StringBuffer("./");
            path.append(loginAccount.getName());
            File loginAccountFolder = new File(path.toString());
            if (!loginAccountFolder.exists()) {
                loginAccountFolder.mkdir();

            }
            path.append("/").append(applicant.getPassportNumber());
            File applicantFolder = new File(path.toString());
            if (!applicantFolder.exists()) {
                applicantFolder.mkdir();
            }
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            AtomicInteger currentStep = new AtomicInteger(0);
            HtmlPage currentResponse = null;
            StringBuffer token = new StringBuffer();
            String content = "";
            while (currentStep.get() != 9) {
                try {
                    if (currentStep.get() == 0) {
                        // 模拟一个浏览器
                        // 设置webClient的相关参数
                        webClient.setCssErrorHandler(new SilentCssErrorHandler());
                        //设置ajax
                        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                        //设置支持js
                        webClient.getOptions().setJavaScriptEnabled(false);

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

                        token.append(((HtmlHiddenInput) (htmlPage.getByXPath("//input[@name='__RequestVerificationToken']").get(0))).getValueAttribute());
                        currentResponse = htmlPage;
                        currentStep.set(1);
                        LOGGER.info(String.format("----[ %s ] load login page successfully!----", loginAccount.getName()));
                    }
                    HtmlPage checkPage = null;
                    StringBuffer referer = new StringBuffer();
                    if (currentStep.get() == 1) {
                        checkPage = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), loginAccount);
                        content = checkPage.asText();
                        while (content.contains("Your account has been locked, please login after 2 minutes") || checkIfReturnPageHasException(checkPage)) {
                            if (content.contains("Your account has been locked, please login after 2 minutes")) {
                                LOGGER.error("Sleep 2 minutes, account is locked.");
                                Thread.sleep(2 * 60 * 1001); // sleep 2mins
                            }
                            checkPage = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), loginAccount);
                            content = checkPage.asText();
                        }
                        currentStep.set(2);
                        currentResponse = checkPage;
                        LOGGER.info(String.format("----[ %s ] login on successfully!----", loginAccount.getName()));

                    }
                    if (currentStep.get() == 2) {
                        checkPage = selectVac(currentResponse, webClient, referer);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage;
                            LOGGER.info(String.format("----[ %s ] get select vac page successfully!----", loginAccount.getName()));
                            token.setLength(0);
                            token.append(((HtmlHiddenInput) (currentResponse.getByXPath("//input[@name='__RequestVerificationToken']").get(0))).getValueAttribute());
                            currentStep.set(3);
                        }
                    }
                    if (currentStep.get() == 3) {
                        // sending https://online.vfsglobal.com/Global-Appointment/Home/SelectVAC
                        checkPage = postSelectVac(webClient, referer.toString(), token, applicant.getLocation());
                        if (!checkIfReturnPageHasException(checkPage)) {
                            LOGGER.info(String.format("----[ %s ] post select vac page successfully!----", loginAccount.getName()));
                            currentResponse = checkPage;
                            currentStep.set(4);
                        }
                    }
                    if (currentStep.get() == 4) {
                        checkPage = getAddApplicantPage(currentResponse, webClient, referer);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            LOGGER.info(String.format("----[ %s ] get add applicant page successfully!----", loginAccount.getName()));
                            currentResponse = checkPage;
                            token.setLength(0);
                            token.append(((HtmlHiddenInput) (currentResponse.getByXPath("//input[@name='__RequestVerificationToken']").get(0))).getValueAttribute());
                            currentStep.set(5);
                        }
                    }
                    if (currentStep.get() == 5) {
                        // https://online.vfsglobal.com/Global-Appointment/Applicant/AddApplicant
                        checkPage = postAddApplicantPage(webClient, referer, token, applicant);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            LOGGER.info(String.format("----[ %s ] post applicant information for [ %s ] successfully!----", loginAccount.getName(), applicant.getFirstName()));
                            currentResponse = checkPage;
                            token.setLength(0);
                            token.append(((HtmlHiddenInput) (currentResponse.getByXPath("//input[@name='__RequestVerificationToken']").get(0))).getValueAttribute());
                            currentStep.set(6);
                        } else {
                            LOGGER.info(String.format("##########[ %s ] post applicant information for [ %s ] un-successfully!##########", loginAccount.getName(), applicant.getFirstName()));
                        }
                    }
                    String folder = path.toString();
                    if (currentStep.get() == 6) {
                        checkPage = postApplicantList(currentResponse, webClient, loginAccount, token, applicant);
                        if (!checkIfReturnPageHasException(checkPage)) {

                            FileUtils.write(new File(folder + "/" + "Final_Calendar.html"), checkPage.asXml(), "UTF-8");

                            LOGGER.info(String.format("----[ %s ] post applicant list to final calendar successfully and file has been saved to [ %s ] ----", loginAccount.getName(), path.toString()));
                            currentResponse = checkPage;
                            currentStep.set(7);
                        }
                    }

                    if (currentStep.get() == 7) {
                        checkPage = ScheduleAppointment.submitFinalCalendar(currentResponse, webClient, loginAccount);
                        FileUtils.write(new File(folder + "/" + "Confirm.html"), checkPage.asXml(), "UTF-8");
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage;
                            currentStep.set(8);
                        }
                    }
                    if (currentStep.get() == 8) {
                        checkPage = ScheduleAppointment.submitConfirmPage(currentResponse, webClient, loginAccount);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            FileUtils.write(new File(folder + "/" + "Check.html"), checkPage.asXml(), "UTF-8");
                            currentResponse = checkPage;
                            currentStep.set(9);
                            LOGGER.info(String.format("----[ %s ] with reference number [ %s ] has been submitted successfully!----", applicant.getFirstName(), applicant.getUrn()));
                            queue.put(loginAccount);
                            LOGGER.info(String.format("----[ %s ] has returned to login account list!----", loginAccount.getName()));
                        }
                    }

                } catch (FailingHttpStatusCodeException statusCodeException) {
                    if (statusCodeException.getStatusCode() == 503) {
                        LOGGER.error(String.format("Current step is [ %d ], Exception status code is [ %d ]", currentStep.get(), statusCodeException.getStatusCode()));
                        LOGGER.error(statusCodeException);
                    }
                } catch (Exception e) {
                    //TODO get logout token to log out
                    LOGGER.error(e);
                }
            }
        })).get();
    }

    private static HtmlPage selectVac(HtmlPage currentResponse, WebClient webClient, StringBuffer addApplicantUrl) throws IOException {
        /**
         * get application list page.
         */

        HtmlAnchor scheduleItem = (HtmlAnchor) currentResponse.getByXPath("//li[@class='inactive-link']/a").get(0);
        String url = "https://online.vfsglobal.com" + scheduleItem.getAttribute("href");
        addApplicantUrl.append(url);
        WebRequest webRequest = new WebRequest(new URL(addApplicantUrl.toString()));
        webRequest.setHttpMethod(HttpMethod.GET);
        webRequest.getAdditionalHeaders().put("Referer", "https://online.vfsglobal.com/Global-Appointment/Home/Index");
        webRequest.getAdditionalHeaders().put("Upgrade-Insecure-Requests", "1");
        WebResponse webResponse = webClient.loadWebResponse(webRequest);
        currentResponse = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
        return currentResponse;
    }

    private static HtmlPage postSelectVac(WebClient webClient, String addApplicantUrl, StringBuffer token, String location) throws IOException {
        WebRequest webRequest = new WebRequest(new URL("https://online.vfsglobal.com/Global-Appointment/Home/SelectVAC"));
        webRequest.setHttpMethod(HttpMethod.POST);
        webRequest.setAdditionalHeader("Referer", addApplicantUrl);
        List<NameValuePair> selectVacValuePairList = new ArrayList<>();
        selectVacValuePairList.add(new NameValuePair("__RequestVerificationToken", token.toString()));
        selectVacValuePairList.add(new NameValuePair("paraMissionId", "22"));
        selectVacValuePairList.add(new NameValuePair("paramCountryId", "11"));
        selectVacValuePairList.add(new NameValuePair("paramCenterId", ""));
        selectVacValuePairList.add(new NameValuePair("masterMissionName", "Australia"));
        selectVacValuePairList.add(new NameValuePair("masterCountryName", "China"));
        selectVacValuePairList.add(new NameValuePair("IsApplicationTypeEnabled", "False"));
        selectVacValuePairList.add(new NameValuePair("MainVisaCategoryDisplayEnabled", "False"));
        selectVacValuePairList.add(new NameValuePair("MissionCountryLocationJSON", "[{\"Id\":0,\"Name\":\"Select Visiting Country\",\"CountryJEs\":null,\"ChildMissionJEs\":null},{\"Id\":22,\"Name\":\"Australia\",\"CountryJEs\":[{\"Locations\":null,\"ShowDocumentCheckList\":false,\"MissionId\":0,\"VisaCategories\":null,\"Id\":0,\"Name\":\"Select Residing Country\"},{\"Locations\":[{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"}],\"TypeId\":0,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":0,\"Name\":\"Select Centre\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":161,\"Name\":\"Australia Visa Application Centre - Guangzhou\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":160,\"Name\":\"Australia Visa Application Centre-Beijing\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"},{\"SubVisaCategories\":null,\"Id\":416,\"Name\":\"Work and Holiday Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":163,\"Name\":\"Australia Visa Application Centre-Chengdu\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"},{\"SubVisaCategories\":null,\"Id\":416,\"Name\":\"Work and Holiday Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":162,\"Name\":\"Australia Visa Application Centre-Shanghai\"}],\"ShowDocumentCheckList\":false,\"MissionId\":0,\"VisaCategories\":null,\"Id\":11,\"Name\":\"China\"}],\"ChildMissionJEs\":[{\"ParentMissionId\":0,\"Id\":0,\"Name\":\"Select new NameValuePair(\"Sub-Mission\"}]}]"));
        selectVacValuePairList.add(new NameValuePair("MissionId", "22"));
        selectVacValuePairList.add(new NameValuePair("CountryId", "11"));
        //TODO need use location to confirm using which location id
        selectVacValuePairList.add(new NameValuePair("LocationId", locationMapping.get(location).toString()));
        selectVacValuePairList.add(new NameValuePair("LocationId", "0"));
        selectVacValuePairList.add(new NameValuePair("VisaCategoryId", "416"));
        selectVacValuePairList.add(new NameValuePair("AppointmentType", "PrimeAppointment"));
        selectVacValuePairList.add(new NameValuePair("MultiplePaymentModes", "PrepaymentAtBank"));

        webRequest.setRequestParameters(selectVacValuePairList);
        WebResponse webResponse = webClient.loadWebResponse(webRequest);

        return HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());

    }

    private static HtmlPage getAddApplicantPage(HtmlPage currentResponse, WebClient webClient, StringBuffer referer) throws IOException {
        HtmlAnchor addApplicant = (HtmlAnchor) currentResponse.getByXPath("//a[@class='submitbtn']").get(0);
        String url = "https://online.vfsglobal.com" + addApplicant.getAttribute("href");
        referer.setLength(0);
        referer.append(url);

        // sending https://online.vfsglobal.com/Global-Appointment/Applicant/AddApplicant
        WebRequest webRequest = new WebRequest(new URL(referer.toString()));
        webRequest.setAdditionalHeader("Upgrade-Insecure-Requests", "1");
        webRequest.setHttpMethod(HttpMethod.GET);
        webRequest.setAdditionalHeader("Referer", "https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList");
        WebResponse webResponse = webClient.loadWebResponse(webRequest);
        currentResponse = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
        return currentResponse;
    }

    private static HtmlPage postAddApplicantPage(WebClient webClient, StringBuffer referer, StringBuffer token, ApplicantInfo applicantInfo) throws IOException {
        WebRequest webRequest = new WebRequest(new URL("https://online.vfsglobal.com/Global-Appointment/Applicant/AddApplicant"));
        webRequest.setHttpMethod(HttpMethod.POST);
        webRequest.setAdditionalHeader("Host", "online.vfsglobal.com");
        webRequest.setAdditionalHeader("Origin", "https://online.vfsglobal.com");
        webRequest.setAdditionalHeader("Referer", referer.toString());
        webRequest.setAdditionalHeader("Upgrade-Insecure-Requests", "1");
        List<NameValuePair> nameValuePairArrayList = new ArrayList<>();
        nameValuePairArrayList.add(new NameValuePair("__RequestVerificationToken", token.toString()));
        nameValuePairArrayList.add(new NameValuePair("IsVAFValidationEnabled", "False"));
        nameValuePairArrayList.add(new NameValuePair("IsEndorsedChildEnabled", "False"));
        nameValuePairArrayList.add(new NameValuePair("NoOfEndorsedChild", "0"));
        nameValuePairArrayList.add(new NameValuePair("IsEndorsedChild", "False"));
        nameValuePairArrayList.add(new NameValuePair("EnableValidatePaymentAtCashCounter", "False"));
        nameValuePairArrayList.add(new NameValuePair("Currency", ""));
        nameValuePairArrayList.add(new NameValuePair("Amount", "0"));
        nameValuePairArrayList.add(new NameValuePair("IsAppointmentExists", "False"));
        nameValuePairArrayList.add(new NameValuePair("CancellationCount", "0"));
        nameValuePairArrayList.add(new NameValuePair("MissionId", "0"));
        nameValuePairArrayList.add(new NameValuePair("CountryId", "0"));
        nameValuePairArrayList.add(new NameValuePair("ShowNextAvailableSlot", "False"));
        nameValuePairArrayList.add(new NameValuePair("ShowLegalizationDocuments", "False"));
        nameValuePairArrayList.add(new NameValuePair("IsEndrosedChildDisclaimerEnabled", "True"));
        nameValuePairArrayList.add(new NameValuePair("EndrosedChildAge", "0"));
        nameValuePairArrayList.add(new NameValuePair("IsApplicantCompanyInfoRequired", "False"));
        nameValuePairArrayList.add(new NameValuePair("StateJsonString", "[{\"stateId\":0,\"Municipalities\":null,\"Pincodes\":null,\"Id\":0,\"Name\":\"Select nameValuePairArrayList.add(Province\"}]"));
        nameValuePairArrayList.add(new NameValuePair("CanShowAdditionalFields", "False"));
        nameValuePairArrayList.add(new NameValuePair("ApplicationTypeId", "0"));

        //TODO using applicant info to overwrite the information
        nameValuePairArrayList.add(new NameValuePair("PassportNumber", applicantInfo.getPassportNumber()));
        nameValuePairArrayList.add(new NameValuePair("DateOfBirth", applicantInfo.getDateOfBirth()));
        nameValuePairArrayList.add(new NameValuePair("PassportExpiryDate", applicantInfo.getPassportExpiryDate()));
        nameValuePairArrayList.add(new NameValuePair("NationalityId", "165"));
        nameValuePairArrayList.add(new NameValuePair("FirstName", applicantInfo.getFirstName()));
        nameValuePairArrayList.add(new NameValuePair("LastName", applicantInfo.getLastName()));
        nameValuePairArrayList.add(new NameValuePair("GenderId", applicantInfo.getGenderId()));
        nameValuePairArrayList.add(new NameValuePair("GenderId", "0"));
        nameValuePairArrayList.add(new NameValuePair("DialCode", applicantInfo.getDialCode()));
        nameValuePairArrayList.add(new NameValuePair("Mobile", applicantInfo.getMobile()));
        nameValuePairArrayList.add(new NameValuePair("EmailId", applicantInfo.getEmailId()));
        webRequest.setRequestParameters(nameValuePairArrayList);
        WebResponse webResponse = webClient.loadWebResponse(webRequest);
        return HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
    }

    private static HtmlPage postApplicantList(HtmlPage currentResponse, WebClient webClient, LoginAccount loginAccount, StringBuffer token, ApplicantInfo applicantInfo) throws IOException {
        String URN = ((HtmlElement) currentResponse.getByXPath("//div[@class='mandatory-txt']//b").get(0)).getTextContent();
        applicantInfo.setUrn(URN);

//        WebRequest webRequest = new WebRequest(new URL("https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList"));
//        webRequest.setHttpMethod(HttpMethod.POST);
//        webRequest.setAdditionalHeader("Host", "online.vfsglobal.com");
//        webRequest.setAdditionalHeader("Origin", "https://online.vfsglobal.com");
//        webRequest.setAdditionalHeader("Referer", "https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList");
//        webRequest.setAdditionalHeader("Upgrade-Insecure-Requests", "1");
//        LOGGER.info(String.format("------------Login account [ %s ] has reference number [ %s ]------------", loginAccount.getName(), URN));
//        List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>() {{
//            add(new NameValuePair("__RequestVerificationToken", token.toString()));
//            add(new NameValuePair("URN", URN));
//            add(new NameValuePair("EnablePaymentGatewayIntegration", "False"));
//            add(new NameValuePair("IsVAFValidationEnabled", "False"));
//            add(new NameValuePair("IsEndorsedChildChecked", "0"));
//            add(new NameValuePair("NoOfEndorsedChild", "0"));
//            add(new NameValuePair("IsEndorsedChild", "0"));
//        }};
//        webRequest.setRequestParameters(listNameValuePairs);
//        webClient.getOptions().setJavaScriptEnabled(true);
//        WebResponse webResponse = webClient.loadWebResponse(webRequest);
//        currentResponse = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
        LOGGER.info(String.format("------------Login account [ %s ] has reference number [ %s ]------------", loginAccount.getName(), URN));
        webClient.getOptions().setJavaScriptEnabled(true);
        HtmlPage webResponse = ((HtmlSubmitInput) currentResponse.getByXPath("//input[@class='submitbtn']").get(0)).click();
        webClient.waitForBackgroundJavaScript(TIME_OUT);
        return webResponse;
    }

    private static boolean checkIfReturnPageHasException(HtmlPage htmlPage) {
        String content = htmlPage.asText();
        if (content.contains("Exception") || content.contains("exception")) {
            return true;
        }
        return false;
    }

    private static List<ApplicantInfo> loadApplicant(String path) {
        BufferedReader bufferedReader = null;
        if (path == "") {
            bufferedReader = new BufferedReader(new InputStreamReader((CrackWhv.class.getClassLoader().getResourceAsStream("account/applicants.txt"))));
        }
        List<ApplicantInfo> applicantInfoList = new ArrayList<>();
        try {
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
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
                    applicantInfoList.add(applicantInfo);
                }
            }

        } catch (IOException e) {
            LOGGER.error(e);
        }

        return applicantInfoList;

    }


    private static void loadLoginAccounts(String path) {
        BufferedReader bufferedReader = null;
        if (path == "") {
            bufferedReader = new BufferedReader(new InputStreamReader((CrackWhv.class.getClassLoader().getResourceAsStream("account/login_account.txt"))));
            String line = "";
            try {
                while ((line = bufferedReader.readLine()) != null) {
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

    }

    private static void loadJsCache() {
        List<String> cacheFileNames = new ArrayList<>();
        cacheFileNames.add("analytics.js");
        cacheFileNames.add("bootstrap.min.js");
        cacheFileNames.add("bootstrap-datetimepicker.min.js");
        cacheFileNames.add("common.js");
        cacheFileNames.add("finalCalendar.js");
        cacheFileNames.add("fullcalendar.js");
        cacheFileNames.add("global-appointment-services.js");
        cacheFileNames.add("gtag_js.js");
        cacheFileNames.add("jquery.countdown.js");
        cacheFileNames.add("jquery.jqtransform.js");
        cacheFileNames.add("jquery.magnific-popup.min.js");
        cacheFileNames.add("jquery.modalbox-1.5.0-min.js");
        cacheFileNames.add("jquery.selectBox.js");
        cacheFileNames.add("jquery-3.3.1.min.js");
        cacheFileNames.add("jquery-migrate-3.0.1.js");
        cacheFileNames.add("jquery-ui.js");
        cacheFileNames.add("jqueryval.js");
        cacheFileNames.add("moment.min.js");
        cacheFileNames.add("SpryAccordion.js");
        cacheFileNames.parallelStream().forEach(fileName -> {
            try {
                String content = new BufferedReader(new InputStreamReader(CrackWhv.class.getClassLoader().getResourceAsStream("js/" + fileName)))
                        .lines().collect(Collectors.joining(System.lineSeparator()));
                // jquery
                List<NameValuePair> responseHeaders = new ArrayList<>();
                responseHeaders.add(new NameValuePair("content-type", "text/javascript"));
                WebResponseData data = new WebResponseData(content.getBytes("UTF-8"),
                        200, "OK", responseHeaders);
                WebResponse response = new WebResponse(data, new WebRequest(new URL("https://online.vfsglobal.com/Global-Appointment/Scripts/" + fileName)), System.currentTimeMillis());
                if (fileName.equals("gtag_js")) {
                    jsResponseMap.put("gtag/js", response);
                } else {
                    jsResponseMap.put(fileName, response);
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
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
        private String urn;

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

        public String getUrn() {
            return urn;
        }

        public void setUrn(String urn) {
            this.urn = urn;
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
