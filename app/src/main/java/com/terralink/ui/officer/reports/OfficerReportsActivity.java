package com.terralink.ui.officer.reports;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.terralink.R;
import com.terralink.databinding.ActivityOfficerReportsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficerReportsActivity extends AppCompatActivity {

    private ActivityOfficerReportsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficerReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_reports);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reports) return true;
            
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
}
