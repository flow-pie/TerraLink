package com.terralink.ui.officer.scoring;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.databinding.ActivityOfficerClientScoringBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.client.scoring.AssetAdapter;
import com.terralink.ui.client.scoring.AssetViewModel;
import com.terralink.ui.client.scoring.IncomeAssessmentAdapter;
import com.terralink.ui.common.SnackbarUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientScoringActivity extends AppCompatActivity {

    private AssetViewModel viewModel;
    private ActivityOfficerClientScoringBinding binding;
    private AssetAdapter assetAdapter;
    private IncomeAssessmentAdapter incomeAdapter;
    private long clientId;
    private String clientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficerClientScoringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clientId = getIntent().getLongExtra("clientId", -1);
        clientName = getIntent().getStringExtra("clientName");

        if (clientId == -1) {
            Toast.makeText(this, "Invalid Client ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvClientName.setText(clientName != null ? clientName + " - Scoring" : "Client Scoring");

        viewModel = new ViewModelProvider(this).get(AssetViewModel.class);

        setupRecyclerViews();
        setupListeners();
        loadData();
    }

    private void setupRecyclerViews() {
        assetAdapter = new AssetAdapter(new AssetAdapter.OnAssetClickListener() {
            @Override
            public void onAssetClick(AssetResponse asset) {}

            @Override
            public void onVerifyClick(AssetResponse asset) {
                showVerifyAssetDialog(asset);
            }
        });
        assetAdapter.setOfficerMode(true);
        binding.rvAssets.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAssets.setAdapter(assetAdapter);

        incomeAdapter = new IncomeAssessmentAdapter();
        incomeAdapter.setOfficerMode(true, new IncomeAssessmentAdapter.OnVerifyClickListener() {
            @Override
            public void onVerifyClick(IncomeAssessmentResponse assessment) {
                verifyIncome(assessment);
            }

            @Override
            public void onRejectClick(IncomeAssessmentResponse assessment) {
                rejectIncome(assessment);
            }
        });
        binding.rvIncome.setLayoutManager(new LinearLayoutManager(this));
        binding.rvIncome.setAdapter(incomeAdapter);
    }

    private void setupListeners() {
        binding.appBarContent.btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        binding.loadingView.getRoot().setVisibility(View.VISIBLE);
        
        viewModel.getClientAssets(clientId).observe(this, resource -> {
            if (resource.getStatus() == LoginStatus.SUCCESS) {
                assetAdapter.setAssets(resource.getData());
            }
            checkLoadingState();
        });

        viewModel.getIncomeAssessments(clientId).observe(this, resource -> {
            if (resource.getStatus() == LoginStatus.SUCCESS) {
                incomeAdapter.setAssessments(resource.getData());
            }
            checkLoadingState();
        });
    }
    
    private int loadCount = 0;
    private void checkLoadingState() {
        loadCount++;
        if (loadCount >= 2) {
            binding.loadingView.getRoot().setVisibility(View.GONE);
            loadCount = 0;
        }
    }

    private void showVerifyAssetDialog(AssetResponse asset) {
        VerifyAssetDialog dialog = new VerifyAssetDialog(asset, new VerifyAssetDialog.OnVerifyListener() {
            @Override
            public void onConfirm(double verifiedValue) {
                viewModel.verifyAsset(clientId, asset.getId(), verifiedValue).observe(ClientScoringActivity.this, resource -> {
                    switch (resource.getStatus()) {
                        case SUCCESS:
                            SnackbarUtils.showSuccess(binding.getRoot(), "Asset verified");
                            loadData();
                            break;
                        case ERROR:
                            SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                            break;
                    }
                });
            }

            @Override
            public void onReject() {
                viewModel.rejectAsset(clientId, asset.getId()).observe(ClientScoringActivity.this, resource -> {
                    switch (resource.getStatus()) {
                        case SUCCESS:
                            SnackbarUtils.showSuccess(binding.getRoot(), "Asset rejected");
                            loadData();
                            break;
                        case ERROR:
                            SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                            break;
                    }
                });
            }
        });
        dialog.show(getSupportFragmentManager(), "VerifyAssetDialog");
    }

    private void verifyIncome(IncomeAssessmentResponse assessment) {
        viewModel.verifyIncomeAssessment(clientId, assessment.getId()).observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    SnackbarUtils.showSuccess(binding.getRoot(), "Income assessment verified");
                    loadData();
                    break;
                case ERROR:
                    SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                    break;
            }
        });
    }

    private void rejectIncome(IncomeAssessmentResponse assessment) {
        viewModel.rejectIncomeAssessment(clientId, assessment.getId()).observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    SnackbarUtils.showSuccess(binding.getRoot(), "Income assessment rejected");
                    loadData();
                    break;
                case ERROR:
                    SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                    break;
            }
        });
    }
}
