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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LauncherActivity extends AppCompatActivity {

    @Inject
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO!! check user Role
        if(tokenManager.hasSession()){
            startActivity(
                    new Intent(this, ClientHomepageActivity.class)
            );
        }else {
            startActivity(
                    new Intent(this, LoginActivity.class)
            );
        }

        finish();
    }
}