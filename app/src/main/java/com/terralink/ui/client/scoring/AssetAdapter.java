package com.terralink.ui.client.scoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.AssetResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.AssetViewHolder> {

    private List<AssetResponse> assets = new ArrayList<>();
    private final OnAssetClickListener listener;
    private boolean isOfficerMode = false;

    public interface OnAssetClickListener {
        void onAssetClick(AssetResponse asset);
        default void onVerifyClick(AssetResponse asset) {}
    }

    public AssetAdapter(OnAssetClickListener listener) {
        this.listener = listener;
    }

    public void setOfficerMode(boolean officerMode) {
        isOfficerMode = officerMode;
    }

    public void setAssets(List<AssetResponse> assets) {
        this.assets = assets != null ? assets : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset_card, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        holder.bind(assets.get(position), listener, isOfficerMode);
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    static class AssetViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAssetType;
        private final TextView tvVerificationStatus;
        private final TextView tvDescription;
        private final TextView tvQuantity;
        private final TextView tvEstimatedValue;
        private final View btnVerify;

        public AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAssetType = itemView.findViewById(R.id.tvAssetType);
            tvVerificationStatus = itemView.findViewById(R.id.tvVerificationStatus);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvEstimatedValue = itemView.findViewById(R.id.tvEstimatedValue);
            btnVerify = itemView.findViewById(R.id.btnVerify);
        }

        public void bind(AssetResponse asset, OnAssetClickListener listener, boolean isOfficerMode) {
            tvAssetType.setText(asset.getAssetType());
            tvDescription.setText(asset.getDescription());
            tvQuantity.setText(String.valueOf(asset.getQuantity()));

            NumberFormat kshFormat = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));
            tvEstimatedValue.setText(kshFormat.format(asset.getEstimatedValue()));

            tvVerificationStatus.setText(asset.getVerificationStatus());
            
            int statusColor;
            int statusBg;
            
            boolean isPending = "PENDING".equalsIgnoreCase(asset.getVerificationStatus());
            
            switch (asset.getVerificationStatus().toUpperCase()) {
                case "VERIFIED":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_green);
                    statusBg = R.drawable.bg_status_badge_green;
                    break;
                case "REJECTED":
                case "SOLD":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_red);
                    statusBg = R.drawable.bg_status_badge_red;
                    break;
                case "PENDING":
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_amber);
                    statusBg = R.drawable.bg_status_badge_amber;
                    break;
            }
            
            tvVerificationStatus.setTextColor(statusColor);
            tvVerificationStatus.setBackgroundResource(statusBg);

            if (isOfficerMode && isPending) {
                btnVerify.setVisibility(View.VISIBLE);
                btnVerify.setOnClickListener(v -> listener.onVerifyClick(asset));
            } else {
                btnVerify.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onAssetClick(asset));
        }
    }
}
