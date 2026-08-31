package com.terralink.ui.client.loan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.terralink.R;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.databinding.ItemLoanCardBinding;
import java.util.List;
import java.util.Locale;

public class LoanListAdapter extends RecyclerView.Adapter<LoanListAdapter.ViewHolder> {

    private final List<ClientLoansResponse> loans;
    private final OnLoanClickListener listener;

    public interface OnLoanClickListener {
        void onLoanClick(ClientLoansResponse loan);
    }

    public LoanListAdapter(List<ClientLoansResponse> loans, OnLoanClickListener listener) {
        this.loans = loans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLoanCardBinding binding = ItemLoanCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(loans.get(position));
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoanCardBinding binding;

        ViewHolder(ItemLoanCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClientLoansResponse loan) {
            binding.tvClientName.setText(loan.getReferenceNo());
            String subtitle = String.format(Locale.getDefault(), "%s • %s", loan.getType(), loan.getStatus());
            binding.tvLoanSub.setText(subtitle);
            binding.tvAmount.setText(String.format(Locale.getDefault(), "KES %,.2f", loan.getApprovedAmount()));
            binding.tvStatus.setText(loan.getStatus());

            boolean isActive = "ACTIVE".equals(loan.getStatus());
            boolean isRejected = "REJECTED".equals(loan.getStatus());
            boolean isClosed = "CLOSED".equals(loan.getStatus());
            
            // UI Styling based on status
            int statusColor;
            int statusBg;
            if (isActive || isClosed) {
                statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_green);
                statusBg = R.drawable.bg_status_badge_green;
            } else if (isRejected) {
                statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_red);
                statusBg = R.drawable.bg_status_badge_red;
            } else {
                statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_amber);
                statusBg = R.drawable.bg_status_badge_amber;
            }

            binding.tvStatus.setTextColor(statusColor);
            binding.tvStatus.setBackgroundResource(statusBg);
            binding.viewStatusIndicator.setBackgroundColor(statusColor);
            binding.viewStatusIndicator.setVisibility(View.VISIBLE);

            // Progress visibility
            if ("Loan".equals(loan.getType()) && (isActive || isClosed)) {
                binding.tvProgressLabel.setVisibility(View.VISIBLE);
                binding.tvProgressPercent.setVisibility(View.VISIBLE);
                binding.progressRepayment.setVisibility(View.VISIBLE);
                
                if (isClosed) {
                    binding.tvProgressLabel.setText("Loan Status");
                    binding.tvProgressPercent.setText("Fully Repaid");
                    binding.progressRepayment.setProgress(100);
                } else {
                    double totalDue = loan.getRepaymentAmount();
                    double balance = loan.getBalance();
                    int progress = 0;
                    if (totalDue > 0) {
                        progress = (int) (((totalDue - balance) / totalDue) * 100);
                    }
                    progress = Math.max(0, Math.min(100, progress));
                    
                    binding.tvProgressLabel.setText("Repayment Progress");
                    binding.tvProgressPercent.setText(progress + "%");
                    binding.progressRepayment.setProgress(progress);
                }
            } else {
                binding.tvProgressLabel.setVisibility(View.GONE);
                binding.tvProgressPercent.setVisibility(View.GONE);
                binding.progressRepayment.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onLoanClick(loan));
        }
    }
}
