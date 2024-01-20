package com.aaroncomo.escape.ui.home;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaroncomo.escape.R;
import com.aaroncomo.escape.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {
    private ObjectAnimator animator;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // 创建ObjectAnimator对象，设置动画
        animator = ObjectAnimator.ofFloat(binding.container, "alpha", 0f, 1f);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();

        binding.cardHome.setX(1000);
        animator = ObjectAnimator.ofFloat(binding.cardHome, "translationX", 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(1300);
        animator.start();

        binding.background.setY(-1000);
        animator = ObjectAnimator.ofFloat(binding.background, "translationY", 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(1300);
        animator.start();

        binding.github.setOnClickListener(v -> {
            String url = "https://github.com/aaroncomo";
            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(intent);
            binding.github.shrink();
        });

//        binding.logo.setOnClickListener(v -> {
//            FragmentManager fm = requireActivity().getSupportFragmentManager();
//            FragmentTransaction transaction = fm.beginTransaction();
//            transaction.replace(R.id.fragment_container, new UserPageFragment(), null);
//
////            transaction.hide(fm.getPrimaryNavigationFragment());
//            transaction.addToBackStack(null);
//            transaction.commit();
//
//        });

        return binding.getRoot();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}