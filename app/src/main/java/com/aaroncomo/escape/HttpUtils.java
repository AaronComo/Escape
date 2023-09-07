package com.aaroncomo.escape;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    private static final OkHttpClient client = new OkHttpClient();
    private static String ret;
    /**
     *
     * @param address  服务器地址
     * @param requestBody  请求体数据
     * @param callback  回调接口
     */
    public static void uploadFile(String address, RequestBody requestBody, okhttp3.Callback callback) {

        //发送请求
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static String getURL(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 处理成功响应
                if (response.isSuccessful()) {
                    // 处理响应体的数据
                    ret = response.body().string();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
        return ret;
    }


    public static void main(String[] args) throws IOException {
        String response = HttpUtils.getURL("https://raw.githubusercontent.com/square/okhttp/master/samples/guide/src/main/java/okhttp3/guide/GetExample.java");
        System.out.println(response);
    }

}