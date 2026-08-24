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
                                        // Open security screen
                                    });

                                    profileBinding.dataSyncCard.setOnClickListener(v -> {
                                        // Open sync screen
                                    });

                                    profileBinding.logoutCard.setOnClickListener(v -> {
                                        logout();
                                    });

                                }

                                break;

                            case ERROR:
                                profileBinding.loadingView.getRoot().setVisibility(View.GONE);
                                // Show an error message.
                                Toast.makeText(
                                        this,
                                        result.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                Log.e("ProfileActivity", "onCreate: "+ result.getMessage());

                                break;
                        }
                }
        );

    }

    private void logout() {
        tokenManager.clearTokens();

        Intent intent = new Intent(this, LoginActivity.class);

        //clear activity stack
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
    }
}