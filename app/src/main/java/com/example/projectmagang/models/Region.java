package com.example.projectmagang.models;

import com.google.firebase.Timestamp;

public class Region {
    private String id;
    private String name;
    private String status;
    private String info;
    private Timestamp lastUpdate;

    public Region() {
        // Empty constructor required for Firestore
    }

    public Region(String id, String name, String status, String info, Timestamp lastUpdate) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.info = info;
        this.lastUpdate = lastUpdate;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getInfo() { return info; }
    public Timestamp getLastUpdate() { return lastUpdate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setInfo(String info) { this.info = info; }
    public void setLastUpdate(Timestamp lastUpdate) { this.lastUpdate = lastUpdate; }

    // Get color based on status
    public int getColorResId() {
        switch (status.toLowerCase()) {
            case "normal":
                return android.R.color.holo_green_light;
            case "gangguan":
                return android.R.color.holo_red_light;
            case "dikerjakan":
                return android.R.color.holo_orange_light;
            default:
                return android.R.color.darker_gray;
        }
    }

    // Get status display text
    public String getStatusDisplay() {
        switch (status.toLowerCase()) {
            case "normal":
                return "🟩 Normal";
            case "gangguan":
                return "🟥 Gangguan";
            case "dikerjakan":
                return "🟧 Sedang Dikerjakan";
            default:
                return status;
        }
    }
}