package com.terralink.ui.officer.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.terralink.data.model.KycDocumentResponse;
import com.terralink.databinding.LayoutClientVerificationBottomSheetBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.SnackbarUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientVerificationBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_CLIENT_ID = "client_id";
    private static final String ARG_CLIENT_NAME = "client_name";
    private static final String ARG_CLIENT_NO = "client_no";

    private LayoutClientVerificationBottomSheetBinding binding;
    private ClientVerificationViewModel viewModel;
    private int clientId;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static ClientVerificationBottomSheetFragment newInstance(int clientId, String name, String clientNo) {
        ClientVerificationBottomSheetFragment fragment = new ClientVerificationBottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CLIENT_ID, clientId);
        args.putString(ARG_CLIENT_NAME, name);
        args.putString(ARG_CLIENT_NO, clientNo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            clientId = getArguments().getInt(ARG_CLIENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutClientVerificationBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ClientVerificationViewModel.class);

        binding.tvClientName.setText(getArguments().getString(ARG_CLIENT_NAME));
        binding.tvClientId.setText("ID: " + getArguments().getString(ARG_CLIENT_NO));

        loadKycDocuments();

        binding.btnApprove.setOnClickListener(v -> verifyClient());
        binding.btnReject.setOnClickListener(v -> showRejectionForm());
        binding.btnSubmitRejection.setOnClickListener(v -> rejectClient());
    }

    private void loadKycDocuments() {
        viewModel.getKycDocuments(clientId).observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == LoginStatus.SUCCESS && resource.getData() != null) {
                for (KycDocumentResponse doc : resource.getData()) {
                    String url = doc.getFileUrl();
                    if (url != null && !url.startsWith("http")) {
                        url = "http://192.168.0.104:5031" + url;
                    }
                    
                    if ("ID_FRONT".equals(doc.getDocType())) {
                        loadImage(url, binding.ivIdFront);
                    } else if ("ID_BACK".equals(doc.getDocType())) {
                        loadImage(url, binding.ivIdBack);
                    } else if ("PASSPORT_PHOTO".equals(doc.getDocType())) {
                        loadImage(url, binding.ivPassport);
                    }
                }
            } else if (resource.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Failed to load KYC: " + resource.getMessage());
            }
        });
    }

    private void loadImage(String url, ImageView imageView) {
        if (url == null || url.isEmpty()) return;
        executor.execute(() -> {
            try {
                InputStream in = new URL(url).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                mainHandler.post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void verifyClient() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.verifyClient(clientId).observe(getViewLifecycleOwner(), resource -> {
            binding.progressBar.setVisibility(View.GONE);
            if (resource.getStatus() == LoginStatus.SUCCESS) {
                SnackbarUtils.showSuccess(binding.getRoot(), "Client verified successfully");
                binding.progressBar.postDelayed(this::dismiss, 2000);
            } else if (resource.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
            }
        });
    }

    private void showRejectionForm() {
        binding.tilRejectionReason.setVisibility(View.VISIBLE);
        binding.btnSubmitRejection.setVisibility(View.VISIBLE);
        binding.btnApprove.setVisibility(View.GONE);
        binding.btnReject.setVisibility(View.GONE);
    }

    private void rejectClient() {
        String reason = binding.etRejectionReason.getText().toString();
        if (reason.isEmpty()) {
            binding.etRejectionReason.setError("Reason is required");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.rejectClient(clientId, reason).observe(getViewLifecycleOwner(), resource -> {
            binding.progressBar.setVisibility(View.GONE);
            if (resource.getStatus() == LoginStatus.SUCCESS) {
                SnackbarUtils.showSuccess(binding.getRoot(), "Client verification rejected");
                binding.progressBar.postDelayed(this::dismiss, 2000);
            } else if (resource.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), resource.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
