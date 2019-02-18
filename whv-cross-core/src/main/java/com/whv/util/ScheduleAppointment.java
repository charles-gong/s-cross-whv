package com.whv.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.whv.entity.LoginAccount;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by gonglongmin on 2018/11/21.
 */
public class ScheduleAppointment {

    public static final Logger LOGGER = Logger.getLogger(ScheduleAppointment.class);

    public static final int TIME_OUT = 5 * 60 * 1000; // 5 mins

    /**
     * Get available date
     *
     * @param afterSubmitApplicantList
     * @param webClient
     * @return
     * @throws IOException
     */
    public static HtmlPage submitFinalCalendar(HtmlPage afterSubmitApplicantList, WebClient webClient, String accountName, AtomicBoolean wait, String folder) throws Exception {
        HtmlPage afterSubmitCalendar = null;
        int month = 2;
        while (afterSubmitApplicantList != null) {
            if (month == 2) { // skip to March.
                HtmlSpan htmlSpan = (HtmlSpan) afterSubmitApplicantList.getByXPath("//td[@class='fc-header-right']/span").get(0);
                afterSubmitApplicantList = htmlSpan.click();
            }
            List<HtmlTableDataCell> tds = afterSubmitApplicantList.getByXPath("//div[@class='fc-content']//tbody//td");

            month++;
            FileUtils.write(new File(folder + "/" + "Final_Calendar.html"), afterSubmitApplicantList.asXml(), "UTF-8");
            LOGGER.info(String.format("--------------  Loading dates for month [ %d ] successfully! -------------- ", month));
            List<HtmlTableDataCell> availableList = tds.parallelStream().filter(td -> td.getAttribute("style").contains("background-color: rgb(188,237,145)")).collect(Collectors.toList());
            if (availableList == null || availableList.size() == 0) {
                HtmlSpan htmlSpan = (HtmlSpan) afterSubmitApplicantList.getByXPath("//td[@class='fc-header-right']/span").get(0);
                afterSubmitApplicantList = htmlSpan.click();
            } else {
                availableList.get(0).click();
                LOGGER.info(String.format("-------------- [ %s ] Choosing a date successfully! -------------- ", accountName));
                // choose time.
                List<HtmlElement> htmlElementList = afterSubmitApplicantList.getElementById("TimeBandsDiv").getElementsByTagName("input");
                if (htmlElementList != null && htmlElementList.size() > 0) {
                    List<HtmlRadioButtonInput> htmlRadioButtonInputList = new ArrayList<>();
                    htmlElementList.forEach(htmlElement -> {
                        if (htmlElement instanceof HtmlRadioButtonInput) {
                            htmlRadioButtonInputList.add((HtmlRadioButtonInput) htmlElement);
                        }
                    });

                    if (htmlRadioButtonInputList.size() > 4) {
                        htmlRadioButtonInputList.get(4).click();
                        LOGGER.info(String.format("-------------- [ %s ] Choosing a time range successfully! -------------- ", accountName));
                    }
                }
                HtmlSubmitInput htmlSubmitInput = (HtmlSubmitInput) afterSubmitApplicantList.getElementById("btnConfirm");
                afterSubmitCalendar = htmlSubmitInput.click();
                webClient.waitForBackgroundJavaScript(TIME_OUT);
            }
        }

        return afterSubmitCalendar;
    }

    public static HtmlPage submitConfirmPage(HtmlPage afterSubmitCalendar, WebClient webClient, String accountName, String folder) throws IOException {
        FileUtils.write(new File(folder + "/" + "Check.html"), afterSubmitCalendar.asXml(), "UTF-8");
        List<HtmlElement> checkBoxList = afterSubmitCalendar.getByXPath("//input[@type='checkbox']");
        if (checkBoxList == null || checkBoxList.size() == 0) {
            LOGGER.info(String.format("-------------- [ %s ] cannot find checkbox in confirm page! -------------- ", accountName));
        } else {
            HtmlCheckBoxInput htmlCheckBoxInput = (HtmlCheckBoxInput) checkBoxList.get(0);
            htmlCheckBoxInput.click();
        }
        //choose submit
        HtmlPage afterConfirm = null;
        List<HtmlElement> htmlElementList = afterSubmitCalendar.getByXPath("//a[@class='submitbtn']");
        if (htmlElementList != null && htmlElementList.size() > 0) {
            afterConfirm = ((HtmlAnchor) htmlElementList.get(0)).click();
            LOGGER.info(String.format("-------------- [ %s ] confirm successfully! -------------- ", accountName));
        } else {
            List<HtmlElement> submitInputList = afterSubmitCalendar.getByXPath("//input[@class='submitbtn']");
            if (submitInputList == null || submitInputList.size() == 0) {
                LOGGER.info(String.format("-------------- [ %s ] cannot find submit button in confirm page! -------------- ", accountName));
            } else {
                afterConfirm = ((HtmlSubmitInput) submitInputList.get(0)).click();
                LOGGER.info(String.format("-------------- [ %s ] confirm successfully! -------------- ", accountName));
            }
        }
        webClient.waitForBackgroundJavaScript(TIME_OUT);

        return afterConfirm;
    }


}
