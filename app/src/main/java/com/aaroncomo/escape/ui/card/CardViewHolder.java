package com.aaroncomo.escape.ui.card;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.R;

public class CardViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public Button like, save;
    public CardViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.card_picture);
        like = itemView.findViewById(R.id.like);
        save = itemView.findViewById(R.id.save);
    }
}
