package com.terralink.ui.officer.scoring;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.terralink.data.model.AssetResponse;
import com.terralink.databinding.DialogVerifyAssetBinding;

public class VerifyAssetDialog extends DialogFragment {

    private final AssetResponse asset;
    private final OnVerifyListener listener;

    public interface OnVerifyListener {
        void onConfirm(double verifiedValue);
        void onReject();
    }

    public VerifyAssetDialog(AssetResponse asset, OnVerifyListener listener) {
        this.asset = asset;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogVerifyAssetBinding binding = DialogVerifyAssetBinding.inflate(LayoutInflater.from(getContext()));
        
        binding.tvAssetInfo.setText(String.format("%s declared at KES %,.2f", asset.getAssetType(), asset.getEstimatedValue()));
        binding.etEstimatedValue.setText(String.valueOf(asset.getEstimatedValue()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), com.terralink.R.style.Theme_TerraLink_Dialog)
                .setView(binding.getRoot())
                .create();

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        binding.btnReject.setOnClickListener(v -> {
            listener.onReject();
            dialog.dismiss();
        });
        binding.btnConfirm.setOnClickListener(v -> {
            String valStr = binding.etEstimatedValue.getText().toString().trim();
            if (!valStr.isEmpty()) {
                try {
                    double val = Double.parseDouble(valStr);
                    listener.onConfirm(val);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    binding.etEstimatedValue.setError("Invalid value");
                }
            }
        });

        return dialog;
    }
}
