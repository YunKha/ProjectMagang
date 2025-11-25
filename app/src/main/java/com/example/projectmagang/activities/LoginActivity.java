package com.example.projectmagang.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmagang.R;
import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.managers.RoleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseManager firebaseManager;
    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views first
        initViews();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firebaseManager = FirebaseManager.getInstance();
        roleManager = RoleManager.getInstance(this);

        // Check if user already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already logged in, re-fetching role...");
            showProgress(true);
            fetchUserRoleAndNavigate(currentUser.getUid());
            return;
        }

        // Setup listeners
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> showRegisterDialog());
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);

        if (progressBar == null) {
            Log.e(TAG, "❌ ProgressBar not found!");
        }
    }

    // ========== LOGIN ==========
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "===== LOGIN ATTEMPT =====");
        Log.d(TAG, "Email: [" + email + "]");
        Log.d(TAG, "Email length: " + email.length());
        Log.d(TAG, "Password length: " + password.length());
        Log.d(TAG, "=========================");

        // Validate
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email harus diisi");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password harus diisi");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }

        showProgress(true);

        // Sign in
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Login successful for: " + user.getEmail());
                            Log.d(TAG, "User UID: " + user.getUid());
                            fetchUserRoleAndNavigate(user.getUid());
                        }
                    } else {
                        showProgress(false);
                        handleLoginError(task.getException(), email);
                    }
                });
    }

    private void handleLoginError(Exception exception, String email) {
        String errorMessage = "Login gagal";

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Email atau password salah";
        } else if (exception != null) {
            String exceptionMsg = exception.getMessage();
            if (exceptionMsg != null) {
                if (exceptionMsg.contains("no user record") ||
                        exceptionMsg.contains("user not found")) {
                    errorMessage = "Email tidak terdaftar";
                    showRegisterSuggestion(email);
                    return;
                } else if (exceptionMsg.contains("disabled")) {
                    errorMessage = "Akun telah dinonaktifkan";
                } else if (exceptionMsg.contains("network")) {
                    errorMessage = "Tidak ada koneksi internet";
                }
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "❌ Login failed: " + errorMessage, exception);
    }

    private void showRegisterSuggestion(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email Tidak Terdaftar")
                .setMessage("Email " + email + " belum terdaftar.\n\nApakah Anda ingin mendaftar?")
                .setPositiveButton("Daftar", (d, w) -> {
                    showRegisterDialog();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ========== REGISTRATION ==========
    private void showRegisterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register, null);

        EditText etRegName = dialogView.findViewById(R.id.et_reg_name);
        EditText etRegEmail = dialogView.findViewById(R.id.et_reg_email);
        EditText etRegPassword = dialogView.findViewById(R.id.et_reg_password);
        EditText etRegPasswordConfirm = dialogView.findViewById(R.id.et_reg_password_confirm);

        // Pre-fill email if already entered
        String currentEmail = etEmail.getText().toString().trim();
        if (!TextUtils.isEmpty(currentEmail)) {
            etRegEmail.setText(currentEmail);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("📝 Daftar Akun Baru")
                .setView(dialogView)
                .setPositiveButton("Daftar", null) // Set null first
                .setNegativeButton("Batal", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnRegister = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnRegister.setOnClickListener(v -> {
                String name = etRegName.getText().toString().trim();
                String email = etRegEmail.getText().toString().trim();
                String password = etRegPassword.getText().toString().trim();
                String passwordConfirm = etRegPasswordConfirm.getText().toString().trim();

                if (validateRegistration(name, email, password, passwordConfirm,
                        etRegName, etRegEmail, etRegPassword, etRegPasswordConfirm)) {
                    dialog.dismiss();
                    registerUser(name, email, password);
                }
            });
        });

        dialog.show();
    }

    private boolean validateRegistration(String name, String email, String password,
                                         String passwordConfirm,
                                         EditText etName, EditText etEmail,
                                         EditText etPassword, EditText etPasswordConfirm) {
        // Validate name
        if (TextUtils.isEmpty(name)) {
            etName.setError("Nama harus diisi");
            etName.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            etName.setError("Nama minimal 3 karakter");
            etName.requestFocus();
            return false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email harus diisi");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password harus diisi");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return false;
        }

        // Validate password confirmation
        if (TextUtils.isEmpty(passwordConfirm)) {
            etPasswordConfirm.setError("Konfirmasi password harus diisi");
            etPasswordConfirm.requestFocus();
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            etPasswordConfirm.setError("Password tidak cocok");
            etPasswordConfirm.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String name, String email, String password) {
        showProgress(true);

        Log.d(TAG, "Creating new user account: " + email);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Firebase Auth user created: " + user.getUid());

                            // Update display name in Firebase Auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "✅ Display name updated");
                                        }
                                    });

                            // Create user document in Firestore
                            createUserDocument(user.getUid(), name, email);
                        }
                    } else {
                        showProgress(false);
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void createUserDocument(String uid, String name, String email) {
        firebaseManager.createUserDocument(uid, name, email, "user",
                new FirebaseManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ User document created in Firestore");

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this,
                                    "✅ Registrasi berhasil! Selamat datang, " + name,
                                    Toast.LENGTH_SHORT).show();

                            // Auto login after registration
                            fetchUserRoleAndNavigate(uid);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        showProgress(false);
                        Log.e(TAG, "❌ Failed to create user document: " + error);

                        // Even if Firestore fails, auth succeeded
                        Toast.makeText(LoginActivity.this,
                                "Registrasi berhasil! Silakan login",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRegistrationError(Exception exception) {
        String errorMessage = "Registrasi gagal";

        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "Email sudah terdaftar. Silakan login.";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "Password terlalu lemah. Gunakan kombinasi huruf dan angka.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Format email tidak valid";
        } else if (exception != null) {
            String exceptionMsg = exception.getMessage();
            if (exceptionMsg != null && exceptionMsg.contains("network")) {
                errorMessage = "Tidak ada koneksi internet";
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "❌ Registration failed: " + errorMessage, exception);
    }

    // ========== FETCH USER DATA ==========
    private void fetchUserRoleAndNavigate(String uid) {
        Log.d(TAG, "Fetching role for UID: " + uid);

        firebaseManager.getUserRole(uid, role -> {
            roleManager.setRole(role);
            Log.d(TAG, "Role saved: " + role + " (isAdmin: " + roleManager.isAdmin() + ")");

            runOnUiThread(() -> {
                showProgress(false);

                String roleDisplay = roleManager.isAdmin() ? "Admin" : "User";
                Toast.makeText(this, "Login berhasil sebagai " + roleDisplay,
                        Toast.LENGTH_SHORT).show();

                new android.os.Handler().postDelayed(this::navigateToMain, 300);
            });
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ========== UI HELPERS ==========
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!show);
        }
        if (tvRegister != null) {
            tvRegister.setEnabled(!show);
        }
        if (etEmail != null) {
            etEmail.setEnabled(!show);
        }
        if (etPassword != null) {
            etPassword.setEnabled(!show);
        }
    }
}