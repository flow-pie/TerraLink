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
    private final Consumer<LoanListItemResponse> onCloseClick;

    public OfficerLoansAdapter(Consumer<LoanListItemResponse> onItemClick, Consumer<LoanListItemResponse> onCloseClick) {
        super(new DiffUtil.ItemCallback<LoanListItemResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull LoanListItemResponse oldItem, @NonNull LoanListItemResponse newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull LoanListItemResponse oldItem, @NonNull LoanListItemResponse newItem) {
                return Objects.equals(oldItem.getLoanNo(), newItem.getLoanNo()) && 
                       Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                       oldItem.getBalance() == newItem.getBalance() &&
                       oldItem.getRepaymentAmount() == newItem.getRepaymentAmount() &&
                       oldItem.getRepaymentProgress() == newItem.getRepaymentProgress();
            }
        });
        this.onItemClick = onItemClick;
        this.onCloseClick = onCloseClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemLoanCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClick, onCloseClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoanCardBinding binding;

        ViewHolder(ItemLoanCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoanListItemResponse item, Consumer<LoanListItemResponse> onClick, Consumer<LoanListItemResponse> onClose) {
            binding.tvClientName.setText(item.getClientFullName());
            binding.tvLoanSub.setText(String.format("%s • %s", item.getLoanNo(), item.getSector() != null ? item.getSector() : "Micro Enterprise"));
            
            // Show Repayment Amount (Approved + Interest)
            binding.tvAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", item.getRepaymentAmount()));
            
            String status = item.getStatus() != null ? item.getStatus() : "ACTIVE";
            binding.tvStatus.setText(status.toUpperCase());
            
            // Status styling
            int badgeBg = R.drawable.bg_status_badge_green;
            int textColor = R.color.status_green;
            int indicatorColor = R.color.status_green;
            boolean showIndicator = false;
            
            if ("OVERDUE".equalsIgnoreCase(status) || "ARREARS".equalsIgnoreCase(status) || "IN ARREARS".equalsIgnoreCase(status) || "IN_ARREARS".equalsIgnoreCase(status)) {
                badgeBg = R.drawable.bg_status_badge_red;
                textColor = R.color.status_red;
                indicatorColor = R.color.status_red;
                showIndicator = true;
            } else if ("PENDING".equalsIgnoreCase(status)) {
                badgeBg = R.drawable.bg_status_badge_red; 
                textColor = R.color.terracotta_primary;
                indicatorColor = R.color.status_amber;
                showIndicator = true;
            } else if ("COMPLETED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) {
                badgeBg = R.drawable.bg_status_badge_blue;
                textColor = R.color.status_blue;
                indicatorColor = R.color.status_blue;
            }
            
            binding.tvStatus.setBackgroundResource(badgeBg);
            binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));
            
            binding.viewStatusIndicator.setVisibility(showIndicator ? View.VISIBLE : View.GONE);
            binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), indicatorColor));

            // Repayment Progress
            int progress = item.getRepaymentProgress();
            binding.progressRepayment.setProgress(progress);
            binding.tvProgressPercent.setText(String.format(Locale.getDefault(), "%d%%", progress));
            
            // Differentiate fully paid loans visually (Green progress bar)
            if (progress >= 100) {
                binding.progressRepayment.setIndicatorColor(ContextCompat.getColor(itemView.getContext(), R.color.status_green));
            } else {
                binding.progressRepayment.setIndicatorColor(ContextCompat.getColor(itemView.getContext(), R.color.terracotta_primary));
            }

            // Logic for "Close Loan" button
            if ("ACTIVE".equalsIgnoreCase(status) && item.getBalance() <= 0) {
                binding.btnCloseLoan.setVisibility(View.VISIBLE);
                binding.btnCloseLoan.setOnClickListener(v -> onClose.accept(item));
            } else {
                binding.btnCloseLoan.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> onClick.accept(item));
        }
    }
}
