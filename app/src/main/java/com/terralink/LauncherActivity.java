package com.terralink;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.terralink.ui.auth.LoginActivity;
import com.terralink.ui.auth.TokenManager;
import com.terralink.ui.client.home.ClientHomepageActivity;
import com.terralink.ui.officer.dashboard.DashboardActivity;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LauncherActivity extends AppCompatActivity {

    @Inject
    TokenManager tokenManager;

    @Inject
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(tokenManager.hasSession()){
            userRepository.getMe().observe(this, result -> {
                if (result.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS && result.getData() != null) {
                    String role = result.getData().getRoleName();
                    if ("Client".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, ClientHomepageActivity.class));
                    } else if ("Admin".equalsIgnoreCase(role)) {
                        // Admins manage the system via the web portal. Sign them
                        // out of the mobile session and send them to login.
                        tokenManager.clearTokens();
                        startActivity(new Intent(this, LoginActivity.class));
                    } else {
                        startActivity(new Intent(this, DashboardActivity.class));
                    }
                    finish();
                } else if (result.getStatus() == com.terralink.ui.auth.LoginStatus.ERROR) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            });
        }else {
            startActivity(
                    new Intent(this, LoginActivity.class)
            );
            finish();
        }
    }
}