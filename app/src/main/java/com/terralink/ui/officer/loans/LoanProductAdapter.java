package com.terralink.ui.officer.loans;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.data.model.LoanProductResponse;
import com.terralink.databinding.ItemLoanProductCardBinding;

import java.util.Locale;
import java.util.function.Consumer;

public class LoanProductAdapter extends ListAdapter<LoanProductResponse, LoanProductAdapter.ViewHolder> {

    private final Consumer<LoanProductResponse> onItemClick;

    public LoanProductAdapter(Consumer<LoanProductResponse> onItemClick) {
        super(new DiffUtil.ItemCallback<LoanProductResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull LoanProductResponse oldItem, @NonNull LoanProductResponse newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull LoanProductResponse oldItem, @NonNull LoanProductResponse newItem) {
                return oldItem.getName().equals(newItem.getName()) && oldItem.getStatus().equals(newItem.getStatus());
            }
        });
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemLoanProductCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoanProductCardBinding binding;

        ViewHolder(ItemLoanProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoanProductResponse item, Consumer<LoanProductResponse> onClick) {
            binding.tvProductName.setText(item.getName() != null ? item.getName() : "Unknown Product");
            binding.tvProductDetails.setText(String.format(Locale.getDefault(), 
                "Interest: %.1f%% • %d-%d Months", 
                item.getInterestRate(), 
                item.getMinimumDuration(), 
                item.getMaximumDuration()));
            
            String status = item.getStatus() != null ? item.getStatus() : "ACTIVE";
            binding.tvStatus.setText(status.toUpperCase());
            
            itemView.setOnClickListener(v -> onClick.accept(item));
        }
    }
}
