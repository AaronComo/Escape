package com.aaroncomo.escape.ui.gallery;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.aaroncomo.escape.databinding.FragmentGalleryBinding;
import com.aaroncomo.escape.ui.card.CardItem;
import com.aaroncomo.escape.ui.card.CardAdapter;
import com.aaroncomo.escape.ui.userpage.UserpageViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private JSONObject allData;
    private ObjectAnimator animator;
    private Boolean VIPStatus;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel viewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0x0:   // 获取用户VIP数据
                        JSONObject userData = (JSONObject) msg.obj;
                        int vipTTL = 0;
                        try {
                            vipTTL = userData.getInt("vip_ttl");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        VIPStatus = vipTTL > 0;
                        break;
                    case 0x1:   // 服务器连接失败
                        Toast.makeText(requireContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    case 0x10:  // 初始化成功应答, 构建normal界面
                        allData = (JSONObject) msg.obj;
                        List<CardItem> cards = viewModel.getCardData(allData, "normal");
                        binding.recyclerView.setAdapter(new CardAdapter(cards, this));
                        animator.start();

                        // 设置Tab监听
                        Objects.requireNonNull(binding.tabs.getTabAt(0)).view.setOnClickListener(v -> {
                            binding.noVip.setVisibility(View.INVISIBLE);
                            CardAdapter adapter = (CardAdapter) binding.recyclerView.getAdapter();
                            List<CardItem> c = viewModel.getCardData(allData, "normal");
                            assert adapter != null;
                            adapter.setData(c);
                            adapter.notifyDataSetChanged();
                            animator.start();

                        });
                        Objects.requireNonNull(binding.tabs.getTabAt(1)).view.setOnClickListener(v -> {
                            CardAdapter adapter = (CardAdapter) binding.recyclerView.getAdapter();
                            List<CardItem> c = new ArrayList<>();
                            if (!VIPStatus) {
                                binding.noVip.setVisibility(View.VISIBLE);
                            } else {
                                c = viewModel.getCardData(allData, "vip");
                            }
                            assert adapter != null;
                            adapter.setData(c);
                            adapter.notifyDataSetChanged();
                            animator.start();
                        });
                    default:
                        break;
                }
            }
        };

        // 向服务器请求数据
        try {
            viewModel.getDataFromServer(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 设置RecyclerView的管理器和适配器
        binding.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        binding.recyclerView.setAdapter(new CardAdapter(new ArrayList<>(), handler));

        animator = ObjectAnimator.ofFloat(binding.recyclerView, "alpha", 0f, 1f);
        animator.setDuration(1500);

        UserpageViewModel.requestUserInfo("AaronComo", handler, "get");

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}