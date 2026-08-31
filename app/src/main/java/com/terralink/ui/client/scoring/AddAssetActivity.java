package com.terralink.ui.client.scoring;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.data.model.CreateAssetRequest;
import com.terralink.databinding.ActivityAddAssetBinding;
import com.terralink.ui.common.SnackbarUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddAssetActivity extends AppCompatActivity {

    private AssetViewModel viewModel;
    private ActivityAddAssetBinding binding;
    private long clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAssetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clientId = getIntent().getLongExtra("clientId", -1);
        if (clientId == -1) {
            Toast.makeText(this, "Invalid Client ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AssetViewModel.class);

        setupSpinner();
        setupListeners();
    }

    private void setupSpinner() {
        String[] assetTypes = {"LIVESTOCK", "MOTORBIKE", "WATER_PUMP", "OTHER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, assetTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.assetTypeSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.appBarContent.tvTitle.setText("Add Asset");
        binding.appBarContent.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmitAsset.setOnClickListener(v -> submitAsset());
    }

    private void submitAsset() {
        String assetType = binding.assetTypeSpinner.getSelectedItem().toString();
        String description = binding.descriptionInput.getText().toString().trim();
        String quantityStr = binding.quantityInput.getText().toString().trim();
        String valueStr = binding.estimatedValueInput.getText().toString().trim();

        if (description.isEmpty() || quantityStr.isEmpty() || valueStr.isEmpty()) {
            SnackbarUtils.showInfo(binding.getRoot(), "Please fill all fields");
            return;
        }

        int quantity;
        double value;
        try {
            quantity = Integer.parseInt(quantityStr);
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            SnackbarUtils.showError(binding.getRoot(), "Invalid numeric values");
            return;
        }

        binding.btnSubmitAsset.setEnabled(false);
        binding.btnSubmitAsset.setText("Submitting...");

        CreateAssetRequest request = new CreateAssetRequest(assetType, description, quantity, value);
        viewModel.createAsset(clientId, request).observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    SnackbarUtils.showSuccess(binding.getRoot(), "Asset declared successfully");
                    binding.btnSubmitAsset.postDelayed(this::finish, 1500);
                    break;
                case ERROR:
                    binding.btnSubmitAsset.setEnabled(true);
                    binding.btnSubmitAsset.setText("Declare Asset");
                    SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
                    break;
            }
        });
    }
}
