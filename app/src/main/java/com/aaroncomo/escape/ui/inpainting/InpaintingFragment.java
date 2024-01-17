package com.aaroncomo.escape.ui.inpainting;

import static com.aaroncomo.escape.R.drawable.place_holder;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.R;
import com.google.android.material.R.style;
import com.aaroncomo.escape.databinding.FragmentInpaintingBinding;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

public class InpaintingFragment extends Fragment {

    private FragmentInpaintingBinding binding;
    private static Uri selectedImgUri = null;
    public static final int CHOOSE_PHOTO = 2;
    private Boolean running = false;
    private ObjectAnimator textViewAnimator, imageViewAnimator;
    private String response = null;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // 实例化ViewModel并和Fragment绑定
        InpaintingViewModel viewModel =
                new ViewModelProvider(this).get(InpaintingViewModel.class);

        // 绑定ui组件
        binding = FragmentInpaintingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化控件
        binding.picture.setImageResource(R.drawable.default_img);
        binding.picture.setAdjustViewBounds(true);
        binding.extendedFab.setText(R.string.start);

        // 创建ObjectAnimator对象，设置TextView、ImageView的动画属性
        textViewAnimator = ObjectAnimator.ofFloat(binding.hint, "alpha", 0f, 1f);
        textViewAnimator.setDuration(500);
        textViewAnimator.start();
        imageViewAnimator = ObjectAnimator.ofFloat(binding.picture, "alpha", 0f, 1f);
        imageViewAnimator.setDuration(1000);
        imageViewAnimator.start();

        // 使用Looper参数构造Handler, 使得Handler与特定线程的消息队列关联, 避免内存泄漏
        // msg.what: 0x0_: 失败   0x1_: 成功
        Handler handler = new Handler(Looper.getMainLooper()) {
            Boolean type = false;
            Object hint;

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    // 上传成功
                    case 0x10:
                        type = true;
                        hint = ((String[]) msg.obj)[0];
                        response = ((String[]) msg.obj)[1];

                        // 启动模型生成
                        viewModel.startModel(response, this);
                        break;

                    // 修复完成
                    case 0x11:
                        binding.progressbar.hide();
                        binding.extendedFab.extend();
                        binding.extendedFab.setText(R.string.end);
                        type = true;
                        hint = ((String[]) msg.obj)[0];
                        response = ((String[]) msg.obj)[1];
                        imageViewAnimator.start();
                        Picasso.get().load(response).placeholder(place_holder).into(binding.picture);

                        // 监听保存图片按钮
                        binding.savePicture.setOnClickListener(v -> viewModel.saveImage(response, this));
                        break;

                    // 保存成功
                    case 0x12:
                        binding.savePicture.setOnClickListener(null);
                        hint = msg.obj;
                        log((String) hint, true);

                    // 出错
                    default:
                        hint = msg.obj;
                        binding.progressbar.hide();
                        binding.extendedFab.extend();
                }
                log((String) hint, type);
            }
        };

        // 选择图片按钮
        binding.selectPicture.setOnClickListener(v -> {
            // 动态申请文件读写权限
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            // 选择图片时取消保存按钮监听
            binding.savePicture.setOnClickListener(null);
            running = false;
            openAlbum();
        });

        // 开始修复按钮
        binding.extendedFab.setOnClickListener(v -> startRepair(viewModel, handler));

        return root;
    }


    public void startRepair(InpaintingViewModel viewModel, Handler handler) {
        if (running) {
            return;
        }
        running = true;
        // 检查是否选中图片, 选中则监听上传按钮
        if (selectedImgUri != null) {
            binding.extendedFab.shrink();
            binding.progressbar.show();
            log("正在上传中...", true);

//            Context context = getContext();
            new Thread(() -> {
                // 新建一个png格式缓存图像
                File file = viewModel.createCacheFile(selectedImgUri, requireContext());
                viewModel.uploadImg(file, handler);
            }).start();
        } else {
            log("请选择一张图片", false);
        }
    }


    public void log(String message, Boolean type) {
        if (!type) {
            binding.hint.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            binding.hint.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        }
        binding.hint.setTypeface(null, Typeface.BOLD);
        textViewAnimator.start();
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
        binding.extendedFab.setText(R.string.start);
        if (data == null) {
            log("没有选择图像", false);
            binding.picture.setImageResource(R.drawable.default_img);
        } else {
            selectedImgUri = data.getData();
            // 显示图像
//                Bitmap bitmap = BitmapFactory.decodeStream(super.getActivity().getContentResolver().openInputStream(uri));
//                binding.picture.setImageBitmap(bitmap);
            imageViewAnimator.start();
            Picasso.get().load(selectedImgUri).placeholder(place_holder).into(binding.picture);
            log("图片读取成功, 点击最下方按钮开始修复", true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}