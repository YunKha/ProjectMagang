package com.example.projectmagang.managers;

import android.util.Log;

import com.example.projectmagang.models.Region;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static final String COLLECTION_REGIONS = "regions";
    private static final String COLLECTION_USERS = "users";
    private static FirebaseManager instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private ListenerRegistration regionsListener;

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ✅ Enable offline persistence
        try {
            db.enableNetwork();
        } catch (Exception e) {
            Log.w(TAG, "Network already enabled", e);
        }
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // Update region status with validation
    public void updateRegion(String regionId, String status, String info, OnCompleteListener listener) {
        // ✅ Validate inputs
        if (regionId == null || regionId.trim().isEmpty()) {
            if (listener != null) listener.onFailure("Region ID tidak valid");
            return;
        }

        if (status == null || status.trim().isEmpty()) {
            if (listener != null) listener.onFailure("Status tidak valid");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status.toLowerCase()); // ✅ Normalize status
        updates.put("info", info != null ? info : "");
        updates.put("lastUpdate", FieldValue.serverTimestamp());

        db.collection(COLLECTION_REGIONS)
                .document(regionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Region updated successfully: " + regionId);
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating region: " + regionId, e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // Get all regions once
    public void getRegions(OnRegionsLoadedListener listener) {
        db.collection(COLLECTION_REGIONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Region> regions = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Region region = doc.toObject(Region.class);
                            if (region != null) {
                                region.setId(doc.getId());
                                regions.add(region);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing region: " + doc.getId(), e);
                        }
                    }
                    Log.d(TAG, "Loaded " + regions.size() + " regions");
                    if (listener != null) listener.onRegionsLoaded(regions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting regions", e);
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Add real-time listener for regions
    public void addRegionsListener(OnRegionsLoadedListener listener) {
        // ✅ Remove old listener if exists
        removeRegionsListener();

        regionsListener = db.collection(COLLECTION_REGIONS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        if (listener != null) listener.onError(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Region> regions = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Region region = doc.toObject(Region.class);
                                if (region != null) {
                                    region.setId(doc.getId());
                                    regions.add(region);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing region: " + doc.getId(), e);
                            }
                        }
                        Log.d(TAG, "Real-time update: " + regions.size() + " regions");
                        if (listener != null) listener.onRegionsLoaded(regions);
                    }
                });
    }

    // Remove listener safely
    public void removeRegionsListener() {
        if (regionsListener != null) {
            regionsListener.remove();
            regionsListener = null;
            Log.d(TAG, "Regions listener removed");
        }
    }

    // Get user role with fallback
    public void getUserRole(String uid, OnRoleLoadedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = "user"; // Default role

                    if (documentSnapshot.exists()) {
                        String fetchedRole = documentSnapshot.getString("role");
                        if (fetchedRole != null && !fetchedRole.trim().isEmpty()) {
                            role = fetchedRole.toLowerCase(); // ✅ Normalize role
                        }
                    }

                    Log.d(TAG, "User role: " + role + " for UID: " + uid);
                    if (listener != null) listener.onRoleLoaded(role);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user role", e);
                    // ✅ Return default role on error
                    if (listener != null) listener.onRoleLoaded("user");
                });
    }

    // Create user document after registration
    public void createUserDocument(String uid, String name, String email, OnCompleteListener listener) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", "user");
        user.put("createdAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION_USERS)
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created for: " + uid);
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // Logout and cleanup
    public void logout() {
        removeRegionsListener();
        auth.signOut();
        Log.d(TAG, "User logged out");
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnRegionsLoadedListener {
        void onRegionsLoaded(List<Region> regions);
        void onError(String error);
    }

    public interface OnRoleLoadedListener {
        void onRoleLoaded(String role);
    }
}