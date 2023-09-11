package com.aaroncomo.escape.ui.gallery;

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


public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private static Boolean tabNormalActivated = false, tabVIPActivated = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel viewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0x10:
                        List<Card> cards = viewModel.getCardData((JSONObject) msg.obj, "normal");
                        binding.recyclerView.setAdapter(new CardAdapter(cards));

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

        binding.recyclerView.setAdapter(new CardAdapter(new ArrayList<>()));


//        binding.tabNormal.setOnClickListener(v -> {
//            if (!tabNormalActivated) {
//
//            }
//        });

//        final TextView textView = binding.textDashboard;
//        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}