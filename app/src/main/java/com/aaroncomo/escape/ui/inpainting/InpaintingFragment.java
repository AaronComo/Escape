package com.aaroncomo.escape.ui.inpainting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.HttpUtils;
import com.aaroncomo.escape.R;
import com.aaroncomo.escape.databinding.FragmentInpaintingBinding;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.aaroncomo.escape.ui.inpainting.InpaintingViewModel;

public class InpaintingFragment extends Fragment {

    private FragmentInpaintingBinding binding;
    private final String ip = "192.168.0.103";
    private final String port = "8000";
    private static Uri selectedImgUri = null;
    public static final int CHOOSE_PHOTO = 2;
    private String localFile = null, response = null, fileName = null;
    private static InpaintingViewModel viewModel;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Object hint;
            Boolean type = false;
            if (msg.what == 100) {
                type = true;
                hint = ((String[]) msg.obj)[0];
                response = ((String[]) msg.obj)[1];
            } else {
                hint = msg.obj;
            }
            log((String) hint, type);
        }
    };



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InpaintingViewModel inpaintingViewModelViewModel =
                new ViewModelProvider(this).get(InpaintingViewModel.class);

        binding = FragmentInpaintingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化ImageView
        binding.picture.setImageResource(R.drawable.default_img);
        binding.picture.setAdjustViewBounds(true);

        // 实例化ViewModel并和Fragment绑定
        viewModel = new ViewModelProvider(this).get(InpaintingViewModel.class);

        binding.selectPicture.setOnClickListener(v -> {
            // 动态申请文件读写权限
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            openAlbum();
        });


        // 开始修复按钮
        binding.extendedFab.setOnClickListener(v -> {
            // 检查是否选中图片, 选中则监听上传按钮
            if (selectedImgUri != null) {
                // 新建一个png格式缓存图像
                File file = viewModel.createCacheFile(selectedImgUri, super.getContext());

                // 创建线程, 上传图像
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("img", "uploadImg", RequestBody.create(file, MediaType.get("image/png")))
                                .build();
                        HttpUtils.uploadFile("http://" + ip + ":" + port + "/backend/upload/", requestBody, new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Message msg = new Message();
                                msg.what = 0;
                                msg.obj = "图像上传失败, 请重新选择图像上传";
                                handler.sendMessage(msg);
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String[] obj = { "正在修复中...", response.body().string()};
                                Message msg = new Message();
                                msg.what = 100;
                                msg.obj = obj;
                                handler.sendMessage(msg);
                                response.close();
                            }
                        });
                    }
                }).start();
            } else {
                log("请选择一张图片", false);
            }
        });

        return root;
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


    private String getRealPathFromUri(Uri uri) {
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = super.getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    public void log(String message, Boolean type) {
        if (!type) {
            binding.hint.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        } else {
            binding.hint.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        }
        binding.hint.setText(message);
    }

    public void openAlbum() {
        selectedImgUri = null;
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            log("没有选择图像", false);
            binding.picture.setImageResource(R.drawable.default_img);
        } else {
            selectedImgUri = data.getData();
            // 显示图像
//                Bitmap bitmap = BitmapFactory.decodeStream(super.getActivity().getContentResolver().openInputStream(uri));
//                binding.picture.setImageBitmap(bitmap);
            Picasso.get().load(selectedImgUri).into(binding.picture);
            log("图片读取成功, 点击最下方按钮开始修复", true);
        }
    }


    private void saveImage() {
        if (fileName != null) {
            File file = new File(localFile.concat("/").concat(fileName));
            File dstFile = new File("/storage/emulated/0/DCIM/".concat(fileName).replaceFirst(".png", "").concat("_generated.png"));
            boolean ret = file.renameTo(dstFile);
            if (ret) {
                fileName = localFile = null;   // 清空数据
                log("成功保存到系统相册", true);
            } else {
                log("保存失败", true);
            }
        } else {
            log("图片已经保存", true);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}