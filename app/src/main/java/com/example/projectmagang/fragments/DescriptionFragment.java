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

        Log.d(TAG, "🎬 onCreateView started");

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
        Log.d(TAG, "🔧 Initializing views...");

        try {
            // Total Regions
            tvTotalRegions = view.findViewById(R.id.tv_total_regions);

            // Normal Status - dari included layout
            View normalView = view.findViewById(R.id.include_status_normal);
            if (normalView != null) {
                tvNormalCount = normalView.findViewById(R.id.tv_normal_count);
                tvNormalPercentage = normalView.findViewById(R.id.tv_normal_percentage);
                Log.d(TAG, "✅ Normal views found");
            } else {
                Log.e(TAG, "❌ Normal view is null!");
            }

            // Gangguan Status - dari included layout
            View gangguanView = view.findViewById(R.id.include_status_gangguan);
            if (gangguanView != null) {
                tvGangguanCount = gangguanView.findViewById(R.id.tv_gangguan_count);
                tvGangguanPercentage = gangguanView.findViewById(R.id.tv_gangguan_percentage);
                Log.d(TAG, "✅ Gangguan views found");
            } else {
                Log.e(TAG, "❌ Gangguan view is null!");
            }

            // Dikerjakan Status - dari included layout
            View dikerjakanView = view.findViewById(R.id.include_status_dikerjakan);
            if (dikerjakanView != null) {
                tvDikerjakanCount = dikerjakanView.findViewById(R.id.tv_dikerjakan_count);
                tvDikerjakanPercentage = dikerjakanView.findViewById(R.id.tv_dikerjakan_percentage);
                Log.d(TAG, "✅ Dikerjakan views found");
            } else {
                Log.e(TAG, "❌ Dikerjakan view is null!");
            }

            tvLastUpdate = view.findViewById(R.id.tv_last_update);

            // RecyclerView
            recyclerView = view.findViewById(R.id.recycler_view);

            // Validate all views
            if (tvTotalRegions == null) Log.e(TAG, "❌ tvTotalRegions is null!");
            if (tvNormalCount == null) Log.e(TAG, "❌ tvNormalCount is null!");
            if (tvNormalPercentage == null) Log.e(TAG, "❌ tvNormalPercentage is null!");
            if (tvGangguanCount == null) Log.e(TAG, "❌ tvGangguanCount is null!");
            if (tvGangguanPercentage == null) Log.e(TAG, "❌ tvGangguanPercentage is null!");
            if (tvDikerjakanCount == null) Log.e(TAG, "❌ tvDikerjakanCount is null!");
            if (tvDikerjakanPercentage == null) Log.e(TAG, "❌ tvDikerjakanPercentage is null!");
            if (tvLastUpdate == null) Log.e(TAG, "❌ tvLastUpdate is null!");
            if (recyclerView == null) Log.e(TAG, "❌ recyclerView is null!");

            // Set default text
            if (tvLastUpdate != null) {
                tvLastUpdate.setText("🔄 Memuat data...");
            }

            // Set default values
            setDefaultValues();

            Log.d(TAG, "✅ All views initialized");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing views", e);
        }
    }

    private void setDefaultValues() {
        try {
            if (tvTotalRegions != null) tvTotalRegions.setText("0");
            if (tvNormalCount != null) tvNormalCount.setText("0");
            if (tvNormalPercentage != null) tvNormalPercentage.setText("0%");
            if (tvGangguanCount != null) tvGangguanCount.setText("0");
            if (tvGangguanPercentage != null) tvGangguanPercentage.setText("0%");
            if (tvDikerjakanCount != null) tvDikerjakanCount.setText("0");
            if (tvDikerjakanPercentage != null) tvDikerjakanPercentage.setText("0%");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting default values", e);
        }
    }

    private void setupRecyclerView() {
        if (getContext() == null || !isAdded()) {
            Log.e(TAG, "❌ Context is null or fragment not attached");
            return;
        }

        if (recyclerView == null) {
            Log.e(TAG, "❌ RecyclerView is null!");
            return;
        }

        try {
            adapter = new RegionAdapter(getContext(), regionsList);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "✅ RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up RecyclerView", e);
        }
    }

    private void loadRegionsData() {
        Log.d(TAG, "🔄 Loading regions data...");

        if (!isAdded()) {
            Log.w(TAG, "⚠️ Fragment not attached, skipping data load");
            return;
        }

        // Add real-time listener
        firebaseManager.addRegionsListener(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "⚠️ Fragment not attached, skipping UI update");
                    return;
                }

                Log.d(TAG, "========== REGIONS LOADED ==========");
                Log.d(TAG, "Total regions: " + regions.size());

                // Debug: Print all region names
                for (int i = 0; i < regions.size(); i++) {
                    Region r = regions.get(i);
                    Log.d(TAG, "Region " + i + ": " + r.getName() + " - " + r.getStatus());
                }
                Log.d(TAG, "====================================");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Clear and update list
                            regionsList.clear();
                            regionsList.addAll(regions);

                            // Notify adapter
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "✅ Adapter notified with " + regionsList.size() + " items");
                            } else {
                                Log.e(TAG, "❌ Adapter is null!");
                            }

                            // Update statistics
                            updateStatistics(regions);

                            // Update last update time
                            updateLastUpdateTime();

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error updating UI", e);
                            showErrorState("Error: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error loading regions: " + error);

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorState(error);
                        }
                    });
                }
            }
        });
    }

    private void updateStatistics(List<Region> regions) {
        try {
            // Calculate statistics
            RegionStatistics stats = RegionStatistics.calculate(regions);

            Log.d(TAG, "📊 Statistics: " + stats.toString());

            // Update Total Regions
            if (tvTotalRegions != null) {
                tvTotalRegions.setText(String.valueOf(stats.getTotalRegions()));
            }

            // Update Normal Status
            if (tvNormalCount != null) {
                tvNormalCount.setText(String.valueOf(stats.getNormalCount()));
            }
            if (tvNormalPercentage != null) {
                tvNormalPercentage.setText(stats.getNormalPercentage() + "%");
            }

            // Update Gangguan Status
            if (tvGangguanCount != null) {
                tvGangguanCount.setText(String.valueOf(stats.getGangguanCount()));
            }
            if (tvGangguanPercentage != null) {
                tvGangguanPercentage.setText(stats.getGangguanPercentage() + "%");
            }

            // Update Dikerjakan Status
            if (tvDikerjakanCount != null) {
                tvDikerjakanCount.setText(String.valueOf(stats.getDikerjakanCount()));
            }
            if (tvDikerjakanPercentage != null) {
                tvDikerjakanPercentage.setText(stats.getDikerjakanPercentage() + "%");
            }

            // Log status
            if (stats.isAllNormal()) {
                Log.d(TAG, "✅ All regions are normal!");
            } else if (stats.hasIssues()) {
                Log.w(TAG, "⚠️ Some regions have issues");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating statistics", e);
        }
    }

    private void updateLastUpdateTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                    new Locale("id", "ID"));
            String currentTime = sdf.format(new Date());

            if (tvLastUpdate != null) {
                tvLastUpdate.setText("🕐 Update terakhir: " + currentTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating timestamp", e);
            if (tvLastUpdate != null) {
                tvLastUpdate.setText("🕐 Update terakhir: -");
            }
        }
    }

    private void showErrorState(String error) {
        if (tvLastUpdate != null) {
            tvLastUpdate.setText("⚠️ Error: " + error);
        }

        // Set default values
        setDefaultValues();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listener
        if (firebaseManager != null) {
            firebaseManager.removeRegionsListener();
        }
        Log.d(TAG, "🗑️ Fragment destroyed, listener removed");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ Fragment resumed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ Fragment paused");
    }
}