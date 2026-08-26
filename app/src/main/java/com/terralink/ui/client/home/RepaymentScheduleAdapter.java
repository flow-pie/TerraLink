package com.terralink.ui.client.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.RepaymentInstallments;
import com.terralink.databinding.ItemPaymentHistoryBinding;
import com.terralink.ui.client.payment.PaymentDialogActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RepaymentScheduleAdapter extends RecyclerView.Adapter<RepaymentScheduleAdapter.ViewHolder> {

    private static List<RepaymentInstallments> schedules = Collections.emptyList();
    private LoanDetailsResponse loanDetails;

    public RepaymentScheduleAdapter(
            List<RepaymentInstallments> schedules
    ){
        RepaymentScheduleAdapter.schedules =new ArrayList<>(schedules);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentHistoryBinding binding = ItemPaymentHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
                );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepaymentInstallments schedule = schedules.get(position);
        holder.bind(schedule);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void setSchedules(
            List<RepaymentInstallments> schedules, LoanDetailsResponse loanDetails) {

        this.schedules.clear();
        this.schedules.addAll(schedules);
        this.loanDetails = loanDetails;

        notifyDataSetChanged();
    }

    public static  RepaymentInstallments getNextPendingInstallment(){
        for(RepaymentInstallments schedule : schedules){
            if("PENDING".equals(schedule.getStatus())){
                return schedule;
            }
        }
        return null;
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

            // show Pay button for pending installments
            if ("PENDING".equalsIgnoreCase(schedule.getStatus())){
                binding.btnPay.setVisibility(View.VISIBLE);
                binding.btnPay.setOnClickListener(v -> {
                    Context ctx = v.getContext();
                    Intent paymentIntent = new Intent(ctx, PaymentDialogActivity.class);
                    if (loanDetails != null) {
                        paymentIntent.putExtra(PaymentDialogActivity.EXTRA_LOAN_ID, Long.parseLong(loanDetails.getLoanId()));
                        paymentIntent.putExtra(PaymentDialogActivity.EXTRA_SCHEDULE_ID, Objects.requireNonNull(RepaymentScheduleAdapter.getNextPendingInstallment()).getRepaymentScheduleId());
                        paymentIntent.putExtra(PaymentDialogActivity.EXTRA_AMOUNT, loanDetails.getOutStandingAmount());
                        paymentIntent.putExtra(PaymentDialogActivity.EXTRA_INSTALLMENT, loanDetails.getNextInstallmentAmount());
                        paymentIntent.putExtra(PaymentDialogActivity.EXTRA_INSTALLMENT_DUE_DATE, loanDetails.getNextDueDate());
                        ctx.startActivity(paymentIntent);
                    }
                });
            } else {
                binding.btnPay.setVisibility(View.GONE);
            }
        }
    }
}
