package com.terralink.ui.officer.registration;

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

        // Populate existing data if any
        binding.etFullName.setText(viewModel.fullName);
        binding.etGovId.setText(viewModel.nationalId);
        binding.etPhone.setText(viewModel.phone);
        binding.etDob.setText(viewModel.dateOfBirth);
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
