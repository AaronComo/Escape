package com.aaroncomo.escape.ui.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.R;
import com.squareup.picasso.Picasso;

import java.util.List;


class Card {
    private String imageURL;

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

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        Picasso.get().load(card.getImageURL()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}