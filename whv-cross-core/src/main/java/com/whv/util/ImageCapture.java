package com.whv.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.stream.IntStream;

public class ImageCapture {


    public static void main(String[] args){
        StringBuffer stringBuffer  = new StringBuffer("2e34d64b823c4731bd1b8110746a9ed2");
        IntStream.range(1,30).forEach(i->{
            try {
                // Step1 refresh
                Connection connection = Jsoup.connect("https://online.vfsglobal.com/Global-Appointment/DefaultCaptcha/Refresh");
                connection.header("Accept-Encoding", "gzip, deflate, br");
                connection.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
                connection.header("Connection", "keep-alive");
                connection.header("Cookie", "_ga=GA1.2.815269254.1542635131; ASP.NET_SessionId=2jx5vzxlw0tigatrnx5npfda; __RequestVerificationToken_L0dsb2JhbC1BcHBvaW50bWVudA2=j19Fk2oWnWdnGayPMczbGJ3nLqv7S74EpV7PAW-DhMFc3BGRCXj7tWmtrh9-IMzl98mUroURCIVZdXagE6_GV2R8zm01; BIGipServerCUST100052_wynvfsrowtweb443=rd1615o00000000000000000000ffff1eb8cb2do443; _gid=GA1.2.526368041.1543124942; _culture=en-US; _gat=1");
                connection.header("HOST", "online.vfsglobal.com");
                connection.header("Referer", "https://online.vfsglobal.com/Global-Appointment");
                connection.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
                Connection.Response res = connection.ignoreContentType(true).data("t", stringBuffer.toString()).timeout(500000).method(Connection.Method.POST).execute();
                String url = "https://online.vfsglobal.com" + Jsoup.parse(res.body()).select("#CaptchaImage").attr("src");

                Connection connection2 = Jsoup.connect(url);
//        connection.header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
//        connection.header("Content-Type","text/*,image/gif");
                connection2.header("Accept-Encoding", "gzip, deflate, br");
                connection2.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
                connection2.header("Connection", "keep-alive");
                connection2.header("Cookie", "_ga=GA1.2.815269254.1542635131; ASP.NET_SessionId=2jx5vzxlw0tigatrnx5npfda; __RequestVerificationToken_L0dsb2JhbC1BcHBvaW50bWVudA2=j19Fk2oWnWdnGayPMczbGJ3nLqv7S74EpV7PAW-DhMFc3BGRCXj7tWmtrh9-IMzl98mUroURCIVZdXagE6_GV2R8zm01; BIGipServerCUST100052_wynvfsrowtweb443=rd1615o00000000000000000000ffff1eb8cb2do443; _gid=GA1.2.526368041.1543124942; _culture=en-US; _gat=1");
                connection2.header("HOST", "online.vfsglobal.com");
                connection2.header("Referer", "https://online.vfsglobal.com/Global-Appointment");
                connection2.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
                Connection.Response res2 = connection2.ignoreContentType(true).timeout(500000).method(Connection.Method.GET).execute();

                byte[] bytes = res2.bodyAsBytes();
                InputStream buffin = new ByteArrayInputStream(bytes, 0, bytes.length);
                BufferedImage img = ImageIO.read(buffin);
                File outputfile = new File("/Users/gonglongmin/ij_workspace/gonglongmin/s-cross-whv/whv-cross-core/src/main/resources/" + UUID.randomUUID() + ".png");
                ImageIO.write(img, "png", outputfile);
                System.out.println("current [ "+i+" ] stored!!");
            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }
}
