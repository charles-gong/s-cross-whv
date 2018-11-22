package com.whv.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class LoginAction {

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment/";

    public static Map<String, String> getDataSet(Connection.Response response) throws Exception {
        Document html = Jsoup.parse(response.body());
        Map<String, String> dataMap = new HashMap<>();
        html.select("#ApplicantListForm").get(0).select("input[type=hidden]").forEach(element -> {
                    if (element.attr("value") != null && element.attr("value").length() > 0)
                        dataMap.put(element.attr("name"), element.attr("value"));
                }
        );
        dataMap.put("EmailId", "glmlyf@163.com");
        dataMap.put("Password", "$Gong12081");
        return dataMap;
    }

    public static Connection.Response submitLoginAction(Map<String, String> loginFormData, Connection.Response previousResponse) throws Exception {
        Connection connection = Jsoup.connect(LOGIN_URL);
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");

        // 设置cookie和post上面的map数据
        Connection.Response afterLogin = connection.ignoreContentType(true)
                .timeout(500 * 1000)
                .method(Connection.Method.POST)
                .headers(previousResponse.headers())
                .data(loginFormData).cookies(previousResponse.cookies()).execute();

        return afterLogin;
    }
}
