package com.whv.util;

import net.sourceforge.tess4j.Tesseract;

import java.io.File;

public class OcrInstance {

    private static final Tesseract instance = new Tesseract();

    static {
       // instance.setDatapath("/Users/gonglongmin/ij_workspace/gonglongmin/s-cross-whv/whv-cross-core/src/main/resources/");
    }


    public static void main(String[] args) throws Exception {
        String result = ocr("/Users/gonglongmin/ij_workspace/gonglongmin/s-cross-whv/saved.png");
        System.out.println(result);
    }

    public static String ocr(String path) throws Exception {
        instance.setDatapath("/Users/gonglongmin/Downloads/Tess4J");
        instance.setLanguage("eng");
        return instance.doOCR(new File(path));
    }
}
