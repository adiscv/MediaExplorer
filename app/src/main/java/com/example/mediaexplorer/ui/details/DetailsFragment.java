package com.example.mediaexplorer.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.model.UserReview;
import com.example.mediaexplorer.ui.adapters.CastAdapter;
import com.example.mediaexplorer.viewmodel.DetailsViewModel;

import java.util.ArrayList;

/**
 * Фрагмент, отображающий детальную информацию о конкретном фильме.
 * 
 * Этот фрагмент представляет подробную информацию о фильме, включая постер, описание,
 * рейтинги, информацию об актерах и пользовательские взаимодействия. Он поддерживает
 * как онлайн, так и офлайн режимы, позволяя пользователям получать доступ к сохраненному
 * контенту без подключения к интернету. Фрагмент реализует функциональность персональных
 * отзывов и предоставляет возможности внешней интеграции для поделиться и просмотра трейлера.
 * 
 * Ключевые обязанности:
 * - Отображение постера фильма и детальной информации
 * - Показ списка актеров с фотографиями
 * - Управление статусом избранного с визуальной обратной связью
 * - Обработка персональных отзывов и оценок
 * - Предоставление офлайн-доступа к сохраненному контенту
 * - Включение функций поделиться и просмотра трейлера
 * - Обнаружение и обработка проблем с сетевым подключением
 * 
 * @author Команда Media Explorer
 * @version 1.0
 * @since 2025-02-14
 */
public class DetailsFragment extends Fragment {

    private DetailsViewModel viewModel;
    private ImageView posterImage;
    private TextView titleText;
    private TextView overviewText;
    private TextView ratingText;
    private TextView releaseDateText;
    private Button addToFavoritesButton;
    private Button shareButton;
    private Button trailerButton;
    private ProgressBar progressBar;
    private RecyclerView castRecyclerView;
    private CastAdapter castAdapter;
    private MediaItem currentMovie;
    
    // User review fields
    private RatingBar userRatingBar;
    private EditText userCommentEditText;
    private Button saveReviewButton;
    
    // No internet connection fields
    private LinearLayout noInternetLayout;
    private Button refreshButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        posterImage = view.findViewById(R.id.iv_poster);
        titleText = view.findViewById(R.id.tv_title);
        overviewText = view.findViewById(R.id.tv_overview);
        ratingText = view.findViewById(R.id.tv_rating);
        releaseDateText = view.findViewById(R.id.tv_release_date);
        addToFavoritesButton = view.findViewById(R.id.btn_add_favorites);
        shareButton = view.findViewById(R.id.btn_share);
        trailerButton = view.findViewById(R.id.btn_trailer);
        progressBar = view.findViewById(R.id.progress_bar);
        castRecyclerView = view.findViewById(R.id.recycler_cast);
        
        // User review fields
        userRatingBar = view.findViewById(R.id.rating_bar_user);
        userCommentEditText = view.findViewById(R.id.et_user_comment);
        saveReviewButton = view.findViewById(R.id.btn_save_review);
        
        // No internet connection fields
        noInternetLayout = view.findViewById(R.id.layout_no_internet);
        refreshButton = view.findViewById(R.id.btn_refresh);

        castRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        castAdapter = new CastAdapter();
        castRecyclerView.setAdapter(castAdapter);

        viewModel = new ViewModelProvider(this).get(DetailsViewModel.class);

        // Get movie ID from arguments
        long movieId = 0;
        if (getArguments() != null) {
            movieId = getArguments().getLong("movie_id", 0);
        }

        if (movieId > 0) {
            viewModel.loadMovieDetails(movieId);
        }

        // Observe movie details
        viewModel.getMovieDetails().observe(getViewLifecycleOwner(), movie -> {
            if (movie != null) {
                currentMovie = movie;
                bindMovieData(movie);
                // Check if movie is in favorites and update button
                updateFavoriteButtonState(movie.id);
                // Load user review
                loadUserReview(movie.id);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                if (error.contains("Фильм недоступен офлайн")) {
                    // Show no internet message
                    noInternetLayout.setVisibility(View.VISIBLE);
                } else {
                    // Hide no internet message for other errors
                    noInternetLayout.setVisibility(View.GONE);
                }
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            } else {
                // Hide no internet message when error is cleared
                noInternetLayout.setVisibility(View.GONE);
            }
        });

        // Observe cast list
        viewModel.getCastList().observe(getViewLifecycleOwner(), castList -> {
            if (castList != null && !castList.isEmpty()) {
                castAdapter.setItems(castList);
            }
        });


