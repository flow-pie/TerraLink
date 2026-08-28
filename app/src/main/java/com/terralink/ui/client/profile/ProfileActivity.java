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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    private  ProfileViewModel viewModel;

    @Inject
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityProfileBinding profileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(profileBinding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileBinding.bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        profileBinding.fabNewAction.setOnClickListener(v -> {
            startActivity(new Intent(this, ApplyLoanActivity.class));
        });

        profileBinding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshProfile();
            profileBinding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getActiveUser().observe(this,
                result -> {
                        switch (result.getStatus()){
                            case LOADING:

                                // Show loading UI.
                                profileBinding.loadingView.getRoot().setVisibility(View.VISIBLE);
                                break;

                            case SUCCESS:
                                UserProfileResponse client =
                                        result.getData();
                                profileBinding.loadingView.getRoot().setVisibility(View.GONE);

                                // Populate your existing XML.
                                if (client != null && client.getFullName() != null) {
                                    profileBinding.tvProfileName.setText(client.getFullName());
                                    profileBinding.tvEmployeeId.setText(client.getEmployeeNo());

                                    profileBinding.securityDetails.setText(
                                            client.isMfaEnabled()
                                                    ? "MFA enabled"
                                                    : "MFA not enabled"
                                    );

                                    profileBinding.securityCard.setOnClickListener(v -> {
                                        Toast.makeText(this, "Security settings coming soon", Toast.LENGTH_SHORT).show();
                                    });

                                    profileBinding.dataSyncCard.setOnClickListener(v -> {
                                        Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show();
                                    });

                                    profileBinding.helpSupportCard.setOnClickListener(v -> {
                                        Toast.makeText(this, "Support line: +254 700 000 000", Toast.LENGTH_LONG).show();
                                    });

                                    profileBinding.logoutCard.setOnClickListener(v -> {
                                        logout();
                                    });

                                    profileBinding.appBarContent.btnNotifications.setOnClickListener(v -> {
                                        startActivity(new Intent(this, NotificationStatusActivity.class));
                                    });

                                }

                                break;

                            case ERROR:
                                profileBinding.loadingView.getRoot().setVisibility(View.GONE);
                                // Show an error message.
                                String message = result.getMessage() != null ? result.getMessage() : "Unknown error occurred";
                                Toast.makeText(
                                        this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();

                                Log.e("ProfileActivity", "onCreate: "+ message);

                                break;
                        }
                }
        );

        profileBinding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if(itemId == R.id.nav_home) {
                startActivity(new Intent(this, ClientHomepageActivity.class));
                return true;
            } else if(itemId == R.id.nav_loans) {
                startActivity(new Intent(this, ClientLoansActivity.class));
                return true;
            } else if(itemId == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                return true;
            } else if(itemId == R.id.nav_profile) {
                return true;
            }

            return false;
        });

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