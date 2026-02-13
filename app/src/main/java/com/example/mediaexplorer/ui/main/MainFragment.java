package com.example.mediaexplorer.ui.main;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaexplorer.R;
import com.example.mediaexplorer.model.Genre;
import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.ui.adapters.MediaAdapter;
import com.example.mediaexplorer.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный фрагмент, отображающий сетку популярных фильмов с возможностями поиска и фильтрации.
 * 
 * Этот фрагмент служит основной точкой входа для пользователей для обнаружения и просмотра фильмов.
 * Он реализует адаптивную сетку с бесконечной прокруткой, поиск в реальном времени,
 * фильтрацию по жанрам/годам и комплексную офлайн-поддержку. Фрагмент следует
 * архитектурному паттерну MVVM и обрабатывает все взаимодействия UI, делегируя
 * бизнес-логику в MainViewModel.
 * 
 * Ключевые возможности:
 * - Сеточное отображение популярных фильмов с постерами
 * - Поиск в реальном времени с пагинацией
 * - Фильтрация по жанрам и годам через выпадающие списки
 * - Определение офлайн-режима с понятными сообщениями
 * - Бесконечная прокрутка с автоматической пагинацией
 * - Навигация к деталям фильмов и экрану избранного
 * 
 * @author Команда Media Explorer
 * @version 1.0
 * @since 2025-02-14
 */
public class MainFragment extends Fragment {

    private MainViewModel viewModel;
    private MediaAdapter adapter;
    private ProgressBar progressBar;
    private boolean isLoading = false;
    private GridLayoutManager layoutManager;

    // Элементы фильтров
    private Spinner spinnerGenre;
    private Spinner spinnerYear;
    private Button btnClearFilters;
    private EditText searchEditText;
    private ImageView filterIcon;
    private ImageView favoritesIcon;
    
    // Поля для отсутствия интернет-соединения
    private LinearLayout noInternetLayout;
    private Button refreshButton;
    private View filterContainer;
    private TextView filterActive;

