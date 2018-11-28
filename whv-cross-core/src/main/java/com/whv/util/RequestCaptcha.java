package com.whv.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.log4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

public class RequestCaptcha {

    private static Logger LOGGER = Logger.getLogger(RequestCaptcha.class);

    private static final String REQUEST_URL = "http://upload.chaojiying.net/Upload/Processing.php";
    private static final String ERROR_REPORT_URL = "http://upload.chaojiying.net/Upload/ReportError.php";


    private static RestTemplate restTemplate;
    private static final Map<String, Object> params = new HashMap<>();

    static {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(60)).setReadTimeout(Duration.ofSeconds(60)).build();
        params.put("user", "charlesgong123");
        params.put("pass", "!Admin123");
        params.put("softid", "bdf654795f26103d412a0ce78e39bca8");
        params.put("codetype", "4005");
        params.put("len_min", "0");
    }

    /**
     * post请求(用于key-value格式的参数)
     *
     * @param params
     * @return
     */
    public static String postForCaptcha(Map params) {

        try {
            params.remove("userfile");
            params.put("userfile", params.get("userfile"));
            ResponseEntity<CaptchaResponse> responseResponseEntity = restTemplate.exchange(REQUEST_URL, HttpMethod.POST, new HttpEntity<>(new HttpHeaders()), CaptchaResponse.class, params);
            if (responseResponseEntity.getStatusCodeValue() == 200) {
                CaptchaResponse captchaResponse = responseResponseEntity.getBody();
                if (captchaResponse.getErrNo() == 0) {
                    return captchaResponse.getPicStr();
                } else {
                    // 返回要求的题分
                    String picId = captchaResponse.getPicId();
                    postForReturnMoney(picId);
                }
            }

        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    public static void postForReturnMoney(String picId) {
        Map<String, Object> returnParams = new HashMap<>();
        returnParams.put("user", "charlesgong123");
        returnParams.put("pass", "!Admin123");
        returnParams.put("softid", "bdf654795f26103d412a0ce78e39bca8");
        returnParams.put("id", picId);
        restTemplate.exchange(ERROR_REPORT_URL, HttpMethod.POST, new HttpEntity<>(new HttpHeaders()), CaptchaResponse.class, returnParams);
    }

    public class CaptchaResponse {

        @JsonProperty("err_no")
        private Integer errNo;

        @JsonProperty("err_str")
        private String errStr;

        @JsonProperty("pic_id")
        private String picId;

        @JsonProperty("pic_str")
        private String picStr;

        private String md5;

        public Integer getErrNo() {
            return errNo;
        }

        public void setErrNo(Integer errNo) {
            this.errNo = errNo;
        }

        public String getErrStr() {
            return errStr;
        }

        public void setErrStr(String errStr) {
            this.errStr = errStr;
        }

        public String getPicId() {
            return picId;
        }

        public void setPicId(String picId) {
            this.picId = picId;
        }

        public String getPicStr() {
            return picStr;
        }

        public void setPicStr(String picStr) {
            this.picStr = picStr;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }
    }

}
