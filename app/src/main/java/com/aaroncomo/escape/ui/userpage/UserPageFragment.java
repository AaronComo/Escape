package com.aaroncomo.escape.ui.userpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.databinding.FragmentUserpageBinding;
import com.aaroncomo.escape.ui.inpainting.InpaintingViewModel;

import org.json.JSONException;
import org.json.JSONObject;

public class UserPageFragment extends Fragment {
    private FragmentUserpageBinding binding;
    public static String username = "AaronComo";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUserpageBinding.inflate(inflater, container, false);

        UserpageViewModel viewModel =
                new ViewModelProvider(this).get(UserpageViewModel.class);
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
                        break;
                    case 0x1:
                        Toast.makeText(requireContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        };
        viewModel.getUserInfo(username, handler);
        return binding.getRoot();
    }
}
