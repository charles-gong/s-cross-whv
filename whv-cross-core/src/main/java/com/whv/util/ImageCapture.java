package com.whv.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageCapture {

    public static void main(String[] args) throws Exception {

        refresh(new HashMap<>(), "c3037aa88c8e410a92f7cefff98dfc1d");
    }


    public static List<Object> refresh(Map<String, String> cookies, String t) throws Exception {
        Connection connection = Jsoup.connect("https://online.vfsglobal.com/Global-Appointment/DefaultCaptcha/Refresh");
        Map<String,String> headers = LoginAction.getCurrentHeaders(cookies);
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Referer"," https://online.vfsglobal.com/Global-Appointment/Account/RegisteredLogin");
        connection.headers(headers);
        Connection.Response res = connection.ignoreContentType(true).data("t", t).timeout(500000).method(Connection.Method.POST).execute();
        String url = "https://online.vfsglobal.com" + Jsoup.parse(res.body()).select("#CaptchaImage").attr("src");

        return getCaptchaImage(cookies, url);
    }


    public static List<Object> getCaptchaImage(Map<String, String> cookies, String url) throws Exception {
        Connection connection = Jsoup.connect(url);
        String q = url.substring(url.lastIndexOf("=") + 1);
        connection.header("Connection", "keep-alive");
        connection.header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
        connection.header("Accept-Encoding", "gzip, deflate, br");
        connection.header("Origin", "https://online.vfsglobal.com");
        connection.header("Referer", "https://online.vfsglobal.com/Global-Appointment/Account/RegisteredLogin");
        List<String> c = new ArrayList<>();
        cookies.forEach((k, v) -> c.add(k.concat("=").concat(v)));
        connection.header("Cookie", "_ga=GA1.2.815269254.1542635131;".concat(StringUtils.join(c, ';')).concat(";_gid=GA1.2.526368041.1543124942; _gat=1"));
        Connection.Response res2 = connection.ignoreContentType(true).timeout(500000).method(Connection.Method.GET).execute();

        byte[] bytes = res2.bodyAsBytes();
        List<Object> objects = new ArrayList<>();
        objects.add(q);
        objects.add(bytes);
        return objects;
    }
}
