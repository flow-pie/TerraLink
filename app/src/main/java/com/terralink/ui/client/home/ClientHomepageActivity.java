package com.terralink.ui.client.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.terralink.ui.client.loan.ApplyLoanActivity;
import com.terralink.ui.client.loan.ClientLoansActivity;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.client.payment.PaymentDialogActivity;
import com.terralink.ui.client.profile.ProfileActivity;
import com.terralink.ui.client.transaction.TransactionHistoryActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientHomepageActivity extends AppCompatActivity {

    private HomeViewModel viewModel;
    private RepaymentScheduleAdapter repaymentScheduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityClientHomepageBinding homepageBinding = ActivityClientHomepageBinding
                .inflate(getLayoutInflater());

        setContentView(homepageBinding.getRoot());

        homepageBinding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        repaymentScheduleAdapter =
                new RepaymentScheduleAdapter(new ArrayList<>());

        homepageBinding.rvRecentPayments.setLayoutManager(
                new LinearLayoutManager(this)
        );

        homepageBinding.rvRecentPayments.setAdapter(
                repaymentScheduleAdapter
        );

        viewModel = new ViewModelProvider(this)
                .get(HomeViewModel.class);

        // get active user info
        viewModel.getActiveUser().observe(this,
                result -> {
                    switch (result.getStatus()){
                        case LOADING:

                            // Show loading UI.
                            homepageBinding.tvBorrowerName.setText("Loading...");

                            break;

                        case SUCCESS:

                            UserProfileResponse client =
                                    result.getData();

                            if (client != null && client.getFullName() != null) {
                                homepageBinding.tvBorrowerName.setText(client.getFullName());

                                viewModel.getClientLoans(client.getClientId()).observe(this,
                                        loanResult->{
                                                switch (loanResult.getStatus()){
                                                    case LOADING:

                                                        homepageBinding.tvLoanBalance.setText(
                                                                "Loading..."
                                                        );

                                                        break;

                                                    case SUCCESS:

                                                        List<ClientLoansResponse> loans = loanResult.getData();

                                                        if (loans != null && !loans.isEmpty()) {

                                                            ClientLoansResponse loan = loans.get(0);
                                                            //get loan detail using the obtained loan ID
                                                            viewModel.getClientDetails(loan.getLoanId()).observe(
                                                                    this,
                                                                    loanDetailsResults ->{
                                                                        switch (loanDetailsResults.getStatus()){
                                                                            case LOADING:

                                                                                homepageBinding.tvLoanBalance.setText(
                                                                                        "Loading..."
                                                                                );

                                                                                break;

                                                                            case SUCCESS:
                                                                                LoanDetailsResponse loanDetails = loanDetailsResults.getData();

                                                                                homepageBinding.tvLoanBalance.setText(
                                                                                        String.format(
                                                                                                Locale.getDefault(),
                                                                                                "KES %,.2f",
                                                                                                loanDetails.getOutStandingAmount()
                                                                                        )
                                                                                );
                                                                                homepageBinding.nextInstallment.setText(
                                                                                        String.format(
                                                                                                Locale.getDefault(),
                                                                                                "KES %,.2f",
                                                                                                loanDetails.getNextInstallmentAmount()
                                                                                        )
                                                                                );
                                                                                homepageBinding.nextInstallmentDueDate.setText("Due by "+loanDetails.getNextDueDate());

                                                                                 homepageBinding.cardNewLoanApp.setOnClickListener(v -> {
                                                                                     startActivity(new Intent(this, ApplyLoanActivity.class));
                                                                                 });

                                                                                 homepageBinding.btnMakePayment.setOnClickListener(v -> {
                                                                                     Intent paymentIntent = new Intent(this, PaymentDialogActivity.class);
                                                                                     paymentIntent.putExtra(PaymentDialogActivity.EXTRA_LOAN_ID, Long.parseLong(loanDetails.getLoanId()));
                                                                                     paymentIntent.putExtra(PaymentDialogActivity.EXTRA_SCHEDULE_ID, Objects.requireNonNull(RepaymentScheduleAdapter.getNextPendingInstallment()).getRepaymentScheduleId());
                                                                                     paymentIntent.putExtra(PaymentDialogActivity.EXTRA_AMOUNT, loanDetails.getOutStandingAmount());
                                                                                     paymentIntent.putExtra(PaymentDialogActivity.EXTRA_INSTALLMENT, loanDetails.getNextInstallmentAmount());
                                                                                     paymentIntent.putExtra(PaymentDialogActivity.EXTRA_INSTALLMENT_DUE_DATE, loanDetails.getNextDueDate());
                                                                                     startActivity(paymentIntent);
                                                                                 });
                                                                                homepageBinding.intrestRate.setText(loanDetails.getInterestRate());

                                                                                long days = loanDetails.getDaysUntilNextDueDate();

                                                                                if (days > 0) {
                                                                                    homepageBinding.nextCycle.setText(
                                                                                            days + " days remaining"
                                                                                    );
                                                                                } else if (days == 0) {
                                                                                    homepageBinding.nextCycle.setText(
                                                                                            "Due today"
                                                                                    );
                                                                                } else {
                                                                                    homepageBinding.nextCycle.setText(
                                                                                            "Overdue by " + Math.abs(days) + " days"
                                                                                    );
                                                                                }

                                                                                int paid = loanDetails.getInstallmentsPaid();
                                                                                int total = loanDetails.getInstallmentsTotal();

                                                                                int progress = 0;

                                                                                if (total > 0)
                                                                                    progress = (int) ((paid / (double) total) * 100); //eg 3/6 = 50

                                                                                homepageBinding.progressRepayment.setMax(total);
                                                                                homepageBinding.progressRepayment.setProgress(progress);

                                                                                homepageBinding.tvInstallmentsProgress.setText(
                                                                                        String.format(
                                                                                                Locale.getDefault(),
                                                                                                "%d of %d installments paid",
                                                                                                paid,
                                                                                                total
                                                                                        )
                                                                                );

                                                                                double totalAmountPaid = loanDetails.getTotalRepayment()- loanDetails.getOutStandingAmount();
                                                                                homepageBinding.tvTotalPaid.setText(
                                                                                        String.format(
                                                                                                Locale.getDefault(),
                                                                                                "KES %,.2f",
                                                                                                totalAmountPaid
                                                                                        )
                                                                                );

                                                                                homepageBinding.loanAmountTotal.setText(
                                                                                        String.format(
                                                                                                Locale.getDefault(),
                                                                                                "KSH %,.2f",
                                                                                                loanDetails.getTotalRepayment()
                                                                                        )
                                                                                );

                                                                                viewModel.getRepaymentInstallments(
                                                                                        loanDetails.getLoanId()
                                                                                ).observe(this, scheduleResult -> {

                                                                                    switch (scheduleResult.getStatus()) {

                                                                                        case LOADING:

                                                                                            break;

                                                                                        case SUCCESS:

                                                                                            List<RepaymentInstallments> schedules =
                                                                                                    scheduleResult.getData();

                                                                                            if (schedules != null) {

                                                                                                repaymentScheduleAdapter.setSchedules(
                                                                                                        schedules,
                                                                                                        loanDetails
                                                                                                );
                                                                                            }

                                                                                            break;

                                                                                        case ERROR:

                                                                                            Log.e(
                                                                                                    "HomeActivity",
                                                                                                    "Failed to load repayment schedule: "
                                                                                                            + scheduleResult.getMessage()
                                                                                            );

                                                                                            break;
                                                                                    }
                                                                                });


                                                                                break;

                                                                            case ERROR:
                                                                                String message = loanDetailsResults.getMessage() != null ? loanDetailsResults.getMessage() : "Unknown error occurred";
                                                                                Toast.makeText(
                                                                                        this,
                                                                                        message,
                                                                                        Toast.LENGTH_LONG
                                                                                ).show();

                                                                                Log.e("HomeActivity", "onCreate: "+ message);
                                                                        }
                                                                    }
                                                            );
                                                        }

                                                        break;

                                                    case ERROR:

                                                        homepageBinding.tvLoanBalance.setText(
                                                                "Unable to load"
                                                        );

                                                        break;
                                                }
                                        }
                                );

                                homepageBinding.bottomNavigationView.setOnItemSelectedListener(item->{

                                    int itemId = item.getItemId();

                                    if(itemId == R.id.nav_home)
                                        return true;

                                    else if (itemId == R.id.nav_loans) {
                                        startActivity(new Intent(this, ClientLoansActivity.class));
                                        return true;
                                    }

                                    else if (itemId == R.id.nav_history) {
                                        Intent historyIntent = new Intent(this, TransactionHistoryActivity.class);
                                        startActivity(historyIntent);
                                        return true;
                                    }

                                    else if(itemId == R.id.nav_profile) {
                                        Intent intent = new Intent(
                                                ClientHomepageActivity.this,
                                                ProfileActivity.class
                                        );

                                        startActivity(intent);
                                        return true;
                                    }

                                    return true;
                                });
                            }

                            break;

                        case ERROR:

                            // Show an error message.
                            homepageBinding.tvBorrowerName.setText("Error loading profile");
                            String message = result.getMessage() != null ? result.getMessage() : "Unknown error occurred";
                            Toast.makeText(
                                this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();

                            Log.e("HomeActivity", "onCreate: "+ message);

                            break;
                     }
                 }
         );

        homepageBinding.bottomNavigationView.setSelectedItemId(R.id.nav_home);

        homepageBinding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

    }
}