package com.aaroncomo.escape.ui.home;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.material.transition.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.aaroncomo.escape.databinding.FragmentHomeBinding;
import com.google.android.material.snackbar.Snackbar;

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
        animator.setDuration(500);
        animator.start();

        binding.cardHome.setX(1000);
        animator = ObjectAnimator.ofFloat(binding.cardHome, "translationX", 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(2000);
        animator.start();

        binding.background.setY(-1000);
        animator = ObjectAnimator.ofFloat(binding.background, "translationY", 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(2000);
        animator.start();
//
//        animator = ObjectAnimator.ofFloat(binding.github, "alpha", 0f, 1f);
//        animator.setDuration(1500);
//        animator.start();

        binding.github.setOnClickListener(v -> {
            String url = "https://github.com/aaroncomo";
            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(intent);
            binding.github.shrink();
        });

        return binding.getRoot();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}