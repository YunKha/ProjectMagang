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
    private static FirebaseManager instance;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration regionsListener;

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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

    // Update region status
    public void updateRegion(String regionId, String status, String info, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("info", info);
        updates.put("lastUpdate", FieldValue.serverTimestamp());

        db.collection("regions")
                .document(regionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Region updated successfully");
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating region", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // Get all regions once
    public void getRegions(OnRegionsLoadedListener listener) {
        db.collection("regions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Region> regions = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Region region = doc.toObject(Region.class);
                        if (region != null) {
                            region.setId(doc.getId());
                            regions.add(region);
                        }
                    }
                    if (listener != null) listener.onRegionsLoaded(regions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting regions", e);
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Add real-time listener for regions
    public void addRegionsListener(OnRegionsLoadedListener listener) {
        regionsListener = db.collection("regions")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        if (listener != null) listener.onError(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Region> regions = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Region region = doc.toObject(Region.class);
                            if (region != null) {
                                region.setId(doc.getId());
                                regions.add(region);
                            }
                        }
                        if (listener != null) listener.onRegionsLoaded(regions);
                    }
                });
    }

    // Remove listener
    public void removeRegionsListener() {
        if (regionsListener != null) {
            regionsListener.remove();
            regionsListener = null;
        }
    }

    // Get user role
    public void getUserRole(String uid, OnRoleLoadedListener listener) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (listener != null) listener.onRoleLoaded(role != null ? role : "user");
                    } else {
                        if (listener != null) listener.onRoleLoaded("user");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user role", e);
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

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created");
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // Logout
    public void logout() {
        auth.signOut();
        removeRegionsListener();
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