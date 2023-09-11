package com.aaroncomo.escape.ui.gallery;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.squareup.picasso.Picasso;

import java.util.List;


class Card {
    private String imageURL;
    public Boolean liked = false;

    Card(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageURL() {
        return imageURL;
    }

}


public class CardAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private final List<Card> cardList;

    public CardAdapter(List<Card> cardList) {
        this.cardList = cardList;
    }

    @NonNull
    @Override
    // 创建ViewHolder, 用来
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

        holder.like.setOnClickListener(v -> {
//            if (card.liked) {
//            } else {
//
//            }
//            card.liked = !card.liked;
        });

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