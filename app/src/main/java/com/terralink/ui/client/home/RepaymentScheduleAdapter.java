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

    private final List<RepaymentInstallments> schedules = new ArrayList<>();
    private LoanDetailsResponse loanDetails;

    public RepaymentScheduleAdapter(List<RepaymentInstallments> schedules) {
        this.schedules.addAll(schedules);
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
            binding.tvInstallmentAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", schedule.getTotalDue()));
            binding.tvInstallmentStatus.setText(schedule.getStatus());

            if ("PENDING".equalsIgnoreCase(schedule.getStatus())) {
                binding.tvInstallmentStatus.setBackgroundResource(R.drawable.bg_quick_action1_background);
                binding.tvInstallmentStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.on_terracotta));
                binding.paymentImage.setColorFilter(ContextCompat.getColor(binding.paymentImage.getContext(), R.color.terracotta_primary));
            }
        }
    }
}
