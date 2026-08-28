package com.terralink.ui.client.loan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.R;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityClientLoansBinding;
import com.terralink.ui.client.notification.NotificationStatusActivity;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        setupNavigation(binding);

        binding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        binding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadData(binding);
            binding.swipeRefresh.setRefreshing(false);
        });

        loadData(binding);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_loans);
    }

    private void setupNavigation(ActivityClientLoansBinding binding) {
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

    private void loadData(ActivityClientLoansBinding binding) {
        viewModel.getActiveUser().observe(this, userResult -> {
            switch (userResult.getStatus()) {
                case LOADING:
                    binding.loadingView.getRoot().setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    UserProfileResponse user = userResult.getData();
                    if (user != null) {
                        viewModel.getClientLoans(user.getClientId()).observe(this, result -> {
                            switch (result.getStatus()) {
                                case LOADING:
                                    binding.loadingView.getRoot().setVisibility(View.VISIBLE);
                                    break;
                                case SUCCESS:
                                    binding.loadingView.getRoot().setVisibility(View.GONE);
                                    List<ClientLoansResponse> loans = result.getData();
                                    if (loans != null && !loans.isEmpty()) {
                                        bindLoanCard(binding, loans.get(0), 1);
                                        if (loans.size() > 1) {
                                            bindLoanCard(binding, loans.get(1), 2);
                                            binding.loanCard2.setVisibility(View.VISIBLE);
                                        } else {
                                            binding.loanCard2.setVisibility(View.GONE);
                                        }
                                        binding.loanCard1.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.loanCard1.setVisibility(View.GONE);
                                        binding.loanCard2.setVisibility(View.GONE);
                                    }
                                    break;
                                case ERROR:
                                    binding.loadingView.getRoot().setVisibility(View.GONE);
                                    String message = result.getMessage() != null ? result.getMessage() : "Failed to load loans";
                                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                                    break;
                            }
                        });
                    }
                    break;
                case ERROR:
                    binding.loadingView.getRoot().setVisibility(View.GONE);
                    String message = userResult.getMessage() != null ? userResult.getMessage() : "Failed to load user profile";
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void bindLoanCard(ActivityClientLoansBinding binding, ClientLoansResponse loan, int cardNumber){
        if(cardNumber == 1){
            binding.loan1Title.setText(loan.getReferenceNo() != null ? loan.getReferenceNo() : "LOAN");
            binding.loan1Id.setText("ID: " + (loan.getLoanId() != null ? loan.getLoanId() : "N/A"));
            
            if ("Application".equals(loan.getType())) {
                binding.loan1AmountValue.setText("Application Pending");
                binding.loan1MonthsLeft.setText("Submitted: " + (loan.getSubmittedAt() != null ? loan.getSubmittedAt().split("T")[0] : ""));
                binding.loan1PaidBadge.setText("SUBMITTED");
                binding.loan1PayButton.setVisibility(View.GONE);
            } else {
                binding.loan1AmountValue.setText(String.format(
                        Locale.US,
                        "KES %,.2f / KES %,.2f",
                        loan.getRepaymentAmount() - loan.getBalance(),
                        loan.getRepaymentAmount()
                ));
                binding.loan1MonthsLeft.setText(loan.getStatus() != null ? loan.getStatus() : "");
                binding.loan1PaidBadge.setText(loan.getStatus() != null ? loan.getStatus() : "ACTIVE");
                binding.loan1PayButton.setVisibility(View.VISIBLE);
            }

            binding.loan1PayButton.setOnClickListener(v -> {
                Toast.makeText(this, "Please manage payments from the Home dashboard", Toast.LENGTH_SHORT).show();
            });

            binding.loan1DetailsButton.setOnClickListener(v -> {
                Toast.makeText(this, "Loan: " + loan.getReferenceNo() + "\nStatus: " + loan.getStatus(), Toast.LENGTH_LONG).show();
            });

        }else if(cardNumber == 2){
            binding.loan2Title.setText(loan.getReferenceNo() != null ? loan.getReferenceNo() : "LOAN");
            binding.loan2Id.setText("ID: " + (loan.getLoanId() != null ? loan.getLoanId() : "N/A"));
            
            if ("Application".equals(loan.getType())) {
                binding.loan2AmountValue.setText("Application Pending");
                binding.loan2MonthsLeft.setText("Submitted: " + (loan.getSubmittedAt() != null ? loan.getSubmittedAt().split("T")[0] : ""));
                binding.loan2DueBadge.setText("SUBMITTED");
                binding.loan2PayButton.setVisibility(View.GONE);
            } else {
                binding.loan2AmountValue.setText(String.format(
                        Locale.US,
                        "KES %,.2f / KES %,.2f",
                        loan.getRepaymentAmount() - loan.getBalance(),
                        loan.getRepaymentAmount()
                ));
                binding.loan2MonthsLeft.setText(loan.getStatus() != null ? loan.getStatus() : "");
                binding.loan2DueBadge.setText(loan.getStatus() != null ? loan.getStatus() : "ACTIVE");
                binding.loan2PayButton.setVisibility(View.VISIBLE);
            }

            binding.loan2PayButton.setOnClickListener(v -> {
                Toast.makeText(this, "Please manage payments from the Home dashboard", Toast.LENGTH_SHORT).show();
            });

            binding.loan2DetailsButton.setOnClickListener(v -> {
                Toast.makeText(this, "Loan: " + loan.getReferenceNo() + "\nStatus: " + loan.getStatus(), Toast.LENGTH_LONG).show();
            });
        }
    }
}
