package com.terralink.ui.officer.registration;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.databinding.FragmentAddressGroupBinding;

public class AddressFragment extends Fragment {

    private FragmentAddressGroupBinding binding;
    private RegisterClientViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(RegisterClientViewModel.class);

        binding.etAddress.setText(viewModel.address);
        binding.etAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.address = s.toString();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    public boolean validate() {
        if (viewModel.address == null || viewModel.address.trim().isEmpty()) {
            binding.tilAddress.setError("Address is required");
            return false;
        }
        binding.tilAddress.setError(null);
        return true;
    }
}
