package com.aaroncomo.escape.ui.inpainting;

import java.io.File;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.R;
import com.aaroncomo.escape.databinding.FragmentInpaintingBinding;

import static com.aaroncomo.escape.R.drawable.place_holder;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;


public class InpaintingFragment extends Fragment {

    private FragmentInpaintingBinding binding;
    private static Uri selectedImgUri;
    private static String fileName;
    public static final int CHOOSE_PHOTO = 2;
    private Boolean running = false;
    private ObjectAnimator textViewAnimator, imageViewAnimator;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private String response = null;
    private int style = -1;
    private String[] items = {"修复", "合成"};
    private int action;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // 实例化ViewModel并和Fragment绑定
        InpaintingViewModel viewModel =
                new ViewModelProvider(this).get(InpaintingViewModel.class);

        // 绑定binding
        binding = FragmentInpaintingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化控件
        binding.picture.setImageResource(R.drawable.default_img);
        binding.picture.setAdjustViewBounds(true);
        binding.extendedFab.shrink();
        binding.selectPicture.setText("选择图片");

        // 创建ObjectAnimator对象，设置TextView、ImageView的动画属性
        textViewAnimator = ObjectAnimator.ofFloat(binding.hint, "alpha", 0f, 1f);
        textViewAnimator.setDuration(500);
        textViewAnimator.start();
        imageViewAnimator = ObjectAnimator.ofFloat(binding.picture, "alpha", 0f, 1f);
        imageViewAnimator.setDuration(1000);
        imageViewAnimator.start();

        selectedImgUri = null;

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
                        viewModel.startModel(response, this, action, style);
                        break;

                    // 后端返回
                    case 0x11:
                        response = (String) msg.obj;
                        Picasso.get().load(response).noPlaceholder().into(binding.picture);
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
                        binding.extendedFab.setOnClickListener(v -> startRepairOrSynthesis(viewModel, this));
                        action = (int) msg.obj;
                        binding.extendedFab.setText(String.format("开始%s", items[action]));
                        binding.extendedFab.setText(String.format("开始%s", items[action]));
                        binding.extendedFab.extend();
                        hint = String.format("请选择一张待%s的图片", items[action]);
                        type = true;
                        binding.selectPicture.setOnClickListener(v -> selectImage());

                        // 选择图片按钮
                        if (action == 1) {
                            // 展开风格菜单在onResume()中进行
                            binding.selectPicture.setText("选择图像和风格");
                            binding.selectPicture.setOnClickListener(v -> selectImage());
                        } else {
                            binding.selectPicture.setText("选择图片");
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            binding.selectPicture.setOnClickListener(v -> selectImage());
                        }
                        break;

                    // 选择的风格
                    case 0x14:
                        style = (int) msg.obj;
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        type = true;
                        hint = String.format("点击最下方按钮开始%s", items[action]);
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

        binding.menu.setSimpleItems(items);
        binding.menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message msg = new Message();
                msg.what = 0x13;
                msg.obj = position;
                handler.sendMessage(msg);
            }
        });

        // 初始化风格菜单RecycleView, 设置FrameLayout管理对象
        viewModel.initList(binding.bottomSheet, getResources(), requireActivity().getPackageName(), handler);
        FrameLayout bottomSheetLayout = binding.standardBottomSheet;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        return root;
    }

    private void selectImage() {
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
    }

    /**
     * Extended Floating Button的监听, 包含上传、启动模型
     *
     * @param viewModel viewModel
     * @param handler   消息处理
     */
    public void startRepairOrSynthesis(InpaintingViewModel viewModel, Handler handler) {
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
            String a = getResources().getResourceName(R.drawable.style_0);
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
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedImgUri == null) {   // 没选图片
            // 由于选择图像返回后会调用这个方法, 因此根据uri判断是否展开菜单
            binding.menu.setText(null);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {    // 选了图片
            if (action == 1) {  // 合成
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                log("请选择一个壁画风格", true);
            } else {
                log(String.format("点击最下方按钮开始%s", items[action]), true);
            }
            binding.extendedFab.setText(String.format("开始%s", items[action]));
        }
    }
}