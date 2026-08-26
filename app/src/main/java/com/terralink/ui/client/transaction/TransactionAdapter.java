package com.terralink.ui.client.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.PaymentHistoryResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<PaymentHistoryResponse> transactions;
    private List<PaymentHistoryResponse> fullList;

    public TransactionAdapter(List<PaymentHistoryResponse> transactions){
        this.transactions = transactions;
        this.fullList = new ArrayList<>(transactions);
    }

    public void setTransactions(List<PaymentHistoryResponse> transactions){
        this.transactions = transactions;
        this.fullList = new ArrayList<>(transactions);
        notifyDataSetChanged();
    }

    public android.widget.Filter getFilter(){
        return new android.widget.Filter() {
            @Override
            protected android.widget.Filter.FilterResults performFiltering(CharSequence constraint) {
                List<PaymentHistoryResponse> filtered = new ArrayList<>();
                if(constraint == null || constraint.length() == 0){
                    filtered.addAll(fullList);
                }else{
                    String query = constraint.toString().toLowerCase().trim();
                    for(PaymentHistoryResponse tx : fullList){
                        if((tx.getLoanNo() != null && tx.getLoanNo().toLowerCase().contains(query)) ||
                           (tx.getReceiptNumber() != null && tx.getReceiptNumber().toLowerCase().contains(query)) ||
                           (tx.getStatus() != null && tx.getStatus().toLowerCase().contains(query))){
                            filtered.add(tx);
                        }
                    }
                }
                android.widget.Filter.FilterResults results = new android.widget.Filter.FilterResults();
                results.values = filtered;
                results.count = filtered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, android.widget.Filter.FilterResults results) {
                transactions = (List<PaymentHistoryResponse>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_card, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position){
        PaymentHistoryResponse tx = transactions.get(position);
        holder.tvTxTitle.setText(tx.getLoanNo() != null ? tx.getLoanNo() : "Payment");
        holder.tvTxRef.setText("Ref: " + (tx.getReceiptNumber() != null ? tx.getReceiptNumber() : ("#"+tx.getId())));
        holder.tvTxDate.setText(formatDate(tx.getPaymentDate()));
        holder.tvTxAmount.setText(String.format("KES %,.2f", tx.getAmount()));
        holder.tvTxStatus.setText(tx.getStatus() != null ? tx.getStatus() : "PENDING");

        int statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
        if(tx.getStatus() != null && tx.getStatus().equalsIgnoreCase("PENDING")){
            statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
        }
        holder.tvTxStatus.setBackgroundColor(statusColor);
    }

    @Override
    public int getItemCount(){
        return transactions != null ? transactions.size() : 0;
    }

    private String formatDate(String dateStr){
        if(dateStr == null || dateStr.isEmpty()) return "";
        try{
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault());
            Date date = input.parse(dateStr);
            return output.format(date);
        }catch(ParseException e){
            return dateStr;
        }
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder{
        TextView tvTxTitle, tvTxRef, tvTxDate, tvTxAmount, tvTxStatus;

        public TransactionViewHolder(@NonNull View itemView){
            super(itemView);
            tvTxTitle = itemView.findViewById(R.id.tvTxTitle);
            tvTxRef = itemView.findViewById(R.id.tvTxRef);
            tvTxDate = itemView.findViewById(R.id.tvTxDate);
            tvTxAmount = itemView.findViewById(R.id.tvTxAmount);
            tvTxStatus = itemView.findViewById(R.id.tvTxStatus);
        }
    }
}
