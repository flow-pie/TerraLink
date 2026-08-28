package com.terralink.ui.officer.registration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.databinding.FragmentClientReviewBinding;

public class ReviewFragment extends Fragment {

    private FragmentClientReviewBinding binding;
    private RegisterClientViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClientReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(RegisterClientViewModel.class);

        binding.tvFullName.setText(viewModel.fullName);
        binding.tvGovernmentId.setText(viewModel.nationalId);
        binding.tvPhone.setText(viewModel.phone);
        binding.tvDateOfBirth.setText(viewModel.dateOfBirth);
        binding.tvEmail.setText(viewModel.email);
        binding.tvAddressNote.setText(viewModel.address);

        // Previews for images
        if (viewModel.idFront != null) {
            binding.imgIdFront.setImageURI(android.net.Uri.fromFile(viewModel.idFront));
        }
        if (viewModel.idBack != null) {
            binding.imgIdBack.setImageURI(android.net.Uri.fromFile(viewModel.idBack));
        }
        if (viewModel.passportPhoto != null) {
            binding.imgFace.setImageURI(android.net.Uri.fromFile(viewModel.passportPhoto));
        }
    }
}
