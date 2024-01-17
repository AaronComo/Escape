package com.aaroncomo.escape.ui.userpage;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.aaroncomo.escape.HttpUtils;

import static com.aaroncomo.escape.HttpUtils.ip;
import static com.aaroncomo.escape.HttpUtils.port;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserpageViewModel extends ViewModel {
    public void getUserInfo(String username, Handler handler) {
        Message msg = new Message();
        new Thread(() -> {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .build();
            HttpUtils.POST("http://" + ip + ":" + port + "/backend/vip/", requestBody, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    msg.what = 0x1;
                    msg.obj = "无法连接服务器";
                    handler.sendMessage(msg);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    msg.what = 0x0;
                    try {
                        msg.obj = new JSONObject(response.body().string());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    handler.sendMessage(msg);
                }
            });
        }).start();
    }

}