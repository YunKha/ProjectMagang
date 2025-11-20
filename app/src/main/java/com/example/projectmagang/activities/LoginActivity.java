package com.example.projectmagang.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmagang.R;
import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.managers.RoleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseManager firebaseManager;
    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firebaseManager = FirebaseManager.getInstance();
        roleManager = RoleManager.getInstance(this);

        // Check if user is already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // ✅ RE-FETCH role to ensure it's updated
            Log.d(TAG, "User already logged in, re-fetching role...");
            showProgress(true);
            fetchUserRoleAndNavigate(currentUser.getUid());
            return;
        }

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        // Login button click
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email harus diisi");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password harus diisi");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            return;
        }

        // Show progress
        showProgress(true);

        // Sign in with Firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login success
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Login successful for: " + user.getEmail());
                            // ✅ Fetch role BEFORE navigating
                            // Fetch user role
                            fetchUserRoleAndNavigate(user.getUid());
                        }
                    } else {
                        // Login failed
                        showProgress(false);
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Login gagal";
                        Toast.makeText(this, "Login gagal: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Login failed", task.getException());
                    }
                });
    }

    private void fetchUserRoleAndNavigate(String uid) {
        Log.d(TAG, "Fetching role for UID: " + uid);

        firebaseManager.getUserRole(uid, role -> {
            // ✅ Save role to SharedPreferences
            roleManager.setRole(role);
            Log.d(TAG, "Role saved: " + role + " (isAdmin: " + roleManager.isAdmin() + ")");

            // ✅ Small delay to ensure role is persisted
            runOnUiThread(() -> {
                showProgress(false);

                // Show success message with role
                String roleDisplay = roleManager.isAdmin() ? "Admin" : "User";
                Toast.makeText(this, "Login berhasil sebagai " + roleDisplay,
                        Toast.LENGTH_SHORT).show();

                // Navigate after a short delay
                new android.os.Handler().postDelayed(() -> {
                    navigateToMain();
                }, 300); // 300ms delay
            });
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }
}