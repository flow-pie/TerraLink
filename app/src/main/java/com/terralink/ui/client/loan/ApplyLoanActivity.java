package com.terralink.ui.client.loan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.LoanApplicationRequest;
import com.terralink.data.model.LoanProductResponse;
import com.terralink.databinding.ActivityApplyLoanBinding;
import com.terralink.databinding.LayoutSummaryRowBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.SnackbarUtils;
import com.terralink.ui.officer.loans.LoanProductAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ApplyLoanActivity extends AppCompatActivity {

    private ApplyLoanViewModel viewModel;
    private ActivityApplyLoanBinding binding;
    private LoanProductAdapter productAdapter;
    private List<LoanProductResponse> loanProducts = new ArrayList<>();
    private LoanProductResponse selectedProduct;
    private int currentStep = 1;
    private boolean isEligible = false;
    private final NumberFormat ksh = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplyLoanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ApplyLoanViewModel.class);

        setupRecyclerView();
        setupListeners();
        showStep(1);
        fetchProducts();
    }

    private void setupRecyclerView() {
        productAdapter = new LoanProductAdapter(product -> {
            selectedProduct = product;
            goToNextStep();
        });
        binding.rvLoanProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLoanProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        binding.appBarContent.btnBack.setOnClickListener(v -> handleBack());
        binding.btnActionBack.setOnClickListener(v -> handleBack());
        binding.btnNext.setOnClickListener(v -> goToNextStep());

        binding.etRequestedAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calculateRepayment(); }
        });

        binding.sbTenure.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int months = Math.max(1, progress);
                if (selectedProduct != null) {
                    months = Math.max(selectedProduct.getMinimumDuration(), progress);
                }
                binding.tvTenureDisplay.setText(String.format(Locale.getDefault(), "%d Months", months));
                calculateRepayment();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void fetchProducts() {
        viewModel.getLoanProducts().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                loanProducts = result.getData();
                productAdapter.submitList(loanProducts);
            }
        });
    }

    private void showStep(int step) {
        currentStep = step;
        binding.layoutStep1.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.layoutStep2.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.layoutStep3.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        binding.btnActionBack.setVisibility(step > 1 ? View.VISIBLE : View.GONE);
        
        if (step == 3) {
            binding.btnNext.setText(isEligible ? "ACCEPT & SUBMIT" : "BACK TO HOME");
        } else {
            binding.btnNext.setText("CONTINUE");
        }
        
        binding.btnNext.setVisibility(step == 1 ? View.GONE : View.VISIBLE);

        updateStepperUI(step);

        if (step == 2 && selectedProduct != null) {
            updateUIConstraints();
        }
    }

    private void updateStepperUI(int step) {
        setStepActive(findViewById(R.id.step1Indicator), findViewById(R.id.step1Line), step >= 1);
        setStepActive(findViewById(R.id.step2Indicator), findViewById(R.id.step2Line), step >= 2);
        setStepActive(findViewById(R.id.step3Indicator), null, step >= 3);
    }

    private void setStepActive(TextView indicator, View line, boolean active) {
        if (indicator == null) return;
        indicator.setBackgroundResource(active ? R.drawable.bg_stepper_active : R.drawable.bg_stepper_inactive);
        indicator.setTextColor(ContextCompat.getColor(this, active ? R.color.on_terracotta : R.color.text_secondary));
        if (line != null) {
            line.setBackgroundColor(ContextCompat.getColor(this, active ? R.color.terracotta_primary : R.color.stepper_track));
        }
    }

    private void goToNextStep() {
        if (currentStep == 1 && selectedProduct != null) {
            showStep(2);
        } else if (currentStep == 2) {
            if (validateStep2()) {
                checkEligibility();
            }
        } else if (currentStep == 3) {
            if (isEligible) {
                if (binding.checkTerms.isChecked()) {
                    submitApplication();
                } else {
                    SnackbarUtils.showInfo(binding.getRoot(), "Please accept terms and conditions");
                }
            } else {
                finish();
            }
        }
    }

    private void checkEligibility() {
        binding.loadingView.getRoot().setVisibility(View.VISIBLE);
        
        viewModel.getActiveUser().observe(this, userResult -> {
            if (userResult.getStatus() == LoginStatus.SUCCESS && userResult.getData() != null) {
                String clientId = String.valueOf(userResult.getData().getClientId());
                double amount = Double.parseDouble(binding.etRequestedAmount.getText().toString());
                
                viewModel.calculateCreditScore(clientId, amount).observe(this, scoreResult -> {
                    binding.loadingView.getRoot().setVisibility(View.GONE);
                    if (scoreResult.getStatus() == LoginStatus.SUCCESS && scoreResult.getData() != null) {
                        int score = scoreResult.getData().getCreditScore();
                        // Self-eligibility threshold
                        isEligible = score >= 60;
                        
                        showResult(isEligible, scoreResult.getData().getRating());
                        showStep(3);
                    } else if (scoreResult.getStatus() == LoginStatus.ERROR) {
                        SnackbarUtils.showError(binding.getRoot(), "Error checking eligibility");
                    }
                });
            } else if (userResult.getStatus() == LoginStatus.ERROR) {
                binding.loadingView.getRoot().setVisibility(View.GONE);
                SnackbarUtils.showError(binding.getRoot(), "User session error");
            }
        });
    }

    private void showResult(boolean eligible, String rating) {
        binding.layoutEligible.setVisibility(eligible ? View.VISIBLE : View.GONE);
        binding.layoutNotEligible.setVisibility(eligible ? View.GONE : View.VISIBLE);
        
        if (eligible) {
            populateSummary();
            binding.tvEligibleMsg.setText("Congrats! You are eligible with a " + rating + " rating.");
        } else {
            binding.tvNotEligibleReason.setText("Based on our assessment, your current credit rating (" + rating + ") does not meet the criteria for this loan. Enhance your profile by providing more financial data.");
        }
    }

    private void handleBack() {
        if (currentStep > 1) {
            showStep(currentStep - 1);
        } else {
            finish();
        }
    }

    private void updateUIConstraints() {
        binding.tvMinAmount.setText(String.format(Locale.getDefault(), "Min: %,.0f", selectedProduct.getMinimumAmount()));
        binding.tvMaxAmount.setText(String.format(Locale.getDefault(), "Max: %,.0f", selectedProduct.getMaximumAmount()));
        
        binding.sbTenure.setMin(selectedProduct.getMinimumDuration());
        binding.sbTenure.setMax(selectedProduct.getMaximumDuration());
        binding.sbTenure.setProgress(selectedProduct.getMinimumDuration());
        
        binding.etRequestedAmount.setText(String.valueOf((int)selectedProduct.getMinimumAmount()));
    }

    private void calculateRepayment() {
        if (selectedProduct == null) return;
        String amountStr = binding.etRequestedAmount.getText().toString().trim();
        if (amountStr.isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            int tenure = binding.sbTenure.getProgress();
            double rate = selectedProduct.getInterestRate() / 100.0;
            
            double totalInterest = amount * rate;
            double totalRepayable = amount + totalInterest;
            double monthly = totalRepayable / tenure;

            binding.tvMonthlyInstallment.setText(ksh.format(monthly));
            binding.tvTotalInterest.setText(ksh.format(totalInterest));
        } catch (Exception ignored) {}
    }

    private boolean validateStep2() {
        String amountStr = binding.etRequestedAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            binding.etRequestedAmount.setError("Amount required");
            return false;
        }
        double amount = Double.parseDouble(amountStr);
        if (amount < selectedProduct.getMinimumAmount() || amount > selectedProduct.getMaximumAmount()) {
            binding.etRequestedAmount.setError("Out of range");
            return false;
        }
        return true;
    }

    private void populateSummary() {
        setSummaryRow(binding.summaryProduct, "Purpose of Loan", selectedProduct.getName());
        setSummaryRow(binding.summaryAmount, "Requested Amount", ksh.format(Double.parseDouble(binding.etRequestedAmount.getText().toString())));
        setSummaryRow(binding.summaryTenure, "No of Payments", binding.sbTenure.getProgress() + " Months");
        
        double amount = Double.parseDouble(binding.etRequestedAmount.getText().toString());
        double rate = selectedProduct.getInterestRate();
        double totalRepayable = amount + (amount * (rate / 100.0));
        double monthly = totalRepayable / binding.sbTenure.getProgress();

        setSummaryRow(binding.summaryMonthly, "Monthly Payment", ksh.format(monthly));
        setSummaryRow(binding.summaryRate, "Interest Rate", rate + "%");
        setSummaryRow(binding.summaryTotal, "Total Payback Amount", ksh.format(totalRepayable));
    }

    private void setSummaryRow(LayoutSummaryRowBinding row, String label, String value) {
        row.tvLabel.setText(label);
        row.tvValue.setText(value);
    }

    private void submitApplication() {
        binding.btnNext.setEnabled(false);
        binding.btnNext.setText("Submitting...");
        
        LoanApplicationRequest request = new LoanApplicationRequest(
                selectedProduct.getId(),
                Double.parseDouble(binding.etRequestedAmount.getText().toString()),
                binding.sbTenure.getProgress(),
                "Mobile application"
        );

        viewModel.submitApplication(request).observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS) {
                SnackbarUtils.showSuccess(binding.getRoot(), "Application Submitted!");
                binding.btnNext.postDelayed(this::finish, 2000);
            } else if (result.getStatus() == LoginStatus.ERROR) {
                binding.btnNext.setEnabled(true);
                binding.btnNext.setText("ACCEPT & SUBMIT");
                SnackbarUtils.showError(binding.getRoot(), result.getMessage());
            }
        });
    }
}
