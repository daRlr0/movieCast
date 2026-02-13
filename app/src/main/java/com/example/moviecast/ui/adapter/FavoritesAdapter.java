package com.example.moviecast.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviecast.R;
import com.example.moviecast.data.local.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * FavoritesAdapter - адаптер списка избранных фильмов (данные из Room).
 * По клику открывается экран деталей.
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
    
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private List<MediaItem> favorites;
    private OnFavoriteClickListener listener;
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(MediaItem mediaItem);
    }
    
    public FavoritesAdapter(OnFavoriteClickListener listener) {
        this.favorites = new ArrayList<>();
        this.listener = listener;
    }
    
    public void setFavorites(List<MediaItem> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new FavoriteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        MediaItem mediaItem = favorites.get(position);
        holder.bind(mediaItem);
    }
    
    @Override
    public int getItemCount() {
        return favorites.size();
    }
    
    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private ImageView posterImageView;
        private TextView titleTextView;
        private TextView releaseDateTextView;
        private TextView ratingTextView;
        
        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.posterImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            releaseDateTextView = itemView.findViewById(R.id.releaseDateTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFavoriteClick(favorites.get(position));
                }
            });
        }
        
        public void bind(MediaItem mediaItem) {
            titleTextView.setText(mediaItem.getTitle());
            releaseDateTextView.setText(mediaItem.getReleaseDate());
            ratingTextView.setText(String.format("%.1f", mediaItem.getVoteAverage()));
            
            String posterUrl = IMAGE_BASE_URL + mediaItem.getPosterPath();
            Glide.with(itemView.getContext()) // Glide - постер
                    .load(posterUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(posterImageView);
        }
    }
}
