package com.terralink.ui.officer.clients;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.databinding.ActivityOfficerClientsBinding;
import com.terralink.ui.auth.LoginStatus;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficerClientsActivity extends AppCompatActivity {

    private ActivityOfficerClientsBinding binding;
    private OfficerClientsViewModel viewModel;
    private OfficerClientsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficerClientsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        viewModel = new ViewModelProvider(this).get(OfficerClientsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupBottomNavigation();

        loadClients("");
        
        binding.swipeRefresh.setOnRefreshListener(() -> loadClients(binding.etSearch.getText().toString()));
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_clients);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_clients) return true;
            
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, com.terralink.ui.officer.dashboard.DashboardActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_loans) {
                startActivity(new Intent(this, com.terralink.ui.officer.loans.OfficerLoansActivity.class));
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

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new OfficerClientsAdapter(client -> {
            ClientDetailsBottomSheetFragment fragment = ClientDetailsBottomSheetFragment.newInstance(client);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
        binding.rvClients.setLayoutManager(new LinearLayoutManager(this));
        binding.rvClients.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadClients(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadClients(String search) {
        viewModel.getClients(1, 50, search).observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    if (result.getData() != null) {
                        adapter.submitList(result.getData().getItems());
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
