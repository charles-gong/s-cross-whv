package com.whv.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import javax.imageio.ImageIO;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class LoginAction {

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment/";

    public static Map<String, String> getCurrentHeaders(Map<String, String> cookies) {
        Map<String, String> headers = new HashMap<>();
        headers.put("HOST", "online.vfsglobal.com");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
        List<String> c = new ArrayList<>();
        cookies.forEach((k, v) -> c.add(k.concat("=").concat(v)));
        headers.put("Cookie", "_ga=GA1.2.815269254.1542635131;".concat(Strings.join(c, ';')).concat(";_gid=GA1.2.526368041.1543124942; _gat=1"));
        return headers;
    }


    public static Connection.Response submitLoginAction(Connection.Response previousResponse) throws Exception {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("EmailId", "glmlyf@163.com");
        dataMap.put("Password", "$Gong12081");
        Document html = Jsoup.parse(previousResponse.body());
        html.select("#ApplicantListForm").get(0).select("input[type=hidden]").forEach(element -> {
                    if (element.attr("value") != null && element.attr("value").length() > 0)
                        dataMap.put(element.attr("name"), element.attr("value"));
                }
        );

        List<Object> results = ImageCapture.refresh(previousResponse.cookies(), html.select("#CaptchaDeText").val());

        try {
            InputStream buffin = new ByteArrayInputStream((byte[]) results.get(1), 0, ((byte[]) results.get(1)).length);
            BufferedImage img = ImageIO.read(buffin);
            File outputfile = new File("/Users/gonglongmin/ij_workspace/gonglongmin/s-cross-whv/whv-cross-core/src/main/resources/captcha/" + UUID.randomUUID() + ".png");
            ImageIO.write(img, "png", outputfile);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }

        if (results.size() == 1) {
            throw new RuntimeException("Exception");
        }
        CaptchaUtil.HttpResp httpResp = RequestCaptchaApi.Predict("20500", (byte[]) results.get(1));
        if (httpResp.getRet_code() != 0) {
            // error
            throw new RuntimeException("Exception");
        } else {
            JSONObject jsonObject = JSONObject.parseObject(httpResp.getRsp_data());
            String captchaText = jsonObject.getString("result").toUpperCase();
            dataMap.put("CaptchaInputText", captchaText);
            dataMap.put("CaptchaDeText", (String) results.get(0));

            Connection connection = Jsoup.connect(LOGIN_URL);
            Map<String, String> headers = getCurrentHeaders(previousResponse.cookies());
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.put("Host", "online.vfsglobal.com");
            headers.put("Origin", "https://online.vfsglobal.com");
            headers.put("Referer", "https://online.vfsglobal.com/Global-Appointment/");

            // 设置cookie和post上面的map数据
            Connection.Response afterLogin = connection.ignoreContentType(true)
                    .timeout(ScheduleAppointment.TIME_OUT)
                    .method(Connection.Method.POST)
                    .headers(headers)
                    .data(dataMap).execute();
            return afterLogin;
        }
    }
}
