package com.whv.util;


import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gonglongmin on 2018/11/19.
 */
public class CrackWhv {

    private static Logger LOGGER = Logger.getLogger(CrackWhv.class);

    private static final String LOGIN_URL = "https://online.vfsglobal.com/Global-Appointment/Account/RegisteredLogin";

    public static void main(String[] args) throws Exception {
        List<Map<String, String>> applicants = loadApplicant("/Users/gonglongmin/ij_workspace/gonglongmin/s-cross-whv/applicants.txt");
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        forkJoinPool.submit(() ->
                applicants.parallelStream().forEach(applicant -> {
                    AtomicInteger currentStep = new AtomicInteger(0);
                    Connection.Response currentResponse = null;
                    while (currentStep.get() != 5) {
                        try {
                            if (currentStep.get() == 0) {
                                Connection con = Jsoup.connect(LOGIN_URL);// 获取连接
                                con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
                                con.header("Connection", "keep-alive");
                                Connection.Response pageResponse = con.timeout(ScheduleAppointment.TIME_OUT).execute();
                                currentStep.set(1);
                                currentResponse = pageResponse;
                            }
                            if (currentStep.get() == 1) {
                                Connection.Response afterLogin = LoginAction.submitLoginAction(currentResponse);
                                String content = Jsoup.parse(afterLogin.body()).toString();
                                while (content.contains("Your account has been locked, please login after 2 minutes")) {
                                    LOGGER.error("Sleep 2 mins, account is locked.");
                                    Thread.sleep(2 * 60 * 1001); // sleep 2mins
                                    afterLogin = LoginAction.submitLoginAction(currentResponse);
                                    content = Jsoup.parse(afterLogin.body()).toString();
                                }
                                currentStep.set(2);
                                currentResponse = afterLogin;
                            }
                            if (currentStep.get() == 2) {
                                Connection.Response afterSelectCenter = ScheduleAppointment.submitSelectCenter(currentResponse, applicant.get("Location").toLowerCase()); //TODO location needs to be provided.
                                currentStep.set(3);
                                currentResponse = afterSelectCenter;
                            }
                            if (currentStep.get() == 3) {
                                // This step can be skipped.
                                Connection.Response afterAddApplicant = ScheduleAppointment.submitAddApplicant(currentResponse, applicant); // TODO  need customers personal information, detailed fields see function: submitAddApplicant
                                currentStep.set(4);
                                currentResponse = afterAddApplicant;
                            }
                            if (currentStep.get() == 4) {
                                // This step can pass afterSelectCenter or afterAddApplicant
                                Connection.Response afterSubmitApplicantList = ScheduleAppointment.submitApplicantList(currentResponse);
                                currentStep.set(5);
                                currentResponse = afterSubmitApplicantList;

                            }
                        } catch (Exception e) {
                            LOGGER.error(String.format("[ %s %s ] step [%d] error, retry...", applicant.get("FirstName"), applicant.get("LastName"), currentStep.get()), e);
                        }
                    }
                })
        ).get();
    }

    /**
     * Submit add applicant page
     * <p>
     * InfoMap contains:
     * PassportNumber: value
     * DateOfBirth: value --> format DD/MM/YY
     * PassportExpiryDate: value --> format DD/MM/YY
     * NationalityId: value --> China is 165 so hard the code
     * FirstName: value
     * LastName: value
     * GenderId: value --> 2 is female, 1 is male, 3 is others
     * Mobile: value --> phone number
     * DialCode: value --> +86 so hard the code
     * EmailId: value
     * Location: value
     * <p>
     * <p>
     * FirstName, LastName, PassportNumber, DateOfBirth, PassportExpiryDate, GenderId, DialCode, Mobile, EmailId, Location
     */
    private static List<List<Map<String, String>>> loadApplicantGroup(String path) {
        List<List<Map<String, String>>> result = new ArrayList<>();
        List<Map<String, String>> applicantList = loadApplicant(path);
        for (int i = 0; i < applicantList.size(); i = i + 5) {
            List<Map<String, String>> subList = new ArrayList<>();
            if (i + 5 > applicantList.size()) {
                subList.addAll(applicantList.subList(i, applicantList.size()));
            } else {
                subList.addAll(applicantList.subList(i, i + 5));
            }
            result.add(subList);
        }


        return result;

    }

