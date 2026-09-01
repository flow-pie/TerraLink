package com.terralink.ui.client.loan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.SnackbarUtils;
import com.terralink.ui.officer.loans.LoanProductAdapter;

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
                int months = progress;
                if (selectedProduct != null) {
                    months = Math.max(selectedProduct.getMinimumDuration(), progress);
                }
                binding.tvTenureDisplay.setText(months + " Months");
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
        binding.btnNext.setText(step == 3 ? "SUBMIT APPLICATION" : "CONTINUE");
        binding.btnNext.setVisibility(step == 1 ? View.GONE : View.VISIBLE);

        updateStepperUI(step);

        if (step == 2 && selectedProduct != null) {
            updateUIConstraints();
        } else if (step == 3) {
            populateSummary();
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
            if (validateStep2()) showStep(3);
        } else if (currentStep == 3) {
            submitApplication();
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

            binding.tvMonthlyInstallment.setText(String.format(Locale.getDefault(), "KES %,.2f", monthly));
            binding.tvTotalInterest.setText(String.format(Locale.getDefault(), "KES %,.2f", totalInterest));
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
        binding.tvSummaryProduct.setText(selectedProduct.getName());
        binding.tvSummaryAmount.setText(String.format(Locale.getDefault(), "KES %,.2f", Double.parseDouble(binding.etRequestedAmount.getText().toString())));
        binding.tvSummaryTenure.setText(binding.sbTenure.getProgress() + " Months");
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
                binding.btnNext.setText("SUBMIT APPLICATION");
                SnackbarUtils.showError(binding.getRoot(), result.getMessage());
            }
        });
    }
}
