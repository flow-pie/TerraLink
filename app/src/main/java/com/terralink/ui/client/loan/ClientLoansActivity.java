package com.terralink.ui.client.loan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.terralink.R;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityClientLoansBinding;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.common.SnackbarUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientLoansActivity extends AppCompatActivity {

    private ClientLoansViewModel viewModel;
    private ActivityClientLoansBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClientLoansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ClientLoansViewModel.class);

        binding.bottomNavigationView.setSelectedItemId(R.id.nav_loans);
        setupNavigation();
        setupRecyclerView();

        binding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        binding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadData();
            binding.swipeRefresh.setRefreshing(false);
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_loans);
    }

    private void setupNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, com.terralink.ui.client.home.ClientHomepageActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if (id == R.id.nav_loans) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, com.terralink.ui.client.transaction.TransactionHistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, com.terralink.ui.client.profile.ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        binding.rvLoanList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        viewModel.getActiveUser().observe(this, userResult -> {
            if (userResult.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS) {
                UserProfileResponse user = userResult.getData();
                if (user != null) {
                    viewModel.getClientLoans(user.getClientId()).observe(this, result -> {
                        switch (result.getStatus()) {
                            case LOADING:
                                binding.loadingView.getRoot().setVisibility(View.VISIBLE);
                                break;
                            case SUCCESS:
                                binding.loadingView.getRoot().setVisibility(View.GONE);
                                if (result.getData() != null) {
                                    updateUI(result.getData());
                                }
                                break;
                            case ERROR:
                                binding.loadingView.getRoot().setVisibility(View.GONE);
                                SnackbarUtils.showError(binding.getRoot(), result.getMessage());
                                break;
                        }
                    });
                }
            }
        });
    }

    private void updateUI(List<ClientLoansResponse> loans) {
        List<ClientLoansResponse> portfolioLoans = new ArrayList<>();
        List<ClientLoansResponse> historyLoans = new ArrayList<>();

        for (ClientLoansResponse loan : loans) {
            String status = loan.getStatus();
            if ("REJECTED".equals(status) || "CLOSED".equals(status) || "APPROVED".equals(status)) {
                historyLoans.add(loan);
            } else {
                portfolioLoans.add(loan);
            }
        }

        // Setup Portfolio Adapter
        LoanListAdapter portfolioAdapter = new LoanListAdapter(portfolioLoans, loan -> {
            LoanDetailsBottomSheetFragment fragment = LoanDetailsBottomSheetFragment.newInstance(loan);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
        binding.rvLoanList.setAdapter(portfolioAdapter);

        // Setup History Adapter
        if (!historyLoans.isEmpty()) {
            binding.tvHistoryHeader.setVisibility(View.VISIBLE);
            binding.rvLoanHistory.setVisibility(View.VISIBLE);
            LoanListAdapter historyAdapter = new LoanListAdapter(historyLoans, loan -> {
                LoanDetailsBottomSheetFragment fragment = LoanDetailsBottomSheetFragment.newInstance(loan);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            });
            binding.rvLoanHistory.setLayoutManager(new LinearLayoutManager(this));
            binding.rvLoanHistory.setAdapter(historyAdapter);
        } else {
            binding.tvHistoryHeader.setVisibility(View.GONE);
            binding.rvLoanHistory.setVisibility(View.GONE);
        }

        binding.tvEmptyState.setVisibility(loans.isEmpty() ? View.VISIBLE : View.GONE);
        binding.totalCountBadge.setText(portfolioLoans.size() + " Active");

        double totalActiveBalance = 0;
        for (ClientLoansResponse loan : portfolioLoans) {
            if ("Loan".equals(loan.getType()) && "ACTIVE".equals(loan.getStatus())) {
                totalActiveBalance += loan.getBalance();
            }
        }

        binding.activeBalanceValue.setText(String.format(Locale.getDefault(), "KES %,.2f", totalActiveBalance));
        binding.nextPaymentValue.setText("Oct 12"); // Placeholder
    }
}
