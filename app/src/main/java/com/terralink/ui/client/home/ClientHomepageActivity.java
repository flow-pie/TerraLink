package com.terralink.ui.client.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.RepaymentInstallments;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityClientHomepageBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.client.loan.ApplyLoanActivity;
import com.terralink.ui.client.loan.ClientLoansActivity;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.client.payment.PaymentBottomSheetFragment;
import com.terralink.ui.client.profile.ProfileActivity;
import com.terralink.ui.client.transaction.TransactionHistoryActivity;
import com.terralink.ui.common.Resource;
import com.terralink.ui.common.SnackbarUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientHomepageActivity extends AppCompatActivity {

    private HomeViewModel viewModel;
    private RepaymentScheduleAdapter repaymentScheduleAdapter;
    private LoanSelectorAdapter loanSelectorAdapter;
    private ActivityClientHomepageBinding binding;
    private final NumberFormat ksh = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClientHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        initView();
        observeViewModel();

        binding.swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void refreshData() {
        viewModel.refreshProfile();
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void initView() {
        binding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        binding.appBarContent.btnSupport.setOnClickListener(v -> {
            SnackbarUtils.showInfo(binding.getRoot(), "Support feature coming soon");
        });

        binding.btnApplyFirstLoan.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        binding.cardMyAssets.setOnClickListener(v -> {
            startActivity(new Intent(this, com.terralink.ui.client.scoring.AssetListActivity.class));
        });

        binding.cardIncomeAssessment.setOnClickListener(v -> {
            startActivity(new Intent(this, com.terralink.ui.client.scoring.IncomeAssessmentListActivity.class));
        });

        repaymentScheduleAdapter = new RepaymentScheduleAdapter(new ArrayList<>());
        binding.rvRecentPayments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentPayments.setAdapter(repaymentScheduleAdapter);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            
            if (itemId == R.id.nav_loans) {
                startActivity(new Intent(this, ClientLoansActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
            return true;
        });

        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);

        binding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });
        
        binding.tvViewAllPayments.setOnClickListener(v -> {
            startActivity(new Intent(this, TransactionHistoryActivity.class));
        });
    }

    private void observeViewModel() {
        viewModel.getActiveUser().observe(this, this::handleUserResult);
        viewModel.getClientLoansStream().observe(this, this::handleLoansResult);
        viewModel.getLoanDetailsStream().observe(this, this::handleLoanDetailsResult);
        viewModel.getRepaymentInstallmentsStream().observe(this, this::handleRepaymentsResult);
    }

    private void handleUserResult(Resource<UserProfileResponse> result) {
        if (result.getStatus() == LoginStatus.SUCCESS) {
            UserProfileResponse client = result.getData();
            if (client != null) {
                binding.tvBorrowerName.setText(client.getFullName());
                viewModel.refreshLoans(client.getClientId());
            }
        }
    }

    private void handleLoansResult(Resource<List<ClientLoansResponse>> result) {
        if (result.getStatus() == LoginStatus.SUCCESS) {
            List<ClientLoansResponse> allLoans = result.getData();
            if (allLoans != null && !allLoans.isEmpty()) {
                binding.emptyStateContainer.setVisibility(View.GONE);
                
                List<ClientLoansResponse> activeLoans = allLoans.stream()
                        .filter(l -> "Loan".equals(l.getType()))
                        .collect(Collectors.toList());

                boolean hasPending = allLoans.stream()
                        .anyMatch(l -> "Application".equals(l.getType()) && 
                                !"REJECTED".equals(l.getStatus()) &&
                                !"APPROVED".equals(l.getStatus()));

                binding.cardPendingApplication.setVisibility(hasPending ? View.VISIBLE : View.GONE);
                binding.cardPendingApplication.setOnClickListener(v -> {
                    startActivity(new Intent(this, ClientLoansActivity.class));
                });

                if (!activeLoans.isEmpty()) {
                    binding.loanContentContainer.setVisibility(View.VISIBLE);
                    binding.rvLoanSelector.setVisibility(View.VISIBLE);
                    
                    ClientLoansResponse primaryLoan = activeLoans.get(0);
                    viewModel.refreshLoanDetails(primaryLoan.getLoanId());
                    setupLoanSelector(activeLoans);
                } else {
                    binding.loanContentContainer.setVisibility(View.GONE);
                    binding.rvLoanSelector.setVisibility(View.GONE);
                    if (!hasPending) binding.emptyStateContainer.setVisibility(View.VISIBLE);
                }
            } else {
                binding.emptyStateContainer.setVisibility(View.VISIBLE);
                binding.loanContentContainer.setVisibility(View.GONE);
            }
        }
    }

    private void handleLoanDetailsResult(Resource<LoanDetailsResponse> result) {
        if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
            updateLoanDetailsUI(result.getData());
            viewModel.refreshRepayments(result.getData().getLoanId());
        }
    }

    private void handleRepaymentsResult(Resource<List<RepaymentInstallments>> result) {
        if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
            repaymentScheduleAdapter.setSchedules(result.getData(), viewModel.getLoanDetailsStream().getValue() != null ? viewModel.getLoanDetailsStream().getValue().getData() : null);
        }
    }

    private void setupLoanSelector(List<ClientLoansResponse> loans) {
        loanSelectorAdapter = new LoanSelectorAdapter(loans, loan -> {
            viewModel.refreshLoanDetails(loan.getLoanId());
        });
        binding.rvLoanSelector.setAdapter(loanSelectorAdapter);
    }

    private void updateLoanDetailsUI(LoanDetailsResponse details) {
        binding.tvLoanBalance.setText(ksh.format(details.getOutStandingAmount()));
        binding.nextInstallment.setText(ksh.format(details.getNextInstallmentAmount()));
        binding.nextInstallmentDueDate.setText("Due " + details.getNextDueDate());

        // Update Progress
        int progress = (details.getInstallmentsTotal() > 0) 
            ? (int) ((details.getInstallmentsPaid() / (double) details.getInstallmentsTotal()) * 100) : 0;
        
        binding.progressRepayment.setProgress(progress);
        binding.tvInstallmentsProgress.setText(details.getInstallmentsPaid() + " of " + details.getInstallmentsTotal());

        double repaid = details.getTotalRepayment() - details.getOutStandingAmount();
        binding.tvTotalPaid.setText(ksh.format(repaid));
        binding.loanAmountTotal.setText(ksh.format(details.getTotalRepayment()));

        setupLoanActions(details);
    }

    private void setupLoanActions(LoanDetailsResponse details) {
        binding.btnMakePayment.setOnClickListener(v -> {
            RepaymentInstallments next = repaymentScheduleAdapter.getNextPendingInstallment();
            if (next == null) {
                SnackbarUtils.showInfo(binding.getRoot(), "No pending installments");
                return;
            }
            PaymentBottomSheetFragment fragment = PaymentBottomSheetFragment.newInstance(
                    Long.parseLong(details.getLoanId()),
                    next.getRepaymentScheduleId(),
                    details.getOutStandingAmount(),
                    details.getNextInstallmentAmount(),
                    details.getNextDueDate()
            );
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
    }
}
