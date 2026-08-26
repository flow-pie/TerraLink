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

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        binding.btnRegisterClient.setOnClickListener(v -> {
            startActivity(new Intent(this, com.terralink.ui.officer.registration.RegisterClientActivity.class));
        });

        binding.btnNewLoanProduct.setOnClickListener(v -> {
            // TODO: Navigate to new loan product
            Toast.makeText(this, "New Loan Product Clicked", Toast.LENGTH_SHORT).show();
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) return true;
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
                
                // Update the pending count metric from the pagination total
                binding.tvPendingAppraisalsCount.setText(String.format(Locale.getDefault(), "%02d", paginated.getTotalCount()));

                List<LoanApplicationResponse> apps = paginated.getItems();
                
                // Reset visibility
                binding.cardAppraisal1.setVisibility(View.GONE);
                binding.cardAppraisal2.setVisibility(View.GONE);
                binding.tvNoPendingAppraisals.setVisibility(View.GONE);

                if (apps != null && !apps.isEmpty()) {
                    binding.cardAppraisal1.setVisibility(View.VISIBLE);
                    binding.tvApplicantName1.setText(apps.get(0).getClientFullName());
                    binding.tvLoanId1.setText("Loan ID: #" + apps.get(0).getApplicationNo());
                    binding.btnReview1.setOnClickListener(v -> {
                        LoanAppraisalBottomSheetFragment fragment = LoanAppraisalBottomSheetFragment.newInstance(
                                apps.get(0).getId()
                        );
                        fragment.show(getSupportFragmentManager(), fragment.getTag());
                    });

                    if (apps.size() >= 2) {
                        binding.cardAppraisal2.setVisibility(View.VISIBLE);
                        binding.tvApplicantName2.setText(apps.get(1).getClientFullName());
                        binding.tvLoanId2.setText("Loan ID: #" + apps.get(1).getApplicationNo());
                        binding.btnReview2.setOnClickListener(v -> {
                            LoanAppraisalBottomSheetFragment fragment = LoanAppraisalBottomSheetFragment.newInstance(
                                    apps.get(1).getId()
                            );
                            fragment.show(getSupportFragmentManager(), fragment.getTag());
                        });
                    }
                } else {
                    binding.tvNoPendingAppraisals.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
