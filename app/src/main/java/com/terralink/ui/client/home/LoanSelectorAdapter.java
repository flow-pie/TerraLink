package com.terralink.ui.client.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.databinding.ItemLoanSelectorBinding;

import java.util.List;
import java.util.Locale;

public class LoanSelectorAdapter extends RecyclerView.Adapter<LoanSelectorAdapter.ViewHolder> {

    private final List<ClientLoansResponse> loans;
    private final OnLoanSelectedListener listener;
    private int selectedPosition = 0;

    public interface OnLoanSelectedListener {
        void onLoanSelected(ClientLoansResponse loan);
    }

    public LoanSelectorAdapter(List<ClientLoansResponse> loans, OnLoanSelectedListener listener) {
        this.loans = loans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemLoanSelectorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(loans.get(position), position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            int newPos = holder.getBindingAdapterPosition();
            if (newPos != RecyclerView.NO_POSITION) {
                selectedPosition = newPos;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                listener.onLoanSelected(loans.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoanSelectorBinding binding;

        ViewHolder(ItemLoanSelectorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClientLoansResponse loan, boolean isSelected) {
            binding.tvLoanNo.setText("#" + loan.getReferenceNo());
            
            if ("Application".equals(loan.getType())) {
                binding.tvLoanAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", loan.getApprovedAmount()));
                binding.tvStatus.setText("SUBMITTED");
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_badge_amber);
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_amber));
                
                binding.progressRepayment.setVisibility(View.GONE);
                binding.tvProgressPercent.setText("0%");
                binding.progressRepayment.setProgress(0);
            } else {
                binding.tvLoanAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", loan.getRepaymentAmount()));
                binding.tvStatus.setText(loan.getStatus().replace("_", " "));
                
                int color = R.color.status_green;
                int bg = R.drawable.bg_status_badge_green;
                
                String status = loan.getStatus();
                if ("PENDING_DISBURSEMENT".equals(status)) {
                    color = R.color.status_amber;
                    bg = R.drawable.bg_status_badge_amber;
                } else if ("ARREARS".equals(status)) {
                    color = R.color.status_red;
                    bg = R.drawable.bg_status_badge_red;
                } else if ("COMPLETED".equals(status)) {
                    color = R.color.status_blue;
                    bg = R.drawable.bg_status_badge_blue;
                }
                
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), color));
                binding.tvStatus.setBackgroundResource(bg);
                
                binding.progressRepayment.setVisibility(View.VISIBLE);
                
                // Calculate Progress
                double total = loan.getRepaymentAmount();
                double balance = loan.getBalance();
                int progress = 0;
                if (total > 0) {
                    progress = (int) (((total - balance) / total) * 100);
                }
                progress = Math.max(0, Math.min(100, progress));
                
                binding.tvProgressPercent.setText(progress + "%");
                binding.progressRepayment.setProgress(progress);
            }

            int strokeColor = isSelected 
                ? ContextCompat.getColor(itemView.getContext(), R.color.terracotta_primary)
                : ContextCompat.getColor(itemView.getContext(), R.color.surface_stroke);
            
            binding.loanCard.setStrokeColor(strokeColor);
            binding.loanCard.setStrokeWidth(isSelected ? 6 : 2);
        }
    }
}
