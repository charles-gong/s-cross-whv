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


    private static final String ADD_APPLICANT_POST = "https://online.vfsglobal.com/Global-Appointment/Applicant/AddApplicant";

    private static final String APPLICANT_LIST_POST = "https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList";

    public static Connection.Response submitSelectCenter(Connection.Response loginResponse, String location) throws Exception {
        Map<String, String> formDataMap = new HashMap<>();

        String submitUrl = "https://online.vfsglobal.com" + Jsoup.parse(loginResponse.body()).select(".inactive-link").first().children().first().attr("href");

//        Document html = Jsoup.parse(loginResponse.body());
//        html.select("#VisaApplicationForm").get(0).select("input[type=hidden]").forEach(element -> {
//                    if (element.attr("value") != null && element.attr("value").length() > 0)
//                        formDataMap.put(element.attr("name"), element.attr("value"));
//                }
//        );
        Map<String, String> headers = LoginAction.getCurrentHeaders(loginResponse.cookies());
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Host", "online.vfsglobal.com");
        headers.put("Origin", "https://online.vfsglobal.com");
        headers.put("Referer", submitUrl);

        Connection connection = Jsoup.connect("https://online.vfsglobal.com/Global-Appointment/Home/SelectVAC");
        formDataMap.put("paraMissionId", "22");
        formDataMap.put("paramCountryId", "11");
        formDataMap.put("masterMissionName", "Australia");
        formDataMap.put("masterCountryName", "China");
        formDataMap.put("IsApplicationTypeEnabled", "False");
        formDataMap.put("MainVisaCategoryDisplayEnabled", "False");
        formDataMap.put("MissionId", "22");
        formDataMap.put("CountryId", "11");
        formDataMap.put("MissionCountryLocationJSON", "[{\"Id\":0,\"Name\":\"Select Visiting Country\",\"CountryJEs\":null,\"ChildMissionJEs\":null},{\"Id\":22,\"Name\":\"Australia\",\"CountryJEs\":[{\"Locations\":null,\"ShowDocumentCheckList\":false,\"MissionId\":0,\"VisaCategories\":null,\"Id\":0,\"Name\":\"Select Residing Country\"},{\"Locations\":[{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"}],\"TypeId\":0,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":0,\"Name\":\"Select Centre\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":161,\"Name\":\"Australia Visa Application Centre - Guangzhou\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":160,\"Name\":\"Australia Visa Application Centre-Beijing\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"},{\"SubVisaCategories\":null,\"Id\":416,\"Name\":\"Work and Holiday Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":163,\"Name\":\"Australia Visa Application Centre-Chengdu\"},{\"VisaCategories\":[{\"SubVisaCategories\":null,\"Id\":0,\"Name\":\"Select Purpose of Travel\"},{\"SubVisaCategories\":null,\"Id\":419,\"Name\":\"Biometrics Enrolment\"},{\"SubVisaCategories\":null,\"Id\":418,\"Name\":\"General Visa\"},{\"SubVisaCategories\":null,\"Id\":416,\"Name\":\"Work and Holiday Visa\"}],\"TypeId\":1,\"IsGratisApplicable\":false,\"IsPaymentAtVac\":false,\"IsPaymentAtBankEnabled\":false,\"IsOnlinePaymentEnabled\":false,\"Id\":162,\"Name\":\"Australia Visa Application Centre-Shanghai\"}],\"ShowDocumentCheckList\":false,\"MissionId\":0,\"VisaCategories\":null,\"Id\":11,\"Name\":\"China\"}],\"ChildMissionJEs\":[{\"ParentMissionId\":0,\"Id\":0,\"Name\":\"Select Sub-Mission\"}]}]");
        formDataMap.put("LocationId", locationMapping.get(location).toString());
        formDataMap.put("VisaCategoryId", visaCategoryMapping.get("WHV").toString());
        formDataMap.put("VisaCategoryId", "416");
        formDataMap.put("AppointmentType", "PrimeAppointment");
        formDataMap.put("MultiplePaymentModes", "PrepaymentAtBank");
        // 设置cookie和post上面的map数据
        Connection.Response afterSelectCenterResponse = connection.ignoreContentType(true).method(Connection.Method.POST)
                .timeout(TIME_OUT)
                .headers(headers)
                .data(formDataMap)
                .execute();

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
        String addApplicantUrl = "https://online.vfsglobal.com".concat(beforeAddApplicantPage.selectFirst(".submitbtn").attr("href"));
        Connection connection = Jsoup.connect(addApplicantUrl);
        Map<String, String> headers = LoginAction.getCurrentHeaders(afterSelectCenterResponse.cookies());
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Host", "online.vfsglobal.com");
        headers.put("Origin", "https://online.vfsglobal.com");
        headers.put("Referer", "https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList");
        Connection.Response afterClickAddApplicationResponse = connection.method(Connection.Method.GET)
                .timeout(TIME_OUT)
                .headers(headers)
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


        headers = LoginAction.getCurrentHeaders(afterClickAddApplicationResponse.cookies());
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Host", "online.vfsglobal.com");
        headers.put("Origin", "https://online.vfsglobal.com");
        headers.put("Referer", addApplicantUrl);
        // 设置cookie和post上面的map数据
        connection = Jsoup.connect(ADD_APPLICANT_POST);
        Connection.Response afterAddApplicantResponse = connection.ignoreContentType(true).method(Connection.Method.POST)
                .timeout(TIME_OUT)
                .headers(headers)
                .data(addApplicantFormData)
                .execute();

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

        Map<String, String> headers = LoginAction.getCurrentHeaders(afterAddApplicantResponse.cookies());
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Host", "online.vfsglobal.com");
        headers.put("Origin", "https://online.vfsglobal.com");
        headers.put("Referer", "https://online.vfsglobal.com/Global-Appointment/Applicant/ApplicantList");

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
