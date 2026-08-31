package com.terralink.ui.officer.dashboard;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.databinding.ItemPendingAppraisalBinding;
import com.terralink.ui.officer.scoring.ClientScoringActivity;

import java.util.function.Consumer;

public class PendingAppraisalAdapter extends ListAdapter<LoanApplicationResponse, PendingAppraisalAdapter.ViewHolder> {

    private final Consumer<LoanApplicationResponse> onReviewClick;

    public PendingAppraisalAdapter(Consumer<LoanApplicationResponse> onReviewClick) {
        super(new DiffUtil.ItemCallback<LoanApplicationResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull LoanApplicationResponse oldItem, @NonNull LoanApplicationResponse newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull LoanApplicationResponse oldItem, @NonNull LoanApplicationResponse newItem) {
                return oldItem.getStatus().equals(newItem.getStatus()) && oldItem.getClientFullName().equals(newItem.getClientFullName());
            }
        });
        this.onReviewClick = onReviewClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPendingAppraisalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onReviewClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPendingAppraisalBinding binding;

        ViewHolder(ItemPendingAppraisalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoanApplicationResponse item, Consumer<LoanApplicationResponse> onClick) {
            binding.tvApplicantName.setText(item.getClientFullName());
            binding.tvLoanId.setText("Loan ID: #" + item.getApplicationNo());
            binding.tvPriority.setText(item.getStatus());
            
            // Set priority color based on status
            int badgeColor = R.color.status_red_bg;
            int textColor = R.color.status_red;
            
            if ("UNDER_REVIEW".equals(item.getStatus())) {
                badgeColor = R.drawable.bg_status_badge_green;
                textColor = R.color.status_green;
            } else if ("INFO_REQUESTED".equals(item.getStatus())) {
                badgeColor = R.drawable.bg_status_badge_amber;
                textColor = R.color.status_amber;
            }
            
            binding.tvPriority.setBackgroundResource(badgeColor);
            binding.tvPriority.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));
            
            binding.btnReview.setOnClickListener(v -> onClick.accept(item));
            
            binding.btnDismiss.setText("SCORING");
            binding.btnDismiss.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ClientScoringActivity.class);
                intent.putExtra("clientId", item.getClientId());
                intent.putExtra("clientName", item.getClientFullName());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
