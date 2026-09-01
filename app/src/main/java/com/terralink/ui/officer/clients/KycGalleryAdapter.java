package com.terralink.ui.officer.clients;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.terralink.R;
import com.terralink.data.model.KycDocumentResponse;
import com.terralink.databinding.ItemKycGalleryCardBinding;

import java.util.ArrayList;
import java.util.List;

public class KycGalleryAdapter extends RecyclerView.Adapter<KycGalleryAdapter.ViewHolder> {

    private List<KycDocumentResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(KycDocumentResponse item);
    }

    public KycGalleryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<KycDocumentResponse> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemKycGalleryCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemKycGalleryCardBinding binding;

        ViewHolder(ItemKycGalleryCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(KycDocumentResponse item) {
            binding.tvDocType.setText(item.getDocType().replace("_", " "));
            
            String url = item.getFileUrl();
            if (url != null && !url.isEmpty()) {
                // Prepend base URL if it's a relative path
                if (!url.startsWith("http")) {
                    url = "http://10.0.2.2:5044/" + url; 
                }
                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.avatar)
                        .centerCrop()
                        .into(binding.ivKycDoc);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
