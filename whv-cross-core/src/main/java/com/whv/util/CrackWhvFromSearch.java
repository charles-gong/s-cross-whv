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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.whv.util.CrackWhvFromRegister.checkIfReturnPageHasException;
import static com.whv.util.LoadHandler.loadApplicant;
import static com.whv.util.LoadHandler.loadJsCache;
import static com.whv.util.LoadHandler.loadLoginAccounts;
import static com.whv.util.ScheduleAppointment.TIME_OUT;

/**
 * Created by gonglongmin on 2018/11/19.
 */
public class CrackWhvFromSearch {

    public static final Logger LOGGER = Logger.getLogger(CrackWhvFromSearch.class);

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment";

    public static final Map<String, String> jsResponseMap = new ConcurrentHashMap<>();

    public static long specifiedTime = 0;

    static {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date specifiedDate = dateFormat.parse("2019-02-18 01:00:00");
            calendar.setTime(specifiedDate);
            specifiedTime = calendar.getTimeInMillis();
            LOGGER.info("========Specified date is " + specifiedTime + "========");
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public static void main(String[] args) throws Exception {
        String accountWithRegisterPath = "";
        AtomicBoolean waiting = new AtomicBoolean(false);
        if (args.length == 0) {
        } else {
            if (args.length == 1 && args[0].equalsIgnoreCase("true")) {
                waiting.set(true);
            } else {
                accountWithRegisterPath = args[0];
            }
        }

        jsResponseMap.putAll(loadJsCache());

        List<AccountWithRegister> accountWithRegisters = LoadHandler.loadAccountWithRegister(accountWithRegisterPath);

        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<String> currentRunList = Collections.synchronizedList(new ArrayList<>());
        forkJoinPool.submit(() -> accountWithRegisters.parallelStream().forEach(accountWithRegister -> {
            while (currentRunList.contains(accountWithRegister.getName())) {
                try {
                    Thread.sleep(100000);
                    LOGGER.info(String.format("[ %s ] is processing one account, so waiting!", accountWithRegister.getName()));
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
            currentRunList.add(accountWithRegister.getName());
            StringBuffer path = new StringBuffer("./");
            path.append(accountWithRegister.getName());
            File loginAccountFolder = new File(path.toString());
            if (!loginAccountFolder.exists()) {
                loginAccountFolder.mkdir();

            }
            path.append("/").append(accountWithRegister.getUrn());
            File applicantFolder = new File(path.toString());
            if (!applicantFolder.exists()) {
                applicantFolder.mkdir();
            }
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            AtomicInteger currentStep = new AtomicInteger(0);
            HtmlPage currentResponse = null;
            StringBuffer token = new StringBuffer();
            String content = "";
            String folder = path.toString();
            while (currentStep.get() != 7) {
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
                        LOGGER.info(String.format("----[ %s ] load login page successfully!----", accountWithRegister.getName()));
                    }
                    HtmlPage checkPage = null;
                    if (currentStep.get() == 1) {
                        StringBuffer reqId = new StringBuffer();
                        checkPage = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), accountWithRegister.getName(), accountWithRegister.getPassword(), token.toString(), reqId);
                        content = checkPage.asText();
                        while (content.contains("Your account has been locked, please login after 2 minutes")) {
                            if (content.contains("Your account has been locked, please login after 2 minutes")) {
                                LOGGER.error("Sleep 2 minutes, account is locked.");
                                Thread.sleep(2 * 60 * 1001); // sleep 2mins
                            }
                            reqId.setLength(0);
                            checkPage = LoginAction.submitLoginAction(currentResponse, webClient, webClient.getCookies(new URL(LOGIN_URL)), accountWithRegister.getName(), accountWithRegister.getPassword(), token.toString(), reqId);
                            content = checkPage.asText();
                        }

                        if (content.contains("Registered Login") || content.contains("The verification words are incorrect") || checkIfReturnPageHasException(checkPage)) {
                            if (content.contains("The verification words are incorrect")) {
                                RequestCaptchaApi.Justice(reqId.toString());
                                currentStep.set(0);
                                continue;
                            }
                            if (checkIfReturnPageHasException(checkPage)) {
                                LOGGER.error(checkPage.asText());
                                Thread.sleep(2000);
                                currentStep.set(0);
                                continue;
                            } else {
                                LOGGER.error(content);
                                currentStep.set(0);
                                continue;
                            }
                        }
                        if (content == null || content.length() == 0) {
                            currentStep.set(0);
                            LOGGER.info(String.format("----[ %s ] login content is empty!----", accountWithRegister.getName()));
                            continue;
                        } else {
                            currentStep.set(2);
                            currentResponse = checkPage;
                            LOGGER.info(String.format("----[ %s ] login on successfully!----", accountWithRegister.getName()));
                        }
                    }
                    if (currentStep.get() == 2) {
                        checkPage = postSearchPage(webClient, currentResponse, accountWithRegister);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage; // this is track application page
                            currentStep.set(3);
                        }
                    }
                    if (currentStep.get() == 3) {
                        // this is application list page
                        checkPage = getApplicationList(webClient, currentResponse);
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage;
                            currentStep.set(4);
                        }
                    }
                    if (currentStep.get() == 4) {
                        checkPage = postApplicantList(webClient, currentResponse, accountWithRegister.getUrn(), accountWithRegister.getName());
                        FileUtils.write(new File(folder + "/" + "Final_Calendar.html"), checkPage.asXml(), "UTF-8");
                        LOGGER.info(String.format("----[ %s ] with reference number [ %s ], Final Calendar html has been stored successfully!----", accountWithRegister.getName(), accountWithRegister.getUrn()));
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage;
                            currentStep.set(5);
                        }
                    }
                    if (currentStep.get() == 5) {
                        checkPage = ScheduleAppointment.submitFinalCalendar(currentResponse, webClient, accountWithRegister.getName(), waiting, folder);
                        FileUtils.write(new File(folder + "/" + "Confirm.html"), checkPage.asXml(), "UTF-8");
                        LOGGER.info(String.format("----[ %s ] with reference number [ %s ], Confirm html has been stored successfully!----", accountWithRegister.getName(), accountWithRegister.getUrn()));
                        if (!checkIfReturnPageHasException(checkPage)) {
                            currentResponse = checkPage;
                            currentStep.set(6);
                        }
                    }
                    if (currentStep.get() == 6) {
                        checkPage = ScheduleAppointment.submitConfirmPage(currentResponse, webClient, accountWithRegister.getName(), folder);
                        LOGGER.info(String.format("----[ %s ] with reference number [ %s ], Check html has been stored successfully!----", accountWithRegister.getName(), accountWithRegister.getUrn()));
                        if (!checkIfReturnPageHasException(checkPage)) {
                            FileUtils.write(new File(folder + "/" + "Check.html"), checkPage.asXml(), "UTF-8");
                            currentResponse = checkPage;
                            currentStep.set(7);
                            LOGGER.info(String.format("----[ %s ] with reference number [ %s ] has been submitted successfully!----", accountWithRegister.getName(), accountWithRegister.getUrn()));
                        }
                    }

                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    try {
                        FileUtils.write(new File(accountWithRegister.getName() + "-" + currentStep.get() + ".txt"), currentResponse.asXml(), "UTF-8");
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                } catch (FailingHttpStatusCodeException statusCodeException) {
                    if (statusCodeException.getStatusCode() == 503) {
                        LOGGER.error(String.format("Current step is [ %d ], Exception status code is [ %d ]", currentStep.get(), statusCodeException.getStatusCode()), statusCodeException);
                    }
                } catch (Exception e) {
                    // never logout, the system can support only one account active
                    LOGGER.error(String.format("Login account [ %s ] throws an exception at step [ %d ]", accountWithRegister.getName(), currentStep.get()), e);
                }
            }
            currentRunList.remove(accountWithRegister.getName());
        })).get();
    }


    public static boolean checkBeijingTime(long currrentTime) {
        return currrentTime > specifiedTime;
    }

    private static HtmlPage postSearchPage(WebClient webClient, HtmlPage indexPage, AccountWithRegister accountWithRegister) throws Exception {
        // get search page
        HtmlAnchor retrieveIncompleteAppointments = (HtmlAnchor) indexPage.getByXPath("//li[@class='inactive-link']/a").get(4); // number 4 is search page.
        String url = "https://online.vfsglobal.com" + retrieveIncompleteAppointments.getAttribute("href");
//
        WebRequest webRequest = new WebRequest(new URL(url));
        webRequest.setHttpMethod(HttpMethod.GET);
        webRequest.setAdditionalHeader("Referer", "https://online.vfsglobal.com/Global-Appointment/Home/Index");
        webRequest.setAdditionalHeader("Upgrade-Insecure-Requests", "1");
        WebResponse webResponse = webClient.loadWebResponse(webRequest);

        // post search page
        HtmlPage searchPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
        List<HtmlElement> tokenList = searchPage.getByXPath("//input[@name='__RequestVerificationToken']");
        if (tokenList != null && tokenList.size() > 1) {
            String token = null;
            if (tokenList.size() == 1) {
                token = ((HtmlHiddenInput) tokenList.get(0)).getValueAttribute();
            } else {
                token = ((HtmlHiddenInput) tokenList.get(1)).getValueAttribute();
            }

            webRequest = new WebRequest(new URL("https://online.vfsglobal.com/Global-Appointment/Home/SearchAppointment"));
            webRequest.setHttpMethod(HttpMethod.POST);
            List<NameValuePair> requestParams = new ArrayList<>();
            requestParams.add(new NameValuePair("__RequestVerificationToken", token));
            requestParams.add(new NameValuePair("AURN", accountWithRegister.getUrn()));
            requestParams.add(new NameValuePair("PassportNumber", ""));
            requestParams.add(new NameValuePair("PrimaryEmailId", ""));
            requestParams.add(new NameValuePair("ContactNumber", ""));
            webRequest.setRequestParameters(requestParams);
            webRequest.setAdditionalHeader("Referer", url);
            webRequest.setAdditionalHeader("Upgrade-Insecure-Requests", "1");
            webResponse = webClient.loadWebResponse(webRequest);

            HtmlPage trackApplication = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());

            return trackApplication;
        }
        return null;
    }

    private static HtmlPage getApplicationList(WebClient webClient, HtmlPage trackApplicationPage) throws Exception {
        // go to application list page
        HtmlAnchor htmlAnchor = (HtmlAnchor) trackApplicationPage.getByXPath("//a[@class='btn']").get(0);
        return htmlAnchor.click();
    }

    private static HtmlPage postApplicantList(WebClient webClient, HtmlPage applicationListPage, String URN, String name) throws Exception {
        LOGGER.info(String.format("------------Login account [ %s ] has reference number [ %s ]------------", name, URN));
        webClient.getOptions().setJavaScriptEnabled(true);
        HtmlPage webResponse = ((HtmlSubmitInput) applicationListPage.getByXPath("//input[@class='submitbtn']").get(0)).click();
        webClient.waitForBackgroundJavaScript(TIME_OUT);
        return webResponse;

    }
}
