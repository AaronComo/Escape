package com.aaroncomo.escape.ui.inpainting;

import static com.aaroncomo.escape.ui.userpage.UserPageFragment.username;
import static com.aaroncomo.escape.utils.HttpUtils.ip;
import static com.aaroncomo.escape.utils.HttpUtils.port;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.ui.card.CardItem;
import com.aaroncomo.escape.ui.card.SheetAdapter;
import com.aaroncomo.escape.ui.userpage.UserpageViewModel;
import com.aaroncomo.escape.utils.HttpUtils;
import com.aaroncomo.escape.utils.ImageUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InpaintingViewModel extends ViewModel {
    private List<CardItem> listData;

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

    /**
     * 启动模型修复或合成
     *
     * @param response 后端返回的URL
     * @param handler  消息传递
     * @param action   修复; 0; 合成: 1
     * @param style    修复: -1表示无效; 合成: 0-6
     */
    public void startModel(String response, Handler handler, int action, int style) {
        JSONObject args;
        String filename, url;
        try {
            Message msg = new Message();
//            if (action == 1) { // 上传类型的直接返回
//                msg.what = 0x11;
//                handler.sendMessage(msg);
//                return;
//            }
            args = new JSONObject(response);
            filename = args.getString("filename");
            url = args.getString("url");

            // 新建模型线程
            new Thread(() -> {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("filename", filename)
                        .addFormDataPart("username", username)
                        .addFormDataPart("action", String.valueOf(action))
                        .addFormDataPart("style", String.valueOf(style))
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

    public void initList(RecyclerView view, Resources resources, String packageName, Handler handler) {
        listData = new ArrayList<CardItem>();
        for (int i = 0; i < 7; i++) {
            @SuppressLint("DiscouragedApi")
            int resourceID = resources.getIdentifier("style_" + i, "drawable", packageName);
            listData.add(new CardItem(resourceID, "鎏金"));
        }

        view.setAdapter(new SheetAdapter(listData, handler));
    }
}