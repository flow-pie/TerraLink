package com.terralink.ui.officer.appraisal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.CreditHistoryResponse;
import com.terralink.data.model.LoanAppraisalDetailResponse;
import com.terralink.databinding.FragmentLoanAppraisalBinding;
import com.terralink.databinding.ItemPaymentHistoryBinding;
import com.terralink.ui.auth.LoginStatus;

import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoanAppraisalBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_APPLICATION_ID = "arg_application_id";

    private LoanAppraisalViewModel viewModel;
    private FragmentLoanAppraisalBinding binding;
    private int applicationId;
    private int currentCreditScore = 0;

    public static LoanAppraisalBottomSheetFragment newInstance(int applicationId) {
        LoanAppraisalBottomSheetFragment fragment = new LoanAppraisalBottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_APPLICATION_ID, applicationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            applicationId = getArguments().getInt(ARG_APPLICATION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoanAppraisalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoanAppraisalViewModel.class);

        setupClickListeners();
        fetchData();
    }

    private void setupClickListeners() {
        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnSubmitApproval.setOnClickListener(v -> submitDecision("APPROVED"));
        binding.btnRejectAppraisal.setOnClickListener(v -> submitDecision("REJECTED"));
        binding.btnInfoRequest.setOnClickListener(v -> submitDecision("INFO_REQUESTED"));
    }

    private void fetchData() {
        viewModel.getApplicationDetail(applicationId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                populateUI(result.getData());
            } else if (result.getStatus() == LoginStatus.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(LoanAppraisalDetailResponse data) {
        // 1. Application Details
        LoanAppraisalDetailResponse.Application app = data.getApplication();
        binding.tvAppId.setText(String.format("APP ID: #%s", app.getApplicationNo()));
        binding.tvAppStatus.setText(app.getStatus());
        binding.tvClientName.setText(data.getClient().getFullName());
        binding.tvLoanAmount.setText(String.format(Locale.getDefault(), "KES %,.2f", app.getRequestedAmount()));
        binding.tvLoanDuration.setText(String.format(Locale.getDefault(), "%d Months", app.getDurationMonths()));
        binding.tvLoanPurpose.setText(app.getPurpose());

        // 2. Credit Score
        LoanAppraisalDetailResponse.CreditScore score = data.getCreditScore();
        if (score != null) {
            currentCreditScore = score.getScore();
            binding.creditGaugeView.setScore(currentCreditScore);
            binding.clientStatus.setText(score.getRating());
        }

        // 4. Asset Inventory
        populateAssets(data.getAssets());

        // 5. Repayment History
        populateCreditHistory(data.getCreditHistory());

       }

    private void populateCreditHistory(List<CreditHistoryResponse> history) {
        binding.repaymentHistoryContainer.removeAllViews();
        if (history == null) return;
        for (CreditHistoryResponse item : history) {
            ItemPaymentHistoryBinding itemBinding = ItemPaymentHistoryBinding.inflate(LayoutInflater.from(requireContext()), binding.repaymentHistoryContainer, false);
            itemBinding.tvInstallmentTitle.setText(item.getLoanType());
            itemBinding.tvInstallmentDate.setText(String.format("COMPLETED %s", item.getCompletionDate()));
            itemBinding.tvInstallmentAmount.setText(item.getStatus());
            itemBinding.tvInstallmentStatus.setText(item.getStatus());
            binding.repaymentHistoryContainer.addView(itemBinding.getRoot());
        }
    }

    private void populateAssets(List<AssetResponse> assets) {
        binding.assetFlexbox.removeAllViews();
        if (assets == null) return;
        for (AssetResponse asset : assets) {
            android.widget.TextView tv = new android.widget.TextView(requireContext());
            tv.setText(String.format("● %s (%d)", asset.getAssetType(), asset.getQuantity()));
            tv.setPadding(20, 10, 20, 10);
            binding.assetFlexbox.addView(tv);
        }
    }

    private void submitDecision(String decision) {
        String notes = binding.etDecisionNotes.getText().toString();
        if (notes.isEmpty()) {
            binding.etDecisionNotes.setError("Notes are required");
            return;
        }

        viewModel.submitAppraisal(applicationId, decision, notes, currentCreditScore).observe(getViewLifecycleOwner(), result -> {
            switch (result.getStatus()) {
                case LOADING:
                    binding.btnSubmitApproval.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(requireContext(), "Decision submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                case ERROR:
                    binding.btnSubmitApproval.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to submit decision: " + result.getMessage(), Toast.LENGTH_SHORT).show();
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
