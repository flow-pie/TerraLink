package com.terralink.ui.officer.registration;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.databinding.FragmentKycDocumentsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class KycDocumentsFragment extends Fragment {

    private FragmentKycDocumentsBinding binding;
    private RegisterClientViewModel viewModel;

    private final ActivityResultLauncher<String> pickIdFront = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> handleImageResult(uri, "idFront")
    );

    private final ActivityResultLauncher<String> pickIdBack = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> handleImageResult(uri, "idBack")
    );

    private final ActivityResultLauncher<String> pickPassport = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> handleImageResult(uri, "passport")
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentKycDocumentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(RegisterClientViewModel.class);

        binding.btnCaptureIdFront.setOnClickListener(v -> pickIdFront.launch("image/*"));
        binding.btnCaptureIdBack.setOnClickListener(v -> pickIdBack.launch("image/*"));
        binding.btnCapturePassportPhoto.setOnClickListener(v -> pickPassport.launch("image/*"));
    }

    private void handleImageResult(Uri uri, String type) {
        if (uri == null) return;
        try {
            File file = new File(requireContext().getCacheDir(), type + ".jpg");
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            is.close();

            if (type.equals("idFront")) viewModel.idFront = file;
            else if (type.equals("idBack")) viewModel.idBack = file;
            else if (type.equals("passport")) viewModel.passportPhoto = file;

            Toast.makeText(requireContext(), type + " captured", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
