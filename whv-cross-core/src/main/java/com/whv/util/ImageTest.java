package com.whv.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageTest {

    private static final String brown = "CCD5CC";
    private static final String blueColor = "0000FF"; //-10070580
    private static final String lightBlueColor = "6680FF";
    private static final String white = "FFFFFF";

    private static int blue = 0;

    public static void main(String[] args) throws Exception {
        File[] files = new File("/Users/gonglongmin/ij_workspace/gonglongmin/opencv-test/captcha").listFiles();
        for (int q = 0; q < files.length; q++) {
            BufferedImage bi = ImageIO.read(files[q]);
            int h = bi.getHeight();
            int w = bi.getWidth();
            BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    if (j < 15 || j > 180) {
                        bufferedImage.setRGB(j, i, -1);
                        continue;
                    }
                    if (i < 5 || i > 65) {
                        bufferedImage.setRGB(j, i, -1);
                        continue;
                    }
                    int color = bi.getRGB(j, i);
                    if (color == -1) {
                        bufferedImage.setRGB(j, i, -1);
                        continue;
                    } else {
                        String colorS = Integer.toHexString(color);
//                        if(colorS.endsWith("6655cc")){
//                            System.out.println(color);
//                        }
                        if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                            bufferedImage.setRGB(j, i, -10070580);
                        } else {
                            bufferedImage.setRGB(j, i, -1);
                        }
                    }
                }
            }

//            for (int i = 0; i < w; i++) {
//                for (int j = 0; j < h; j++) {
//                    int color = bi.getRGB(i, j);
//                    if (!Integer.toHexString(color).substring(2).equalsIgnoreCase(white)) {
//                        if (j != h - 1 && Integer.toHexString(bi.getRGB(i, j + 1)).substring(2).equalsIgnoreCase(white)) {
//                            bufferedImage.setRGB(i, j, -1);
//                        } else {
//                            bufferedImage.setRGB(i, j, -10070580);
//                        }
//                    } else {
//                        bufferedImage.setRGB(i, j, -1);
//                    }
//                }
//            }
//
            ImageIO.write(bufferedImage, "png", new File("/Users/gonglongmin/ij_workspace/gonglongmin/opencv-test/captcha/" + files[q].getName() + "_phase_1.png"));
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int color = bufferedImage.getRGB(j, i);
                    if (color == -10070580) {
                        boolean white = fillWithWhite(bufferedImage, j, i, w, h);
                        if (white) {
                            bufferedImage.setRGB(j, i, -1);
                        } else {
                            bufferedImage.setRGB(j, i, -10070580);
                        }

                    } else {
                        boolean white = fillWithWhite(bufferedImage, j, i, w, h);
                        if (white) {
                            bufferedImage.setRGB(j, i, -1);
                        }
                    }
                }
            }


            ImageIO.write(bufferedImage, "png", new File("/Users/gonglongmin/ij_workspace/gonglongmin/opencv-test/captcha/" + files[q].getName() + "_phase_2.png"));
        }
    }

    private static boolean pass(int i) {
        if (i == -10070580)
            return true;
        String s = Integer.toHexString(i);
        return s.substring(2).equalsIgnoreCase(lightBlueColor);
    }


    private static boolean fillWithWhite(BufferedImage bi, int currentW, int currentH, int maximizeW, int maximizedH) {
        int count = 0;
        // check left
        if (currentH == 0 && currentW == 0) {
            return false;
        }
        if (currentW > 0) {
            int left = bi.getRGB(currentW - 1, currentH);
            if (left != -1) {
                String colorS = Integer.toHexString(left);
                if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                    count++;
                }
            }
        }
        // check right
        if (currentW < maximizeW - 1) {
            int right = bi.getRGB(currentW + 1, currentH);
            if (right != -1) {
                String colorS = Integer.toHexString(right);
                if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                    count++;
                }
            }
        }

        // upper
        if (currentH > 0) {
            int upper = bi.getRGB(currentW, currentH - 1);
            String colorS = Integer.toHexString(upper);
            if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                count++;
            }
        }

        // down
        if (currentH < maximizedH - 1) {
            int down = bi.getRGB(currentW, currentH + 1);
            if (down != -1) {
                String colorS = Integer.toHexString(down);
                if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                    count++;
                }
            }
        }

        // upper left
        // down
        if (currentH > 0 && currentW > 0) {
            int upperLeft = bi.getRGB(currentW - 1, currentH - 1);
            if (upperLeft != -1) {
                String colorS = Integer.toHexString(upperLeft);
                if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                    count++;
                }
            }
        }


        // upper right
        // down
        if (currentW < maximizeW - 1 && currentH > 0) {
            int upperRight = bi.getRGB(currentW + 1, currentH - 1);
            if (upperRight != -1) {
                String colorS = Integer.toHexString(upperRight);
                if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                    count++;
                }
            }
        }

        // down left
        // down
        if (currentW > 0 && currentH < maximizedH - 1) {
            int downLeft = bi.getRGB(currentW - 1, currentH + 1);
            if (downLeft != -1) {
                String colorS = Integer.toHexString(downLeft);
                if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                    count++;
                }
            }
        }

        // down right
        // down
        if (currentW < maximizeW - 1 && currentH < maximizedH - 1) {
            int downRight = bi.getRGB(currentW + 1, currentH + 1);
            if (downRight != -1) {
                String colorS = Integer.toHexString(downRight);
                if (colorS.endsWith("ff") || colorS.substring(colorS.length() - 2, colorS.length() - 1).endsWith("c")) {
                    count++;
                }
            }
        }

        if (count <= 4) {
            return true;
        } else {
            return false;
        }
    }

}
