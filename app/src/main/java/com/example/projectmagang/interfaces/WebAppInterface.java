package com.example.projectmagang.interfaces;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.managers.RoleManager;

public class WebAppInterface {
    private Context context;
    private FirebaseManager firebaseManager;
    private RoleManager roleManager;

    public WebAppInterface(Context context) {
        this.context = context;
        this.firebaseManager = FirebaseManager.getInstance();
        this.roleManager = RoleManager.getInstance(context);
    }

    @JavascriptInterface
    public void onPolygonEdited(String regionName, String status, String info) {
        // Check if user is admin
        if (!roleManager.isAdmin()) {
            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, "Hanya admin yang dapat mengedit", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // Convert region name to document ID format
        String regionId = regionName.toLowerCase().replace(" ", "_");

        // Update Firebase
        ((Activity) context).runOnUiThread(() -> {
            firebaseManager.updateRegion(regionId, status, info, new FirebaseManager.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Status berhasil diperbarui", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(context, "Gagal memperbarui: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @JavascriptInterface
    public String getUserRole() {
        return roleManager.getRole();
    }

    @JavascriptInterface
    public void showToast(String message) {
        ((Activity) context).runOnUiThread(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
}