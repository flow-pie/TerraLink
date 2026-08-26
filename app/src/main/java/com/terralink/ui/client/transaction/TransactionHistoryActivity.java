package com.terralink.ui.client.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.PaymentHistoryResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityTransactionHistoryBinding;
import com.terralink.ui.client.loan.ApplyLoanActivity;
import com.terralink.ui.client.notification.NotificationStatusActivity;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionHistoryActivity extends AppCompatActivity {

    private TransactionHistoryViewModel viewModel;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTransactionHistoryBinding binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TransactionHistoryViewModel.class);

        binding.bottomNavigationView.setSelectedItemId(R.id.nav_history);

        binding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        binding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(transactionAdapter);

        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                transactionAdapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.chipAll.setOnClickListener(v -> {
            Toast.makeText(this, "Showing all activity", Toast.LENGTH_SHORT).show();
        });
        binding.chipRepay.setOnClickListener(v -> {
            Toast.makeText(this, "Filtering repayments", Toast.LENGTH_SHORT).show();
        });
        binding.chipDisburse.setOnClickListener(v -> {
            Toast.makeText(this, "Filtering disbursements", Toast.LENGTH_SHORT).show();
        });
        binding.chipFees.setOnClickListener(v -> {
            Toast.makeText(this, "Filtering fees", Toast.LENGTH_SHORT).show();
        });

        binding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        viewModel.getActiveUser().observe(this, userResult -> {
            switch (userResult.getStatus()) {
                case LOADING:
                    binding.loadingView.getRoot().setVisibility(android.view.View.VISIBLE);
                    break;
                case SUCCESS:
                    UserProfileResponse user = userResult.getData();
                    if (user != null) {
                        viewModel.getTransactions(user.getClientId()).observe(this, result -> {
                            switch (result.getStatus()){
                                case LOADING:
                                    binding.loadingView.getRoot().setVisibility(android.view.View.VISIBLE);
                                    break;
                                case SUCCESS:
                                    binding.loadingView.getRoot().setVisibility(android.view.View.GONE);
                                    List<PaymentHistoryResponse> transactions = result.getData();
                                    if(transactions != null){
                                        transactionAdapter.setTransactions(transactions);
                                    }
                                    break;
                                case ERROR:
                                    binding.loadingView.getRoot().setVisibility(android.view.View.GONE);
                                    String message = result.getMessage() != null ? result.getMessage() : "Failed to load transactions";
                                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                                    break;
                            }
                        });
                    }
                    break;
                case ERROR:
                    binding.loadingView.getRoot().setVisibility(android.view.View.GONE);
                    String message = userResult.getMessage() != null ? userResult.getMessage() : "Failed to load user profile";
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}
