package com.whv.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class LoginAction {

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment/";


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

        byte[] userfile = ImageCapture.refresh(previousResponse.cookies(), html.select("#CaptchaDeText").val());

        try {
            InputStream buffin = new ByteArrayInputStream(userfile, 0, userfile.length);
            BufferedImage img = ImageIO.read(buffin);
            File outputfile = new File("/Users/gonglongmin/ij_workspace/gonglongmin/s-cross-whv/whv-cross-core/src/main/resources/" + UUID.randomUUID() + ".png");
            ImageIO.write(img, "png", outputfile);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }

        if (userfile == null) {
            throw new RuntimeException("Exception");
        }

        Map<String, Object> userfileMap = new HashMap<>();
        userfileMap.put("userfile", userfile);
        String captchaInputText = RequestCaptcha.postForCaptcha(userfileMap);
        dataMap.put("CaptchaInputText", captchaInputText);
        Connection connection = Jsoup.connect(LOGIN_URL);
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        connection.header("Connection", "keep-alive");

        // 设置cookie和post上面的map数据
        Connection.Response afterLogin = connection.ignoreContentType(true)
                .timeout(ScheduleAppointment.TIME_OUT)
                .method(Connection.Method.POST)
                .data(dataMap).cookies(previousResponse.cookies()).execute();
        return afterLogin;

    }
}
