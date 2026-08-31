package com.terralink.ui.client.scoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.databinding.ActivityIncomeAssessmentListBinding;
import com.terralink.ui.common.SnackbarUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class IncomeAssessmentListActivity extends AppCompatActivity {

    private AssetViewModel viewModel;
    private ActivityIncomeAssessmentListBinding binding;
    private IncomeAssessmentAdapter adapter;
    private long currentClientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomeAssessmentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AssetViewModel.class);

        setupRecyclerView();
        setupListeners();
        fetchUserAndAssessments();
    }

    private void setupRecyclerView() {
        adapter = new IncomeAssessmentAdapter();
        binding.rvAssessmentList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAssessmentList.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadAssessments);
        binding.fabAddAssessment.setOnClickListener(v -> {
            if (currentClientId != -1) {
                Intent intent = new Intent(this, AddIncomeAssessmentActivity.class);
                intent.putExtra("clientId", currentClientId);
                startActivity(intent);
            }
        });
        binding.appBarContent.btnBack.setOnClickListener(v -> finish());
    }

    private void fetchUserAndAssessments() {
        viewModel.getActiveUser().observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) {
                        try {
                            currentClientId = Long.parseLong(resource.getData().getClientId());
                            loadAssessments();
                        } catch (NumberFormatException e) {
                            SnackbarUtils.showError(binding.getRoot(), "Invalid Client ID");
                        }
                    }
                    break;
                case ERROR:
                    SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                    break;
            }
        });
    }

    private void loadAssessments() {
        if (currentClientId == -1) return;

        binding.loadingView.getRoot().setVisibility(View.VISIBLE);
        viewModel.getIncomeAssessments(currentClientId).observe(this, resource -> {
            binding.swipeRefresh.setRefreshing(false);
            binding.loadingView.getRoot().setVisibility(View.GONE);
            
            switch (resource.getStatus()) {
                case SUCCESS:
                    adapter.setAssessments(resource.getData());
                    binding.tvEmptyState.setVisibility(
                            (resource.getData() == null || resource.getData().isEmpty()) ? View.VISIBLE : View.GONE
                    );
                    break;
                case ERROR:
                    SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                    break;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentClientId != -1) {
            loadAssessments();
        }
    }
}
