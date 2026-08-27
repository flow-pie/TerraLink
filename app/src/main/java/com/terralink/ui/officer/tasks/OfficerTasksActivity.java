package com.terralink.ui.officer.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.terralink.R;
import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityOfficerTasksBinding;
import com.terralink.ui.auth.LoginActivity;
import com.terralink.ui.auth.TokenManager;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.officer.appraisal.LoanAppraisalBottomSheetFragment;
import com.terralink.ui.officer.dashboard.PendingAppraisalAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficerTasksActivity extends AppCompatActivity {

    private ActivityOfficerTasksBinding binding;
    private OfficerTasksViewModel viewModel;
    private PendingAppraisalAdapter adapter;

    @Inject
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficerTasksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(OfficerTasksViewModel.class);

        setupRecyclerView();
        setupBottomNavigation();
        observeProfile();
        loadTasks();

        binding.swipeRefresh.setOnRefreshListener(this::loadTasks);
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void observeProfile() {
        viewModel.getProfile().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                UserProfileResponse profile = result.getData();
                binding.tvName.setText(profile.getFullName());
                binding.tvRole.setText(profile.getRoleName());
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new PendingAppraisalAdapter(app -> {
            LoanAppraisalBottomSheetFragment fragment = LoanAppraisalBottomSheetFragment.newInstance(app.getId());
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTasks.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_tasks);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) return true;
            
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
            if (id == R.id.nav_loans) {
                startActivity(new Intent(this, com.terralink.ui.officer.loans.OfficerLoansActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadTasks() {
        viewModel.getPendingTasks().observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    if (result.getData() != null) {
                        List<LoanApplicationResponse> actionable = new ArrayList<>();
                        for (LoanApplicationResponse app : result.getData().getItems()) {
                            if ("SUBMITTED".equals(app.getStatus()) || "INFO_REQUESTED".equals(app.getStatus())) {
                                actionable.add(app);
                            }
                        }
                        adapter.submitList(actionable);
                        binding.tvEmptyState.setVisibility(actionable.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void logout() {
        viewModel.logout().observe(this, result -> {
            if (result.getStatus() == LoginStatus.SUCCESS || result.getStatus() == LoginStatus.ERROR) {
                tokenManager.clearTokens();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
