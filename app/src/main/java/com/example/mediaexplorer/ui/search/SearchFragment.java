package com.example.mediaexplorer.ui.search;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.ui.adapters.MediaAdapter;
import com.example.mediaexplorer.viewmodel.SearchViewModel;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private MediaAdapter adapter;
    private EditText searchEditText;
    private Button searchButton;
    private ProgressBar progressBar;
    private View emptyState;
    private boolean isLoading = false;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.etSearch);
        searchButton = view.findViewById(R.id.btnSearch);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyState = view.findViewById(R.id.empty_state);
        recyclerView = view.findViewById(R.id.recycler_search);

        layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MediaAdapter();
        recyclerView.setAdapter(adapter);

        // Setup pagination listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        Runnable doSearch = this::performSearch;

        // Fallback: some keyboards don't trigger onEditorAction
        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                doSearch.run();
                return true;
            }
            return false;
        });

        // Auto-search if query passed from MainFragment
        Bundle args = getArguments();
        if (args != null) {
            String initialQuery = args.getString("search_query", "");
            if (initialQuery != null && !initialQuery.trim().isEmpty()) {
                searchEditText.setText(initialQuery);
                doSearch.run();
            }
        }

        // Search button click
        searchButton.setOnClickListener(v -> doSearch.run());

        // IME action (Enter/Search)
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                boolean imeAction = actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE;
                boolean enterKey = event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN;

                if (imeAction || enterKey) {
                    doSearch.run();
                    return true;
                }

                return false;
            }
        });

        // Observe LiveData
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                adapter.setItems(items);
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            } else {
                adapter.setItems(new java.util.ArrayList<>());
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            isLoading = loading;
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Setup item click listeners
        adapter.setOnItemClickListener(new MediaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                Bundle b = new Bundle();
                b.putLong("movie_id", item.id);
                try {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_searchFragment_to_detailsFragment, b);
                } catch (Exception e) {
                    android.util.Log.e("SearchFragment", "Navigation to details failed", e);
                    Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFavoriteClick(MediaItem item) {
                Toast.makeText(requireContext(), "Added to favorites: " + item.title, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            View v = getView();
            if (imm != null && v != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        } catch (Exception ignored) {
        }
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a search query", Toast.LENGTH_SHORT).show();
            adapter.setItems(new java.util.ArrayList<>());
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        hideKeyboard();
        viewModel.search(query, 1);
    }
}

