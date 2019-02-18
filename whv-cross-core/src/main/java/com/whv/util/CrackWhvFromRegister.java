package com.whv.util;


import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.whv.entity.AccountWithRegister;
import com.whv.entity.ApplicantInfo;
import com.whv.entity.LoginAccount;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.whv.util.ScheduleAppointment.TIME_OUT;

/**
 * Created by gonglongmin on 2018/11/19.
 */
public class CrackWhvFromRegister {

    public static final Logger LOGGER = Logger.getLogger(CrackWhvFromRegister.class);

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment";

    public static final Map<String, String> jsResponseMap = new ConcurrentHashMap<>();

    private static final Map<String, Integer> locationMapping = new HashMap<String, Integer>() {{
        put("guangzhou", 161);
        put("beijing", 160);
        put("shanghai", 162);
        put("chengdu", 163);
    }};

    private static final Map<String, Integer> visaCategoryMapping = new HashMap<String, Integer>() {{
        put("BE", 419);
        put("GV", 418);
        put("WHV", 416);
    }};


    private static final ArrayBlockingQueue<LoginAccount> queue = new ArrayBlockingQueue<>(100);
    private static final List<AccountWithRegister> registerInfo = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        String applicantInfoPath = "";
        String loginAccountInfoPath = "";
        Integer threads = null;
        AtomicInteger stoppedAt = new AtomicInteger(0);
        if (args.length == 0) {
            stoppedAt.set(7);
        } else {
            applicantInfoPath = args[0];
            loginAccountInfoPath = args[1];
            threads = Integer.valueOf(args[2]);
        }

        jsResponseMap.putAll(LoadHandler.loadJsCache());

        List<ApplicantInfo> applicants = LoadHandler.loadApplicant(applicantInfoPath);
        List<LoginAccount> loginAccountList = LoadHandler.loadLoginAccounts(loginAccountInfoPath);

