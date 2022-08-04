package com.example.mymusicplayer.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class StreamUtil {
    //从InputStream中读取数据,返回byte数组
    public static byte[] read(InputStream is) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            is.close();
        } catch (Exception e) {
        }
        return outStream.toByteArray();
    }
    public static String readJson(InputStream is){
        String result = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
             result = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return result;


    }
}