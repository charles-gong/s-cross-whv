package com.whv.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class LoginAction {

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment/";

    public static Map<String, String> getDataSet(Connection.Response response) throws Exception {
        //Document html = Jsoup.parse(response.body());
        Map<String, String> dataMap = new HashMap<>();
//        html.select("#ApplicantListForm").get(0).select("input[type=hidden]").forEach(element -> {
//                    if (element.attr("value") != null && element.attr("value").length() > 0)
//                        dataMap.put(element.attr("name"), element.attr("value"));
//                }
//        );
        dataMap.put("__RequestVerificationToken","UURAhvqdKozdrbr6IAYH4Gu4uvEPyk2DLf0HpBAMkbv3FcAubUOZSEpdkyFwMs03VFlp3zLva_i4vjRmbOWrbyEugP41");
        dataMap.put("CaptchaDeText","86eb565b75984c11906c2d2c11bfd1b5");
        dataMap.put("CaptchaInputText","FEBVY");
        dataMap.put("EmailId", "glmlyf@163.com");
        dataMap.put("Password", "$Gong12081");
        return dataMap;
    }

    public static Connection.Response submitLoginAction(Map<String, String> loginFormData, Connection.Response previousResponse) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection","keep-alive");
        headers.put("Cookie","_ga=GA1.2.815269254.1542635131; _gid=GA1.2.151288621.1542635131; ASP.NET_SessionId=v1thscnbkdite0xzga40jbzp; _culture=zh-CN; __RequestVerificationToken_L0dsb2JhbC1BcHBvaW50bWVudA2=z4yf5ZZ_DB6g6R0KqZs1OubBidbjMYmvmwPJf5RCAgFg7JXuXHlBbB37q7-lsVCsl9Mlfh31ps6G1PDuNujP6WN2wEY1; BIGipServerCUST100052_wynvfsrowtweb443=rd1615o00000000000000000000ffff1eb8cb1eo443; _gat=1");

        Connection connection = Jsoup.connect(LOGIN_URL);
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        Connection.Response afterLogin =connection.ignoreContentType(true).timeout(ScheduleAppointment.TIME_OUT)
                .data(loginFormData)
                .method(Connection.Method.POST)
                .headers(headers).execute();
        // 设置cookie和post上面的map数据
//        Connection.Response afterLogin = connection.ignoreContentType(true)
//                .timeout(ScheduleAppointment.TIME_OUT)
//                .method(Connection.Method.POST)
//                .headers(previousResponse.headers())
//                .data(loginFormData).cookies(previousResponse.cookies()).execute();
        System.out.println(Jsoup.parse(afterLogin.body()).toString());
        System.out.println("------------------------------------------------------------------------------------");
        return afterLogin;
    }
}
