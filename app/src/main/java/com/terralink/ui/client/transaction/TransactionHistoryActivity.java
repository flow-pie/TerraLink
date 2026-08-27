package com.terralink.ui.client.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.PaymentHistoryResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityTransactionHistoryBinding;
import com.terralink.ui.client.home.ClientHomepageActivity;
import com.terralink.ui.client.loan.ApplyLoanActivity;
import com.terralink.ui.client.loan.ClientLoansActivity;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.client.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionHistoryActivity extends AppCompatActivity {

    private TransactionHistoryViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private ActivityTransactionHistoryBinding binding;
    private String currentClientId;
    private String currentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TransactionHistoryViewModel.class);

        setupNavigation();
        setupRecyclerView();
        setupFilters();
        observeUser();
    }

    private void setupNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_history);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ClientHomepageActivity.class));
                return true;
            } else if (id == R.id.nav_loans) {
                startActivity(new Intent(this, ClientLoansActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        binding.fabNewAction.setOnClickListener(v -> startActivity(new Intent(this, ApplyLoanActivity.class)));
        binding.appBarContent.btnNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationStatusActivity.class)));
    }

    private void setupRecyclerView() {
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
    }

    private void setupFilters() {
        binding.chipAll.setOnClickListener(v -> {
            currentFilter = null;
            loadTransactions();
        });
        binding.chipRepay.setOnClickListener(v -> {
            currentFilter = "REPAYMENT";
            loadTransactions();
        });
        binding.chipDisburse.setOnClickListener(v -> {
            currentFilter = "DISBURSEMENT";
            loadTransactions();
        });
        binding.chipFees.setOnClickListener(v -> {
            currentFilter = "FEE";
            loadTransactions();
        });
    }

    private void observeUser() {
        viewModel.getActiveUser().observe(this, result -> {
            if (result.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS && result.getData() != null) {
                currentClientId = result.getData().getClientId();
                loadTransactions();
            }
        });
    }

    private void loadTransactions() {
        if (currentClientId == null) return;
        viewModel.getTransactions(currentClientId, currentFilter).observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.loadingView.getRoot().setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.loadingView.getRoot().setVisibility(View.GONE);
                    if (result.getData() != null) {
                        transactionAdapter.setTransactions(result.getData());
                    }
                    break;
                case ERROR:
                    binding.loadingView.getRoot().setVisibility(View.GONE);
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
