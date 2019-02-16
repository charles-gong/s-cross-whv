package com.whv.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public static HtmlPage submitFinalCalendar(HtmlPage afterSubmitApplicantList, WebClient webClient, CrackWhv.LoginAccount loginAccount) throws IOException {
        HtmlPage afterSubmitCalendar = null;
        int month = 2;
        while (afterSubmitApplicantList != null) {
            if (month == 2) { // skip to March.
                HtmlSpan htmlSpan = (HtmlSpan) afterSubmitApplicantList.getByXPath("//td[@class='fc-header-right']/span").get(0);
                afterSubmitApplicantList = htmlSpan.click();
                month++;
            }
            LOGGER.info(String.format("--------------  Loading dates for month [ %d ] successfully! -------------- ", month));
            List<HtmlTableDataCell> tds = afterSubmitApplicantList.getByXPath("//div[@class='fc-content']//tbody//td");
            List<HtmlTableDataCell> availableList = tds.parallelStream().filter(td -> td.getAttribute("style").contains("background-color: rgb(188,237,145)")).collect(Collectors.toList());
            if (availableList == null || availableList.size() == 0) {
                HtmlSpan htmlSpan = (HtmlSpan) afterSubmitApplicantList.getByXPath("//td[@class='fc-header-right']/span").get(0);
                afterSubmitApplicantList = htmlSpan.click();
            } else {
                availableList.get(0).click();
                LOGGER.info(String.format("-------------- [ %s ] Choosing a date successfully! -------------- ", loginAccount.getName()));
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
                        LOGGER.info(String.format("-------------- [ %s ] Choosing a time range successfully! -------------- ", loginAccount.getName()));
                    }
                }
                HtmlSubmitInput htmlSubmitInput = (HtmlSubmitInput) afterSubmitApplicantList.getElementById("btnConfirm");
                afterSubmitCalendar = htmlSubmitInput.click();
                webClient.waitForBackgroundJavaScript(TIME_OUT);
            }
        }

        return afterSubmitCalendar;
    }

    public static HtmlPage submitConfirmPage(HtmlPage afterSubmitCalendar, WebClient webClient, CrackWhv.LoginAccount loginAccount) throws IOException {
        HtmlCheckBoxInput htmlCheckBoxInput = (HtmlCheckBoxInput) afterSubmitCalendar.getByXPath("//input[@type='checkbox']").get(0);
        htmlCheckBoxInput.click();

        //choose submit
        HtmlPage afterConfirm = null;
        List<HtmlElement> htmlElementList = afterSubmitCalendar.getByXPath("//a[@class='submitbtn']");
        if (htmlElementList != null && htmlElementList.size() > 0) {
            afterConfirm = ((HtmlAnchor) htmlElementList.get(0)).click();
            LOGGER.info(String.format("-------------- [ %s ] confirm successfully! -------------- ", loginAccount.getName()));
        } else {
            afterConfirm = ((HtmlSubmitInput) afterSubmitCalendar.getByXPath("//input[@class='submitbtn']").get(0)).click();
            LOGGER.info(String.format("-------------- [ %s ] confirm successfully! -------------- ", loginAccount.getName()));
        }
        webClient.waitForBackgroundJavaScript(TIME_OUT);

        return afterConfirm;
    }


}
