package com.example.projectmagang.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class RoleManager {
    private static final String TAG = "RoleManager";
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_ROLE = "user_role";
    private static RoleManager instance;

    private SharedPreferences preferences;
    private String currentRole;

    private RoleManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentRole = preferences.getString(KEY_ROLE, "user");
        Log.d(TAG, "RoleManager initialized | Current role: " + currentRole);
    }

    public static synchronized RoleManager getInstance(Context context) {
        if (instance == null) {
            instance = new RoleManager(context);
        }
        return instance;
    }

    public void setRole(String role) {
        String normalizedRole = role != null ? role.toLowerCase() : "user";
        this.currentRole = normalizedRole;

        boolean saved = preferences.edit().putString(KEY_ROLE, normalizedRole).commit();

        Log.d(TAG, "🔐 Role set: " + normalizedRole + " | Saved: " + saved + " | isAdmin: " + isAdmin());

        String verifyRole = preferences.getString(KEY_ROLE, "ERROR");
        Log.d(TAG, "✅ Verified from SharedPrefs: " + verifyRole);
    }

    public String getRole() {
        currentRole = preferences.getString(KEY_ROLE, "user");
        return currentRole;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentRole);
    }

    public void clear() {
        preferences.edit().clear().commit();
        currentRole = "user";
        Log.d(TAG, "🗑️ RoleManager cleared");
    }
}