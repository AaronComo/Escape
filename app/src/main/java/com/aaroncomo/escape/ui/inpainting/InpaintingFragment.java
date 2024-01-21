package com.aaroncomo.escape.ui.inpainting;

import static com.aaroncomo.escape.R.drawable.default_img;
import static com.aaroncomo.escape.R.drawable.place_holder;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.R;
import com.aaroncomo.escape.databinding.FragmentInpaintingBinding;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.Objects;

public class InpaintingFragment extends Fragment {

    private FragmentInpaintingBinding binding;
    private static Uri selectedImgUri = null;
    private static String fileName;
    public static final int CHOOSE_PHOTO = 2;
    private Boolean running = false;
    private ObjectAnimator textViewAnimator, imageViewAnimator;
    private String response = null;
    private String[] items = {"修复", "上传"};
    private int action;


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
        binding.extendedFab.shrink();

        // 创建ObjectAnimator对象，设置TextView、ImageView的动画属性
        textViewAnimator = ObjectAnimator.ofFloat(binding.hint, "alpha", 0f, 1f);
        textViewAnimator.setDuration(500);
        textViewAnimator.start();
        imageViewAnimator = ObjectAnimator.ofFloat(binding.picture, "alpha", 0f, 1f);
        imageViewAnimator.setDuration(1000);
        imageViewAnimator.start();

        // 使用Looper参数构造Handler, 使得Handler与特定线程的消息队列关联, 避免内存泄漏
        Handler handler = new Handler(Looper.getMainLooper()) {
            Boolean type = false;
            Object hint;

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    // 更新uploaded
                    case 0x0:
                    case 0x1:
                        break;

                    // 上传成功
                    case 0x10:
                        type = true;
                        hint = String.format("正在%s中...", items[action]);
                        response = (String) msg.obj;

                        // 启动模型生成
                        viewModel.startModel(response, this, action);
                        break;

                    // 后端返回
                    case 0x11:
                        response = (String) msg.obj;
                        RequestCreator rc = action == 1
                                ? Picasso.get().load(default_img) : Picasso.get().load(response);
                        rc.noPlaceholder().into(binding.picture);
                        hint = String.format("%s完成", items[action]);
                        binding.extendedFab.setText((String) hint);
                        binding.progressbar.hide();
                        binding.extendedFab.extend();
                        type = true;
                        if (action == 1) {
                            viewModel.updateUploaded(this);
                        }

                        // 监听保存图片按钮
                        binding.savePicture.setOnClickListener(v -> viewModel.saveImage(response, this));
                        break;

                    // 保存成功
                    case 0x12:
                        binding.savePicture.setOnClickListener(null);
                        hint = msg.obj;
                        log((String) hint, true);
                        break;

                    // 菜单选择
                    case 0x13:
                        // 开始修复按钮
                        binding.extendedFab.setOnClickListener(v -> startRepair(viewModel, this));
                        action = (int) msg.obj;
                        binding.extendedFab.setText(String.format("开始%s", items[action]));
                        binding.extendedFab.setText(String.format("开始%s", items[action]));
                        binding.extendedFab.extend();
                        hint = String.format("请选择一张待%s的图片", items[action]);
                        type = true;

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
                        break;

                    // 出错
                    default:
                        hint = msg.obj;
                        binding.progressbar.hide();
                        binding.extendedFab.extend();
                }
                log((String) hint, type);
            }
        };

        // 创建适配器
        ArrayAdapter<String>  adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_activated_1, items);
        AutoCompleteTextView menu = binding.menu;
        menu.setAdapter(adapter);   // 用适配器数据设置下拉菜单
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message msg = new Message();
                msg.what = 0x13;
                msg.obj = position;
                handler.sendMessage(msg);
            }
        });
        return root;
    }


    public void startRepair(InpaintingViewModel viewModel, Handler handler) {
        if (running) return;
        running = true;
        // 检查是否选中图片, 选中则监听上传按钮
        if (selectedImgUri != null) {
            binding.extendedFab.shrink();
            binding.progressbar.show();
            log("正在上传中...", true);

            new Thread(() -> {
                // 新建一个png格式缓存图像
                File file = viewModel.createCacheFile(selectedImgUri, requireContext());
                viewModel.uploadImg(file, fileName, handler);
            }).start();
        } else {
            log("请选择一张图片", false);
        }
    }


    public void log(String message, Boolean type) {
        if (!type) {
            binding.hint.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            binding.hint.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
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

    @SuppressLint("Range")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            log("没有选择图像", false);
            binding.picture.setImageResource(R.drawable.default_img);
        } else {
            selectedImgUri = data.getData();
            imageViewAnimator.start();
            Picasso.get().load(selectedImgUri).placeholder(place_holder).into(binding.picture);

            // 获取文件名
            Cursor cursor = requireContext().getContentResolver().query(
                    selectedImgUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
            log(String.format("点击最下方按钮开始%s", items[action]), true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}