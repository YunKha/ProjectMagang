package com.example.projectmagang.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectmagang.R;
import com.example.projectmagang.interfaces.WebAppInterface;
import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.managers.RoleManager;
import com.example.projectmagang.models.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MapsFragment extends Fragment {
    private static final String TAG = "MapsFragment";
    private WebView webView;
    private FirebaseManager firebaseManager;
    private RoleManager roleManager;
    private boolean isMapLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // Initialize managers
        firebaseManager = FirebaseManager.getInstance();
        roleManager = RoleManager.getInstance(requireContext());

        // Initialize WebView
        webView = view.findViewById(R.id.webview);
        setupWebView();

        // Setup Firebase listener
        setupFirebaseListener();

        return view;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // ✅ Enable debugging
        WebView.setWebContentsDebuggingEnabled(true);

        // Add JavaScript interface
        webView.addJavascriptInterface(new WebAppInterface(requireContext()), "Android");

        // Set WebView client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isMapLoaded = true;

                // ✅ Get role and log it
                String role = roleManager.getRole();
                boolean isAdmin = roleManager.isAdmin();

                Log.d(TAG, "Map loaded. Setting role: " + role + " (isAdmin: " + isAdmin + ")");

                // Set user role in JavaScript
                String jsCommand = "javascript:setUserRole('" + role + "')";
                webView.evaluateJavascript(jsCommand, value -> {
                    Log.d(TAG, "Role set in WebView: " + role);
                });

                // ✅ Load initial data after role is set
                new android.os.Handler().postDelayed(() -> {
                    loadRegionsData();
                }, 500); // Wait 500ms to ensure JS is ready
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "Map loading started");
            }
        });

        // Load HTML file from assets
        webView.loadUrl("file:///android_asset/map.html");
    }

    private void setupFirebaseListener() {
        firebaseManager.addRegionsListener(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                Log.d(TAG, "Firebase regions updated: " + regions.size() + " regions");
                if (isMapLoaded) {
                    updateMapWithRegions(regions);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Firebase error: " + error);
                // Handle error
            }
        });
    }

    private void loadRegionsData() {
        Log.d(TAG, "Loading initial regions data...");
        firebaseManager.getRegions(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                Log.d(TAG, "Initial regions loaded: " + regions.size());
                updateMapWithRegions(regions);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading initial regions: " + error);
            }
        });
    }

    private void updateMapWithRegions(List<Region> regions) {
        if (!isMapLoaded || getActivity() == null) {
            Log.w(TAG, "Cannot update map - not ready");
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for (Region region : regions) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", region.getId());
                jsonObject.put("name", region.getName());
                jsonObject.put("status", region.getStatus());
                jsonObject.put("info", region.getInfo());
                jsonArray.put(jsonObject);
            }

            String jsonString = jsonArray.toString()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'");

            String javascript = "javascript:updateAllPolygons('" + jsonString + "')";

            getActivity().runOnUiThread(() -> {
                webView.evaluateJavascript(javascript, value -> {
                    Log.d(TAG, "Map updated with " + regions.size() + " regions");
                });
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for map update", e);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // ✅ Re-check role when fragment resumes
        if (isMapLoaded) {
            String role = roleManager.getRole();
            Log.d(TAG, "Fragment resumed, re-setting role: " + role);
            webView.evaluateJavascript("javascript:setUserRole('" + role + "')", null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }
}