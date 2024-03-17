package com.aaroncomo.escape.ui.userpage;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.aaroncomo.escape.utils.HttpUtils;

import static com.aaroncomo.escape.utils.HttpUtils.ip;
import static com.aaroncomo.escape.utils.HttpUtils.port;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserpageViewModel extends ViewModel {
    /**
     * 更新/获取vip信息
     * @apiNote  目标端口: /backend/vip/
     * @param username 用户名
     * @param handler 消息处理器
     *  @param action "reset":重置操作 "update_your_target":更新your_target项目
     */
    public static void requestUserInfo(String username, Handler handler, String action) {
        Message msg = new Message();
        new Thread(() -> {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("action", action)
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
                        assert response.body() != null;
                        msg.obj = new JSONObject(response.body().string());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    handler.sendMessage(msg);
                    response.close();
                }
            });
        }).start();
    }


}