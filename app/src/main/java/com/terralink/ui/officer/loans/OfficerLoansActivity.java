package com.terralink.ui.officer.loans;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.databinding.ActivityOfficerLoansBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficerLoansActivity extends AppCompatActivity {

    private ActivityOfficerLoansBinding binding;
    private OfficerLoansViewModel viewModel;
    private OfficerLoansAdapter adapter;
    private String currentStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficerLoansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(OfficerLoansViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupFilters();

        loadLoans();

        binding.swipeRefresh.setOnRefreshListener(this::loadLoans);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new OfficerLoansAdapter(loan -> {
            // TODO: Navigate to loan detail
            Toast.makeText(this, "Loan clicked: " + loan.getLoanNo(), Toast.LENGTH_SHORT).show();
        });
        binding.rvLoans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLoans.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatus = null;
            } else {
                int id = checkedIds.get(0);
                if (id == binding.chipActive.getId()) currentStatus = "ACTIVE";
                else if (id == binding.chipOverdue.getId()) currentStatus = "OVERDUE";
                else if (id == binding.chipCompleted.getId()) currentStatus = "COMPLETED";
                else currentStatus = null;
            }
            loadLoans();
        });
    }

    private void loadLoans() {
        viewModel.getLoans(currentStatus, "").observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    if (result.getData() != null) {
                        adapter.submitList(result.getData());
                    }
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
