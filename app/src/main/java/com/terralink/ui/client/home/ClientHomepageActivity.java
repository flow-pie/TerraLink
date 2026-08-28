package com.terralink.ui.client.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientHomepageActivity extends AppCompatActivity {

    private HomeViewModel viewModel;
    private RepaymentScheduleAdapter repaymentScheduleAdapter;
    private LoanSelectorAdapter loanSelectorAdapter;
    private ActivityClientHomepageBinding binding;

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
    }

    private void observeViewModel() {
        viewModel.getActiveUser().observe(this, this::handleUserResult);
        viewModel.getClientLoansStream().observe(this, this::handleLoansResult);
        viewModel.getLoanDetailsStream().observe(this, this::handleLoanDetailsResult);
        viewModel.getRepaymentInstallmentsStream().observe(this, this::handleRepaymentsResult);
    }

    private void handleUserResult(Resource<UserProfileResponse> result) {
        switch (result.getStatus()) {
            case LOADING:
                binding.tvBorrowerName.setText("Loading...");
                break;
            case SUCCESS:
                UserProfileResponse client = result.getData();
                if (client != null && client.getFullName() != null) {
                    binding.tvBorrowerName.setText(client.getFullName());
                    viewModel.refreshLoans(client.getClientId());
                }
                break;
            case ERROR:
                binding.tvBorrowerName.setText("Error loading profile");
                handleError("UserProfile", result.getMessage());
                break;
            default:
                break;
        }
    }

    private void handleLoansResult(Resource<List<ClientLoansResponse>> result) {
        switch (result.getStatus()) {
            case LOADING:
                binding.tvLoanBalance.setText("Loading...");
                break;
            case SUCCESS:
                List<ClientLoansResponse> loans = result.getData();
                if (loans != null && !loans.isEmpty()) {
                    binding.emptyStateContainer.setVisibility(View.GONE);
                    
                    ClientLoansResponse primaryLoan = null;
                    for (ClientLoansResponse l : loans) {
                        if ("Loan".equals(l.getType())) {
                            primaryLoan = l;
                            break;
                        }
                    }

                    if (primaryLoan != null) {
                        binding.loanContentContainer.setVisibility(View.VISIBLE);
                        viewModel.refreshLoanDetails(primaryLoan.getLoanId());
                    } else {
                        binding.loanContentContainer.setVisibility(View.GONE);
                    }

                    setupLoanSelector(loans);
                } else {
                    binding.emptyStateContainer.setVisibility(View.VISIBLE);
                    binding.loanContentContainer.setVisibility(View.GONE);
                    binding.rvLoanSelector.setVisibility(View.GONE);
                    
                    binding.btnApplyFirstLoan.setOnClickListener(v -> {
                        startActivity(new Intent(this, ApplyLoanActivity.class));
                    });
                }
                break;
            case ERROR:
                binding.tvLoanBalance.setText("Unable to load");
                handleError("ClientLoans", result.getMessage());
                break;
        }
    }

    private void handleLoanDetailsResult(Resource<LoanDetailsResponse> result) {
        switch (result.getStatus()) {
            case LOADING:
                binding.tvLoanBalance.setText("Loading...");
                repaymentScheduleAdapter.setSchedules(new ArrayList<>(), null);
                break;
            case SUCCESS:
                LoanDetailsResponse loanDetails = result.getData();
                if (loanDetails != null) {
                    updateLoanDetailsUI(loanDetails);
                    viewModel.refreshRepayments(loanDetails.getLoanId());
                }
                break;
            case ERROR:
                handleError("LoanDetails", result.getMessage());
                break;
        }
    }

    private void handleRepaymentsResult(Resource<List<RepaymentInstallments>> result) {
        switch (result.getStatus()) {
            case SUCCESS:
                if (result.getData() != null) {
                    repaymentScheduleAdapter.setSchedules(result.getData(), viewModel.getLoanDetailsStream().getValue() != null ? viewModel.getLoanDetailsStream().getValue().getData() : null);
                }
                break;
            case ERROR:
                Log.e("HomeActivity", "Failed to load repayment schedule: " + result.getMessage());
                break;
        }
    }

    private void fetchClientLoans(String clientId) {
        viewModel.refreshLoans(clientId);
    }

    private void setupLoanSelector(List<ClientLoansResponse> loans) {
        List<ClientLoansResponse> unapprovedLoans = loans.stream()
                .filter(loan -> !"APPROVED".equals(loan.getStatus()))
                .collect(Collectors.toList());

        loanSelectorAdapter = new LoanSelectorAdapter(unapprovedLoans, loan -> {
            if ("Application".equals(loan.getType()) ) {
                Intent intent = new Intent(this, NotificationStatusActivity.class);
                intent.putExtra(NotificationStatusActivity.EXTRA_APPLICATION_ID, Integer.parseInt(loan.getLoanId()));
                intent.putExtra(NotificationStatusActivity.EXTRA_LOAN_NO, loan.getReferenceNo());
                startActivity(intent);
            } else {
                binding.loanContentContainer.setVisibility(View.VISIBLE);
                viewModel.refreshLoanDetails(loan.getLoanId());
            }
        });
        binding.rvLoanSelector.setAdapter(loanSelectorAdapter);
        binding.rvLoanSelector.setVisibility(loans.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void fetchLoanDetails(String loanId) {
        viewModel.refreshLoanDetails(loanId);
    }

    private void fetchRepaymentInstallments(String loanId, LoanDetailsResponse loanDetails) {
        viewModel.refreshRepayments(loanId);
    }

    private void updateLoanDetailsUI(LoanDetailsResponse details) {
        binding.tvLoanBalance.setText(String.format(Locale.getDefault(), "KES %,.2f", details.getOutStandingAmount()));
        binding.nextInstallment.setText(String.format(Locale.getDefault(), "KES %,.2f", details.getNextInstallmentAmount()));
        binding.nextInstallmentDueDate.setText("Due by " + details.getNextDueDate());
        binding.intrestRate.setText(details.getInterestRate());

        setupLoanActions(details);
        updateDueDateCycle(details);
        updateRepaymentProgress(details);
    }

    private void setupLoanActions(LoanDetailsResponse details) {
        binding.cardNewLoanApp.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        binding.btnMakePayment.setOnClickListener(v -> {
            RepaymentInstallments next = repaymentScheduleAdapter.getNextPendingInstallment();
            if (next == null) {
                Toast.makeText(this, "No pending installments", Toast.LENGTH_SHORT).show();
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

    private void updateDueDateCycle(LoanDetailsResponse details) {
        long days = details.getDaysUntilNextDueDate();
        if (days > 0) {
            binding.nextCycle.setText(days + " days remaining");
        } else if (days == 0) {
            binding.nextCycle.setText("Due today");
        } else {
            binding.nextCycle.setText("Overdue by " + Math.abs(days) + " days");
        }
    }

    private void updateRepaymentProgress(LoanDetailsResponse details) {
        int paid = details.getInstallmentsPaid();
        int total = details.getInstallmentsTotal();
        int progress = (total > 0) ? (int) ((paid / (double) total) * 100) : 0;

        binding.progressRepayment.setMax(total);
        binding.progressRepayment.setProgress(progress);
        binding.tvInstallmentsProgress.setText(String.format(Locale.getDefault(), "%d of %d installments paid", paid, total));

        double totalAmountPaid = details.getTotalRepayment() - details.getOutStandingAmount();
        binding.tvTotalPaid.setText(String.format(Locale.getDefault(), "KES %,.2f", totalAmountPaid));
        binding.loanAmountTotal.setText(String.format(Locale.getDefault(), "KSH %,.2f", details.getTotalRepayment()));
    }

    private void handleError(String tag, String message) {
        String displayMessage = message != null ? message : "Unknown error occurred";
        Toast.makeText(this, displayMessage, Toast.LENGTH_LONG).show();
        Log.e("HomeActivity", tag + ": " + displayMessage);
    }
}
