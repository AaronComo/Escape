package com.aaroncomo.escape.ui.inpainting;

import static com.aaroncomo.escape.HttpUtils.ip;
import static com.aaroncomo.escape.HttpUtils.port;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;


import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.aaroncomo.escape.HttpUtils;
import com.aaroncomo.escape.ImageUtils;

import static com.aaroncomo.escape.ui.userpage.UserPageFragment.username;
import com.aaroncomo.escape.ui.userpage.UserpageViewModel;

public class InpaintingViewModel extends ViewModel {
    public File createCacheFile(Uri uri, Context context) {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String path = "/storage/emulated/0/DCIM/SavedImages/CacheImg.png";
        return convertBitmapToFile(path, bitmap);
    }

    private File convertBitmapToFile(String path, Bitmap bitmap) {
        return ImageUtils.convertBitmapToFile(path, bitmap);
    }

    public void uploadImg(File file, String name, Handler handler) {
        // 创建线程, 上传图像
        new Thread(() -> {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("img", name, RequestBody.create(file, MediaType.get("image/png")))
                    .build();
            HttpUtils.POST("http://" + ip + ":" + port + "/backend/upload/", requestBody, new Callback() {
                final Message msg = new Message();

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    file.delete();
                    msg.what = 0x00;
                    msg.obj = "图像上传失败, 请检查网络重试";
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    file.delete();
                    msg.what = 0x10;
                    msg.obj = response.body().string();
                    handler.sendMessage(msg);
                    response.close();
                }
            });
        }).start();
    }

    public void startModel(String response, Handler handler, int action) {
        JSONObject args;
        String filename, url;
        try {
            Message msg = new Message();
            if (action == 1) { // 上传类型的直接返回
                msg.what = 0x11;
                handler.sendMessage(msg);
                return;
            }
            args = new JSONObject(response);
            filename = args.getString("filename");
            url = args.getString("url");

            // 新建模型线程
            new Thread(() -> {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("filename", filename)
                        .addFormDataPart("username", username)
                        .build();
                HttpUtils.POST("http://" + ip + ":" + port + "/backend/model/", requestBody, new Callback() {

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        msg.what = 0x01;
                        msg.obj = "服务器无应答, 生成失败";
                        handler.sendMessage(msg);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        msg.what = 0x11;
                        msg.obj = response.body().string();
                        handler.sendMessage(msg);
                    }
                });
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUploaded(Handler handler) {
        UserpageViewModel.requestUserInfo(username, handler, "update_uploaded");
    }

    public void saveImage(String url, Handler handler) {
        ImageUtils.saveImage(url, handler);
    }

}