    private boolean genreTouched = false;
    private boolean yearTouched = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_grid, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.recycler_media);
        progressBar = view.findViewById(R.id.progress_bar);

        // Новые элементы интерфейса
        searchEditText = view.findViewById(R.id.search_view);
        filterIcon = view.findViewById(R.id.filter_icon);
        favoritesIcon = view.findViewById(R.id.favorites_icon);
        filterActive = view.findViewById(R.id.tv_filter_active);
        filterContainer = view.findViewById(R.id.filter_container);
        btnClearFilters = view.findViewById(R.id.btn_clear_filters);

        // Initialize no internet connection fields
        noInternetLayout = view.findViewById(R.id.layout_no_internet_main);
        refreshButton = view.findViewById(R.id.btn_refresh_main);

        // Initialize filter views
        spinnerGenre = view.findViewById(R.id.spinner_genre);
        spinnerYear = view.findViewById(R.id.spinner_year);

        layoutManager = new GridLayoutManager(requireContext(), 2);
        rv.setLayoutManager(layoutManager);

        adapter = new MediaAdapter();
        rv.setAdapter(adapter);

        // Setup pagination listener
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    viewModel.loadNextPage();
                }
            }
        });

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Клик по иконке фильтра — показать/скрыть панель фильтров
        filterIcon.setOnClickListener(v -> {
            if (filterContainer.getVisibility() == View.VISIBLE) {
                filterContainer.setVisibility(View.GONE);
            } else {
                filterContainer.setVisibility(View.VISIBLE);
            }
        });

        favoritesIcon.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_mainFragment_to_favoritesFragment);
            } catch (Exception e) {
                android.util.Log.e("MainFragment", "Favorites navigation failed", e);
                Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear filters
        btnClearFilters.setOnClickListener(v -> {
            spinnerGenre.setSelection(0);
            spinnerYear.setSelection(0);
            genreTouched = false;
            yearTouched = false;
            filterContainer.setVisibility(View.GONE);
            viewModel.clearFilters();
            updateFilterIndicator();
        });

        // Refresh button listener
        refreshButton.setOnClickListener(v -> {
            noInternetLayout.setVisibility(View.GONE);
            viewModel.refreshData();
            Toast.makeText(requireContext(), "Обновление...", Toast.LENGTH_SHORT).show();
        });

        // Клик по кнопке поиска
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                    return true;
                }
                return false;
            }
        });

        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        // Setup item click listeners
        adapter.setOnItemClickListener(new MediaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                navigateToDetails(item);
            }

            @Override
            public void onFavoriteClick(MediaItem item) {
                // Navigate to details instead of adding to favorites
                navigateToDetails(item);
            }
        });

        // Setup genre spinner
        setupGenreSpinner();

        // Setup year spinner
        setupYearSpinner();

        spinnerGenre.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                genreTouched = true;
            }
            return false;
        });

        spinnerYear.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                yearTouched = true;
            }
            return false;
        });

        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyCurrentFilters(genreTouched);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyCurrentFilters(yearTouched);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });

        // Observe LiveData
        viewModel.getPopular().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                adapter.setItems(items);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            isLoading = loading;
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Show no internet message for network errors
                if (error.contains("network") || error.contains("internet") || 
                    error.contains("connection") || error.contains("Unable")) {
                    noInternetLayout.setVisibility(View.VISIBLE);
                } else {
                    noInternetLayout.setVisibility(View.GONE);
                }
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            } else {
                // Hide no internet message when error is cleared
                noInternetLayout.setVisibility(View.GONE);
            }
        });

        // Load initial data
        if (viewModel.getCurrentPage() == 0) {
            viewModel.loadPopular(1);
        }

        updateFilterIndicator();
    }

    private void applyCurrentFilters(boolean fromUserAction) {
        String selectedGenreName = String.valueOf(spinnerGenre.getSelectedItem());
        String selectedYearStr = String.valueOf(spinnerYear.getSelectedItem());

        String genreIds = "";
        if (!"All".equalsIgnoreCase(selectedGenreName)) {
            int genreId = Genre.getIdByName(selectedGenreName);
            if (genreId > 0) {
                genreIds = String.valueOf(genreId);
            }
        }

        Integer year = null;
        if (!"All Years".equalsIgnoreCase(selectedYearStr)) {
            try {
                year = Integer.parseInt(selectedYearStr);
            } catch (NumberFormatException ignored) {
                year = null;
            }
        }

        if ((genreIds == null || genreIds.isEmpty()) && year == null) {
            viewModel.clearFilters();
        } else {
            viewModel.applyFilters(genreIds, year);
        }

        updateFilterIndicator();
        if (fromUserAction) {
            filterContainer.setVisibility(View.GONE);
        }
    }

    private void updateFilterIndicator() {
        boolean active = viewModel.isFiltering();
        filterActive.setVisibility(active ? View.VISIBLE : View.GONE);
        btnClearFilters.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    private void setupGenreSpinner() {
        List<String> genres = new ArrayList<>();
        genres.add("All");
        for (Genre genre : Genre.POPULAR_GENRES) {
            genres.add(genre.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                android.R.layout.simple_spinner_item, genres);
        spinnerGenre.setAdapter(adapter);
    }

    private void setupYearSpinner() {
        List<String> years = new ArrayList<>();
        years.add("All Years");

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        for (int i = currentYear; i >= 1950; i--) {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(0);
    }

    private void navigateToDetails(MediaItem item) {
        Bundle bundle = new Bundle();
        bundle.putLong("movie_id", item.id);
        try {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_mainFragment_to_detailsFragment, bundle);
        } catch (Exception e) {
            android.util.Log.e("MainFragment", "Navigation failed", e);
            Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSearch(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("search_query", query);

        try {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_mainFragment_to_searchFragment, bundle);
        } catch (Exception e) {
            android.util.Log.e("MainFragment", "Search navigation failed", e);
            Toast.makeText(requireContext(), "Search navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch(String query) {
        // Hide no internet message when searching
        noInternetLayout.setVisibility(View.GONE);
        
        // Reset state without loading popular movies, then search
        viewModel.resetState();
        viewModel.searchMovies(query);
        
        // Update UI to show search mode
        Toast.makeText(requireContext(), "Поиск: " + query, Toast.LENGTH_SHORT).show();
    }
}
