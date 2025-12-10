package com.example.projectmagang.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.projectmagang.R;
import com.example.projectmagang.activities.LoginActivity;
import com.example.projectmagang.managers.FirebaseManager;
import com.example.projectmagang.managers.RoleManager;
import com.google.firebase.auth.FirebaseUser;

public class UserFragment extends Fragment {

    private TextView tvName, tvEmail, tvRole, tvAppVersion;
    private CardView cardAbout, cardDistricts, cardFeatures;
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
        initViews(view);

        // Load user info
        loadUserInfo();

        // Set version
        setAppVersion();

        // Setup click listeners
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvRole = view.findViewById(R.id.tv_role);
        tvAppVersion = view.findViewById(R.id.tv_app_version);

        cardAbout = view.findViewById(R.id.card_about);
        cardDistricts = view.findViewById(R.id.card_districts);
        cardFeatures = view.findViewById(R.id.card_features);

        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void loadUserInfo() {
        FirebaseUser user = firebaseManager.getCurrentUser();

        if (user != null) {
            // Set email
            String email = user.getEmail();
            tvEmail.setText(email != null ? email : "No email");

            // Set name - get from display name or derive from email
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvName.setText(displayName);
            } else if (email != null) {
                // Extract name from email (before @)
                String nameFromEmail = email.split("@")[0];
                // Capitalize first letter
                nameFromEmail = nameFromEmail.substring(0, 1).toUpperCase() +
                        nameFromEmail.substring(1);
                tvName.setText(nameFromEmail);
            } else {
                tvName.setText("User");
            }

            // Set role with icon
            String role = roleManager.getRole();
            String roleDisplay = roleManager.isAdmin() ? "👑 Administrator" : "👤 User";
            tvRole.setText(roleDisplay);
        }
    }

    private void setAppVersion() {
        try {
            PackageInfo pInfo = requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            tvAppVersion.setText("Versi " + version);
        } catch (PackageManager.NameNotFoundException e) {
            tvAppVersion.setText("Versi 1.0");
        }
    }

    private void setupClickListeners() {
        // About Dialog
        cardAbout.setOnClickListener(v -> showAboutDialog());

        // Districts Dialog
        cardDistricts.setOnClickListener(v -> showDistrictsDialog());

        // Features Dialog
        cardFeatures.setOnClickListener(v -> showFeaturesDialog());

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // ========== ABOUT DIALOG ==========
    private void showAboutDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_about, null);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Tutup", null)
                .show();
    }

    // ========== DISTRICTS DIALOG (KELURAHAN) ==========
    private void showDistrictsDialog() {
        // Data kelurahan dikelompokkan per kecamatan
        String[] districts = {
                "PALU BARAT (7 kelurahan):",
                "  • Kawatuna",
                "  • Lasoani",
                "  • Layana Indah",
                "  • Poboya",
                "  • Talise",
                "  • Tanamodindi",
                "  • Tondo",
                "",
                "PALU SELATAN (6 kelurahan):",
                "  • Balaroa",
                "  • Baru",
                "  • Kamonji",
                "  • Lere",
                "  • Siranindi",
                "  • Ujuna",
                "",
                "PALU TIMUR (5 kelurahan):",
                "  • Birobuli Selatan",
                "  • Birobuli Utara",
                "  • Petobo",
                "  • Tatura Selatan",
                "  • Tatura Utara",
                "",
                "PALU UTARA (5 kelurahan):",
                "  • Besusu Barat",
                "  • Besusu Tengah",
                "  • Besusu Timur",
                "  • Lolu Selatan",
                "  • Lolu Utara",
                "",
                "TATANGA (5 kelurahan):",
                "  • Lambara",
                "  • Kayumalue Pajeko",
                "  • Mamboro",
                "  • Mamboro Barat",
                "  • Taipa",
                "",
                "ULUJADI (5 kelurahan):",
                "  • Bayaoge",
                "  • Duyu",
                "  • Nunu",
                "  • Palupi",
                "  • Pengawu",
                "",
                "MANTIKULORE (4 kelurahan):",
                "  • Tawanjuka",
                "  • Baiya",
                "  • Lambara",
                "  • Panau",
                "",
                "TAWAELI (8 kelurahan):",
                "  • Pantoloan",
                "  • Pantoloan Boya",
                "  • Buluri",
                "  • Donggala Kodi",
                "  • Kabonena",
                "  • Silae",
                "  • Tipo",
                "  • Watusampu"
        };

        StringBuilder message = new StringBuilder();
        message.append("Aplikasi ini memantau status jaringan di 46 kelurahan Kota Palu:\n\n");

        for (String district : districts) {
            message.append(district).append("\n");
        }

        message.append("\n\n📊 LEGEND STATUS:\n");
        message.append("🟩 Normal - Jaringan beroperasi normal\n");
        message.append("🟥 Gangguan - Terdapat gangguan jaringan\n");
        message.append("🟧 Dikerjakan - Sedang dalam perbaikan");

        new AlertDialog.Builder(requireContext())
                .setTitle("🗺️ Daftar Kelurahan")
                .setMessage(message.toString())
                .setPositiveButton("Tutup", null)
                .show();
    }

    // ========== FEATURES DIALOG ==========
    private void showFeaturesDialog() {
        String features = "📱 Fitur Aplikasi:\n\n" +
                "🗺️ PETA INTERAKTIF\n" +
                "• Visualisasi status jaringan real-time\n" +
                "• Peta berbasis Leaflet.js\n" +
                "• Zoom dan navigasi peta\n" +
                "• 46 kelurahan Kota Palu\n\n" +

                "📊 MONITORING STATUS\n" +
                "• Status: Normal, Gangguan, Dikerjakan\n" +
                "• Update otomatis dari database\n" +
                "• Informasi detail per kelurahan\n" +
                "• Statistik real-time\n\n" +

                "✏️ EDIT STATUS (Admin)\n" +
                "• Ubah status jaringan\n" +
                "• Tambah informasi gangguan\n" +
                "• Sinkronisasi real-time\n\n" +

                "📋 DESKRIPSI LENGKAP\n" +
                "• Daftar semua kelurahan\n" +
                "• Detail informasi jaringan\n" +
                "• Waktu update terakhir\n" +
                "• Pengelompokan per kecamatan\n\n" +

                "👤 MANAJEMEN PENGGUNA\n" +
                "• Login dengan Email/Password\n" +
                "• Role: Admin & User\n" +
                "• Firebase Authentication\n\n" +

                "🔔 NOTIFIKASI\n" +
                "• Update status real-time\n" +
                "• Monitoring gangguan\n" +
                "• Riwayat perubahan";

        new AlertDialog.Builder(requireContext())
                .setTitle("⚡ Fitur-Fitur")
                .setMessage(features)
                .setPositiveButton("Tutup", null)
                .show();
    }

    // ========== LOGOUT DIALOG ==========
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