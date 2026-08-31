package com.terralink.ui.client.scoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.IncomeAssessmentResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IncomeAssessmentAdapter extends RecyclerView.Adapter<IncomeAssessmentAdapter.ViewHolder> {

    private List<IncomeAssessmentResponse> assessments = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final NumberFormat kshFormat = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));
    private boolean isOfficerMode = false;
    private OnVerifyClickListener listener;

    public interface OnVerifyClickListener {
        void onVerifyClick(IncomeAssessmentResponse assessment);
        void onRejectClick(IncomeAssessmentResponse assessment);
    }

    public void setAssessments(List<IncomeAssessmentResponse> assessments) {
        this.assessments = assessments != null ? assessments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOfficerMode(boolean isOfficerMode, OnVerifyClickListener listener) {
        this.isOfficerMode = isOfficerMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_income_assessment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(assessments.get(position), isOfficerMode, listener);
    }

    @Override
    public int getItemCount() {
        return assessments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvVerificationStatus, tvRevenue, tvExpenses, tvOtherIncome, tvDisposable;
        private final View btnVerify, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvVerificationStatus = itemView.findViewById(R.id.tvVerificationStatus);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            tvExpenses = itemView.findViewById(R.id.tvExpenses);
            tvOtherIncome = itemView.findViewById(R.id.tvOtherIncome);
            tvDisposable = itemView.findViewById(R.id.tvDisposable);
            btnVerify = itemView.findViewById(R.id.btnVerify);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(IncomeAssessmentResponse assessment, boolean isOfficerMode, OnVerifyClickListener listener) {
            if (assessment.getAssessedAt() != null) {
                tvDate.setText(dateFormat.format(assessment.getAssessedAt()));
            } else {
                tvDate.setText("N/A");
            }
            
            tvRevenue.setText(kshFormat.format(assessment.getBusinessRevenue()));
            tvExpenses.setText(kshFormat.format(assessment.getHouseholdExpenses()));
            tvOtherIncome.setText(kshFormat.format(assessment.getOtherIncome()));
            tvDisposable.setText(kshFormat.format(assessment.getDisposableIncome()));

            tvVerificationStatus.setText(assessment.getVerificationStatus());
            
            int statusColor;
            int statusBg;
            
            boolean isPending = "PENDING".equalsIgnoreCase(assessment.getVerificationStatus());

            switch (assessment.getVerificationStatus().toUpperCase()) {
                case "VERIFIED":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_green);
                    statusBg = R.drawable.bg_status_badge_green;
                    break;
                case "REJECTED":
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
                btnReject.setVisibility(View.VISIBLE);
                btnVerify.setOnClickListener(v -> {
                    if (listener != null) listener.onVerifyClick(assessment);
                });
                btnReject.setOnClickListener(v -> {
                    if (listener != null) listener.onRejectClick(assessment);
                });
            } else {
                btnVerify.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
            }
        }
    }
}
