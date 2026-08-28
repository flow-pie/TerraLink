package com.terralink.ui.client.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.R;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityProfileBinding;
import com.terralink.ui.auth.LoginActivity;
import com.terralink.ui.auth.TokenManager;
import com.terralink.ui.client.home.ClientHomepageActivity;
import com.terralink.ui.client.loan.ApplyLoanActivity;
import com.terralink.ui.client.loan.ClientLoansActivity;
import com.terralink.ui.client.notification.NotificationStatusActivity;
import com.terralink.ui.client.transaction.TransactionHistoryActivity;
import com.terralink.ui.common.SnackbarUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    private  ProfileViewModel viewModel;
    private ActivityProfileBinding binding;

    @Inject
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding.bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        binding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshProfile();
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getActiveUser().observe(this,
                result -> {
                        switch (result.getStatus()){
                            case LOADING:

                                // Show loading UI.
                                binding.loadingView.getRoot().setVisibility(View.VISIBLE);
                                break;

                            case SUCCESS:
                                UserProfileResponse client =
                                        result.getData();
                                binding.loadingView.getRoot().setVisibility(View.GONE);

                                // Populate your existing XML.
                                if (client != null && client.getFullName() != null) {
                                    binding.tvProfileName.setText(client.getFullName());
                                    binding.tvEmployeeId.setText(client.getEmployeeNo());

                                    binding.securityDetails.setText(
                                            client.isMfaEnabled()
                                                    ? "MFA enabled"
                                                    : "MFA not enabled"
                                    );

                                    binding.securityCard.setOnClickListener(v -> {
                                        SnackbarUtils.showInfo(binding.getRoot(), "Security settings coming soon");
                                    });

                                    binding.dataSyncCard.setOnClickListener(v -> {
                                        SnackbarUtils.showInfo(binding.getRoot(), "Syncing data...");
                                    });

                                    binding.helpSupportCard.setOnClickListener(v -> {
                                        SnackbarUtils.showInfo(binding.getRoot(), "Support line: +254 700 000 000");
                                    });

                                    binding.logoutCard.setOnClickListener(v -> {
                                        logout();
                                    });

                                    binding.appBarContent.btnNotifications.setOnClickListener(v -> {
                                        startActivity(new Intent(this, NotificationStatusActivity.class));
                                    });

                                }

                                break;

                            case ERROR:
                                binding.loadingView.getRoot().setVisibility(View.GONE);
                                // Show an error message.
                                String message = result.getMessage() != null ? result.getMessage() : "Unknown error occurred";
                                SnackbarUtils.showError(binding.getRoot(), message);

                                Log.e("ProfileActivity", "onCreate: "+ message);

                                break;
                        }
                }
        );

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if(itemId == R.id.nav_home) {
                startActivity(new Intent(this, ClientHomepageActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if(itemId == R.id.nav_loans) {
                startActivity(new Intent(this, ClientLoansActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if(itemId == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            } else if(itemId == R.id.nav_profile) {
                return true;
            }

            return false;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_profile);
    }

    private void logout() {
        viewModel.logout().observe(this, result -> {
            switch (result.getStatus()){
                case LOADING:
                    break;
                case SUCCESS:
                    tokenManager.clearTokens();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );
                    startActivity(intent);
                    break;
                case ERROR:
                    tokenManager.clearTokens();
                    Intent intentErr = new Intent(this, LoginActivity.class);
                    intentErr.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );
                    startActivity(intentErr);
                    break;
            }
        });
    }
}