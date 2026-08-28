package com.terralink.ui.officer.registration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.terralink.databinding.FragmentKycDocumentsBinding;
import com.terralink.ui.common.SnackbarUtils;

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

        displayImages();
    }

    private void displayImages() {
        if (viewModel.idFront != null) showImage(viewModel.idFront, binding.ivIdFront, binding.ivIdFrontPlaceholder);
        if (viewModel.idBack != null) showImage(viewModel.idBack, binding.ivIdBack, binding.ivIdBackPlaceholder);
        if (viewModel.passportPhoto != null) showImage(viewModel.passportPhoto, binding.ivPassport, binding.ivPassportPlaceholder);
    }

    private void showImage(File file, ImageView imageView, ImageView placeholder) {
        if (file == null || !file.exists()) return;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            placeholder.setVisibility(View.GONE);
        }
    }

    private void handleImageResult(Uri uri, String type) {
        if (uri == null) return;
        try {
            File file = new File(requireContext().getCacheDir(), type + "_" + System.currentTimeMillis() + ".jpg");
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            is.close();

            if (type.equals("idFront")) {
                viewModel.idFront = file;
                showImage(file, binding.ivIdFront, binding.ivIdFrontPlaceholder);
            } else if (type.equals("idBack")) {
                viewModel.idBack = file;
                showImage(file, binding.ivIdBack, binding.ivIdBackPlaceholder);
            } else if (type.equals("passport")) {
                viewModel.passportPhoto = file;
                showImage(file, binding.ivPassport, binding.ivPassportPlaceholder);
            }

            SnackbarUtils.showSuccess(binding.getRoot(), type + " captured");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validate() {
        if (viewModel.idFront == null || viewModel.idBack == null || viewModel.passportPhoto == null) {
            SnackbarUtils.showInfo(binding.getRoot(), "Please capture all required KYC documents");
            return false;
        }
        return true;
    }
}
