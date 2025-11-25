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

        firebaseManager = FirebaseManager.getInstance();
        roleManager = RoleManager.getInstance(requireContext());

        webView = view.findViewById(R.id.webview);
        setupWebView();
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

        WebView.setWebContentsDebuggingEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(requireContext()), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isMapLoaded = true;

                String role = roleManager.getRole();
                boolean isAdmin = roleManager.isAdmin();

                Log.d(TAG, "🗺️ Map loaded | Role: " + role + " | isAdmin: " + isAdmin);

                webView.evaluateJavascript("javascript:setUserRole('" + role + "')", value -> {
                    Log.d(TAG, "✅ Role set in WebView: " + role);
                });

                new android.os.Handler().postDelayed(() -> {
                    loadRegionsData();
                }, 500);
            }
        });

        webView.loadUrl("file:///android_asset/map.html");
    }

    private void setupFirebaseListener() {
        firebaseManager.addRegionsListener(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                Log.d(TAG, "🔄 Firebase update: " + regions.size() + " regions");
                if (isMapLoaded) {
                    updateMapWithRegions(regions);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Firebase error: " + error);
            }
        });
    }

    private void loadRegionsData() {
        firebaseManager.getRegions(new FirebaseManager.OnRegionsLoadedListener() {
            @Override
            public void onRegionsLoaded(List<Region> regions) {
                Log.d(TAG, "📊 Initial load: " + regions.size() + " regions");
                updateMapWithRegions(regions);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error loading: " + error);
            }
        });
    }

    private void updateMapWithRegions(List<Region> regions) {
        if (!isMapLoaded || getActivity() == null) return;

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
                    Log.d(TAG, "✅ Map updated with " + regions.size() + " regions");
                });
            });
        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON error", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMapLoaded) {
            String role = roleManager.getRole();
            Log.d(TAG, "🔄 Fragment resumed | Re-setting role: " + role);
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