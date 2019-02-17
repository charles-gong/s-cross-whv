package com.whv.util;

import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class LoginAction {


    public static Map<String, String> getCurrentHeaders(Map<String, String> cookies) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "online.vfsglobal.com");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
        List<String> c = new ArrayList<>();
        cookies.forEach((k, v) -> c.add(k.concat("=").concat(v)));
        headers.put("Cookie", "_ga=GA1.2.815269254.1542635131;".concat(StringUtils.join(c, ';')).concat(";_gid=GA1.2.526368041.1543124942; _gat=1"));
        return headers;
    }


    public static HtmlPage submitLoginAction(HtmlPage previousResponse, WebClient webClient, Set<Cookie> cookieSet, CrackWhv.LoginAccount loginAccount) throws Exception {
        HtmlHiddenInput captchaDeText = (HtmlHiddenInput) previousResponse.getElementById("CaptchaDeText");
        String q = captchaDeText.getValueAttribute();
        Map<String, String> cookieMap = new HashMap<>();
        cookieSet.forEach(cookie -> cookieMap.put(cookie.getName(), cookie.getValue()));
        byte[] results = null;

        try {

            BufferedImage img = ((HtmlImage) previousResponse.getElementById("CaptchaImage")).getImageReader().read(0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            baos.flush();
            //使用toByteArray()方法转换成字节数组
            results = baos.toByteArray();
            baos.close();

        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }

        CaptchaUtil.HttpResp httpResp = RequestCaptchaApi.Predict("20500", results);
        if (httpResp.getRet_code() != 0) {
            // error
            throw new RuntimeException("Exception");
        } else {
            JSONObject jsonObject = JSONObject.parseObject(httpResp.getRsp_data());
            String captchaText = jsonObject.getString("result").toUpperCase();


            // got form
            HtmlForm loginForm = (HtmlForm) previousResponse.getElementById("ApplicantListForm");
            ((HtmlTextInput) loginForm.getInputByName("EmailId")).setText(loginAccount.getName());
            ((HtmlPasswordInput) loginForm.getInputByName("Password")).setText(loginAccount.getPassword());
            ((HtmlTextInput) loginForm.getInputByName("CaptchaInputText")).setText(captchaText);

            HtmlPage afterLogin = ((HtmlSubmitInput) previousResponse.getByXPath("//input[@class='submitbtn']").get(0)).click();

            // 等待JS驱动dom完成获得还原后的网页
            webClient.waitForBackgroundJavaScript(ScheduleAppointment.TIME_OUT);

            return afterLogin;
        }
    }
}
