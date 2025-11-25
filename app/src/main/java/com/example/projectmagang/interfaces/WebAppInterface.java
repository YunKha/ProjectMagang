
package com.example.projectmagang.interfaces;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.managers.RoleManager;

public class WebAppInterface {
    private static final String TAG = "WebAppInterface";
    private Context context;
    private FirebaseManager firebaseManager;
    private RoleManager roleManager;

    public WebAppInterface(Context context) {
        this.context = context;
        this.firebaseManager = FirebaseManager.getInstance();
        this.roleManager = RoleManager.getInstance(context);
    }

    @JavascriptInterface
    public void onPolygonEdited(String regionId, String status, String info) {
        Log.d(TAG, "📝 Edit request: " + regionId + " | status: " + status);

        if (!roleManager.isAdmin()) {
            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, "❌ Hanya admin yang dapat mengedit",
                        Toast.LENGTH_SHORT).show();
            });
            return;
        }

        ((Activity) context).runOnUiThread(() -> {
            firebaseManager.updateRegion(regionId, status, info,
                    new FirebaseManager.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(context, "✅ Status berhasil diperbarui",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(context, "❌ Gagal: " + error,
                                    Toast.LENGTH_SHORT).show();
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