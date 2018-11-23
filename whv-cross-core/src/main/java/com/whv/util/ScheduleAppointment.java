package com.whv.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    private static final String SELECT_CENTER_SUBMIT_URL = "https://online.vfsglobal.com/Global-Appointment/Home/SelectVAC";

    private static final String ADD_APPLICANT_POST = "https://online.vfsglobal.com/Global-Appointment/Applicant/AddApplicant";

    private static final String APPLICANT_LIST_POST = "https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList";

    public static Connection.Response submitSelectCenter(Connection.Response loginResponse, String location) throws Exception {
        Map<String, String> formDataMap = new HashMap<>();
        formDataMap.put("MissionId", "22");
        formDataMap.put("CountryId", "11");
        formDataMap.put("LocationId", locationMapping.get(location).toString());
        formDataMap.put("VisaCategoryId", visaCategoryMapping.get("WHV").toString());
        Connection connection = Jsoup.connect(SELECT_CENTER_SUBMIT_URL);
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");

        // 设置cookie和post上面的map数据
        Connection.Response afterSelectCenterResponse = connection.ignoreContentType(true).method(Connection.Method.POST)
                .timeout(TIME_OUT)
                .headers(loginResponse.headers())
                .data(formDataMap)
                .cookies(loginResponse.cookies()).execute();

        return afterSelectCenterResponse;
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
    public static Connection.Response submitAddApplicant(Connection.Response afterSelectCenterResponse, Map<String, String> addNewApplicantInfoMap) throws IOException {
        Document beforeAddApplicantPage = Jsoup.parse(afterSelectCenterResponse.body());
        String addApplicantUrl = "https://online.vfsglobal.com".concat(beforeAddApplicantPage.selectFirst(".submitbtn").attr("href").toString());
        Connection connection = Jsoup.connect(addApplicantUrl);
        Connection.Response afterClickAddApplicationResponse = connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                .timeout(TIME_OUT)
                .headers(afterSelectCenterResponse.headers())
                .cookies(afterSelectCenterResponse.cookies())
                .execute();

        Document addApplicantPage = Jsoup.parse(afterClickAddApplicationResponse.body());
        Map<String, String> addApplicantFormData = new HashMap<>();
        addApplicantPage.select("#AddApplicantFormID").get(0).select("input[type=hidden]").forEach(element -> {
                    if (element.attr("value") != null && element.attr("value").length() > 0)
                        addApplicantFormData.put(element.attr("name"), element.attr("value"));
                }
        );
        addApplicantFormData.putAll(addNewApplicantInfoMap);
        addApplicantFormData.put("NationalityId", "165"); // 165表示中国

        // 设置cookie和post上面的map数据
        connection = Jsoup.connect(ADD_APPLICANT_POST);
        Connection.Response afterAddApplicantResponse = connection.ignoreContentType(true).method(Connection.Method.POST)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                .timeout(TIME_OUT)
                .headers(afterClickAddApplicationResponse.headers())
                .data(addApplicantFormData)
                .cookies(afterClickAddApplicationResponse.cookies()).execute();

        return afterAddApplicantResponse;
    }


    /**
     * Submit applicant list page
     *
     * @param afterAddApplicantResponse afterAddApplicantResponse|afterSelectCenterResponse
     * @return
     * @throws IOException
     */
    public static Connection.Response submitApplicantList(Connection.Response afterAddApplicantResponse) throws IOException {

        Document addApplicantPage = Jsoup.parse(afterAddApplicantResponse.body());
        Map<String, String> addApplicantFormData = new HashMap<>();
        addApplicantPage.select("#ApplicantListForm").get(0).select("input[type=hidden]").forEach(element -> {
                    if (element.attr("value") != null && element.attr("value").length() > 0)
                        addApplicantFormData.put(element.attr("name"), element.attr("value"));
                }
        );

        Connection connection = Jsoup.connect(APPLICANT_LIST_POST);
        Connection.Response afterSubmitApplicantList = connection.ignoreContentType(true).method(Connection.Method.POST)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                .timeout(TIME_OUT)
                .data(addApplicantFormData)
                .headers(afterAddApplicantResponse.headers())
                .cookies(afterAddApplicantResponse.cookies()).execute();

        return afterSubmitApplicantList;
    }
}
