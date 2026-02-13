package com.example.mediaexplorer.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.mediaexplorer.viewmodel.FavoritesViewModel;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel viewModel;
    private MediaAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.recycler_favorites);
        emptyView = view.findViewById(R.id.tv_empty);

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new MediaAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        // Observe favorites
        viewModel.getFavorites().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                adapter.setItems(items);
                emptyView.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
                        .navigate(R.id.action_favoritesFragment_to_detailsFragment, b);
                } catch (Exception e) {
                    android.util.Log.e("FavoritesFragment", "Navigation to details failed", e);
                    Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFavoriteClick(MediaItem item) {
                viewModel.removeFromFavorites(item);
            }
        });
    }
}

