package com.terralink.ui.client.scoring;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.data.model.CreateIncomeAssessmentRequest;
import com.terralink.databinding.ActivityAddIncomeAssessmentBinding;
import com.terralink.ui.common.SnackbarUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddIncomeAssessmentActivity extends AppCompatActivity {

    private AssetViewModel viewModel;
    private ActivityAddIncomeAssessmentBinding binding;
    private long clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddIncomeAssessmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clientId = getIntent().getLongExtra("clientId", -1);
        if (clientId == -1) {
            Toast.makeText(this, "Invalid Client ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AssetViewModel.class);

        setupListeners();
    }

    private void setupListeners() {
        binding.appBarContent.tvTitle.setText("New Assessment");
        binding.appBarContent.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmitAssessment.setOnClickListener(v -> submitAssessment());
    }

    private void submitAssessment() {
        String revenueStr = binding.revenueInput.getText().toString().trim();
        String expensesStr = binding.expensesInput.getText().toString().trim();
        String otherIncomeStr = binding.otherIncomeInput.getText().toString().trim();

        if (revenueStr.isEmpty() || expensesStr.isEmpty()) {
            SnackbarUtils.showInfo(binding.getRoot(), "Revenue and Expenses are required");
            return;
        }

        double revenue, expenses, otherIncome = 0;
        try {
            revenue = Double.parseDouble(revenueStr);
            expenses = Double.parseDouble(expensesStr);
            if (!otherIncomeStr.isEmpty()) {
                otherIncome = Double.parseDouble(otherIncomeStr);
            }
        } catch (NumberFormatException e) {
            SnackbarUtils.showError(binding.getRoot(), "Invalid numeric values");
            return;
        }

        binding.btnSubmitAssessment.setEnabled(false);
        binding.btnSubmitAssessment.setText("Submitting...");

        CreateIncomeAssessmentRequest request = new CreateIncomeAssessmentRequest(revenue, otherIncome, expenses);
        viewModel.createIncomeAssessment(clientId, request).observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    SnackbarUtils.showSuccess(binding.getRoot(), "Assessment submitted successfully");
                    binding.btnSubmitAssessment.postDelayed(this::finish, 1500);
                    break;
                case ERROR:
                    binding.btnSubmitAssessment.setEnabled(true);
                    binding.btnSubmitAssessment.setText("Submit Assessment");
                    SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                    break;
            }
        });
    }
}
