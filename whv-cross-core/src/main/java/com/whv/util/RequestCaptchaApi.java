package com.whv.util;

import java.util.Date;

import java.net.URL;

public class RequestCaptchaApi {

    protected static String app_id;
    protected static String app_key;
    protected static String pd_id;
    protected static String pd_key;
    protected static String pred_url;

    static {
        app_id = "308648";
        app_key = "PmXp29FanOGhzT9hBzhVA4TbCVxKUq+S";
        pd_id = "108648";
        pd_key = "W4d2zEFLQsE766qnGgMCVRfHYF0LA/UG";
        pred_url = "http://pred.fateadm.com";
    }

    /**
     * 查询余额
     * 参数：无
     * 返回值：
     * resp.ret_code：正常返回0
     * resp.err_msg：异常时返回异常详情
     */
    public CaptchaUtil.HttpResp QueryBalc() throws Exception {
        long cur_tm = new Date().getTime() / 1000;    // 时间戳精确到秒。所以除以1000
        String stm = String.valueOf(cur_tm);
        String sign = CaptchaUtil.CalcSign(pd_id, pd_key, stm);
        String url = this.pred_url + "/api/custval";
        String params = "user_id=" + this.pd_id + "&timestamp=" + stm + "&sign=" + sign;
        String pres = CaptchaUtil.HttpPost(url, params);
        CaptchaUtil.HttpResp resp = CaptchaUtil.ParseHttpResp(pres);
        return resp;
    }

    /***
     * 查询余额：直接返回余额结果
     * 参数：无
     * 返回值： 用户余额:double
     */
    public double QueryBalcExtend() throws Exception {
        CaptchaUtil.HttpResp resp = QueryBalc();
        return resp.cust_val;
    }

    /**
     * 充值接口
     * 参数：cardid：充值卡号, cardkey：充值卡签名串
     * 返回值：
     * resp.ret_code：正常返回0
     * resp.err_msg：异常时返回异常详情
     */
    public static CaptchaUtil.HttpResp Charge(String cardid, String cardkey) throws Exception {
        long cur_tm = new Date().getTime() / 1000;    // 时间戳精确到秒。所以除以1000
        String stm = String.valueOf(cur_tm);
        String sign = CaptchaUtil.CalcSign(pd_id, pd_key, stm);
        String csign = CaptchaUtil.CalcMd5(pd_key + stm + cardid + cardkey);
        String url = pred_url + "/api/charge";
        String params = "user_id=" + pd_id + "&timestamp=" + stm + "&sign=" + sign + "&cardid=" + cardid + "&csign=" + csign;
        String pres = CaptchaUtil.HttpPost(url, params);
        CaptchaUtil.HttpResp resp = CaptchaUtil.ParseHttpResp(pres);
        return resp;
    }

    /***
     * 充值接口：直接返回是否成功
     * 参数：cardid：充值卡号, cardkey：充值卡签名串
     * 返回值： 充值成功返回 0
     */
    private static int ChargeExtend(String cardid, String cardkey) throws Exception {
        CaptchaUtil.HttpResp resp = Charge(cardid, cardkey);
        return resp.ret_code;
    }


    /**
     * 文件形式进行验证码识别
     * 参数： pred_type：识别类型  file_name：文件名
     * 返回值：
     * resp.ret_code：正常返回0
     * resp.err_msg：异常时返回异常详情
     * resp.req_Id：唯一订单号
     * resp.pred_resl：识别的结果
     */
    public static CaptchaUtil.HttpResp PredictFromFile(String pred_type, String file_name) throws Exception {
        byte[] file_data = CaptchaUtil.ReadBinaryFile(file_name);
        if (file_data == null) {
            CaptchaUtil.HttpResp resp = new CaptchaUtil.HttpResp();
            resp.ret_code = -1;
            resp.err_msg = "ERROR: read file failed! file_name: " + file_name;
            return resp;
        }
        CaptchaUtil.HttpResp resp = Predict(pred_type, file_data);
        return resp;
    }

    /***
     * 文件形式进行验证码识别：直接返回识别结果
     * 参数： pred_type：识别类型  file_name：文件名
     * 返回值： 识别的结果:String
     */
    public static String PredictFromFileExtend(String pred_type, String file_name) throws Exception {
        CaptchaUtil.HttpResp resp = PredictFromFile(pred_type, file_name);
        return resp.pred_resl;
    }

    /**
     * 验证码识别
     * 参数： pred_type：识别类型  img_data：图片数据
     * 返回值：
     * resp.ret_code：正常返回0
     * resp.err_msg：异常时返回异常详情
     * resp.req_Id：唯一订单号
     * resp.pred_resl：识别的结果
     */
    public static CaptchaUtil.HttpResp Predict(String pred_type, byte[] img_data) throws Exception {
        long cur_tm = new Date().getTime() / 1000;    // 时间戳精确到秒。所以除以1000
        String stm = String.valueOf(cur_tm);
        String sign = CaptchaUtil.CalcSign(pd_id, pd_key, stm);
        String asign = "";
        URL url = new URL(pred_url + "/api/capreg");
        if (!app_id.isEmpty()) {
            asign = CaptchaUtil.CalcSign(app_id, app_key, stm);
        }
        String pres = CaptchaUtil.MFPost(url, img_data, stm, pd_id, sign, app_id, asign, pred_type);
        // System.out.println(pres);
        CaptchaUtil.HttpResp resp = CaptchaUtil.ParseHttpResp(pres);
        return resp;
    }

    /***
     * 验证码识别
     * 参数： pred_type：识别类型  img_data：图片数据
     * 返回值： 识别的结果:String
     */
    public String PredictExtend(String pred_type, byte[] img_data) throws Exception {
        CaptchaUtil.HttpResp resp = Predict(pred_type, img_data);
        return resp.pred_resl;
    }

    /**
     * 识别失败，进行退款请求
     * 参数： req_id：需要退款的订单号
     * 返回值：
     * resp.ret_code：正常返回0
     * resp.err_msg：异常时返回异常详情
     * <p>
     * 注意：
     * Predict识别接口，仅在RetCode == 0时才会进行扣款，才需要进行退款请求，否则无需进行退款操作
     * 注意2：
     * 退款仅在正常识别出结果后，无法通过网站验证的情况，请勿非法或者滥用，否则可能进行封号处理
     */
    public CaptchaUtil.HttpResp Justice(String req_id) throws Exception {
        long cur_tm = new Date().getTime() / 1000;     // 时间戳精确到秒。所以除以1000
        String stm = String.valueOf(cur_tm);
        String sign = CaptchaUtil.CalcSign(pd_id, pd_key, stm);
        String url = pred_url + "/api/capjust";
        String params = "user_id=" + pd_id + "&timestamp=" + stm + "&sign=" + sign + "&request_id=" + req_id;
        String pres = CaptchaUtil.HttpPost(url, params);
        CaptchaUtil.HttpResp resp = CaptchaUtil.ParseHttpResp(pres);
        return resp;
    }

    /***
     * 退款请求： 直接返回是否成功 
     * 参数： req_id：需要退款的订单号
     * 返回值： 返回 0 代表成功
     */
    public int JusticeExtend(String req_id) throws Exception {
        CaptchaUtil.HttpResp resp = Justice(req_id);
        return resp.ret_code;
    }
}
