package com.example.projectmagang.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmagang.R;
import com.example.projectmagang.adapters.RegionAdapter;
import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.models.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DescriptionFragment extends Fragment {

    private RecyclerView recyclerView;
    private RegionAdapter adapter;
    private ProgressBar progressBar;
    private FirebaseManager firebaseManager;
    private List<Region> regionList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_description, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize Firebase manager
        firebaseManager = FirebaseManager.getInstance();
        regionList = new ArrayList<>();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data
        loadRegions();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RegionAdapter(regionList);
        recyclerView.setAdapter(adapter);
    }

    private void loadRegions() {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.addRegionsListener(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                progressBar.setVisibility(View.GONE);

                // Sort regions by name
                Collections.sort(regions, new Comparator<Region>() {
                    @Override
                    public int compare(Region r1, Region r2) {
                        return r1.getName().compareTo(r2.getName());
                    }
                });

                regionList.clear();
                regionList.addAll(regions);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Listener will be removed in MainActivity onDestroy
    }
}