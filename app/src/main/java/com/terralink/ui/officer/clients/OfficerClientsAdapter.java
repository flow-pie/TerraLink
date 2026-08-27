package com.terralink.ui.officer.clients;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.data.model.ClientListItemResponse;
import com.terralink.databinding.ItemClientCardBinding;

import java.util.Objects;
import java.util.function.Consumer;

public class OfficerClientsAdapter extends ListAdapter<ClientListItemResponse, OfficerClientsAdapter.ViewHolder> {

    private final Consumer<ClientListItemResponse> onItemClick;

    public OfficerClientsAdapter(Consumer<ClientListItemResponse> onItemClick) {
        super(new DiffUtil.ItemCallback<ClientListItemResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull ClientListItemResponse oldItem, @NonNull ClientListItemResponse newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ClientListItemResponse oldItem, @NonNull ClientListItemResponse newItem) {
                return Objects.equals(oldItem.getClientNo(), newItem.getClientNo()) && 
                       Objects.equals(oldItem.getStatus(), newItem.getStatus());
            }
        });
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemClientCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemClientCardBinding binding;

        ViewHolder(ItemClientCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClientListItemResponse item, Consumer<ClientListItemResponse> onClick) {
            binding.tvFullName.setText(item.getFullName());
            binding.tvClientNo.setText(item.getClientNo());
            binding.tvStatus.setText(item.getStatus() != null ? item.getStatus().toUpperCase() : "");
            
            // Set status color/badge logic here if needed
            
            itemView.setOnClickListener(v -> onClick.accept(item));
        }
    }
}
