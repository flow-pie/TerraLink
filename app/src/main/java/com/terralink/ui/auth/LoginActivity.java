package com.terralink.ui.auth;
import static com.terralink.ui.auth.LoginStatus.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.terralink.data.model.LoginRequest;
import com.terralink.data.model.LoginResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityLoginBinding;
import com.terralink.ui.client.home.ClientHomepageActivity;
import com.terralink.ui.common.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private  ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //provided by android to make view model instances
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getLoginResult().observe(
                this,
                result -> handleLoginResult(result)
        );

        binding.btnLogin.setOnClickListener(view -> {

            String loginIdentifier = binding.etIdentifierId.getText().toString().trim();
            String password = binding.etPassword.getText().toString();

            if(loginIdentifier.isEmpty()){
                binding.etIdentifierId.setError("Login identifier required");
                return;
            }

            if(password.isEmpty()){
                binding.etPassword.setError("Password required");
                return;
            }

            LoginRequest request = new LoginRequest(loginIdentifier, password);

            viewModel.login(request);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot password flow", Toast.LENGTH_SHORT).show();
        });

        binding.tvSupport.setOnClickListener(v -> {
            Toast.makeText(this, "Contact support@terralink.com", Toast.LENGTH_SHORT).show();
        });

        binding.tvRegionStatus.setOnClickListener(v -> {
            Toast.makeText(this, "All systems operational", Toast.LENGTH_SHORT).show();
        });

        binding.tvNoAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Registration is officer-assisted. Please visit a branch.", Toast.LENGTH_LONG).show();
        });

    }

    private void handleLoginResult(Resource<LoginResponse> result) {
            switch (result.getStatus()) {
                case LOADING:
                    binding.btnLogin.setEnabled(false);
                    binding.progressLogin.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    binding.progressLogin.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);

                    LoginResponse response = result.getData();

                    startActivity(new Intent(LoginActivity.this, ClientHomepageActivity.class));
                    finish(); //Pressing Back won't take the user back to login.

                    break;

                case ERROR:
                    binding.progressLogin.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);

                    String message = result.getMessage() != null ? result.getMessage() : "Unknown error occurred";
                    Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();

                    break;

            }
    }
}
