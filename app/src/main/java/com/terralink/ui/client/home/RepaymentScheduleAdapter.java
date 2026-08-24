package com.terralink.ui.client.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.data.model.RepaymentInstallments;
import com.terralink.databinding.ItemPaymentHistoryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RepaymentScheduleAdapter extends RecyclerView.Adapter<RepaymentScheduleAdapter.ViewHolder> {

    private final List<RepaymentInstallments> schedules;

    public RepaymentScheduleAdapter(
            List<RepaymentInstallments> schedules
    ){
        this.schedules=new ArrayList<>(schedules);
    }

    @NonNull
    @Override
    public RepaymentScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentHistoryBinding binding = ItemPaymentHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
                );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RepaymentScheduleAdapter.ViewHolder holder, int position) {
        RepaymentInstallments schedule = schedules.get(position);
        holder.bind(schedule);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void setSchedules(
            List<RepaymentInstallments> schedules) {

        this.schedules.clear();
        this.schedules.addAll(schedules);

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPaymentHistoryBinding binding;

        ViewHolder(ItemPaymentHistoryBinding binding){
            super(binding.getRoot());
            this.binding =binding;
        }

        void bind(RepaymentInstallments schedule){
            binding.tvInstallmentTitle.setText(
                    String.format(
                            Locale.getDefault(),
                            "Installment %d",
                            schedule.getInstallmentNumber()
                    )
            );

            binding.tvInstallmentDate.setText(schedule.getDueDate());

            binding.tvInstallmentAmount.setText(
                    String.format(
                            Locale.getDefault(),
                            "KES %, .0f",
                            schedule.getTotalDue()
                    )
            );

            binding.tvInstallmentStatus.setText(
                    schedule.getStatus()
            );
        }
    }
}
