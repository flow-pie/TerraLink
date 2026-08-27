package com.terralink.ui.client.home;

import android.view.LayoutInflater;
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
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onLoanSelected(loans.get(selectedPosition));
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
                binding.tvLoanAmount.setText("Applied for KES");
                binding.tvStatus.setText("SUBMITTED");
                binding.indicatorStatus.getBackground().setTint(ContextCompat.getColor(itemView.getContext(), R.color.status_amber));
            } else {
                binding.tvLoanAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", loan.getApprovedAmount()));
                binding.tvStatus.setText(loan.getStatus().replace("_", " "));
                
                // Status dot color
                int dotColor = R.color.status_green;
                String status = loan.getStatus();
                if ("PENDING_DISBURSEMENT".equals(status)) {
                    dotColor = R.color.status_amber;
                } else if ("OVERDUE".equals(status)) {
                    dotColor = R.color.status_red;
                } else if ("COMPLETED".equals(status)) {
                    dotColor = R.color.status_blue;
                }
                binding.indicatorStatus.getBackground().setTint(ContextCompat.getColor(itemView.getContext(), dotColor));
            }

            int strokeColor = isSelected 
                ? ContextCompat.getColor(itemView.getContext(), R.color.terracotta_primary)
                : ContextCompat.getColor(itemView.getContext(), R.color.surface_stroke);
            
            binding.loanCard.setStrokeColor(strokeColor);
        }
    }
}
