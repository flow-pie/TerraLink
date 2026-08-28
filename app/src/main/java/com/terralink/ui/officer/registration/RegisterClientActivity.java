package com.terralink.ui.officer.registration;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.R;
import com.terralink.databinding.ActivityRegisterClientBinding;
import com.terralink.ui.auth.LoginStatus;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterClientActivity extends AppCompatActivity {

    private ActivityRegisterClientBinding binding;
    private RegisterClientViewModel viewModel;
    private int currentStep = 1;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        viewModel = new ViewModelProvider(this).get(RegisterClientViewModel.class);

        showStep(1);

        binding.btnNextStep.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < 4) {
                    currentStep++;
                    showStep(currentStep);
                } else {
                    performRegistration();
                }
            }
        });

        binding.btnPreviousStep.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                showStep(currentStep);
            }
        });
    }

    private void showStep(int step) {
        switch (step) {
            case 1:
                currentFragment = new PersonalInfoFragment();
                binding.btnPreviousStep.setVisibility(View.GONE);
                binding.btnNextStep.setText("NEXT STEP");
                break;
            case 2:
                currentFragment = new AddressFragment();
                binding.btnPreviousStep.setVisibility(View.VISIBLE);
                binding.btnNextStep.setText("NEXT STEP");
                break;
            case 3:
                currentFragment = new KycDocumentsFragment();
                binding.btnPreviousStep.setVisibility(View.VISIBLE);
                binding.btnNextStep.setText("NEXT STEP");
                break;
            case 4:
                currentFragment = new ReviewFragment();
                binding.btnPreviousStep.setVisibility(View.VISIBLE);
                binding.btnNextStep.setText("REGISTER CLIENT");
                break;
            default:
                return;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.stepFragmentContainer, currentFragment)
                .commit();

        updateStepperUI(step);
    }

    private boolean validateCurrentStep() {
        if (currentFragment instanceof PersonalInfoFragment) {
            return ((PersonalInfoFragment) currentFragment).validate();
        } else if (currentFragment instanceof AddressFragment) {
            return ((AddressFragment) currentFragment).validate();
        } else if (currentFragment instanceof KycDocumentsFragment) {
            return ((KycDocumentsFragment) currentFragment).validate();
        }
        return true;
    }

    private void updateStepperUI(int step) {
        setStepActive(findViewById(R.id.step1Indicator), findViewById(R.id.step1Line), step >= 1);
        setStepActive(findViewById(R.id.step2Indicator), findViewById(R.id.step2Line), step >= 2);
        setStepActive(findViewById(R.id.step3Indicator), findViewById(R.id.step3Line), step >= 3);
        setStepActive(findViewById(R.id.step4Indicator), findViewById(R.id.step4Line), step >= 4);
    }

    private void setStepActive(TextView indicator, View line, boolean active) {
        if (indicator == null) return;
        indicator.setBackgroundResource(active ? R.drawable.bg_stepper_active : R.drawable.bg_stepper_inactive);
        indicator.setTextColor(ContextCompat.getColor(this, active ? R.color.on_terracotta : R.color.text_secondary));
        if (line != null) {
            line.setBackgroundColor(ContextCompat.getColor(this, active ? R.color.terracotta_primary : R.color.stepper_track));
        }
    }

    private void performRegistration() {
        if (!validateForm()) return;

        viewModel.register().observe(this, result -> {
            if (result.getStatus() == LoginStatus.LOADING) {
                binding.loadingView.getRoot().setVisibility(View.VISIBLE);
            } else if (result.getStatus() == LoginStatus.SUCCESS) {
                binding.loadingView.getRoot().setVisibility(View.GONE);
                Toast.makeText(this, "Client registered successfully!", Toast.LENGTH_LONG).show();
                finish();
            } else if (result.getStatus() == LoginStatus.ERROR) {
                binding.loadingView.getRoot().setVisibility(View.GONE);
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                Log.d( "performRegistration: An error occured",result.getMessage());
            }
        });
    }

    private boolean validateForm() {
        if (isEmpty(viewModel.fullName) || isEmpty(viewModel.nationalId) || 
            isEmpty(viewModel.phone) || isEmpty(viewModel.dateOfBirth) || 
            isEmpty(viewModel.email) || isEmpty(viewModel.password) ||
            isEmpty(viewModel.address)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (viewModel.idFront == null || viewModel.idBack == null || viewModel.passportPhoto == null) {
            Toast.makeText(this, "Please capture all required KYC documents", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
