package com.example.mediaexplorer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.model.Cast;

import java.util.ArrayList;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.VH> {

    private final List<Cast> items = new ArrayList<>();

    public void setItems(List<Cast> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Cast cast = items.get(position);
        holder.nameText.setText(cast.name != null ? cast.name : "");
        holder.characterText.setText(cast.character != null ? cast.character : "");

        if (cast.profilePath != null) {
            String url = "https://image.tmdb.org/t/p/w185" + cast.profilePath;
            Glide.with(holder.profileImage.getContext()).load(url).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView nameText;
        TextView characterText;

        VH(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_cast_photo);
            nameText = itemView.findViewById(R.id.tv_cast_name);
            characterText = itemView.findViewById(R.id.tv_cast_character);
        }
    }
}

