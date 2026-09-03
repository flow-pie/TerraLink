package com.terralink.ui.client.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.RepaymentInstallments;
import com.terralink.databinding.ItemPaymentHistoryBinding;
import com.terralink.ui.client.payment.PaymentBottomSheetFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RepaymentScheduleAdapter extends RecyclerView.Adapter<RepaymentScheduleAdapter.ViewHolder> {

    public interface OnInstallmentClickListener {
        void onInstallmentClick(RepaymentInstallments schedule);
    }

    private final List<RepaymentInstallments> schedules = new ArrayList<>();
    private final OnInstallmentClickListener listener;
    private LoanDetailsResponse loanDetails;

    public RepaymentScheduleAdapter(List<RepaymentInstallments> schedules, OnInstallmentClickListener listener) {
        this.schedules.addAll(schedules);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentHistoryBinding binding = ItemPaymentHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(schedules.get(position));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void setSchedules(List<RepaymentInstallments> newSchedules, LoanDetailsResponse loanDetails) {
        this.schedules.clear();
        if (newSchedules != null) {
            this.schedules.addAll(newSchedules);
        }
        this.loanDetails = loanDetails;
        notifyDataSetChanged();
    }

    public RepaymentInstallments getNextPendingInstallment() {
        for (RepaymentInstallments schedule : schedules) {
            if ("PENDING".equals(schedule.getStatus())) {
                return schedule;
            }
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPaymentHistoryBinding binding;

        ViewHolder(ItemPaymentHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RepaymentInstallments schedule) {
            binding.tvInstallmentTitle.setText(String.format(Locale.getDefault(), "Installment %d", schedule.getInstallmentNumber()));
            binding.tvInstallmentDate.setText(schedule.getDueDate());
            
            double balance = schedule.getTotalDue() - schedule.getPaidAmount();
            binding.tvInstallmentAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", balance > 0 ? balance : schedule.getTotalDue()));
            
            if (schedule.getPaidAmount() > 0 && !"PAID".equalsIgnoreCase(schedule.getStatus())) {
                binding.installmentProgress.setVisibility(View.VISIBLE);
                int progress = (int) ((schedule.getPaidAmount() / schedule.getTotalDue()) * 100);
                binding.installmentProgress.setProgress(progress);
            } else {
                binding.installmentProgress.setVisibility(View.GONE);
            }

            if ("PENDING".equalsIgnoreCase(schedule.getStatus())) {
                if (schedule.getPaidAmount() > 0) {
                    binding.tvInstallmentStatus.setText(String.format(Locale.getDefault(), "PARTIAL (Paid KES %,.0f)", schedule.getPaidAmount()));
                } else {
                    binding.tvInstallmentStatus.setText(schedule.getStatus());
                }
                binding.tvInstallmentStatus.setBackgroundResource(R.drawable.bg_quick_action1_background);
                binding.tvInstallmentStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.on_terracotta));
                binding.paymentImage.setColorFilter(ContextCompat.getColor(binding.paymentImage.getContext(), R.color.terracotta_primary));
            } else {
                binding.tvInstallmentStatus.setText(schedule.getStatus());
                binding.tvInstallmentStatus.setBackgroundResource(R.drawable.bg_pill_success);
                binding.tvInstallmentStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_green));
                binding.paymentImage.setColorFilter(ContextCompat.getColor(binding.paymentImage.getContext(), R.color.status_green));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInstallmentClick(schedule);
                }
            });
        }
    }
}
