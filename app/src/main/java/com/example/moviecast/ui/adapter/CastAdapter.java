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
import com.example.moviecast.data.remote.model.Cast;

import java.util.ArrayList;
import java.util.List;

/**
 * CastAdapter - адаптер списка актёров на экране деталей фильма.
 * Фото, имя, роль. Glide для загрузки фото. Показываем до 10 человек.
 */
public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {
    
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w185";
    private List<Cast> castList;
    
    public CastAdapter() {
        this.castList = new ArrayList<>();
    }
    
    public void setCastList(List<Cast> castList) {
        this.castList = castList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.bind(cast);
    }
    
    @Override
    public int getItemCount() {
        return Math.min(castList.size(), 10);
    }
    
    static class CastViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImageView;
        private TextView nameTextView;
        private TextView characterTextView;
        
        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            characterTextView = itemView.findViewById(R.id.characterTextView);
        }
        
        public void bind(Cast cast) {
            nameTextView.setText(cast.getName());
            characterTextView.setText(cast.getCharacter());
            
            if (cast.getProfilePath() != null) {
                String profileUrl = IMAGE_BASE_URL + cast.getProfilePath();
                Glide.with(itemView.getContext()) // Glide - загрузка фото
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}
