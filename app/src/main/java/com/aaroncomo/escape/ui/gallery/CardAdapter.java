package com.aaroncomo.escape.ui.gallery;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;
import com.aaroncomo.escape.ImageUtils;

import java.util.List;


class Card {
    private String imageURL;
    public Boolean saved = false;

    Card(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageURL() {
        return imageURL;
    }
}


public class CardAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private List<Card> cardList;
    private Handler handler;

    public CardAdapter(List<Card> cardList, Handler handler) {
        this.cardList = cardList;
        this.handler = handler;
    }

    public void setData(List<Card> cardList) {
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
        Card card = cardList.get(position);
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
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
            ImageView largeImageView = new ImageView(v.getContext());
            Picasso.get().load(card.getImageURL()).into(largeImageView);
            builder.setView(largeImageView).setTitle("大图");
            builder.setPositiveButton("关闭", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}