    private static List<Map<String, String>> loadApplicant(String path) {
        List<Map<String, String>> applicantList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    Map<String, String> applicantInfo = new HashMap<>();
                    String[] columns = line.trim().split(",");
                    applicantInfo.put("FirstName", columns[0].trim());
                    applicantInfo.put("LastName", columns[1].trim());
                    applicantInfo.put("PassportNumber", columns[2].trim());
                    applicantInfo.put("DateOfBirth", columns[3].trim());
                    applicantInfo.put("PassportExpiryDate", columns[4].trim());
                    if (columns[5].trim().equalsIgnoreCase("female")) {
                        applicantInfo.put("GenderId", "2");
                    } else if (columns[5].trim().equalsIgnoreCase("male")) {
                        applicantInfo.put("GenderId", "1");

                    }
                    applicantInfo.put("DialCode", columns[6].trim());
                    applicantInfo.put("Mobile", columns[7].trim());
                    applicantInfo.put("EmailId", columns[8].trim());
                    applicantInfo.put("Location", columns[9].trim());
                    applicantList.add(applicantInfo);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return applicantList;

    }

    // Add applicant
    /**
     *
     *
     * //                Map<String, ArrayList> headers = new HashMap<String, ArrayList>();
     //                headers.put("Cache-Control", new ArrayList(){{
     //                    add("no-cache, no-store, must-revalidate,no-cache, no-store, must-revalidate");
     //                }});
     //                headers.put("Pragma", new ArrayList(){{
     //                    add("no-cache,no-cache");
     //                }});
     //                headers.put("Content-Type", new ArrayList(){{
     //                    add("text/html; charset=utf-8");
     //                }});
     //                headers.put("Expires", new ArrayList(){{
     //                    add("-1,0");
     //                }});
     //                headers.put("Set-Cookie", new ArrayList(){{
     //                    add("ASP.NET_SessionId=tvy5d4kpxy2uuvlpjqzjvxnw; path=/; secure; HttpOnly");
     //                    add("ASP.NET_SessionId=tvy5d4kpxy2uuvlpjqzjvxnw; path=/; secure; HttpOnly");
     //                    add("_culture=en-US; path=/; secure; HttpOnly");
     //                    add("__RequestVerificationToken_L0dsb2JhbC1BcHBvaW50bWVudA2=1JEljZC1LgZwat-xak28fkXppeAM9LpuJKtVqfuahvKv81YWy5f9Z64V0fVk5acGwuW_JvYvSUeAKUIU9VnugPtZCro1; path=/; secure; HttpOnly");
     //                    add("BIGipServerCUST100052_wynvfsrowtweb443=rd1615o00000000000000000000ffff1eb8cb2do443; path=/; Httponly; Secure");
     //                }});
     //                headers.put("X-Content-Type-Options", new ArrayList(){{
     //                    add("nosniff");
     //                }});
     //               Map<String, String> cookies = new HashMap<String, String>();
     //                cookies.put("ASP.NET_SessionId","tvy5d4kpxy2uuvlpjqzjvxnw");
     //                cookies.put("_culture","en-US");
     //                cookies.put("__RequestVerificationToken_L0dsb2JhbC1BcHBvaW50bWVudA2","1JEljZC1LgZwat-xak28fkXppeAM9LpuJKtVqfuahvKv81YWy5f9Z64V0fVk5acGwuW_JvYvSUeAKUIU9VnugPtZCro1");
     //                cookies.put("BIGipServerCUST100052_wynvfsrowtweb443","rd1615o00000000000000000000ffff1eb8cb2do443");

     __RequestVerificationToken: 1lK4xItKYVz8HfW7pQqhzSgsz3jaVmzYhc-Y69Wz3PBUlxq1lhkBu8wKNSqlsomn-tKLH_Yow-wfOzv1vmf7-hvP3wXCv1gnsGHH1j3ag4NaDmeo0
     IsVAFValidationEnabled: False
     IsEndorsedChildEnabled: False
     NoOfEndorsedChild: 0
     IsEndorsedChild: False
     EnableValidatePaymentAtCashCounter: False
     Currency:
     Amount: 0
     IsAppointmentExists: False
     CancellationCount: 0
     MissionId: 0
     CountryId: 0
     ShowNextAvailableSlot: False
     ShowLegalizationDocuments: False
     IsEndrosedChildDisclaimerEnabled: True
     EndrosedChildAge: 0
     IsApplicantCompanyInfoRequired: False
     StateJsonString: [{"stateId":0,"Municipalities":null,"Pincodes":null,"Id":0,"Name":"Select Province"}]
     CanShowAdditionalFields: False
     ApplicationTypeId: 0
     PassportNumber: E00223322
     DateOfBirth: 08/11/1990
     PassportExpiryDate: 22/02/2030
     NationalityId: 165
     FirstName: GRACE
     LastName: LI
     GenderId: 0
     GenderId: 0
     DialCode: +86
     Mobile: 1515151515151
     EmailId: glmlyf@163.com
     */

