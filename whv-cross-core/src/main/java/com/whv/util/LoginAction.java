package com.whv.util;

import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.whv.entity.LoginAccount;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class LoginAction {
    public static final Logger LOGGER = Logger.getLogger(LoginAction.class);

    public static HtmlPage submitLoginAction(HtmlPage previousResponse, WebClient webClient, Set<Cookie> cookieSet, String name, String password, String token, StringBuffer reqId) throws Exception {
        HtmlHiddenInput captchaDeText = (HtmlHiddenInput) previousResponse.getElementById("CaptchaDeText");
        String q = captchaDeText.getValueAttribute();
        Map<String, String> cookieMap = new HashMap<>();
        cookieSet.forEach(cookie -> cookieMap.put(cookie.getName(), cookie.getValue()));
        byte[] results = null;
        CaptchaUtil.HttpResp httpResp = null;
        boolean retry = true;
        while (retry) {
            try {

                BufferedImage img = ((HtmlImage) previousResponse.getElementById("CaptchaImage")).getImageReader().read(0);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                baos.flush();
                //使用toByteArray()方法转换成字节数组
                results = baos.toByteArray();
                baos.close();

                httpResp = RequestCaptchaApi.Predict("20500", results);
                retry = false;
            } catch (Exception ex) {
                LOGGER.error("Get CaptchaInputText occurs an error... so retry...", ex);
                Thread.sleep(1000);
            }
        }
        if (httpResp == null || httpResp.getRet_code() != 0) {
            // error
            throw new RuntimeException("Exception");
        } else {
            reqId.append(httpResp.getReq_id());
            JSONObject jsonObject = JSONObject.parseObject(httpResp.getRsp_data());
            String captchaText = jsonObject.getString("result").toUpperCase();
            LOGGER.info("-------CaptchaInputText is [ " + captchaText + " ] -------");
            WebRequest webRequest = new WebRequest(new URL("https://online.vfsglobal.com/Global-Appointment"));
            webRequest.setHttpMethod(HttpMethod.POST);
            webRequest.getAdditionalHeaders().put("Referer", "https://online.vfsglobal.com/Global-Appointment");
            webRequest.getAdditionalHeaders().put("Upgrade-Insecure-Requests", "1");
            List<NameValuePair> requestParams = new ArrayList<>();
            requestParams.add(new NameValuePair("__RequestVerificationToken", token));
            requestParams.add(new NameValuePair("Mission", "gXmo8X3D+UukB1I6unACeuv8C5/JyjHAJY5655VLkaM="));
            requestParams.add(new NameValuePair("Country", "58MTy4Z/dXL3/4vr5/4hzg=="));
            requestParams.add(new NameValuePair("Center", ""));
            requestParams.add(new NameValuePair("IsGoogleCaptchaEnabled", "False"));
            requestParams.add(new NameValuePair("reCaptchaURL", "https://www.google.com/recaptcha/api/siteverify?secret={0}&response={1}"));
            requestParams.add(new NameValuePair("reCaptchaPublicKey", "6Ld-Kg8UAAAAAK6U2Ur94LX8-Agew_jk1pQ3meJ1"));
            requestParams.add(new NameValuePair("EmailId", name));
            requestParams.add(new NameValuePair("Password", password));
            requestParams.add(new NameValuePair("CaptchaDeText", q));
            requestParams.add(new NameValuePair("CaptchaInputText", captchaText));
            webRequest.setRequestParameters(requestParams);

            WebResponse webResponse = webClient.loadWebResponse(webRequest);
            return HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
        }
    }
}
