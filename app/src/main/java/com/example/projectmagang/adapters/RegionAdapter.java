package com.example.projectmagang.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmagang.R;
import com.example.projectmagang.models.Region;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.ViewHolder> {
    private static final String TAG = "RegionAdapter";

    private List<Region> regionList;
    private SimpleDateFormat dateFormat;

    public RegionAdapter(Context context, List<Region> regionList) {
        this.regionList = regionList;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_region, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Region region = regionList.get(position);

            // ✅ Null check for region
            if (region == null) {
                Log.e(TAG, "❌ Region at position " + position + " is null!");
                return;
            }

            // Set region name
            String name = region.getName();
            holder.tvRegionName.setText(name != null ? name : "Unknown Region");

            // Set status with null check
            String statusDisplay = region.getStatusDisplay();
            holder.tvStatus.setText(statusDisplay);

            // Set info
            String info = region.getInfo();
            holder.tvInfo.setText(info != null && !info.isEmpty() ? info : "Tidak ada informasi");

            // Format timestamp
            if (region.getLastUpdate() != null) {
                try {
                    String formattedDate = dateFormat.format(region.getLastUpdate().toDate());
                    holder.tvLastUpdate.setText("Terakhir diperbarui: " + formattedDate);
                } catch (Exception e) {
                    Log.e(TAG, "Error formatting date", e);
                    holder.tvLastUpdate.setText("Terakhir diperbarui: -");
                }
            } else {
                holder.tvLastUpdate.setText("Terakhir diperbarui: -");
            }

            // Set status color with null check
            try {
                int color = ContextCompat.getColor(holder.itemView.getContext(), region.getColorResId());
                holder.tvStatus.setTextColor(color);
            } catch (Exception e) {
                Log.e(TAG, "Error setting color", e);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error binding view at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return regionList != null ? regionList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRegionName, tvStatus, tvInfo, tvLastUpdate;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvRegionName = itemView.findViewById(R.id.tv_region_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvInfo = itemView.findViewById(R.id.tv_info);
            tvLastUpdate = itemView.findViewById(R.id.tv_last_update);
        }
    }
}