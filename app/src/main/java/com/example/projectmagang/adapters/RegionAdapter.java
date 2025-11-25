package com.example.projectmagang.adapters;

import android.content.Context;
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
        Region region = regionList.get(position);

        holder.tvRegionName.setText(region.getName());
        holder.tvStatus.setText(region.getStatusDisplay());
        holder.tvInfo.setText(region.getInfo());

        // Format timestamp
        if (region.getLastUpdate() != null) {
            String formattedDate = dateFormat.format(region.getLastUpdate().toDate());
            holder.tvLastUpdate.setText("Terakhir diperbarui: " + formattedDate);
        } else {
            holder.tvLastUpdate.setText("Terakhir diperbarui: -");
        }

        // Set status color
        int color = ContextCompat.getColor(holder.itemView.getContext(), region.getColorResId());
        holder.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return regionList.size();
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