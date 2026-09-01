package com.terralink.ui.officer.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.R;
import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.PortfolioSummaryResponse;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.officer.appraisal.LoanAppraisalBottomSheetFragment;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityDashboardBinding;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.common.SnackbarUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private ActivityDashboardBinding binding;
    private PendingAppraisalAdapter appraisalAdapter;
    private final NumberFormat ksh = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupMetricLabels();
        setupRecyclerView();
        setupClickListeners();
        
        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void setupMetricLabels() {
        binding.metricClients.tvMetricTitle.setText("TOTAL CLIENTS");
        binding.metricPending.tvMetricTitle.setText("PENDING APPS");
        binding.metricActive.tvMetricTitle.setText("ACTIVE LOANS");
        binding.metricOverdue.tvMetricTitle.setText("OVERDUE LOANS");
        
        binding.metricOverdue.tvMetricValue.setTextColor(getResources().getColor(R.color.status_red, getTheme()));
    }

    private void loadData() {
        binding.swipeRefresh.setRefreshing(true);
        
        // Profile
        viewModel.getProfile().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS) {
                UserProfileResponse profile = result.getData();
                if (profile != null) {
                    binding.tvWelcome.setText(String.format("Welcome, %s", profile.getFullName().split(" ")[0]));
                    binding.tvRegionId.setText(String.format("Officer ID: %s | %s", 
                            profile.getEmployeeNo(), profile.getRoleName()));
                }
                checkAllLoaded();
            } else if (result.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Error loading profile");
                checkAllLoaded();
            }
        });

        // Portfolio Summary
        viewModel.getPortfolioSummary().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS) {
                PortfolioSummaryResponse summary = result.getData();
                if (summary != null) {
                    binding.tvOutstandingPortfolio.setText(ksh.format(summary.getOutstandingPortfolio()));
                    binding.tvDisbursed.setText(ksh.format(summary.getDisbursedAmountMtd()));
                    binding.tvArrearsAmount.setText(ksh.format(summary.getArrearsAmount()));
                    
                    binding.metricClients.tvMetricValue.setText(String.valueOf(summary.getTotalClients()));
                    binding.metricActive.tvMetricValue.setText(String.valueOf(summary.getActiveLoansCount()));
                    binding.metricPending.tvMetricValue.setText(String.valueOf(summary.getPendingApplications()));
                    binding.metricOverdue.tvMetricValue.setText(String.valueOf(summary.getOverdueLoans()));
                }
                checkAllLoaded();
            } else if (result.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Error loading summary");
                checkAllLoaded();
            }
        });

        // Pending Appraisals
        viewModel.getPendingAppraisals().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                PaginatedResponse<LoanApplicationResponse> paginated = result.getData();
                List<LoanApplicationResponse> allApps = paginated.getItems();
                java.util.List<LoanApplicationResponse> pendingApps = new java.util.ArrayList<>();
                if (allApps != null) {
                    for (LoanApplicationResponse app : allApps) {
                        String status = app.getStatus();
                        if ("SUBMITTED".equals(status) || "UNDER_REVIEW".equals(status) || "INFO_REQUESTED".equals(status)) {
                            pendingApps.add(app);
                        }
                    }
                }
                binding.tvNoPendingAppraisals.setVisibility(pendingApps.isEmpty() ? View.VISIBLE : View.GONE);
                binding.rvPendingAppraisals.setVisibility(pendingApps.isEmpty() ? View.GONE : View.VISIBLE);
                appraisalAdapter.submitList(pendingApps);
                checkAllLoaded();
            } else if (result.getStatus() == LoginStatus.ERROR) {
                checkAllLoaded();
            }
        });
    }

    private void checkAllLoaded() {
        binding.swipeRefresh.setRefreshing(false);
    }

    private void setupRecyclerView() {
        appraisalAdapter = new PendingAppraisalAdapter(app -> {
            LoanAppraisalBottomSheetFragment fragment = LoanAppraisalBottomSheetFragment.newInstance(app.getId());
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
        binding.rvPendingAppraisals.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.rvPendingAppraisals.setAdapter(appraisalAdapter);
        binding.rvPendingAppraisals.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        binding.btnRegisterClient.setOnClickListener(v -> {
            startActivity(new Intent(this, com.terralink.ui.officer.registration.RegisterClientActivity.class));
        });

        binding.btnNewLoanProduct.setOnClickListener(v -> {
            com.terralink.ui.officer.products.AddProductBottomSheetFragment fragment = 
                com.terralink.ui.officer.products.AddProductBottomSheetFragment.newInstance();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) return true;
            if (id == R.id.nav_clients) {
                startActivity(new Intent(this, com.terralink.ui.officer.clients.OfficerClientsActivity.class));
                return true;
            }
            if (id == R.id.nav_loans) {
                startActivity(new Intent(this, com.terralink.ui.officer.loans.OfficerLoansActivity.class));
                return true;
            }
            if (id == R.id.nav_tasks) {
                startActivity(new Intent(this, com.terralink.ui.officer.tasks.OfficerTasksActivity.class));
                return true;
            }
            return true;
        });
    }
}
