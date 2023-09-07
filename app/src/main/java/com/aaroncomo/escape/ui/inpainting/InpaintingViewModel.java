package com.aaroncomo.escape.ui.inpainting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class InpaintingViewModel extends ViewModel {
    public File createCacheFile(Uri uri, Context context) {
        byte[] byteArray;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return convertBitmapToFile(bitmap);
    }

    private File convertBitmapToFile(Bitmap bitmap) {
        File f = null;
        try {
            // create a file to write bitmap data
            f = new File("/storage/emulated/0/DCIM/img.png");

            // convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            // write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }
}