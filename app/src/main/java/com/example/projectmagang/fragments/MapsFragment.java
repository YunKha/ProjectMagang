package com.example.projectmagang.fragments;

import android.os.Bundle;
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

        // Add JavaScript interface
        webView.addJavascriptInterface(new WebAppInterface(requireContext()), "Android");

        // Set WebView client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isMapLoaded = true;

                // Set user role
                String role = roleManager.getRole();
                webView.evaluateJavascript("setUserRole('" + role + "')", null);

                // Load initial data
                loadRegionsData();
            }
        });

        // Load HTML file from assets
        webView.loadUrl("file:///android_asset/map.html");
    }

    private void setupFirebaseListener() {
        firebaseManager.addRegionsListener(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                if (isMapLoaded) {
                    updateMapWithRegions(regions);
                }
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    private void loadRegionsData() {
        firebaseManager.getRegions(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                updateMapWithRegions(regions);
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    private void updateMapWithRegions(List<Region> regions) {
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

            String jsonString = jsonArray.toString().replace("'", "\\'");
            String javascript = "updateAllPolygons('" + jsonString + "')";

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    webView.evaluateJavascript(javascript, null);
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
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