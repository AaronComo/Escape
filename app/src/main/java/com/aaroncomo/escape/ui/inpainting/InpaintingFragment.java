package com.aaroncomo.escape.ui.inpainting;

import static android.widget.TextView.AUTO_SIZE_TEXT_TYPE_NONE;

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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.R;
import com.aaroncomo.escape.databinding.FragmentInpaintingBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;


public class InpaintingFragment extends Fragment {

    private FragmentInpaintingBinding binding;
    private String realPath = null;
    public static final int CHOOSE_PHOTO = 2;
    private String localFile = null, remoteFile = null, fileName = null;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InpaintingViewModel inpaintingViewModelViewModel =
                new ViewModelProvider(this).get(InpaintingViewModel.class);

        binding = FragmentInpaintingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化ImageView
        binding.picture.setImageResource(R.drawable.default_img);
        binding.picture.setAdjustViewBounds(true);

        binding.selectPicture.setOnClickListener(v -> {
            // 动态申请文件读写权限
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            openAlbum();
        });


        binding.extendedFab.setOnClickListener(v -> {
            Snackbar.make(binding.getRoot(), "你好!", Snackbar.LENGTH_LONG).show();
            binding.extendedFab.setText("Let's start!");
            binding.extendedFab.setIcon(null);

        });

        return root;
    }

    public void log(String message, Boolean type) {
        if (!type) {
            binding.hint.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
        else {
            binding.hint.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        }
        binding.hint.setText(message);
    }

    public void openAlbum() {
        realPath = null;
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            log("没有选择图像", false);
//            Toast.makeText(getContext(), "没有选择图像！", Toast.LENGTH_SHORT).show();
            binding.picture.setImageResource(R.drawable.default_img);
        }
        else {
            Uri uri = data.getData();
            try {
                // 显示图像
                Bitmap bitmap = BitmapFactory.decodeStream(super.getActivity().getContentResolver().openInputStream(uri));
                binding.picture.setImageBitmap(bitmap);
                log("图片读取成功, 点击最下方按钮开始修复", true);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
//            handleImageOnKitKat(data);  // 处理图像并显示
        }
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
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
        displayImage(imagePath); // 根据图片路径显示图片
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     *
     * @param imagePath 图片真实路径
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            binding.picture.setImageBitmap(bitmap);
            realPath = imagePath;
        }
        else {
            Toast.makeText(getContext(), "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        if (fileName != null) {
            File file = new File(localFile.concat("/").concat(fileName));
            File dstFile = new File("/storage/emulated/0/DCIM/".concat(fileName).replaceFirst(".png", "").concat("_generated.png"));
            boolean ret = file.renameTo(dstFile);
            if (ret) {
                fileName = localFile = remoteFile = null;   // 清空数据
                log("成功保存到系统相册", true);
//                Toast.makeText(getContext(), "成功保存到系统相册！", Toast.LENGTH_SHORT).show();
            }
            else {
                log("保存失败", true);
//                Toast.makeText(getContext(), "保存失败！", Toast.LENGTH_SHORT).show();
            }
        } else {
            log("图片已经保存", true);
//            Toast.makeText(getContext(), "图片已经保存！", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}