        // Button listeners
        addToFavoritesButton.setOnClickListener(v -> {
            if (currentMovie != null) {
                new Thread(() -> {
                    boolean isInFavorites = viewModel.isInFavorites(currentMovie.id);
                    requireActivity().runOnUiThread(() -> {
                        if (isInFavorites) {
                            // Remove from favorites
                            viewModel.removeFromFavorites(currentMovie);
                            addToFavoritesButton.setBackgroundResource(R.drawable.item_border);
                            addToFavoritesButton.setText("В избранное");
                            Toast.makeText(requireContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                        } else {
                            // Add to favorites with complete data for offline access
                            MediaItem offlineItem = createOfflineMediaItem(currentMovie);
                            viewModel.addToFavorites(offlineItem);
                            addToFavoritesButton.setBackgroundResource(R.drawable.btn_favorite_pressed);
                            addToFavoritesButton.setText("В избранном");
                            Toast.makeText(requireContext(), "Добавлено в избранное (доступно офлайн)", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });

        shareButton.setOnClickListener(v -> {
            if (currentMovie != null) {
                shareMovie(currentMovie);
            }
        });

        trailerButton.setOnClickListener(v -> {
            if (currentMovie != null) {
                // Open TMDB page in browser
                String url = "https://www.themoviedb.org/movie/" + currentMovie.id;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

        // Save review button listener
        saveReviewButton.setOnClickListener(v -> {
            if (currentMovie != null) {
                saveUserReview();
            }
        });

        // Refresh button listener
        refreshButton.setOnClickListener(v -> {
            if (currentMovie != null) {
                // Hide no internet message and retry loading
                noInternetLayout.setVisibility(View.GONE);
                viewModel.loadMovieDetails(currentMovie.id);
                Toast.makeText(requireContext(), "Обновление...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindMovieData(MediaItem movie) {
        titleText.setText(movie.title != null ? movie.title : "");
        overviewText.setText(movie.overview != null ? movie.overview : "");
        ratingText.setText(String.format("Рейтинг: %.1f/10", movie.voteAverage));
        releaseDateText.setText(movie.releaseDate != null ? movie.releaseDate : "");

        if (movie.posterPath != null) {
            String url = "https://image.tmdb.org/t/p/w500" + movie.posterPath;
            // Enable disk caching for offline access
            Glide.with(requireContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(posterImage);
        }
    }

    private void updateFavoriteButtonState(long movieId) {
        new Thread(() -> {
            boolean isInFavorites = viewModel.isInFavorites(movieId);
            requireActivity().runOnUiThread(() -> {
                if (isInFavorites) {
                    addToFavoritesButton.setBackgroundResource(R.drawable.btn_favorite_pressed);
                    addToFavoritesButton.setText("В избранном");
                } else {
                    addToFavoritesButton.setBackgroundResource(R.drawable.item_border);
                    addToFavoritesButton.setText("В избранное");
                }
            });
        }).start();
    }

    private void shareMovie(MediaItem movie) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = "Check out this movie: " + movie.title +
                          "\nhttps://www.themoviedb.org/movie/" + movie.id;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Movie"));
    }

    private void loadUserReview(long movieId) {
        new Thread(() -> {
            UserReview review = viewModel.getUserReview(movieId);
            requireActivity().runOnUiThread(() -> {
                if (review != null) {
                    userRatingBar.setRating(review.userRating);
                    userCommentEditText.setText(review.userComment != null ? review.userComment : "");
                } else {
                    userRatingBar.setRating(0);
                    userCommentEditText.setText("");
                }
            });
        }).start();
    }

    private void saveUserReview() {
        float rating = userRatingBar.getRating();
        String comment = userCommentEditText.getText().toString().trim();
        
        if (rating == 0 && comment.isEmpty()) {
            Toast.makeText(requireContext(), "Поставьте оценку или напишите комментарий", Toast.LENGTH_SHORT).show();
            return;
        }
        
        UserReview review = new UserReview(currentMovie.id, rating, comment);
        viewModel.saveUserReview(review);
        Toast.makeText(requireContext(), "Отзыв сохранён", Toast.LENGTH_SHORT).show();
    }

    private MediaItem createOfflineMediaItem(MediaItem original) {
        // Create a complete MediaItem with all data for offline access
        return new MediaItem(
            original.id,
            original.title,
            original.overview,
            original.posterPath,
            original.releaseDate,
            original.voteAverage,
            null, // backdropPath - could be fetched if needed
            "", // genres - empty string for now
            "ru" // originalLanguage - assuming Russian from API
        );
    }
}

