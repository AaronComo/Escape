package com.aaroncomo.escape.ui.card;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaroncomo.escape.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import android.os.Handler;

public class SheetAdapter extends RecyclerView.Adapter<SheetViewHolder> {
    private List<CardItem> data;
    private Handler handler;

    public SheetAdapter(List<CardItem> data, Handler handler) {
        this.data = data;
        this.handler = handler;
    }


    @NonNull
    @Override
    public SheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_item, parent, false);
        return new SheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SheetViewHolder holder, int position) {
        CardItem item = data.get(position);
        Picasso.get().load(item.getImageID()).into(holder.image);
        holder.hint.setText(item.getHint());
        holder.image.setOnClickListener(v -> {
            Message msg = new Message();
            msg.what = 0x14;
            msg.obj = holder.getAdapterPosition();
            handler.sendMessage(msg);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
