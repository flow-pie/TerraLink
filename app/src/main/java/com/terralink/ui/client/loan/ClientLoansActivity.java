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
import com.terralink.ui.client.home.RepaymentScheduleAdapter;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.client.payment.PaymentDialogActivity;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientLoansActivity extends AppCompatActivity {

    private ClientLoansViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityClientLoansBinding binding = ActivityClientLoansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ClientLoansViewModel.class);

        binding.bottomNavigationView.setSelectedItemId(R.id.nav_loans);

        binding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        binding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

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
            binding.loan1Title.setText(loan.getLoanNo() != null ? loan.getLoanNo() : "LOAN");
            binding.loan1Id.setText("ID: " + (loan.getLoanId() != null ? loan.getLoanId() : "N/A"));
            binding.loan1AmountValue.setText(String.format(
                    Locale.US,
                    "KES %,.2f / KES %,.2f",
                    loan.getRepaymentAmount() - loan.getBalance(),
                    loan.getRepaymentAmount()
            ));
            binding.loan1MonthsLeft.setText(loan.getStatus() != null ? loan.getStatus() : "");
            binding.loan1PaidBadge.setText(loan.getStatus() != null ? loan.getStatus() : "ACTIVE");

            binding.loan1PayButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, PaymentDialogActivity.class);
                intent.putExtra(PaymentDialogActivity.EXTRA_LOAN_ID, Long.parseLong(loan.getLoanId()));
                intent.putExtra(PaymentDialogActivity.EXTRA_SCHEDULE_ID, Objects.requireNonNull(RepaymentScheduleAdapter.getNextPendingInstallment()).getRepaymentScheduleId());
                intent.putExtra(PaymentDialogActivity.EXTRA_AMOUNT, loan.getBalance());
                intent.putExtra(PaymentDialogActivity.EXTRA_INSTALLMENT, RepaymentScheduleAdapter.getNextPendingInstallment().getTotalDue());
                intent.putExtra(PaymentDialogActivity.EXTRA_INSTALLMENT_DUE_DATE, RepaymentScheduleAdapter.getNextPendingInstallment().getDueDate());
                startActivity(intent);
            });

            binding.loan1DetailsButton.setOnClickListener(v -> {
                Toast.makeText(this, "Loan: " + loan.getLoanNo() + "\nStatus: " + loan.getStatus(), Toast.LENGTH_LONG).show();
            });

        }else if(cardNumber == 2){
            binding.loan2Title.setText(loan.getLoanNo() != null ? loan.getLoanNo() : "LOAN");
            binding.loan2Id.setText("ID: " + (loan.getLoanId() != null ? loan.getLoanId() : "N/A"));
            binding.loan2AmountValue.setText(String.format(
                    Locale.US,
                    "KES %,.2f / KES %,.2f",
                    loan.getRepaymentAmount() - loan.getBalance(),
                    loan.getRepaymentAmount()
            ));
            binding.loan2MonthsLeft.setText(loan.getStatus() != null ? loan.getStatus() : "");
            binding.loan2DueBadge.setText(loan.getStatus() != null ? loan.getStatus() : "ACTIVE");

            binding.loan2PayButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, PaymentDialogActivity.class);
                intent.putExtra(PaymentDialogActivity.EXTRA_LOAN_ID, Long.parseLong(loan.getLoanId()));
                intent.putExtra(PaymentDialogActivity.EXTRA_SCHEDULE_ID, 1);
                intent.putExtra(PaymentDialogActivity.EXTRA_AMOUNT, loan.getBalance());
                startActivity(intent);
            });

            binding.loan2DetailsButton.setOnClickListener(v -> {
                Toast.makeText(this, "Loan: " + loan.getLoanNo() + "\nStatus: " + loan.getStatus(), Toast.LENGTH_LONG).show();
            });
        }
    }
}
