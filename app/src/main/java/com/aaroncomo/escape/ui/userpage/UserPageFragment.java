package com.aaroncomo.escape.ui.userpage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.databinding.FragmentUserpageBinding;
import com.aaroncomo.escape.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class UserPageFragment extends Fragment {
    private FragmentUserpageBinding binding;
    private Boolean hide = true;
    public static String username = "AaronComo";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUserpageBinding.inflate(inflater, container, false);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("DefaultLocale")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0x0:
                        JSONObject userData = (JSONObject) msg.obj;
                        int a = 0, b = 0, c = 0;
                        try {
                            a = userData.getInt("vip_ttl");
                            b = userData.getInt("uploaded");
                            c = userData.getInt("available_time");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        binding.vipTtl.setText(String.format("VIP剩余: %d天", a));
                        binding.uploaded.setText(String.format("上传图片数: %d张", b));
                        binding.availableTime.setText(String.format("剩余修复次数: %d次", c));
                        binding.buyVip.setOnClickListener(v -> UserpageViewModel.requestUserInfo(
                                username, this, "update_vip_ttl"
                        ));
                        break;
                    case 0x1:
                        Toast.makeText(requireContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        };
        UserpageViewModel.requestUserInfo(username, handler, "get");

        // 动态修改ip, port
        binding.submit.setOnClickListener(v -> {
            HttpUtils.ip = Objects.requireNonNull(binding.editIp.getText()).toString();
            HttpUtils.port = Objects.requireNonNull(binding.editPort.getText()).toString();
        });

        // 显示/隐藏ip和端口设置
        binding.avatar.setOnClickListener(v -> {
            if (hide) {
                binding.submit.setVisibility(View.VISIBLE);
                binding.ip.setVisibility(View.VISIBLE);
                binding.port.setVisibility(View.VISIBLE);
            } else {
                binding.submit.setVisibility(View.INVISIBLE);
                binding.ip.setVisibility(View.INVISIBLE);
                binding.port.setVisibility(View.INVISIBLE);
            }
            hide ^= true;
        });

        binding.username.setOnClickListener(v -> UserpageViewModel.requestUserInfo(
                username, handler, "reset"
        ));
        return binding.getRoot();
    }
}
