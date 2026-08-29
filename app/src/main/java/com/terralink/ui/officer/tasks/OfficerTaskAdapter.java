package com.terralink.ui.officer.tasks;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.databinding.ItemPendingAppraisalBinding;

import java.util.function.Consumer;

public class OfficerTaskAdapter extends ListAdapter<OfficerTask, OfficerTaskAdapter.ViewHolder> {

    private final Consumer<OfficerTask> onTaskClick;

    public OfficerTaskAdapter(Consumer<OfficerTask> onTaskClick) {
        super(new DiffUtil.ItemCallback<OfficerTask>() {
            @Override
            public boolean areItemsTheSame(@NonNull OfficerTask oldItem, @NonNull OfficerTask newItem) {
                return oldItem.getType() == newItem.getType() && oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull OfficerTask oldItem, @NonNull OfficerTask newItem) {
                return oldItem.getStatus().equals(newItem.getStatus()) && oldItem.getTitle().equals(newItem.getTitle());
            }
        });
        this.onTaskClick = onTaskClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPendingAppraisalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onTaskClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPendingAppraisalBinding binding;

        ViewHolder(ItemPendingAppraisalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OfficerTask item, Consumer<OfficerTask> onClick) {
            binding.tvApplicantName.setText(item.getTitle());
            binding.tvLoanId.setText(item.getSubtitle());
            binding.tvPriority.setText(item.getStatus());
            
            int badgeColor = R.color.status_red_bg;
            int textColor = R.color.status_red;
            
            String status = item.getStatus();
            if ("UNDER_REVIEW".equals(status) || "ACTIVE".equals(status) || "VERIFIED".equals(status)) {
                badgeColor = R.drawable.bg_status_badge_green;
                textColor = R.color.status_green;
            } else if ("INFO_REQUESTED".equals(status) || "PENDING".equals(status) || "PENDING_VERIFICATION".equals(status)) {
                badgeColor = R.drawable.bg_status_badge_amber;
                textColor = R.color.status_amber;
            } else if ("REJECTED".equals(status)) {
                badgeColor = R.color.status_red_bg;
                textColor = R.color.status_red;
            }
            
            binding.tvPriority.setBackgroundResource(badgeColor);
            binding.tvPriority.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));
            
            binding.btnReview.setText(item.getType() == OfficerTask.Type.APPRAISAL ? "APPRAISE" : "VERIFY");
            binding.btnReview.setOnClickListener(v -> onClick.accept(item));
            
            binding.btnDismiss.setOnClickListener(v -> {
                // Logic to dismiss task if needed
            });
        }
    }
}
