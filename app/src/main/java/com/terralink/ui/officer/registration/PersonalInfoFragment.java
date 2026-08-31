package com.terralink.ui.officer.registration;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.databinding.FragmentPersonalInfoBinding;

import java.util.Calendar;
import java.util.Locale;

public class PersonalInfoFragment extends Fragment {

    private FragmentPersonalInfoBinding binding;
    private RegisterClientViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(RegisterClientViewModel.class);

        setupGenderSpinner();
        setupTextWatchers();
        setupExistenceChecks();

        // Populate existing data if any
        binding.etFullName.setText(viewModel.fullName);
        binding.etGovId.setText(viewModel.nationalId);
        binding.etPhone.setText(viewModel.phone);
        binding.etDob.setText(viewModel.dateOfBirth);
        binding.etEmail.setText(viewModel.email);
        binding.etPassword.setText(viewModel.password);
        binding.etConfirmPassword.setText(viewModel.password);

        binding.etDob.setFocusable(false);
        binding.etDob.setClickable(true);
        binding.etDob.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Calculate max date allowed (18 years ago from today)
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(year - 18, month, day);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    binding.etDob.setText(date);
                    viewModel.dateOfBirth = date;
                }, year - 18, month, day);
        
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void setupGenderSpinner() {
        String[] genders = {"MALE", "FEMALE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGender.setAdapter(adapter);

        if (viewModel.gender != null) {
            int pos = adapter.getPosition(viewModel.gender);
            if (pos >= 0) binding.spinnerGender.setSelection(pos);
        }

        binding.spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.gender = genders[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupTextWatchers() {
        binding.etFullName.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.fullName = s));
        binding.etGovId.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.nationalId = s));
        binding.etPhone.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.phone = s));
        binding.etDob.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.dateOfBirth = s));
        binding.etEmail.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.email = s));
        binding.etPassword.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.password = s));
    }

    private void setupExistenceChecks() {
        binding.etGovId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && viewModel.nationalId != null && viewModel.nationalId.length() == 8) {
                checkExists(viewModel.nationalId, "National ID already exists", binding.tilGovId);
            }
        });

        binding.etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && viewModel.phone != null && viewModel.phone.length() == 10) {
                checkExists(viewModel.phone, "Phone number already exists", binding.tilPhone);
            }
        });

        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && viewModel.email != null && viewModel.email.contains("@")) {
                checkExists(viewModel.email, "Email already exists", binding.tilEmail);
            }
        });
    }

    private void checkExists(String query, String errorMessage, com.google.android.material.textfield.TextInputLayout layout) {
        viewModel.checkClientExists(query).observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS && resource.getData() != null) {
                if (resource.getData().getTotalCount() > 0) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    public boolean validate() {
        boolean valid = true;
        if (isEmpty(viewModel.fullName)) {
            binding.tilFullName.setError("Full name is required");
            valid = false;
        } else {
            binding.tilFullName.setError(null);
        }

        if (isEmpty(viewModel.nationalId)) {
            binding.tilGovId.setError("ID is required");
            valid = false;
        } else if (viewModel.nationalId.length() != 8) {
            binding.tilGovId.setError("ID must be exactly 8 digits");
            valid = false;
        } else if (!viewModel.nationalId.matches("\\d+")) {
            binding.tilGovId.setError("ID must be numeric");
            valid = false;
        } else {
            // Keep existing error if it's "already exists"
            if (binding.tilGovId.getError() == null || !binding.tilGovId.getError().toString().contains("exists")) {
                binding.tilGovId.setError(null);
            }
            if (binding.tilGovId.getError() != null) valid = false;
        }

        if (isEmpty(viewModel.phone)) {
            binding.tilPhone.setError("Phone is required");
            valid = false;
        } else if (viewModel.phone.length() != 10) {
            binding.tilPhone.setError("Phone must be exactly 10 digits");
            valid = false;
        } else if (!viewModel.phone.matches("\\d+")) {
            binding.tilPhone.setError("Phone must be numeric");
            valid = false;
        } else {
            if (binding.tilPhone.getError() == null || !binding.tilPhone.getError().toString().contains("exists")) {
                binding.tilPhone.setError(null);
            }
            if (binding.tilPhone.getError() != null) valid = false;
        }

        if (isEmpty(viewModel.dateOfBirth)) {
            binding.tilDob.setError("DOB is required");
            valid = false;
        } else if (viewModel.dateOfBirth.length() != 10) {
            binding.tilDob.setError("Use format YYYY-MM-DD");
            valid = false;
        } else {
            // Additional check for 18 years
            try {
                String[] parts = viewModel.dateOfBirth.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int day = Integer.parseInt(parts[2]);
                
                Calendar dob = Calendar.getInstance();
                dob.set(year, month, day);
                
                Calendar minAge = Calendar.getInstance();
                minAge.add(Calendar.YEAR, -18);
                
                if (dob.after(minAge)) {
                    binding.tilDob.setError("Client must be at least 18 years old");
                    valid = false;
                } else {
                    binding.tilDob.setError(null);
                }
            } catch (Exception e) {
                binding.tilDob.setError("Invalid date");
                valid = false;
            }
        }

        if (isEmpty(viewModel.email)) {
            binding.tilEmail.setError("Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(viewModel.email).matches()) {
            binding.tilEmail.setError("Invalid email format");
            valid = false;
        } else {
            if (binding.tilEmail.getError() == null || !binding.tilEmail.getError().toString().contains("exists")) {
                binding.tilEmail.setError(null);
            }
            if (binding.tilEmail.getError() != null) valid = false;
        }

        if (isEmpty(viewModel.password)) {
            binding.tilPassword.setError("Password is required");
            valid = false;
        } else if (viewModel.password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        String confirmPass = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";
        if (isEmpty(confirmPass)) {
            binding.tilConfirmPassword.setError("Please confirm password");
            valid = false;
        } else if (!confirmPass.equals(viewModel.password)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        return valid;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> onTextChanged;

        public SimpleTextWatcher(java.util.function.Consumer<String> onTextChanged) {
            this.onTextChanged = onTextChanged;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChanged.accept(s.toString());
        }
        @Override public void afterTextChanged(Editable s) {}
    }
}
