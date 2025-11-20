package com.example.projectmagang.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class RoleManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_ROLE = "user_role";
    private static RoleManager instance;

    private SharedPreferences preferences;
    private String currentRole;

    private RoleManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentRole = preferences.getString(KEY_ROLE, "user");
    }

    public static synchronized RoleManager getInstance(Context context) {
        if (instance == null) {
            instance = new RoleManager(context);
        }
        return instance;
    }

    public void setRole(String role) {
        this.currentRole = role;
        preferences.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        return currentRole;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentRole);
    }

    public void clear() {
        preferences.edit().clear().apply();
        currentRole = "user";
    }
}