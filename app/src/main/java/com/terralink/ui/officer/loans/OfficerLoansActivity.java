package com.terralink.ui.officer.loans;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.CloseLoanResponse;
import com.terralink.data.model.LoanListItemResponse;
import com.terralink.data.model.PortfolioSummaryResponse;
import com.terralink.databinding.ActivityOfficerLoansBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.FileUtils;
import com.terralink.ui.common.SnackbarUtils;
import com.terralink.ui.officer.products.AddProductBottomSheetFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficerLoansActivity extends AppCompatActivity {

    private ActivityOfficerLoansBinding binding;
    private OfficerLoansViewModel viewModel;
    private OfficerLoansAdapter loansAdapter;
    private LoanProductAdapter productsAdapter;
    private String currentStatus = null;
    private List<LoanListItemResponse> currentLoansList = new ArrayList<>();

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
        setupSorting();

        loadData();

        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        
        binding.btnViewMap.setOnClickListener(v -> {
            SnackbarUtils.showInfo(binding.getRoot(), "Map view coming soon");
        });
    }

    private void setupRecyclerViews() {
        loansAdapter = new OfficerLoansAdapter(loan -> {
            SnackbarUtils.showInfo(binding.getRoot(), "LID: " + loan.getLoanNo() + " | Client: " + loan.getClientFullName());
        }, this::confirmLoanClosure);
        
        binding.rvLoans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLoans.setAdapter(loansAdapter);
        binding.rvLoans.setNestedScrollingEnabled(false);

        productsAdapter = new LoanProductAdapter(product -> {
            SnackbarUtils.showInfo(binding.getRoot(), "Product: " + product.getName());
        });
        binding.rvLoanProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLoanProducts.setAdapter(productsAdapter);
        binding.rvLoanProducts.setNestedScrollingEnabled(false);
    }

    private void confirmLoanClosure(LoanListItemResponse loan) {
        new AlertDialog.Builder(this)
                .setTitle("Close Loan")
                .setMessage("Are you sure you want to close loan #" + loan.getLoanNo() + "? This will generate a closure certificate.")
                .setPositiveButton("CLOSE", (dialog, which) -> executeLoanClosure(loan))
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void executeLoanClosure(LoanListItemResponse loan) {
        viewModel.closeLoan(loan.getId()).observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                SnackbarUtils.showSuccess(binding.getRoot(), "Loan Closed Successfully");
                showDownloadCertificateOption(result.getData());
                loadData(); // Refresh list
            } else if (result.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Closure failed: " + result.getMessage());
            }
        });
    }

    private void showDownloadCertificateOption(CloseLoanResponse closure) {
        new AlertDialog.Builder(this)
                .setTitle("Closure Complete")
                .setMessage("Certificate #" + closure.getCertificateNumber() + " generated. Would you like to view it?")
                .setPositiveButton("VIEW PDF", (dialog, which) -> downloadAndOpenCertificate(closure))
                .setNegativeButton("NOT NOW", null)
                .show();
    }

    private void downloadAndOpenCertificate(CloseLoanResponse closure) {
        viewModel.getClosureCertificate(closure.getId()).observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                FileUtils.saveAndOpenPdf(this, result.getData(), "Completion_Certificate_" + closure.getCertificateNumber());
            } else if (result.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Failed to download certificate");
            }
        });
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
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                currentStatus = null;
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipActive) currentStatus = "ACTIVE";
                else if (id == R.id.chipArrears) currentStatus = "IN_ARREARS";
                else if (id == R.id.chipPending) currentStatus = "PENDING_DISBURSEMENT";
                else currentStatus = null;
            }
            loadLoans(binding.etSearchLoan.getText().toString());
        });
    }

    private void setupSorting() {
        binding.btnSort.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Client Name (A-Z)");
            popup.getMenu().add("Client Name (Z-A)");
            popup.getMenu().add("Outstanding Amount (High-Low)");
            popup.getMenu().add("Outstanding Amount (Low-High)");
            
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.contains("Name (A-Z)")) {
                    Collections.sort(currentLoansList, Comparator.comparing(LoanListItemResponse::getClientFullName));
                } else if (title.contains("Name (Z-A)")) {
                    Collections.sort(currentLoansList, (l1, l2) -> l2.getClientFullName().compareTo(l1.getClientFullName()));
                } else if (title.contains("Amount (High-Low)")) {
                    Collections.sort(currentLoansList, (l1, l2) -> Double.compare(l2.getOutstandingAmount(), l1.getOutstandingAmount()));
                } else if (title.contains("Amount (Low-High)")) {
                    Collections.sort(currentLoansList, Comparator.comparingDouble(LoanListItemResponse::getOutstandingAmount));
                }
                loansAdapter.submitList(new ArrayList<>(currentLoansList));
                return true;
            });
            popup.show();
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
            if (id == R.id.nav_tasks) {
                startActivity(new Intent(this, com.terralink.ui.officer.tasks.OfficerTasksActivity.class));
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
                
                double total = summary.getOutstandingPortfolio();
                double arrears = summary.getArrearsAmount();
                if (total > 0) {
                    double par = (arrears / total) * 100;
                    binding.tvPar.setText(String.format(Locale.getDefault(), "%.1f%%", par));
                } else {
                    binding.tvPar.setText("0.0%");
                }

            } else if (result.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Failed to load summary");
            }
        });
    }

    private void loadProducts() {
        viewModel.getLoanProducts().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                productsAdapter.submitList(new ArrayList<>(result.getData()));
                binding.tvProductCount.setText(String.format(Locale.getDefault(), "%d Available products", result.getData().size()));
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
                        currentLoansList = result.getData().getItems();
                        loansAdapter.submitList(new ArrayList<>(currentLoansList));
                    } else {
                        currentLoansList = new ArrayList<>();
                        loansAdapter.submitList(currentLoansList);
                    }
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    SnackbarUtils.showError(binding.getRoot(), "Failed to load loans");
                    break;
            }
        });
    }
}
