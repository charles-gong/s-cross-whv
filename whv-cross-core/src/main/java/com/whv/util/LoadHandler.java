package com.whv.util;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.whv.entity.AccountWithRegister;
import com.whv.entity.ApplicantInfo;
import com.whv.entity.LoginAccount;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadHandler {

    public static final Logger LOGGER = Logger.getLogger(LoadHandler.class);

    public static Map<String, String> loadJsCache() {
        Map<String, String> results = new HashMap<>();
        List<String> cacheFileNames = new ArrayList<>();
        cacheFileNames.add("analytics.js");
        cacheFileNames.add("bootstrap.min.js");
        cacheFileNames.add("bootstrap-datetimepicker.min.js");
        cacheFileNames.add("common.js");
        cacheFileNames.add("finalCalendar.js");
        cacheFileNames.add("fullcalendar.js");
        cacheFileNames.add("global-appointment-services.js");
        cacheFileNames.add("gtag_js.js");
        cacheFileNames.add("jquery.countdown.js");
        cacheFileNames.add("jquery.jqtransform.js");
        cacheFileNames.add("jquery.magnific-popup.min.js");
        cacheFileNames.add("jquery.modalbox-1.5.0-min.js");
        cacheFileNames.add("jquery.selectBox.js");
        cacheFileNames.add("jquery-3.3.1.min.js");
        cacheFileNames.add("jquery-migrate-3.0.1.js");
        cacheFileNames.add("jquery-ui.js");
        cacheFileNames.add("jqueryval.js");
        cacheFileNames.add("moment.min.js");
        cacheFileNames.add("SpryAccordion.js");
        cacheFileNames.parallelStream().forEach(fileName -> {
            try {
                String content = new BufferedReader(new InputStreamReader(CrackWhvFromRegister.class.getClassLoader().getResourceAsStream("js/" + fileName)))
                        .lines().collect(Collectors.joining(System.lineSeparator()));

                if (fileName.equals("gtag_js")) {
                    results.put("gtag/js", content);
                } else {
                    results.put(fileName, content);
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });

        return results;
    }

    public static List<LoginAccount> loadLoginAccounts(String path) {
        List<LoginAccount> results = new ArrayList<>();
        BufferedReader bufferedReader = null;
        String line = "";
        try {
            if (path == "") {
                bufferedReader = new BufferedReader(new InputStreamReader((CrackWhvFromRegister.class.getClassLoader().getResourceAsStream("account/login_account.txt"))));
            } else {
                bufferedReader = new BufferedReader(new FileReader(path));
            }

            while ((line = bufferedReader.readLine()) != null) {
                String[] accountInfos = line.split(",");
                LoginAccount loginAccount = new LoginAccount();
                loginAccount.setName(accountInfos[0].trim());
                loginAccount.setPassword(accountInfos[1].trim());
                results.add(loginAccount);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }

        return results;

    }

    public static List<ApplicantInfo> loadApplicant(String path) {
        BufferedReader bufferedReader = null;
        List<ApplicantInfo> applicantInfoList = new ArrayList<>();
        try {
            if (path == "") {
                bufferedReader = new BufferedReader(new InputStreamReader((CrackWhvFromRegister.class.getClassLoader().getResourceAsStream("account/applicants.txt"))));
            } else {
                bufferedReader = new BufferedReader(new FileReader(path));
            }

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] columns = line.trim().split(",");
                    ApplicantInfo applicantInfo = new ApplicantInfo();
                    applicantInfo.setFirstName(columns[0].trim().toUpperCase());
                    applicantInfo.setLastName(columns[1].trim().toUpperCase());
                    applicantInfo.setPassportNumber(columns[2].trim());
                    applicantInfo.setDateOfBirth(columns[3].trim());
                    applicantInfo.setPassportExpiryDate(columns[4].trim());
                    applicantInfo.setGenderId(columns[5].trim().equalsIgnoreCase("female") ? "2" : "1");
                    applicantInfo.setDialCode(columns[6].trim());
                    applicantInfo.setMobile(columns[7].trim());
                    applicantInfo.setEmailId(columns[8].trim());
                    applicantInfo.setLocation(columns[9].trim());
                    applicantInfoList.add(applicantInfo);
                }
            }

        } catch (IOException e) {
            LOGGER.error(e);
        }
        return applicantInfoList;

    }


    public static List<AccountWithRegister> loadAccountWithRegister(String path) {
        List<AccountWithRegister> results = new ArrayList<>();
        BufferedReader bufferedReader = null;
        if (path == "") {
            bufferedReader = new BufferedReader(new InputStreamReader((CrackWhvFromRegister.class.getClassLoader().getResourceAsStream("account/account_and_register.txt"))));
            String line = "";
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    String[] accountInfos = line.split(",");
                    AccountWithRegister accountWithRegister = new AccountWithRegister();
                    accountWithRegister.setName(accountInfos[0].trim());
                    accountWithRegister.setPassword(accountInfos[1].trim());
                    accountWithRegister.setUrn(accountInfos[2].trim());
                    results.add(accountWithRegister);
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
        return results;
    }

}