        for (LoginAccount loadLoginAccount : loginAccountList) {
            queue.put(loadLoginAccount);
        }
        if (threads == null) {
            threads = loginAccountList.size();
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(applicants.size());
        forkJoinPool.submit(() -> applicants.parallelStream().forEach(applicant -> {
            LOGGER.info("Go for " + applicant.getFirstName());
            LoginAccount loginAccount = null;
            try {
                loginAccount = queue.take();
            } catch (Exception e) {
                LOGGER.error("Take login account from the queue occurs an error", e);
            }
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
            AtomicInteger currentStep = new AtomicInteger(0);
            HtmlPage currentResponse = null;
            StringBuffer token = new StringBuffer();
            String content = "";
            int loopCount = 0;
            WebClient webClient = null;
            while (currentStep.get() != 7) {
                try {
                    if (loopCount++ > 20) {
                        FileUtils.write(new File(loginAccount.getName() + "-" + currentStep.get() + ".txt"), currentResponse.asXml(), "UTF-8");
                        break;
                    }

                    if (currentStep.get() == 0) {
                        webClient = new WebClient(BrowserVersion.CHROME);
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
                    }
                    HtmlPage checkPage = null;
                    StringBuffer referer = new StringBuffer();
                    if (currentStep.get() == 1) {
                        StringBuffer reqId = new StringBuffer();
                        checkPage = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), loginAccount.getName(), loginAccount.getPassword(), token.toString(), reqId);
                        content = checkPage.asText();
                        while (content.contains("Your account has been locked, please login after 2 minutes")) {
                            if (content.contains("Your account has been locked, please login after 2 minutes")) {
                                LOGGER.error("Sleep 2 minutes, account is locked.");
                                Thread.sleep(2 * 60 * 1001); // sleep 2mins
                            }
                            reqId.setLength(0);
                            checkPage = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), loginAccount.getName(), loginAccount.getPassword(), token.toString(), reqId);
                            content = checkPage.asText();
                        }

                        if (content.contains("Registered Login") || content.contains("The verification words are incorrect") || checkIfReturnPageHasException(checkPage)) {
                            if (content.contains("The verification words are incorrect")) {
                                RequestCaptchaApi.Justice(reqId.toString());
                            }
                            LOGGER.error(content);
                            currentStep.set(0);
                            continue;
                        }
                        if (content == null || content.length() == 0) {
                            currentStep.set(0);
                            LOGGER.info(String.format("----[ %s ] login content is empty!----", loginAccount.getName()));
                            continue;
                        } else {
                            currentStep.set(2);
                            currentResponse = checkPage;
                            LOGGER.info(String.format("----[ %s ] login on successfully!----", loginAccount.getName()));
                        }
                    }
                    if (currentStep.get() == 2) {
                        checkPage = selectVac(currentResponse, webClient, referer);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage;
                            LOGGER.info(String.format("----[ %s ] get select vac page successfully!----", loginAccount.getName()));
                            token.setLength(0);
                            token.append(((HtmlHiddenInput) (currentResponse.getByXPath("//input[@name='__RequestVerificationToken']").get(1))).getValueAttribute());
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
                            token.append(((HtmlHiddenInput) (currentResponse.getByXPath("//input[@name='__RequestVerificationToken']").get(1))).getValueAttribute());
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
                            token.append(((HtmlHiddenInput) (currentResponse.getByXPath("//input[@name='__RequestVerificationToken']").get(1))).getValueAttribute());
                            currentStep.set(6);
                        } else {
                            LOGGER.info(String.format("##########[ %s ] post applicant information for [ %s ] un-successfully!##########", loginAccount.getName(), applicant.getFirstName()));
                        }
                    }
                    String folder = path.toString();
                    if (currentStep.get() == 6) {
                        checkPage = postApplicantList(currentResponse, webClient, loginAccount, token, applicant);
                        if (checkPage == null || applicant.getUrn() == null) {
                            currentStep.set(0);
                            LOGGER.error("-----------No Reference Number---------------");
                            continue;
                        } else if (!checkIfReturnPageHasException(checkPage)) {

                            FileUtils.write(new File(folder + "/" + "Final_Calendar.html"), checkPage.asXml(), "UTF-8");

                            LOGGER.info(String.format("----[ %s ] post applicant list to final calendar successfully and file has been saved to [ %s ] ----", loginAccount.getName(), path.toString()));
                            currentResponse = checkPage;
                            currentStep.set(7);
                        }
                    }


//                    if (currentStep.get() == 7) {
//                        AccountWithRegister accountWithRegister = new AccountWithRegister();
//                        accountWithRegister.setName(loginAccount.getName());
//                        accountWithRegister.setPassword(loginAccount.getPassword());
//                        accountWithRegister.setUrn(applicant.getUrn());
//                        registerInfo.add(accountWithRegister);
//                        LOGGER.info(String.format("Account [ %s ] processed [ %s ] for [ %s ] [ %s ]", accountWithRegister.getName(), accountWithRegister.getUrn(), applicant.getFirstName(), applicant.getLastName()));
//                        queue.put(loginAccount);
//                        count.decrementAndGet();
//                        logout(webClient, currentResponse);
//
//                    }
                    if (currentStep.get() == 7) {
                        checkPage = ScheduleAppointment.submitFinalCalendar(currentResponse, webClient, loginAccount.getName(), new AtomicBoolean(false), folder);
                        FileUtils.write(new File(folder + "/" + "Confirm.html"), checkPage.asXml(), "UTF-8");
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage;
                            currentStep.set(8);
                        }
                    }
                    if (currentStep.get() == 8) {
                        checkPage = ScheduleAppointment.submitConfirmPage(currentResponse, webClient, loginAccount.getName(), folder);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            FileUtils.write(new File(folder + "/" + "Check.html"), checkPage.asXml(), "UTF-8");
                            currentResponse = checkPage;
                            currentStep.set(9);
                            LOGGER.info(String.format("----[ %s ] with reference number [ %s ] has been submitted successfully!----", applicant.getFirstName(), applicant.getUrn()));
                            queue.put(loginAccount);
                            LOGGER.info(String.format("----[ %s ] has returned to login account list!----", loginAccount.getName()));
                        }
                    }

                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    try {
                        FileUtils.write(new File(loginAccount.getName() + "-" + currentStep.get() + ".txt"), currentResponse.asXml(), "UTF-8");
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                } catch (FailingHttpStatusCodeException statusCodeException) {
                    if (statusCodeException.getStatusCode() == 503) {
                        LOGGER.error(String.format("Current step is [ %d ], Exception status code is [ %d ]", currentStep.get(), statusCodeException.getStatusCode()), statusCodeException);
                    }
                } catch (Exception e) {
                    // never logout, the system can support only one account active
                    LOGGER.error(String.format("Login account [ %s ] throws an exception at step [ %d ]", loginAccount.getName(), currentStep.get()), e);
                }
            }
        })).get();

        FileUtils.writeLines(new File("./register_info_file.txt"), registerInfo.stream().map(accountWithRegister -> accountWithRegister.getName()
                .concat(",").concat(accountWithRegister.getPassword())
                .concat(",").concat(String.join("&", accountWithRegister.getUrn())))
                .collect(Collectors.toList()));
    }

    private static void logout(WebClient webClient, HtmlPage htmlPage) throws Exception {
        HtmlHiddenInput htmlHiddenInput = (HtmlHiddenInput) htmlPage.getByXPath("//form[@id='logoutForm']//input[@name='__RequestVerificationToken']").get(0);
        String token = htmlHiddenInput.getValueAttribute();
        WebRequest webRequest = new WebRequest(new URL("https://online.vfsglobal.com/Global-Appointment/Account/LogOff"));
        webRequest.setHttpMethod(HttpMethod.POST);
        List<NameValuePair> requestParams = new ArrayList<>();
        requestParams.add(new NameValuePair("__RequestVerificationToken", token));
        webClient.loadWebResponse(webRequest);
    }

    private static HtmlPage selectVac(HtmlPage currentResponse, WebClient webClient, StringBuffer addApplicantUrl) throws IOException {
        /**
         * get application list page.
         */
        List<HtmlElement> htmlElementList = currentResponse.getByXPath("//div[@id='Accordion1']//li[@class='inactive-link']/a");
        HtmlAnchor scheduleItem = (HtmlAnchor) htmlElementList.get(0);
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

    public static HtmlPage postAddApplicantPage(WebClient webClient, StringBuffer referer, StringBuffer token, ApplicantInfo applicantInfo) throws IOException {
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

    public static HtmlPage postApplicantList(HtmlPage currentResponse, WebClient webClient, LoginAccount loginAccount, StringBuffer token, ApplicantInfo applicantInfo) throws IOException {
        List<HtmlElement> htmlElementList = currentResponse.getByXPath("//div[@class='rightpanel']//div[@class='mandatory-txt']//b");
        if (htmlElementList == null || htmlElementList.size() == 0) {
            return null;
        }
        String URN = (htmlElementList.get(0)).getTextContent();
        applicantInfo.setUrn(URN);

        LOGGER.info(String.format("------------Login account [ %s ] has reference number [ %s ]------------", loginAccount.getName(), URN));
        webClient.getOptions().setJavaScriptEnabled(true);
        HtmlPage webResponse = ((HtmlSubmitInput) currentResponse.getByXPath("//input[@class='submitbtn']").get(0)).click();
        webClient.waitForBackgroundJavaScript(TIME_OUT);
        return webResponse;
    }

    public static boolean checkIfReturnPageHasException(HtmlPage htmlPage) {
        String content = htmlPage.asText();
        if (content.contains("Exception") || content.contains("exception")) {
            return true;
        }
        return false;
    }
}
