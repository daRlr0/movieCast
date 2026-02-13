package com.example.moviecast.ui.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviecast.R;
import com.example.moviecast.data.model.MovieWithFavorite;
import com.example.moviecast.data.remote.model.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * MovieAdapter - адаптер списка фильмов на главном экране.
 * Отображает постер, название, рейтинг, кнопку избранного. Glide для картинок.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private List<MovieWithFavorite> movies;
    private OnMovieClickListener listener;
    private OnFavoriteClickListener favoriteListener;
    
    public interface OnMovieClickListener {
        void onMovieClick(Movie movie, View posterView); // posterView для shared transition
    }
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(int movieId, boolean currentStatus);
    }
    
    public MovieAdapter(OnMovieClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.movies = new ArrayList<>();
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }
    
    public void setMovies(List<MovieWithFavorite> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }
    
    public void addMovies(List<MovieWithFavorite> newMovies) {
        int oldSize = movies.size();
        movies.addAll(newMovies);
        notifyItemRangeInserted(oldSize, newMovies.size());
    }
    
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieWithFavorite movieWithFavorite = movies.get(position);
        holder.bind(movieWithFavorite);
    }
    
    @Override
    public int getItemCount() {
        return movies.size();
    }
    
    class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView posterImageView;
        private ImageButton favoriteImageButton;
        private TextView titleTextView;
        private TextView releaseDateTextView;
        private TextView ratingTextView;
        
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.posterImageView);
            favoriteImageButton = itemView.findViewById(R.id.favoriteImageButton);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            releaseDateTextView = itemView.findViewById(R.id.releaseDateTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMovieClick(movies.get(position).getMovie(), posterImageView);
                }
            });
            
            favoriteImageButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && favoriteListener != null) {
                    MovieWithFavorite movieWithFavorite = movies.get(position);
                    animateFavoriteButton();
                    favoriteListener.onFavoriteClick(
                            movieWithFavorite.getMovie().getId(),
                            movieWithFavorite.isFavorite()
                    );
                }
            });
        }
        
        public void bind(MovieWithFavorite movieWithFavorite) {
            Movie movie = movieWithFavorite.getMovie();
            boolean isFavorite = movieWithFavorite.isFavorite();
            
            titleTextView.setText(movie.getTitle());
            releaseDateTextView.setText(movie.getReleaseDate());
            ratingTextView.setText(String.format("%.1f", movie.getVoteAverage()));
            
            // Иконка избранного
            if (isFavorite) {
                favoriteImageButton.setImageResource(R.drawable.ic_heart_filled);
            } else {
                favoriteImageButton.setImageResource(R.drawable.ic_heart_empty);
            }
            
            // Glide - загрузка постера
            String posterUrl = IMAGE_BASE_URL + movie.getPosterPath();
            Glide.with(itemView.getContext())
                    .load(posterUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(posterImageView);
        }
        
        // Анимация при нажатии на сердечко
        private void animateFavoriteButton() {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(favoriteImageButton, "scaleX", 1.0f, 1.3f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(favoriteImageButton, "scaleY", 1.0f, 1.3f, 1.0f);
            
            scaleX.setDuration(300);
            scaleY.setDuration(300);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            
            scaleX.start();
            scaleY.start();
        }
    }
}
