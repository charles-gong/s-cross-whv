package com.whv.util;

import org.bytedeco.javacpp.opencv_core;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class Smoother {

    public static void main(String[] args) {
        smooth("/Users/gonglongmin/ij_workspace/gonglongmin/opencv-test/src/main/resources/saved.png",
                "/Users/gonglongmin/ij_workspace/gonglongmin/opencv-test/src/main/resources/out_100_150.png");
    }

    public static void smooth(String in, String out) {
        opencv_core.Mat image = imread(in);

        if (image != null) {
            threshold(image, image, 100, 150, CV_THRESH_BINARY);
            cvtColor(image, image, COLOR_RGB2GRAY);
//            GaussianBlur(image, image, new opencv_core.Size(3, 3), 0);
            imwrite(out, image);
        }
    }
}
