package com.example.projectmagang.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.projectmagang.R;
import com.example.projectmagang.activities.LoginActivity;
import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.managers.RoleManager;
import com.google.firebase.auth.FirebaseUser;

public class UserFragment extends Fragment {

    private TextView tvName, tvEmail, tvRole;
    private Button btnLogout;

    private FirebaseManager firebaseManager;
    private RoleManager roleManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize managers
        firebaseManager = FirebaseManager.getInstance();
        roleManager = RoleManager.getInstance(requireContext());

        // Initialize views
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvRole = view.findViewById(R.id.tv_role);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Load user info
        loadUserInfo();

        // Logout button
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadUserInfo() {
        FirebaseUser user = firebaseManager.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            String name = user.getDisplayName();

            tvEmail.setText(email != null ? email : "No email");
            tvName.setText(name != null ? name : email);

            String role = roleManager.getRole();
            String roleDisplay = roleManager.isAdmin() ? "👑 Administrator" : "👤 User";
            tvRole.setText(roleDisplay);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> logout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void logout() {
        // Sign out from Firebase
        firebaseManager.logout();

        // Clear role cache
        roleManager.clear();

        // Navigate to login
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}