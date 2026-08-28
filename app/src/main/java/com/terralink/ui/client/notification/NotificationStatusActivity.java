package com.terralink.ui.client.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.LoanApplicationStatusResponse;
import com.terralink.data.model.NotificationResponse;
import com.terralink.databinding.ActivityNotificationStatusBinding;
import com.terralink.databinding.LayoutTimelineItemBinding;
import com.terralink.ui.client.home.ClientHomepageActivity;
import com.terralink.ui.client.loan.ApplyLoanActivity;
import com.terralink.ui.client.loan.ClientLoansActivity;
import com.terralink.ui.client.profile.ProfileActivity;
import com.terralink.ui.client.transaction.TransactionHistoryActivity;
import com.terralink.ui.common.SnackbarUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationStatusActivity extends AppCompatActivity {

    public static final String EXTRA_APPLICATION_ID = "extra_application_id";
    public static final String EXTRA_LOAN_NO = "extra_loan_no";

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

        int applicationId = getIntent().getIntExtra(EXTRA_APPLICATION_ID, -1);
        String loanNo = getIntent().getStringExtra(EXTRA_LOAN_NO);

        if (applicationId != -1) {
            fetchApplicationStatus(applicationId, loanNo);
        } else {
            binding.statusContainer.setVisibility(View.GONE);
        }

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (applicationId != -1) {
                fetchApplicationStatus(applicationId, loanNo);
            }
            setupNotifications();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set selection without triggering navigation
        binding.bottomNavigationView.setOnItemSelectedListener(null);
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
        setupNavigation();
    }

    private void setupNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
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
        binding.appBarContent.btnNotifications.setOnClickListener(v -> { /* Already here */ });
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

    private void fetchApplicationStatus(int id, String loanNo) {
        binding.tvLoanReference.setText(loanNo != null ? "#" + loanNo : "Application #" + id);
        
        viewModel.getLoanStatus(id).observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.loadingView.getRoot().setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.loadingView.getRoot().setVisibility(View.GONE);
                    if (result.getData() != null) {
                        populateTimeline(result.getData());
                    }
                    break;
                case ERROR:
                    binding.loadingView.getRoot().setVisibility(View.GONE);
                    SnackbarUtils.showError(binding.getRoot(), result.getMessage());
                    break;
            }
        });
    }

    private void populateTimeline(LoanApplicationStatusResponse data) {
        binding.statusContainer.setVisibility(View.VISIBLE);
        binding.tvStatusBadge.setText(data.getStatus());

        // Update badge color based on status
        int badgeBg = R.drawable.bg_status_badge_green;
        int badgeText = R.color.status_green;

        if ("REJECTED".equals(data.getStatus())) {
            badgeBg = R.drawable.bg_status_badge_red;
            badgeText = R.color.status_red;
        } else if ("INFO_REQUESTED".equals(data.getStatus())) {
            badgeBg = R.drawable.bg_status_badge_red; 
            badgeText = R.color.terracotta_primary;
        } else if ("SUBMITTED".equals(data.getStatus()) || "UNDER_REVIEW".equals(data.getStatus())) {
            badgeBg = R.drawable.bg_status_badge_red; 
            badgeText = R.color.terracotta_primary;
        }

        binding.tvStatusBadge.setBackgroundResource(badgeBg);
        binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, badgeText));

        // Handle Officer Feedback (Decision Notes)
        if (data.getDecisionNotes() != null && !data.getDecisionNotes().trim().isEmpty()) {
            binding.cardOfficerFeedback.setVisibility(View.VISIBLE);
            
            // Format decision date
            String decisionDate = data.getDecidedAt() != null ? formatDate(data.getDecidedAt()) : "Recent";
            binding.tvFeedbackTitle.setText("OFFICER FEEDBACK • " + decisionDate.toUpperCase());
            binding.tvFeedbackNotes.setText(data.getDecisionNotes());
            
            // Adjust styling based on urgency
            if ("REJECTED".equals(data.getStatus())) {
                binding.cardOfficerFeedback.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_red_bg));
                binding.tvFeedbackTitle.setTextColor(ContextCompat.getColor(this, R.color.status_red));
                binding.ivFeedbackIcon.setImageResource(R.drawable.ic_lock); 
                binding.ivFeedbackIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_red));
            } else if ("INFO_REQUESTED".equals(data.getStatus())) {
                binding.cardOfficerFeedback.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_amber_bg));
                binding.tvFeedbackTitle.setTextColor(ContextCompat.getColor(this, R.color.status_amber));
                binding.ivFeedbackIcon.setImageResource(R.drawable.ic_help_circle); 
                binding.ivFeedbackIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_amber));
            } else {
                binding.cardOfficerFeedback.setCardBackgroundColor(ContextCompat.getColor(this, R.color.terracotta_container));
                binding.tvFeedbackTitle.setTextColor(ContextCompat.getColor(this, R.color.status_green));
                binding.ivFeedbackIcon.setImageResource(R.drawable.ic_check_circle);
                binding.ivFeedbackIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_green));
            }
        } else {
            binding.cardOfficerFeedback.setVisibility(View.GONE);
        }
        
        if (data.getAssignedOfficer() != null) {
            binding.cardOfficer.setVisibility(View.VISIBLE);
            binding.tvOfficerName.setText(data.getAssignedOfficer().getFullName());
            binding.tvOfficerId.setText("Officer ID: " + data.getAssignedOfficer().getEmployeeNo());
        }

        List<LoanApplicationStatusResponse.TimelineStage> timeline = data.getTimeline();
        if (timeline == null) return;

        for (LoanApplicationStatusResponse.TimelineStage stage : timeline) {
            switch (stage.getStage()) {
                case "SUBMITTED":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepSubmitted.getRoot()), "Application Submitted", stage.getCompletedAt(), data.getStatus());
                    break;
                case "UNDER_REVIEW":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepReview.getRoot()), "Under Review", stage.getCompletedAt(), data.getStatus());
                    break;
                case "APPROVAL":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepApproval.getRoot()), "Final Approval", stage.getCompletedAt(), data.getStatus());
                    break;
                case "DISBURSEMENT":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepDisbursement.getRoot()), "Disbursement", stage.getCompletedAt(), data.getStatus());
                    LayoutTimelineItemBinding.bind(binding.stepDisbursement.getRoot()).indicatorLine.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void updateStep(LayoutTimelineItemBinding step, String title, String time, String overallStatus) {
        step.tvTitle.setText(title);
        if (time != null) {
            step.tvTitle.setTextColor(ContextCompat.getColor(this, R.color.navy_text_primary));
            step.tvTime.setText(formatDate(time));
            step.tvTime.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            
            // If the application was rejected at this specific stage
            if ("REJECTED".equals(overallStatus)) {
                step.indicatorDot.setBackgroundResource(R.drawable.bg_status_badge_red);
                step.indicatorLine.setBackgroundColor(ContextCompat.getColor(this, R.color.status_red));
                step.tvTitle.setTextColor(ContextCompat.getColor(this, R.color.status_red));
            } else {
                step.indicatorDot.setBackgroundResource(R.drawable.bg_stepper_active);
                step.indicatorLine.setBackgroundColor(ContextCompat.getColor(this, R.color.terracotta_primary));
            }
        } else {
            step.tvTitle.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            step.tvTime.setText("Pending");
            step.indicatorDot.setBackgroundResource(R.drawable.bg_stepper_inactive);
            step.indicatorLine.setBackgroundColor(ContextCompat.getColor(this, R.color.stepper_track));
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMM, yyyy • hh:mm a", Locale.getDefault());
            Date date = input.parse(dateStr);
            return output.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }
}
