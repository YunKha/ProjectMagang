package com.example.projectmagang.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmagang.R;
import com.example.projectmagang.adapters.RegionAdapter;
import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.models.Region;
import com.example.projectmagang.models.RegionStatistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DescriptionFragment extends Fragment {
    private static final String TAG = "DescriptionFragment";

    // Statistics Views
    private TextView tvTotalRegions;
    private TextView tvNormalCount, tvNormalPercentage;
    private TextView tvGangguanCount, tvGangguanPercentage;
    private TextView tvDikerjakanCount, tvDikerjakanPercentage;
    private TextView tvLastUpdate;

    // RecyclerView
    private RecyclerView recyclerView;
    private RegionAdapter adapter;

    // Firebase
    private FirebaseManager firebaseManager;
    private List<Region> regionsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_description, container, false);

        // Initialize views
        initViews(view);

        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        regionsList = new ArrayList<>();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data
        loadRegionsData();

        return view;
    }

    private void initViews(View view) {
        // Statistics TextViews
        tvTotalRegions = view.findViewById(R.id.tv_total_regions);
        tvNormalCount = view.findViewById(R.id.tv_normal_count);
        tvNormalPercentage = view.findViewById(R.id.tv_normal_percentage);
        tvGangguanCount = view.findViewById(R.id.tv_gangguan_count);
        tvGangguanPercentage = view.findViewById(R.id.tv_gangguan_percentage);
        tvDikerjakanCount = view.findViewById(R.id.tv_dikerjakan_count);
        tvDikerjakanPercentage = view.findViewById(R.id.tv_dikerjakan_percentage);
        tvLastUpdate = view.findViewById(R.id.tv_last_update);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setupRecyclerView() {
        adapter = new RegionAdapter(requireContext(), regionsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadRegionsData() {
        Log.d(TAG, "Loading regions data...");

        // Add real-time listener
        firebaseManager.addRegionsListener(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                Log.d(TAG, "========== REGIONS LOADED ==========");
                Log.d(TAG, "Total regions: " + regions.size());

                // ✅ Debug: Print all region names
                for (int i = 0; i < regions.size(); i++) {
                    Region r = regions.get(i);
                    Log.d(TAG, "Region " + i + ": " + r.getName() + " - " + r.getStatus());
                }
                Log.d(TAG, "====================================");

                // Update list
                regionsList.clear();
                regionsList.addAll(regions);

                // ✅ Notify adapter AFTER data is added
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter notified with " + regionsList.size() + " items");
                }

                // Calculate and update statistics
                updateStatistics(regions);

                // Update last update time
                updateLastUpdateTime();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading regions: " + error);

                // Show error state
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvLastUpdate.setText("⚠️ Error: " + error);
                    });
                }
            }
        });
    }

    /**
     * Calculate and update statistics from regions list
     */
    private void updateStatistics(List<Region> regions) {
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            // Calculate statistics using helper class
            RegionStatistics stats = RegionStatistics.calculate(regions);

            Log.d(TAG, "Statistics: " + stats.toString());

            // Update Total Regions
            tvTotalRegions.setText(String.valueOf(stats.getTotalRegions()));

            // Update Normal Status
            tvNormalCount.setText(String.valueOf(stats.getNormalCount()));
            tvNormalPercentage.setText(stats.getNormalPercentage() + "%");

            // Update Gangguan Status
            tvGangguanCount.setText(String.valueOf(stats.getGangguanCount()));
            tvGangguanPercentage.setText(stats.getGangguanPercentage() + "%");

            // Update Dikerjakan Status
            tvDikerjakanCount.setText(String.valueOf(stats.getDikerjakanCount()));
            tvDikerjakanPercentage.setText(stats.getDikerjakanPercentage() + "%");

            // Log status untuk debugging
            if (stats.isAllNormal()) {
                Log.d(TAG, "✅ All regions are normal!");
            } else if (stats.hasIssues()) {
                Log.w(TAG, "⚠️ Some regions have issues");
            }
        });
    }

    /**
     * Update last update timestamp
     */
    private void updateLastUpdateTime() {
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                    new Locale("id", "ID"));
            String currentTime = sdf.format(new Date());
            tvLastUpdate.setText("🕐 Update terakhir: " + currentTime);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listener when fragment is destroyed
        firebaseManager.removeRegionsListener();
        Log.d(TAG, "Fragment destroyed, listener removed");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
        // Reload data when fragment becomes visible
        if (firebaseManager != null) {
            loadRegionsData();
        }
    }
}