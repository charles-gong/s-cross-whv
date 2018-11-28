package com.whv.util;

import org.apache.logging.log4j.util.Strings;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ImageCapture {

    public static void main(String[] args) throws Exception {

        refresh(new HashMap<>(), "c3037aa88c8e410a92f7cefff98dfc1d");
    }


    public static byte[] refresh(Map<String, String> cookies, String t) throws Exception {
        Connection connection = Jsoup.connect("https://online.vfsglobal.com/Global-Appointment/DefaultCaptcha/Refresh");
        connection.header("Accept-Encoding", "gzip, deflate, br");
        connection.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
        connection.header("Connection", "keep-alive");
        connection.header("HOST", "online.vfsglobal.com");
        connection.header("Content-Length", "34");
        connection.header("Referer", "https://online.vfsglobal.com/Global-Appointment");
        connection.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
        List<String> c = new ArrayList<>();
        cookies.forEach((k, v) -> c.add(k.concat("=").concat(v)));
        connection.header("Cookie", "_ga=GA1.2.815269254.1542635131;".concat(Strings.join(c, ';')).concat(";_gid=GA1.2.526368041.1543124942; _gat=1"));
        Connection.Response res = connection.ignoreContentType(true).data("t", t).timeout(500000).method(Connection.Method.POST).execute();
        String url = "https://online.vfsglobal.com" + Jsoup.parse(res.body()).select("#CaptchaImage").attr("src");

        return getCaptchaImage(res.cookies(), url);
    }


    public static byte[] getCaptchaImage(Map<String, String> cookies, String url) throws Exception {
        Connection connection = Jsoup.connect(url);
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        connection.header("Connection", "keep-alive");
        connection.header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
        connection.header("Accept-Encoding", "gzip, deflate, br");
        connection.header("Origin", "https://online.vfsglobal.com");
        connection.header("Referer", "https://online.vfsglobal.com/Global-Appointment/Account/RegisteredLogin");
        List<String> c = new ArrayList<>();
        cookies.forEach((k, v) -> c.add(k.concat("=").concat(v)));
        connection.header("Cookie", "_ga=GA1.2.815269254.1542635131;".concat(Strings.join(c, ';')).concat(";_gid=GA1.2.526368041.1543124942; _gat=1"));
        Connection.Response res2 = connection.ignoreContentType(true).timeout(500000).method(Connection.Method.GET).execute();

        byte[] bytes = res2.bodyAsBytes();

        return bytes;
    }
}
