package com.terralink.ui.client.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.terralink.R;
import com.terralink.databinding.ActivityNotificationStatusBinding;
import com.terralink.ui.client.home.ClientHomepageActivity;
import com.terralink.ui.client.loan.ApplyLoanActivity;
import com.terralink.ui.client.loan.ClientLoansActivity;
import com.terralink.ui.client.profile.ProfileActivity;
import com.terralink.ui.client.transaction.TransactionHistoryActivity;
import com.terralink.ui.common.SnackbarUtils;
import java.util.ArrayList;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationStatusActivity extends AppCompatActivity {

    private NotificationStatusViewModel viewModel;
    private NotificationAdapter notificationAdapter;
    private ActivityNotificationStatusBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NotificationStatusViewModel.class);

        setupNavigation();
        setupNotifications();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadNotifications();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
        setupNavigation();
    }

    private void setupNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ClientHomepageActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if (id == R.id.nav_loans) {
                startActivity(new Intent(this, ClientLoansActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            }
            return false;
        });

        binding.fabNewAction.setOnClickListener(v -> startActivity(new Intent(this, ApplyLoanActivity.class)));
    }

    private void setupNotifications() {
        notificationAdapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            if (!notification.isRead()) {
                viewModel.markAsRead(notification.getId()).observe(this, result -> {
                    if (result.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS) {
                        loadNotifications();
                    }
                });
            }
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(notificationAdapter);

        binding.btnMarkAllRead.setOnClickListener(v -> {
            viewModel.markAllAsRead().observe(this, result -> {
                if (result.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS) {
                    SnackbarUtils.showSuccess(binding.getRoot(), "All marked as read");
                    loadNotifications();
                }
            });
        });

        loadNotifications();
    }

    private void loadNotifications() {
        viewModel.getNotifications().observe(this, result -> {
            if (result.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS && result.getData() != null) {
                notificationAdapter.setNotifications(result.getData());
            } else if (result.getStatus() == com.terralink.ui.auth.LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), result.getMessage());
            }
        });
    }
}
