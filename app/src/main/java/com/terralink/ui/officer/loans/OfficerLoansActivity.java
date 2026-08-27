package com.terralink.ui.officer.loans;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.PortfolioSummaryResponse;
import com.terralink.databinding.ActivityOfficerLoansBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.officer.products.AddProductBottomSheetFragment;

import java.util.ArrayList;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficerLoansActivity extends AppCompatActivity {

    private ActivityOfficerLoansBinding binding;
    private OfficerLoansViewModel viewModel;
    private OfficerLoansAdapter loansAdapter;
    private LoanProductAdapter productsAdapter;
    private String currentStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficerLoansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        viewModel = new ViewModelProvider(this).get(OfficerLoansViewModel.class);

        setupRecyclerViews();
        setupSearch();
        setupFilters();
        setupBottomNavigation();
        setupAddProduct();

        loadData();

        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        
        binding.btnViewMap.setOnClickListener(v -> {
            Toast.makeText(this, "Map view coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerViews() {
        // Loans Portfolio
        loansAdapter = new OfficerLoansAdapter(loan -> {
            Toast.makeText(this, "Loan: " + loan.getLoanNo(), Toast.LENGTH_SHORT).show();
        });
        binding.rvLoans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLoans.setAdapter(loansAdapter);
        binding.rvLoans.setNestedScrollingEnabled(false);

        // Loan Products
        productsAdapter = new LoanProductAdapter(product -> {
            Toast.makeText(this, "Product: " + product.getName(), Toast.LENGTH_SHORT).show();
        });
        binding.rvLoanProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLoanProducts.setAdapter(productsAdapter);
        binding.rvLoanProducts.setNestedScrollingEnabled(false);
    }

    private void setupSearch() {
        binding.etSearchLoan.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadLoans(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAddProduct() {
        binding.btnAddProduct.setOnClickListener(v -> {
            AddProductBottomSheetFragment fragment = AddProductBottomSheetFragment.newInstance();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
    }

    private void setupFilters() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatus = null;
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipActive) currentStatus = "ACTIVE";
                else if (id == R.id.chipArrears) currentStatus = "ARREARS";
                else if (id == R.id.chipPending) currentStatus = "PENDING_DISBURSEMENT";
                else currentStatus = null;
            }
            loadLoans(binding.etSearchLoan.getText().toString());
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_loans);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_loans) return true;
            
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, com.terralink.ui.officer.dashboard.DashboardActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_clients) {
                startActivity(new Intent(this, com.terralink.ui.officer.clients.OfficerClientsActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_reports) {
                startActivity(new Intent(this, com.terralink.ui.officer.reports.OfficerReportsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadData() {
        loadSummary();
        loadProducts();
        loadLoans(binding.etSearchLoan.getText().toString());
    }

    private void loadSummary() {
        viewModel.getPortfolioSummary().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                PortfolioSummaryResponse summary = result.getData();
                double value = summary.getOutstandingPortfolio();
                if (value >= 1_000_000) {
                    binding.tvPortfolioValue.setText(String.format(Locale.getDefault(), "KES %,.1fM", value / 1_000_000.0));
                } else {
                    binding.tvPortfolioValue.setText(String.format(Locale.getDefault(), "KES %,.0f", value));
                }
                binding.tvActiveLoans.setText(String.valueOf(summary.getActiveLoansCount()));
                binding.tvPar.setText("2.4%"); // Placeholder until backend provides real PAR
            } else if (result.getStatus() == LoginStatus.ERROR) {
                Toast.makeText(this, "Failed to load summary", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        android.util.Log.d("OfficerLoans", "Loading loan products...");
        viewModel.getLoanProducts().observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    android.util.Log.d("OfficerLoans", "Products: LOADING");
                    break;
                case SUCCESS:
                    if (result.getData() != null) {
                        android.util.Log.d("OfficerLoans", "Products: SUCCESS, count=" + result.getData().size());
                        productsAdapter.submitList(new ArrayList<>(result.getData())); // Force update with new list
                        binding.tvProductCount.setText(String.format(Locale.getDefault(), "%d Available products", result.getData().size()));
                    } else {
                        android.util.Log.d("OfficerLoans", "Products: SUCCESS, but data is null");
                        productsAdapter.submitList(new ArrayList<>());
                    }
                    break;
                case ERROR:
                    android.util.Log.e("OfficerLoans", "Products: ERROR - " + result.getMessage());
                    Toast.makeText(this, "Failed to load products: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void loadLoans(String search) {
        viewModel.getLoans(currentStatus, search).observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    if (result.getData() != null && result.getData().getItems() != null) {
                        loansAdapter.submitList(result.getData().getItems());
                    } else {
                        loansAdapter.submitList(new ArrayList<>());
                    }
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, "Failed to load loans: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
