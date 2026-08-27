package com.terralink.ui.officer.clients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.terralink.data.model.ClientListItemResponse;
import com.terralink.databinding.FragmentClientDetailsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientDetailsBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_CLIENT_JSON = "arg_client_json";
    private FragmentClientDetailsBinding binding;
    private ClientListItemResponse client;

    public static ClientDetailsBottomSheetFragment newInstance(ClientListItemResponse client) {
        ClientDetailsBottomSheetFragment fragment = new ClientDetailsBottomSheetFragment();
        Bundle args = new Bundle();
        // For simplicity, we'll pass fields individually or use Gson if available
        args.putString("name", client.getFullName());
        args.putString("no", client.getClientNo());
        args.putString("phone", client.getPhone());
        args.putString("date", client.getCreatedAt());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClientDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            binding.tvClientName.setText(getArguments().getString("name"));
            binding.tvClientNo.setText(getArguments().getString("no"));
            binding.tvPhone.setText(getArguments().getString("phone"));
            binding.tvCreatedAt.setText("Member since " + getArguments().getString("date"));
        }

        binding.btnViewLoans.setOnClickListener(v -> {
            // Logic to view loans for this specific client
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
