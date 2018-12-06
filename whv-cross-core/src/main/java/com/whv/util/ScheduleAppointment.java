package com.whv.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class ScheduleAppointment {

    public static final int TIME_OUT = 5 * 60 * 1000; // 5 mins

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


    private static final String ADD_APPLICANT_POST = "https://online.vfsglobal.com/Global-Appointment/Applicant/AddApplicant";

    private static final String APPLICANT_LIST_POST = "https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList";

    public static HtmlPage submitSelectCenter(HtmlPage loginResponse, WebClient webClient, String location) throws Exception {
        /*
         SetMissionAndCountry :: https://online.vfsglobal.com/Global-Appointment/Account/SetMissionAndCountry :: POST
         */
//        Connection setMissionAndCountryConnection = Jsoup.connect("https://online.vfsglobal.com/Global-Appointment/Account/SetMissionAndCountry");
//        setMissionAndCountryHeaders.put("Accept", "application/json, text/javascript, */*; q=0.01");
//        setMissionAndCountryHeaders.put("Accept-Encoding", "gzip, deflate, br");
//        setMissionAndCountryHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
//        setMissionAndCountryHeaders.put("Connection", "keep-alive");
//        setMissionAndCountryHeaders.put("Origin", "https://online.vfsglobal.com");
//        setMissionAndCountryHeaders.put("Referer", "https://online.vfsglobal.com/Global-Appointment");
//        Connection.Response setMissionAndCountryResponse = setMissionAndCountryConnection.ignoreContentType(true).method(Connection.Method.POST).timeout(TIME_OUT)
//                .headers(setMissionAndCountryHeaders)
//                .execute();
//        System.out.println(Jsoup.parse(setMissionAndCountryResponse.body()));


        /*
        https://online.vfsglobal.com/Global-Appointment/Home/SelectVAC?q=xxxxx
         */
//        Map<String, String> formDataMap = new HashMap<>();
//        String selectVACUrl = "https://online.vfsglobal.com" + Jsoup.parse(loginResponse.body()).select(".inactive-link").first().children().first().attr("href");
//
//        // 设置cookie和post上面的map数据
//        Map<String, String> selectVACHeaders = LoginAction.getCurrentHeaders(loginResponse.cookies());
//        selectVACHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//        selectVACHeaders.put("Accept-Encoding", "gzip, deflate, br");
//        selectVACHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
//        selectVACHeaders.put("Referer", "https://online.vfsglobal.com/Global-Appointment");
//        selectVACHeaders.put("Connection", "keep-alive");
//        selectVACHeaders.put("Origin", "https://online.vfsglobal.com");
//        selectVACHeaders.put("Upgrade-Insecure-Requests", "1");
//        Connection setConnection = Jsoup.connect(selectVACUrl);
//        Connection.Response selectVACResponse = setConnection.ignoreContentType(true).method(Connection.Method.POST)
//                .timeout(TIME_OUT)
//                .headers(selectVACHeaders)
//                .execute();

        HtmlAnchor scheduleItem = (HtmlAnchor) loginResponse.getByXPath("//li[@class='inactive-link']/a").get(0);
        HtmlPage selectCenterPage = scheduleItem.click();


//        Connection submitSACForm = Jsoup.connect("https://online.vfsglobal.com/Global-Appointment/Home/SelectVAC");
//        formDataMap.put("paraMissionId", "22");
//        formDataMap.put("paramCountryId", "11");
//        formDataMap.put("masterMissionName", "Australia");
//        formDataMap.put("masterCountryName", "China");
//        formDataMap.put("IsApplicationTypeEnabled", "False");
//        formDataMap.put("MainVisaCategoryDisplayEnabled", "False");
//        formDataMap.put("MissionId", "22");
//        formDataMap.put("CountryId", "11");
//        formDataMap.put("MissionCountryLocationJSON", "[{\"Id\":0,\"Name\":\"Select Visiting Country\",\"CountryJEs\":null,\"ChildMissionJEs\":null},{\"Id\":22,\"Name\":\"Australia\",\"CountryJEs\":[{\"Locations\":null,\"ShowDocumentCheckList\":false,\"MissionId\":0,\"VisaCategories\":null,\"Id\":0,\"Name\":\"Select Residing Country\"},{\"Locations\":[{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"}],\"TypeId\":0,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":0,\"Name\":\"Select Centre\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":161,\"Name\":\"Australia Visa Application Centre - Guangzhou\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":160,\"Name\":\"Australia Visa Application Centre-Beijing\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"},{\"SubVisaCategories\":null,\"Id\":416,\"Name\":\"Work and Holiday Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":163,\"Name\":\"Australia Visa Application Centre-Chengdu\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"},{\"SubVisaCategories\":null,\"Id\":416,\"Name\":\"Work and Holiday Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":162,\"Name\":\"Australia Visa Application Centre-Shanghai\"}],\"ShowDocumentCheckList\":false,\"MissionId\":0,\"VisaCategories\":null,\"Id\":11,\"Name\":\"China\"}],\"ChildMissionJEs\":[{\"ParentMissionId\":0,\"Id\":0,\"Name\":\"Select Sub-Mission\"}]}]");
//        formDataMap.put("LocationId", locationMapping.get(location).toString());
//        formDataMap.put("VisaCategoryId", visaCategoryMapping.get("WHV").toString());
//        formDataMap.put("VisaCategoryId", "416");
//        formDataMap.put("AppointmentType", "PrimeAppointment");
//        formDataMap.put("MultiplePaymentModes", "PrepaymentAtBank");
//
//
//        selectVACHeaders = LoginAction.getCurrentHeaders(selectVACResponse.cookies());
//        selectVACHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//        selectVACHeaders.put("Accept-Encoding", "gzip, deflate, br");
//        selectVACHeaders.put("Content-Type", "application/x-www-form-urlencoded");
//        selectVACHeaders.put("Host", "online.vfsglobal.com");
//        selectVACHeaders.put("Referer", selectVACUrl);
//        // 设置cookie和post上面的map数据
//        Connection.Response afterSelectCenterResponse = submitSACForm.ignoreContentType(true).method(Connection.Method.POST)
//                .timeout(TIME_OUT)
//                .headers(selectVACHeaders)
//                .data(formDataMap)
//                .execute();
        // 等待JS驱动dom完成获得还原后的网页
        webClient.waitForBackgroundJavaScript(TIME_OUT);

        ((HtmlSelect) selectCenterPage.getElementById("LocationId")).setDefaultValue(locationMapping.get(location).toString());
        ((HtmlSelect) selectCenterPage.getElementById("VisaCategoryId")).setDefaultValue(visaCategoryMapping.get("WHV").toString());

        HtmlPage afterSelectCenter = ((HtmlSubmitInput) selectCenterPage.getElementById("btnContinue")).click();
        webClient.waitForBackgroundJavaScript(TIME_OUT);


        return afterSelectCenter;
    }

    /**
     * Submit add applicant page
     * <p>
     * InfoMap contains:
     * PassportNumber: value
     * DateOfBirth: value --> format DD/MM/YY
     * PassportExpiryDate: value --> format DD/MM/YY
     * NationalityId: value --> China is 165 so hard the code
     * FirstName: value
     * LastName: value
     * GenderId: value --> 2 is female, 1 is male, 3 is others
     * Mobile: value --> phone number
     * DialCode: value --> +86 so hard the code
     * EmailId: value
     */
    public static HtmlPage submitAddApplicant(HtmlPage afterSelectCenterResponse, WebClient webClient, Map<String, String> addNewApplicantInfoMap) throws IOException {

        HtmlPage addApplicantPage = ((HtmlAnchor) afterSelectCenterResponse.getByXPath("//a[@class='submitbtn']").get(0)).click();
        webClient.waitForBackgroundJavaScript(TIME_OUT);
        ((HtmlTextInput) addApplicantPage.getElementById("PassportNumber")).setText(addNewApplicantInfoMap.get("PassportNumber"));
        ((HtmlTextInput) addApplicantPage.getElementById("DateOfBirth")).setText(addNewApplicantInfoMap.get("DateOfBirth"));
        ((HtmlTextInput) addApplicantPage.getElementById("PassportExpiryDate")).setText(addNewApplicantInfoMap.get("PassportExpiryDate"));
        ((HtmlSelect) addApplicantPage.getElementById("NationalityId")).setDefaultValue("165"); // 165 is china
        ((HtmlTextInput) addApplicantPage.getElementById("FirstName")).setText(addNewApplicantInfoMap.get("FirstName"));
        ((HtmlTextInput) addApplicantPage.getElementById("LastName")).setText(addNewApplicantInfoMap.get("LastName"));
        ((HtmlSelect) addApplicantPage.getElementById("GenderId")).setDefaultValue(addNewApplicantInfoMap.get("GenderId")); // 165 is china
        ((HtmlTextInput) addApplicantPage.getElementById("DialCode")).setText(addNewApplicantInfoMap.get("DialCode"));
        ((HtmlTextInput) addApplicantPage.getElementById("Mobile")).setText(addNewApplicantInfoMap.get("Mobile"));
        ((HtmlTextInput) addApplicantPage.getElementByName("EmailId")).setText(addNewApplicantInfoMap.get("EmailId"));

        HtmlSubmitInput submitAddApplicant = (HtmlSubmitInput) (addApplicantPage.getElementById("submitbuttonId"));

        HtmlPage afterSubmitAddApplicant = submitAddApplicant.click();
        webClient.waitForBackgroundJavaScript(TIME_OUT);


        return afterSubmitAddApplicant;
    }


    /**
     * Submit applicant list page
     *
     * @param afterAddApplicantResponse afterAddApplicantResponse|afterSelectCenterResponse
     * @return
     * @throws IOException
     */
    public static HtmlPage submitApplicantList(HtmlPage afterAddApplicantResponse, WebClient webClient) throws IOException {
        HtmlPage afterSubmitApplicantList = ((HtmlSubmitInput) afterAddApplicantResponse.getByXPath("//input[@class='submitbtn']").get(0)).click();
        webClient.waitForBackgroundJavaScript(TIME_OUT);

        return afterSubmitApplicantList;
    }

    /**
     * Get available date
     *
     * @param afterSubmitApplicantList
     * @param webClient
     * @return
     * @throws IOException
     */
    public static HtmlPage submitFinalCalendar(HtmlPage afterSubmitApplicantList, WebClient webClient) throws IOException {
        HtmlPage afterSubmitAll = null;
        while (afterSubmitApplicantList != null) {
            List<HtmlTableDataCell> tds = afterSubmitApplicantList.getByXPath("//div[@class='fc-content']//tbody//td");
            List<HtmlTableDataCell> availables = tds.parallelStream().filter(td -> td.getAttribute("style").contains("background-color: rgb(188,237,145)")).collect(Collectors.toList());
            if (availables == null || availables.size() == 0) {
                HtmlSpan htmlSpan = (HtmlSpan) afterSubmitApplicantList.getByXPath("//td[@class='fc-header-right']/span").get(0);
                afterSubmitApplicantList = htmlSpan.click();
            } else {
                availables.get(0).click();
                HtmlSubmitInput htmlSubmitInput = (HtmlSubmitInput) afterSubmitApplicantList.getElementById("btnConfirm");
                afterSubmitAll = htmlSubmitInput.click();
                webClient.waitForBackgroundJavaScript(TIME_OUT);
            }
        }


        return afterSubmitAll;
    }


}
