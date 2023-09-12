package com.aaroncomo.escape;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Response;

public class ImageUtils {

    public static File convertBitmapToFile(String path, Bitmap bitmap) {
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

    public static void saveImage(String url, Handler handler) {
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
