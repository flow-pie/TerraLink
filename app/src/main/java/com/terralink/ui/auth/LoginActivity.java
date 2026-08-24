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
                Toast.makeText(
                        this,
                        "Login button clicked",
                        Toast.LENGTH_SHORT
                ).show();

            LoginRequest request = new LoginRequest(loginIdentifier, password);

            viewModel.login(request);
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

                    Toast.makeText(
                            this,
                            result.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    break;

            }
    }
}
