package com.aaroncomo.escape.utils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {

    public static String ip = "114.116.195.47";
    public static String port = "8001";
    private static final OkHttpClient client = new OkHttpClient();
    private static String ret;

    /**
     * @param url         地址
     * @param requestBody 请求体数据
     * @param callback    回调接口
     */
    public static void POST(String url, RequestBody requestBody, okhttp3.Callback callback) {

        //发送请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static Response POST(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return client.newCall(request).execute();
    }

    public static Response GET(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request).execute();
    }

    public void initNetwork(String ip, String port) {
        HttpUtils.ip = ip;
        HttpUtils.port = port;
    }
}
