package com.terralink.ui.officer.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.terralink.R;
import com.terralink.data.model.LoanProductRequest;
import com.terralink.databinding.DialogAddNewProductBinding;
import com.terralink.ui.auth.LoginStatus;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddProductBottomSheetFragment extends BottomSheetDialogFragment {

    private DialogAddNewProductBinding binding;
    private AddProductViewModel viewModel;

    public static AddProductBottomSheetFragment newInstance() {
        return new AddProductBottomSheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddNewProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddProductViewModel.class);

        setupSectorDropdown();
        setupClickListeners();
    }

    private void setupSectorDropdown() {
        String[] sectors = {"Agriculture", "Retail", "Manufacturing", "Service", "Education", "Healthcare"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sectors);
        binding.actvTargetSector.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = binding.etProductName.getText().toString().trim();
        String sector = binding.actvTargetSector.getText().toString().trim();
        String minAmountStr = binding.etMinAmount.getText().toString().trim();
        String maxAmountStr = binding.etMaxAmount.getText().toString().trim();
        String interestRateStr = binding.etInterestRate.getText().toString().trim();

        if (name.isEmpty() || minAmountStr.isEmpty() || maxAmountStr.isEmpty() || interestRateStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double minAmount = Double.parseDouble(minAmountStr);
        double maxAmount = Double.parseDouble(maxAmountStr);
        double interestRate = Double.parseDouble(interestRateStr);

        // Get selected tenures
        List<Integer> selectedIds = binding.chipGroupTenures.getCheckedChipIds();
        int minDuration = Integer.MAX_VALUE;
        int maxDuration = Integer.MIN_VALUE;

        if (selectedIds.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one tenure", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Integer id : selectedIds) {
            Chip chip = binding.chipGroupTenures.findViewById(id);
            int duration = Integer.parseInt(chip.getText().toString());
            if (duration < minDuration) minDuration = duration;
            if (duration > maxDuration) maxDuration = duration;
        }

        LoanProductRequest request = new LoanProductRequest(
                name,
                minAmount,
                maxAmount,
                interestRate,
                100.0, // Default processing fee
                50.0,  // Default late fee
                minDuration,
                maxDuration,
                "MONTHLY"
        );

        viewModel.createProduct(request).observe(getViewLifecycleOwner(), result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.btnSaveProduct.setEnabled(false);
                    binding.btnSaveProduct.setText("Saving...");
                    break;
                case SUCCESS:
                    Toast.makeText(requireContext(), "Product saved successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                case ERROR:
                    binding.btnSaveProduct.setEnabled(true);
                    binding.btnSaveProduct.setText("Save Product");
                    Toast.makeText(requireContext(), "Error: " + result.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
