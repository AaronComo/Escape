package com.aaroncomo.escape.ui.gallery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.aaroncomo.escape.databinding.FragmentGalleryBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private JSONObject allData;
    private static Boolean tabNormalActivated = false, tabVIPActivated = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel viewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void handleMessage(@NonNull Message msg) {
                // 初始化成功应答, 构建normal界面
                if (msg.what == 0x10) {
                    allData = (JSONObject) msg.obj;
                    List<Card> cards = viewModel.getCardData(allData, "normal");
                    binding.recyclerView.setAdapter(new CardAdapter(cards, this));

                    // 设置Tab监听
                    Objects.requireNonNull(binding.tabs.getTabAt(0)).view.setOnClickListener(v -> {
                        CardAdapter adapter = (CardAdapter) binding.recyclerView.getAdapter();
                        List<Card> c = viewModel.getCardData(allData, "normal");
                        assert adapter != null;
                        adapter.setData(c);
                        adapter.notifyDataSetChanged();

                    });
                    Objects.requireNonNull(binding.tabs.getTabAt(1)).view.setOnClickListener(v -> {
                        CardAdapter adapter = (CardAdapter) binding.recyclerView.getAdapter();
                        List<Card> c = viewModel.getCardData(allData, "vip");
                        assert adapter != null;
                        adapter.setData(c);
                        adapter.notifyDataSetChanged();
                    });
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


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}