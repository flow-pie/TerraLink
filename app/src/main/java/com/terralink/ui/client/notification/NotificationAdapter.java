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
    private final java.util.function.Consumer<NotificationResponse> onNotificationClick;

    public NotificationAdapter(List<NotificationResponse> notifications, java.util.function.Consumer<NotificationResponse> onNotificationClick){
        this.notifications = notifications;
        this.onNotificationClick = onNotificationClick;
    }

    public void setNotifications(List<NotificationResponse> notifications){
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position){
        NotificationResponse notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Notification");
        holder.tvMessage.setText(notification.getBody() != null ? notification.getBody() : "");
        holder.tvDate.setText(formatDate(notification.getCreatedAt()));
        holder.indicatorDot.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> onNotificationClick.accept(notification));
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
        TextView tvTitle, tvMessage, tvDate;
        View indicatorDot;

        public NotificationViewHolder(@NonNull View itemView){
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);
            indicatorDot = itemView.findViewById(R.id.indicatorDot);
        }
    }
}
