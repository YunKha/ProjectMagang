package com.example.projectmagang.models;

import java.util.List;

/**
 * Model class untuk menghitung statistik region
 */
public class RegionStatistics {
    private int totalRegions;
    private int normalCount;
    private int gangguanCount;
    private int dikerjakanCount;

    public RegionStatistics() {
        this.totalRegions = 0;
        this.normalCount = 0;
        this.gangguanCount = 0;
        this.dikerjakanCount = 0;
    }

    /**
     * Calculate statistics from list of regions
     */
    public static RegionStatistics calculate(List<Region> regions) {
        RegionStatistics stats = new RegionStatistics();

        if (regions == null || regions.isEmpty()) {
            return stats;
        }

        stats.totalRegions = regions.size();

        for (Region region : regions) {
            String status = region.getStatus();
            if (status == null) {
                continue;
            }

            switch (status.toLowerCase()) {
                case "normal":
                    stats.normalCount++;
                    break;
                case "gangguan":
                    stats.gangguanCount++;
                    break;
                case "dikerjakan":
                    stats.dikerjakanCount++;
                    break;
            }
        }

        return stats;
    }

    // Getters
    public int getTotalRegions() {
        return totalRegions;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public int getGangguanCount() {
        return gangguanCount;
    }

    public int getDikerjakanCount() {
        return dikerjakanCount;
    }

    // Get percentage
    public int getNormalPercentage() {
        return totalRegions > 0 ? (normalCount * 100) / totalRegions : 0;
    }

    public int getGangguanPercentage() {
        return totalRegions > 0 ? (gangguanCount * 100) / totalRegions : 0;
    }

    public int getDikerjakanPercentage() {
        return totalRegions > 0 ? (dikerjakanCount * 100) / totalRegions : 0;
    }

    // Check if all regions are normal
    public boolean isAllNormal() {
        return totalRegions > 0 && normalCount == totalRegions;
    }

    // Check if there are issues
    public boolean hasIssues() {
        return gangguanCount > 0 || dikerjakanCount > 0;
    }

    @Override
    public String toString() {
        return "RegionStatistics{" +
                "total=" + totalRegions +
                ", normal=" + normalCount +
                ", gangguan=" + gangguanCount +
                ", dikerjakan=" + dikerjakanCount +
                '}';
    }
}