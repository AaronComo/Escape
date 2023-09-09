package com.aaroncomo.escape.ui.inpainting;

import static com.aaroncomo.escape.HttpUtils.ip;
import static com.aaroncomo.escape.HttpUtils.port;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.aaroncomo.escape.HttpUtils;

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
        File file = null;
        try {
            // 检查下载目录是否存在
            String downloadPath = "/storage/emulated/0/DCIM/SavedImages";
            File folder = new File(downloadPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // create a file to write bitmap data
            file = new File(path);

            // convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapData = bos.toByteArray();

            // write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void uploadImg(File file, Handler handler) {

        // 创建线程, 上传图像
        new Thread(() -> {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("img", "uploadImg", RequestBody.create(file, MediaType.get("image/png")))
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
                    msg.obj = new String[]{"正在修复中...", response.body().string()};
                    handler.sendMessage(msg);
                    response.close();
                }
            });
        }).start();
    }

    public void startModel(String response, Handler handler) {
        JSONObject args;
        String name, url;
        try {
            args = new JSONObject(response);
            name = args.getString("name");
            url = args.getString("url");

            // 新建模型线程
            new Thread(() -> {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name", name)
                        .build();
                HttpUtils.POST("http://" + ip + ":" + port + "/backend/model/", requestBody, new Callback() {
                    final Message msg = new Message();

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        msg.what = 0x01;
                        msg.obj = "服务器无应答, 生成失败";
                        handler.sendMessage(msg);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response1) throws IOException {
                        msg.what = 0x11;
                        msg.obj = new String[]{"修复完成", response1.body().string()};
                        handler.sendMessage(msg);
                    }
                });
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveImage(String url, Handler handler) {
        new Thread(() -> {
            Message msg = new Message();
            try {
                Response response = HttpUtils.GET(url);
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    InputStream is = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    int idx = url.lastIndexOf('/');
                    String name = url.substring(idx + 1);
                    String path = "/storage/emulated/0/DCIM/SavedImages";
                    convertBitmapToFile(path + "/" + name, bitmap);
                    msg.what = 0x12;
                    msg.obj = "成功保存到相册";
                } else {
                    msg.what = 0x00;
                    msg.obj = "保存图片失败, 请检查网络";
                }
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}