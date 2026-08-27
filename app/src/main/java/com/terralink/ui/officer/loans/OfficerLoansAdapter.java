package com.terralink.ui.officer.loans;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.LoanListItemResponse;
import com.terralink.databinding.ItemLoanCardBinding;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class OfficerLoansAdapter extends ListAdapter<LoanListItemResponse, OfficerLoansAdapter.ViewHolder> {

    private final Consumer<LoanListItemResponse> onItemClick;

    public OfficerLoansAdapter(Consumer<LoanListItemResponse> onItemClick) {
        super(new DiffUtil.ItemCallback<LoanListItemResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull LoanListItemResponse oldItem, @NonNull LoanListItemResponse newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull LoanListItemResponse oldItem, @NonNull LoanListItemResponse newItem) {
                return Objects.equals(oldItem.getLoanNo(), newItem.getLoanNo()) && 
                       Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                       oldItem.getRepaymentProgress() == newItem.getRepaymentProgress();
            }
        });
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemLoanCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoanCardBinding binding;

        ViewHolder(ItemLoanCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoanListItemResponse item, Consumer<LoanListItemResponse> onClick) {
            binding.tvClientName.setText(item.getClientFullName());
            binding.tvLoanSub.setText(String.format("%s • %s", item.getLoanNo(), item.getSector() != null ? item.getSector() : "Micro Enterprise"));
            binding.tvAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", item.getApprovedAmount()));
            
            String status = item.getStatus() != null ? item.getStatus() : "ACTIVE";
            binding.tvStatus.setText(status.toUpperCase());
            
            // Status styling
            int badgeBg = R.drawable.bg_status_badge_green;
            int textColor = R.color.status_green;
            int indicatorColor = R.color.status_green;
            boolean showIndicator = false;
            
            if ("OVERDUE".equalsIgnoreCase(status) || "ARREARS".equalsIgnoreCase(status) || "IN ARREARS".equalsIgnoreCase(status)) {
                badgeBg = R.drawable.bg_status_badge_red;
                textColor = R.color.status_red;
                indicatorColor = R.color.status_red;
                showIndicator = true;
            } else if ("PENDING".equalsIgnoreCase(status)) {
                badgeBg = R.drawable.bg_status_badge_red; // Red/Amber
                textColor = R.color.terracotta_primary;
                indicatorColor = R.color.status_amber;
                showIndicator = true;
            }
            
            binding.tvStatus.setBackgroundResource(badgeBg);
            binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));
            
            binding.viewStatusIndicator.setVisibility(showIndicator ? View.VISIBLE : View.GONE);
            binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), indicatorColor));

            int progress = item.getRepaymentProgress();
            binding.progressRepayment.setProgress(progress);
            binding.tvProgressPercent.setText(String.format(Locale.getDefault(), "%d%%", progress));

            itemView.setOnClickListener(v -> onClick.accept(item));
        }
    }
}
