package com.terralink.ui.client.notification;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.NotificationResponse;
import com.terralink.databinding.ActivityNotificationStatusBinding;
import com.terralink.ui.client.home.ClientHomepageActivity;
import com.terralink.ui.client.notification.NotificationStatusViewModel;
import com.terralink.ui.common.Resource;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationStatusActivity extends AppCompatActivity {

    private NotificationStatusViewModel viewModel;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNotificationStatusBinding binding = ActivityNotificationStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NotificationStatusViewModel.class);

        notificationAdapter = new NotificationAdapter(new ArrayList<>());
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(notificationAdapter);

        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);

        binding.fabNewAction.setOnClickListener(v -> {
            finish();
        });

        binding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, ClientHomepageActivity.class));
        });

        binding.btnMarkAllRead.setOnClickListener(v -> {
            viewModel.markAllAsRead().observe(this, result -> {
                switch (result.getStatus()){
                    case LOADING:
                        break;
                    case SUCCESS:
                        Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        Toast.makeText(this, "Failed to update notifications", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        });

        viewModel.getNotifications().observe(this, result -> {
            switch (result.getStatus()){
                case LOADING:
                    binding.loadingView.getRoot().setVisibility(android.view.View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.loadingView.getRoot().setVisibility(android.view.View.GONE);
                    List<NotificationResponse> notifications = result.getData();
                    if(notifications != null){
                        notificationAdapter.setNotifications(notifications);
                        if(notifications.isEmpty()){
                            Toast.makeText(this, "No notifications yet", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case ERROR:
                    binding.loadingView.getRoot().setVisibility(android.view.View.GONE);
                    String message = result.getMessage() != null ? result.getMessage() : "Failed to load notifications";
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}
