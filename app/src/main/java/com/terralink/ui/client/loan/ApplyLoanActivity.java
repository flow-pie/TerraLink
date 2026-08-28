package com.terralink.ui.client.loan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.R;
import com.terralink.data.model.CreditScoreResponse;
import com.terralink.data.model.LoanApplicationRequest;
import com.terralink.data.model.LoanProductResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityApplyLoanBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.common.Resource;
import com.terralink.ui.common.SnackbarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ApplyLoanActivity extends AppCompatActivity {

    private ApplyLoanViewModel viewModel;
    private ActivityApplyLoanBinding binding;
    private List<LoanProductResponse> loanProducts = new ArrayList<>();
    private ArrayAdapter<String> productAdapter;
    private String currentClientId;
    private LoanProductResponse selectedProduct;

    @Override
    public void onCreate(Bundle savedInstances ){
        super.onCreate(savedInstances);

        binding = ActivityApplyLoanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        viewModel = new ViewModelProvider(this).get(ApplyLoanViewModel.class);

        binding.bottomNavigationView.setSelectedItemId(R.id.nav_loans);
        setupNavigation();

        binding.fabNewAction.setOnClickListener(v -> finish());

        binding.appBarContent.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationStatusActivity.class));
        });

        setupSpinners();
        setupListeners();
        fetchInitialData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set selection without triggering navigation
        binding.bottomNavigationView.setOnItemSelectedListener(null);
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_loans);
        setupNavigation();
    }

    private void setupNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, com.terralink.ui.client.home.ClientHomepageActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                finish();
                return true;
            } else if (id == R.id.nav_loans) {
                startActivity(new Intent(this, com.terralink.ui.client.loan.ClientLoansActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                finish();
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, com.terralink.ui.client.transaction.TransactionHistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, com.terralink.ui.client.profile.ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupSpinners() {
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.loanProductSpinner.setAdapter(productAdapter);

        binding.loanProductSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < loanProducts.size()) {
                    selectedProduct = loanProducts.get(position);
                    updateUIConstraints();
                    calculateRepayment();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        binding.requestedAmountInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                calculateRepayment();
            }
        });

        binding.tenureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int months = progress;
                if (selectedProduct != null) {
                    months = Math.max(selectedProduct.getMinimumDuration(), progress);
                }
                binding.tenureValue.setText(String.valueOf(months));
                calculateRepayment();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.submitApplicationButton.setOnClickListener(v -> submitApplication());
    }

    private void fetchInitialData() {
        viewModel.getActiveUser().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                UserProfileResponse user = result.getData();
                currentClientId = user.getClientId();
                binding.clientName.setText(user.getFullName());
                binding.clientDetails.setText(String.format("ID: %s • Role: %s", user.getEmployeeNo(), user.getRoleName()));
                
                // If it's a client, hide the search bar
                if ("Borrower".equals(user.getRoleName()) || "Client".equals(user.getRoleName())) {
                    binding.searchContainer.setVisibility(View.GONE);
                }
                
                fetchCreditScore(user.getClientId());
                fetchLoanProducts();
            }
        });
    }

    private void fetchCreditScore(String clientId) {
        viewModel.getCreditScore(clientId).observe(this, result -> {
            switch (result.getStatus()){
                case SUCCESS:
                    if (result.getData() != null) {
                        CreditScoreResponse score = result.getData();
                        binding.creditScore.setText("CREDIT SCORE: " + score.getCreditScore());
                        binding.clientStatus.setText(score.getRating());
                    }
                    break;
                case ERROR:
                    Log.e("ApplyLoanActivity", "fetchCreditScore error: " + result.getMessage());
                    binding.creditScore.setText("Score: N/A");
            }
        });
    }

    private void fetchLoanProducts() {
        viewModel.getLoanProducts().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                loanProducts = result.getData();
                List<String> names = new ArrayList<>();
                for (LoanProductResponse p : loanProducts) {
                    names.add(p.getName());
                }
                productAdapter.clear();
                productAdapter.addAll(names);
                productAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateUIConstraints() {
        if (selectedProduct == null) return;

        binding.minAmountLabel.setText(String.format(Locale.getDefault(), "Min: %,.0f", selectedProduct.getMinimumAmount()));
        binding.maxAmountLabel.setText(String.format(Locale.getDefault(), "Max: %,.0f", selectedProduct.getMaximumAmount()));
        
        binding.requestedAmountInput.setHint(String.valueOf((int)selectedProduct.getMinimumAmount()));
        
        binding.tenureSeekBar.setMin(selectedProduct.getMinimumDuration());
        binding.tenureSeekBar.setMax(selectedProduct.getMaximumDuration());
        binding.tenureSeekBar.setProgress(selectedProduct.getMinimumDuration());
        binding.tenureValue.setText(String.valueOf(selectedProduct.getMinimumDuration()));
    }

    private void calculateRepayment() {
        if (selectedProduct == null) return;

        String amountStr = binding.requestedAmountInput.getText().toString().trim();
        if (amountStr.isEmpty()) return;

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            return;
        }

        int tenure = Integer.parseInt(binding.tenureValue.getText().toString());
        double rate = selectedProduct.getInterestRate() / 100.0;
        
        //preview calculation
        double totalInterest = amount * rate; //* (tenure/12)
        double totalRepayable = amount + totalInterest;
        double monthlyInstallment = totalRepayable / tenure;

        binding.monthlyInstallmentValue.setText(String.format(Locale.getDefault(), "KES %,.2f", monthlyInstallment));
        binding.totalInterestValue.setText(String.format(Locale.getDefault(), "KES %,.2f", totalInterest));
        binding.totalRepayableValue.setText(String.format(Locale.getDefault(), "KES %,.2f", totalRepayable));
        binding.interestRateValue.setText(String.format(Locale.getDefault(), "%.1f%% RATE", selectedProduct.getInterestRate()));
        
        int interestPercentage = (int) ((totalInterest / totalRepayable) * 100);
        binding.interestProgress.setProgress(interestPercentage);
    }

    private void submitApplication() {
        if (selectedProduct == null || currentClientId == null) {
            SnackbarUtils.showInfo(binding.getRoot(), "Missing application data");
            return;
        }

        String amountText = binding.requestedAmountInput.getText().toString().trim();
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            binding.requestedAmountInput.setError("Invalid amount");
            return;
        }

        if (amount < selectedProduct.getMinimumAmount() || amount > selectedProduct.getMaximumAmount()) {
            binding.requestedAmountInput.setError("Amount must be between " + selectedProduct.getMinimumAmount() + " and " + selectedProduct.getMaximumAmount());
            return;
        }

        int tenure = Integer.parseInt(binding.tenureValue.getText().toString());

        binding.submitApplicationButton.setEnabled(false);
        binding.submitApplicationButton.setText("Submitting...");

        LoanApplicationRequest request = new LoanApplicationRequest(
                selectedProduct.getId(),
                amount,
                tenure,
                "Mobile application"
        );

        viewModel.submitApplication(request).observe(this, result -> {
            switch (result.getStatus()) {
                case SUCCESS:
                    SnackbarUtils.showSuccess(binding.getRoot(), "Application submitted successfully!");
                    binding.submitApplicationButton.postDelayed(this::finish, 2000);
                    break;
                case ERROR:
                    binding.submitApplicationButton.setEnabled(true);
                    binding.submitApplicationButton.setText("Submit Application");
                    SnackbarUtils.showError(binding.getRoot(), result.getMessage());
                    Log.d("submitApplication","Selected product ID: "+selectedProduct.getId());
                    break;
            }
        });
    }
}
