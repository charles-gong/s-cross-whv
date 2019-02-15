package com.whv.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        HtmlAnchor scheduleItem = (HtmlAnchor) loginResponse.getByXPath("//li[@class='inactive-link']/a").get(0);

        webClient.getOptions().setJavaScriptEnabled(true);
        HtmlPage selectCenterPage = scheduleItem.click();
        /**
         * 当前页面有JS一定要执行
         * 可查看是不是有post／get请求
         */
        webClient.waitForBackgroundJavaScript(TIME_OUT);

        // webClient.getOptions().setJavaScriptEnabled(false);
        ((HtmlSelect) selectCenterPage.getElementById("LocationId")).setDefaultValue(locationMapping.get(location).toString());
        ((HtmlSelect) selectCenterPage.getElementById("VisaCategoryId")).setDefaultValue(visaCategoryMapping.get("WHV").toString());
        HtmlPage afterSelectCenter = ((HtmlSubmitInput) selectCenterPage.getElementById("btnContinue")).click();
        /**
         * 当前页面有JS一定要执行
         * 可查看是不是有post／get请求
         */
        webClient.waitForBackgroundJavaScript(TIME_OUT);
        webClient.getOptions().setJavaScriptEnabled(false);
//        webClient.waitForBackgroundJavaScript(TIME_OUT);

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
    public static HtmlPage submitAddApplicant(HtmlPage addApplicantPage, WebClient webClient, Map<String, String> addNewApplicantInfoMap) throws IOException {

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

    public static HtmlPage submitConfirmPage(HtmlPage afterSubmitCalendar, WebClient webClient) throws IOException {
        HtmlCheckBoxInput htmlCheckBoxInput = (HtmlCheckBoxInput) afterSubmitCalendar.getByXPath("//input[@type='checkbox']").get(0);
        htmlCheckBoxInput.click();

        //choose submit
        HtmlPage afterConfirm = null;
        List<HtmlElement> htmlElementList = afterSubmitCalendar.getByXPath("//a[@class='submitbtn']");
        if (htmlElementList != null && htmlElementList.size() > 0) {
            afterConfirm = ((HtmlAnchor) htmlElementList.get(0)).click();
        } else {
            afterConfirm = ((HtmlSubmitInput) afterSubmitCalendar.getByXPath("//input[@class='submitbtn']").get(0)).click();
        }
        webClient.waitForBackgroundJavaScript(TIME_OUT);

        return afterConfirm;
    }


}
