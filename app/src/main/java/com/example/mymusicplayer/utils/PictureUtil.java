package com.example.mymusicplayer.utils;

import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PictureUtil {
    // 定义一个获取网络图片数据的方法:
    public static byte[] getImage(String path) throws Exception {

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Log.e("lcq", "conn: " + conn.toString());
        // 设置连接超时为5秒
        conn.setConnectTimeout(5000);
        // 设置请求类型为Get类型
        conn.setRequestMethod("GET");
        // 判断请求Url是否成功
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("请求url失败");
        }
        InputStream inStream = conn.getInputStream();
        byte[] bt = StreamUtil.read(inStream);
        inStream.close();
        return bt;
    }
    public static String getJson(String path) throws Exception{
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("请求url失败");
        }
        InputStream is = conn.getInputStream();
        String data = StreamUtil.readJson(is);
        is.close();
        return data;
    }

    public static String getFormatPath(String url){
        if(url.startsWith("[") || url.startsWith("\"")){
            return url.split("\"")[1];
        }
        return url;
    }

    // 获取网页的html源代码
    public static String getHtml(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream in = conn.getInputStream();
            byte[] data = StreamUtil.read(in);
            return new String(data, "UTF-8");
        }
        return null;
    }
}