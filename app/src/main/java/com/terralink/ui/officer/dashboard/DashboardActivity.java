package com.terralink.ui.officer.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private ActivityDashboardBinding binding;
    private PendingAppraisalAdapter appraisalAdapter;

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

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        appraisalAdapter = new PendingAppraisalAdapter(app -> {
            LoanAppraisalBottomSheetFragment fragment = LoanAppraisalBottomSheetFragment.newInstance(app.getId());
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
        binding.rvPendingAppraisals.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.rvPendingAppraisals.setAdapter(appraisalAdapter);
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
            // Handle other nav items
            return true;
        });
    }

    private void observeViewModel() {
        viewModel.getProfile().observe(this, result -> {
            switch (result.getStatus()) {
                case SUCCESS:
                    UserProfileResponse profile = result.getData();
                    if (profile != null) {
                        binding.tvWelcome.setText(String.format("Welcome, %s", profile.getFullName().split(" ")[0]));
                        binding.tvRegionId.setText(String.format("Employee ID: %s | Role: %s", 
                                profile.getEmployeeNo(), profile.getRoleName()));
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, "Error loading profile: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getPortfolioSummary().observe(this, result -> {
            switch (result.getStatus()) {
                case SUCCESS:
                    PortfolioSummaryResponse summary = result.getData();
                    if (summary != null) {
                        binding.tvActiveLoans.setText(String.format(Locale.getDefault(), "%d ", summary.getActiveLoansCount()));
                        binding.tvDisbursed.setText(String.format(Locale.getDefault(), "KES %,.0f", summary.getDisbursedAmountMtd()));
                        binding.tvTotalClients.setText(String.format(Locale.getDefault(), "%d ", summary.getTotalClients()));
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, "Error loading summary: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

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

                // Update the pending count metric
                binding.tvPendingAppraisalsCount.setText(String.format(Locale.getDefault(), "%02d", pendingApps.size()));

                // Reset visibility
                binding.tvNoPendingAppraisals.setVisibility(pendingApps.isEmpty() ? View.VISIBLE : View.GONE);
                binding.rvPendingAppraisals.setVisibility(pendingApps.isEmpty() ? View.GONE : View.VISIBLE);
                
                appraisalAdapter.submitList(pendingApps);
            }
        });
    }
}
