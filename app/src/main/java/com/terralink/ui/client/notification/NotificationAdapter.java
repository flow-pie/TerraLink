package com.terralink.ui.client.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terralink.R;
import com.terralink.data.model.NotificationResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationResponse> notifications;

    public NotificationAdapter(List<NotificationResponse> notifications){
        this.notifications = notifications;
    }

    public void setNotifications(List<NotificationResponse> notifications){
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position){
        NotificationResponse notification = notifications.get(position);
        holder.tvTxTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Notification");
        holder.tvTxRef.setText(notification.getMessage() != null ? notification.getMessage() : "");
        holder.tvTxDate.setText(formatDate(notification.getCreatedAt()));
        holder.tvTxAmount.setText(notification.isRead() ? "Read" : "Unread");
    }

    @Override
    public int getItemCount(){
        return notifications != null ? notifications.size() : 0;
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

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{
        TextView tvTxTitle, tvTxRef, tvTxDate, tvTxAmount, tvTxStatus;

        public NotificationViewHolder(@NonNull View itemView){
            super(itemView);
            tvTxTitle = itemView.findViewById(R.id.tvTxTitle);
            tvTxRef = itemView.findViewById(R.id.tvTxRef);
            tvTxDate = itemView.findViewById(R.id.tvTxDate);
            tvTxAmount = itemView.findViewById(R.id.tvTxAmount);
            tvTxStatus = itemView.findViewById(R.id.tvTxStatus);
        }
    }
}
