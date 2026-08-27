package com.terralink.ui.officer.loans;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.data.model.LoanListItemResponse;
import com.terralink.databinding.ItemLoanCardBinding;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class OfficerLoansAdapter extends ListAdapter<LoanListItemResponse, OfficerLoansAdapter.ViewHolder> {

    private final Consumer<LoanListItemResponse> onItemClick;

    public OfficerLoansAdapter(Consumer<LoanListItemResponse> onItemClick) {
        super(new DiffUtil.ItemCallback<LoanListItemResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull LoanListItemResponse oldItem, @NonNull LoanListItemResponse newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull LoanListItemResponse oldItem, @NonNull LoanListItemResponse newItem) {
                return Objects.equals(oldItem.getLoanNo(), newItem.getLoanNo()) && 
                       Objects.equals(oldItem.getStatus(), newItem.getStatus());
            }
        });
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemLoanCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoanCardBinding binding;

        ViewHolder(ItemLoanCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoanListItemResponse item, Consumer<LoanListItemResponse> onClick) {
            binding.tvLoanNo.setText(String.format("#%s", item.getLoanNo()));
            binding.tvStatus.setText(item.getStatus() != null ? item.getStatus().toUpperCase() : "");
            binding.tvClientName.setText(item.getClientFullName());
            binding.tvApprovedAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", item.getApprovedAmount()));
            binding.tvOutstanding.setText(String.format(Locale.getDefault(), "KES %,.0f", item.getOutstandingAmount()));
            binding.tvNextDue.setText(String.format("Next Due: %s", item.getNextDueDate()));

            itemView.setOnClickListener(v -> onClick.accept(item));
        }
    }
}
