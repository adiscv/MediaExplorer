package com.example.mediaexplorer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.VH> {

    private final List<MediaItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
        void onFavoriteClick(MediaItem item);
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    public void setItems(List<MediaItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MediaItem item = items.get(position);
        holder.title.setText(item.title == null ? "" : item.title);
        holder.info.setText(item.releaseDate != null ? item.releaseDate.substring(0, 4) : "");

        double ratingValue = item.voteAverage;
        holder.rating.setText(String.format(java.util.Locale.US, "%.1f", ratingValue));
        int ratingColor;
        if (ratingValue >= 7.0) {
            ratingColor = R.color.rating_high;
        } else if (ratingValue >= 5.0) {
            ratingColor = R.color.rating_medium;
        } else {
            ratingColor = R.color.rating_low;
        }
        holder.rating.setTextColor(ContextCompat.getColor(holder.rating.getContext(), ratingColor));
        
        if (item.posterPath != null) {
            String url = "https://image.tmdb.org/t/p/w342" + item.posterPath;
            Glide.with(holder.poster.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.poster);
        } else {
            holder.poster.setImageResource(R.drawable.ic_launcher_foreground);
        }
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(item); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView poster; TextView rating; TextView title; TextView info;
        VH(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.ivPoster);
            rating = itemView.findViewById(R.id.tvRating);
            title = itemView.findViewById(R.id.tvTitle);
            info = itemView.findViewById(R.id.tvInfo);
        }
    }
}

