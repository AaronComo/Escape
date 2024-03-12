package com.aaroncomo.escape.ui.card;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.R;

public class SheetViewHolder extends RecyclerView.ViewHolder {
    public ImageView image;
    public TextView hint;

    public SheetViewHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.sheet_item_image);
        hint = itemView.findViewById(R.id.sheet_item_text);
    }
}
