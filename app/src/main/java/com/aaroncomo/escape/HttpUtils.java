package com.aaroncomo.escape;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
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

    public static Response GET(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request).execute();
    }
}
