package com.aaroncomo.escape.ui.card;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;
import com.aaroncomo.escape.utils.ImageUtils;

import java.util.List;


public class CardAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private List<CardItem> cardList;
    private Handler handler;

    public CardAdapter(List<CardItem> cardList, Handler handler) {
        this.cardList = cardList;
        this.handler = handler;
    }

    public void setData(List<CardItem> cardList) {
        this.cardList = cardList;
    }

    @NonNull
    @Override
    // 创建ViewHolder, 用来管理一个View
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    @Override
    // 处理数据
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem card = cardList.get(position);
        Picasso.get().load(card.getImageURL()).into(holder.imageView);

        // 重置按钮
        if (!holder.save.isEnabled()) {
            holder.save.setEnabled(true);
            holder.save.performClick();
        }

        if (!card.saved) {
            holder.save.setEnabled(true);
            holder.save.setText(R.string.save);
            holder.save.setOnClickListener(v -> {
                ImageUtils.saveImage(card.getImageURL(), handler);
                holder.save.setText(R.string.saved);
                holder.save.setEnabled(false);
            });
        }

        // 点击弹出大图
        holder.imageView.setOnClickListener(v -> {
            // 创建一个正方形ImageView TODO: 没正
            ImageView imageView = new ImageView(v.getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(layoutParams);
            Picasso.get().load(card.getImageURL()).into(imageView);

            // 创建对话框包裹ImageView
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
            builder.setView(imageView);
            AlertDialog dialog = builder.create();
            dialog.show();
            final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            dialog.getWindow().setAttributes(params);

        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}