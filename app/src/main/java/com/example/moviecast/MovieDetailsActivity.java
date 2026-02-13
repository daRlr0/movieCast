package com.example.moviecast;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.moviecast.databinding.ActivityMovieDetailsBinding;
import com.example.moviecast.ui.adapter.CastAdapter;
import com.example.moviecast.ui.utils.ThemeManager;
import com.example.moviecast.ui.viewmodel.MovieDetailsViewModel;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    
    private ActivityMovieDetailsBinding binding;
    private MovieDetailsViewModel viewModel;
    private CastAdapter castAdapter;
    private int movieId;
    private String trailerKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        binding = ActivityMovieDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable shared element transition
        postponeEnterTransition();

        movieId = getIntent().getIntExtra("movie_id", -1);
        if (movieId == -1) {
            finish();
            return;
        }

        setupCastRecyclerView();
        setupViewModel();
        setupListeners();
        
        viewModel.loadMovieDetails(movieId);
    }

    private void setupCastRecyclerView() {
        castAdapter = new CastAdapter();
        binding.castRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.castRecyclerView.setAdapter(castAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MovieDetailsViewModel.class);
        
        viewModel.getMovie().observe(this, movie -> {
            if (movie != null) {
                binding.titleTextView.setText(movie.getTitle());
                binding.releaseDateTextView.setText(movie.getReleaseDate());
                binding.ratingTextView.setText(String.format("%.1f/10", movie.getVoteAverage()));
                binding.overviewTextView.setText(movie.getOverview());
                
                String posterUrl = IMAGE_BASE_URL + movie.getPosterPath();
                Glide.with(this)
                        .load(posterUrl)
                        .into(binding.posterImageView);
                
                // Start transition after image loads
                binding.posterImageView.post(() -> startPostponedEnterTransition());
            }
        });
        
        viewModel.getGenres().observe(this, genres -> {
            if (genres != null && !genres.isEmpty()) {
                binding.genresTextView.setText(genres);
            }
        });
        
        viewModel.getCast().observe(this, cast -> {
            if (cast != null) {
                castAdapter.setCastList(cast);
            }
        });
        
        viewModel.getTrailerKey().observe(this, key -> {
            trailerKey = key;
            binding.trailerButton.setEnabled(key != null);
        });
        
        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
        
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getIsFavorite().observe(this, isFavorite -> {
            if (isFavorite != null) {
                updateFavoriteIcon(isFavorite);
            }
        });
        
        viewModel.getUserComment().observe(this, comment -> {
            if (comment != null && !comment.equals(binding.commentEditText.getText().toString())) {
                binding.commentEditText.setText(comment);
            }
        });
    }

    private void setupListeners() {
        binding.favoriteImageButton.setOnClickListener(v -> {
            // Animate the button click
            animateFavoriteButton();
            // Toggle favorite status
            viewModel.toggleFavorite(movieId);
        });
        
        binding.saveCommentButton.setOnClickListener(v -> {
            String comment = binding.commentEditText.getText().toString().trim();
            viewModel.updateComment(movieId, comment);
            Toast.makeText(this, "Комментарий сохранён", Toast.LENGTH_SHORT).show();
        });
        
        binding.shareButton.setOnClickListener(v -> {
            shareMovie();
        });
        
        binding.trailerButton.setOnClickListener(v -> {
            openTrailer();
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            binding.favoriteImageButton.setImageResource(R.drawable.ic_heart_filled);
        } else {
            binding.favoriteImageButton.setImageResource(R.drawable.ic_heart_empty);
        }
    }

    private void animateFavoriteButton() {
        // Scale animation for visual feedback
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.favoriteImageButton, "scaleX", 1.0f, 1.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.favoriteImageButton, "scaleY", 1.0f, 1.3f, 1.0f);
        
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleX.start();
        scaleY.start();
    }

    private void shareMovie() {
        if (viewModel.getMovie().getValue() != null) {
            String movieTitle = viewModel.getMovie().getValue().getTitle();
            String shareText = "Посмотрите этот фильм: " + movieTitle;
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Поделиться через"));
        }
    }

    private void openTrailer() {
        if (trailerKey != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(YOUTUBE_BASE_URL + trailerKey));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Трейлер недоступен", Toast.LENGTH_SHORT).show();
        }
    }
}