    /**
     Request URL: https://online.vfsglobal.com/Global-Appointment/Account/SetMissionAndCountry
     Request Method: POST
     Accept: application/json, text/javascript, *"/"*; q=0.01
     Accept-Encoding: gzip, deflate, br
     Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7
     Connection: keep-alive
     Content-Length: 0
     Cookie: .ASPXFORMSAUTH=E657650EC9B4CF1AAE4EE97DEC4AA83AD61094E44838F26A25386E9F6CD8CE16F3836DE69706140F77397708DBF485A31160DC03E6F8DE6AC2A983EE3403DE305B7074A20266D475130EF67DA8EC3F0160D5C11D6F0F266B7FEB32B561D5791C7FB0042C812B7FD4C64E28EDFC52ECDB3EE52B84666EC87A0655A3ED364269C4A9CAF10AC9D868DCD2F20464848918460DD61F89FBACB7D0B9FCF1AFB5798F6801FE0D17EE49E2FE88C4BD6FDFF439D929507CADF6DCD0A7F7942A5FD74D83A24A9E35762FEBA5700D80298BBED9934EA330712D16FAADD72A73A0F71D32A9CAC76CD1261B6472178B0E69B4E768276962553441D042B5B32940795F40068D2F0AAD1DF3EC8DE31E83167E6C70AE80E67B68670B619BFE213487A2A8E6321EEE1C248BA8FDFDF3C0EE88794B1EAE83D484016032F00E749D05DD18DF3989790D61BBFA05584941526C90C31760243B76FC0CB76152F9DF69D50FD7136A04D582CB774EC2EFA615657AC1EF32EFA5D5BBD4418510696B3AD64D113B914794E6707AB5ECD73CC36664389BB14ED22F6EE17E58F87717F725AB10A8860BC74606CDC3147E7069B0CE0D9B924C384367A9E8EBB168968462D17C22A9254CA373AB389E0BCDCC425D37503A4C44EB7C3A49C52B4D6050B74DA8479C1B61E8B66070D48CA13926687BD229A1115A9C9EA90D623719D9D8189194307849D1E4F76C889A333F80EA683F43DF21C510846DE50DE8FAA50E4ECF9110D4A7F8F625CA7EAD3EE2A550181E44277CB09C9B57EB59AA73500383171FA5B5C43A154B242C28A7B7F68C129BD6977399D1BE0A7401D9B22D070DCBEABD3C8433D6D490CA33A8B79EFDCA894F9425671ACF4DFB7B21652B9471AAD1566F8A1E0F64DC46A46AE9DFB31E3BB44D6664EC701915A0034636CC117A534DA7FA406717BF71ED5ECCFD2AC3B896E94860306D6A1BC1CE5F96A2CBD4BD440F946C400C986D012C7B2E6FBAF44D7AF08ADFD8B1D213AAC22DB3768C1FB737F527C396EDB880DA81754ACE0481430A7FA97DC251928F2C9778EECE93C9D1D58EEBFC8C7FA75782527A514693DA54EEC46B87586E6152E3029650B2585E52BFAAB9; _ga=GA1.2.815269254.1542635131; _gid=GA1.2.151288621.1542635131; ASP.NET_SessionId=400bv1mrtlyqthewwu3q5qcz; __RequestVerificationToken_L0dsb2JhbC1BcHBvaW50bWVudA2=rBFe2ecN_nGZZlde4yLt5XSh1jXkDJVqytsH619HMVUvKqGptSmvpj6yiWxsbAMTXYxkpf09X2uSHmsS2TVW8t6-q9E1; BIGipServerCUST100052_wynvfsrowtweb443=rd1615o00000000000000000000ffff1eb8cb2do443; _culture=en-US; .ASPXFORMSAUTH=D15F6F55C163B97356C8BA524E0A6A8D84DF1BC4197A45B0CB61E0EC18E0DCD8C5AAA3A796B97A0670B1813CB2B44257A45CC1B00EB588FC8256EF0F526994EC5CB043FD9F9921C3F585028015B2051E9495C97221D4CF7069AA8CC08ABA13A44C55C66F11307F0625349D831C68D752BFF8030954A68D8B38EFAEAA316D4CC715E2D46A6E0118576811309F560367F8000B5825207722BEBCD6DDEC109F7F14735591EA; _Role=Individual
     Host: online.vfsglobal.com
     Origin: https://online.vfsglobal.com
     Referer: https://online.vfsglobal.com/Global-Appointment/Calendar/FinalCalendar
     User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36
     X-Requested-With: XMLHttpRequest
     *
     *
     */


}
