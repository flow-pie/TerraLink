package com.terralink.ui.officer.appraisal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.terralink.R;
import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.CreditHistoryResponse;
import com.terralink.data.model.CreditScoreResponse;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.data.model.LoanAppraisalDetailResponse;
import com.terralink.databinding.FragmentLoanAppraisalBinding;
import com.terralink.databinding.ItemLoanProgressCardBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.SnackbarUtils;
import com.terralink.ui.officer.clients.ClientDetailsBottomSheetFragment;
import com.terralink.ui.officer.scoring.ClientScoringActivity;

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
                LoanAppraisalDetailResponse data = result.getData();
                populateUI(data);
                
                // Recalculate credit score to ensure it's up to date with verified assets/income
                viewModel.calculateCreditScore(String.valueOf(data.getClient().getId()), data.getApplication().getRequestedAmount()).observe(getViewLifecycleOwner(), scoreResult -> {
                    if (scoreResult.getStatus() == LoginStatus.SUCCESS && scoreResult.getData() != null) {
                        CreditScoreResponse newScore = scoreResult.getData();
                        currentCreditScore = newScore.getCreditScore();
                        binding.creditGaugeView.setScore(currentCreditScore);
                        binding.clientStatus.setText(newScore.getRating());

                        // Update breakdown from live score
                        updateBreakdownFromLive(newScore);
                    }
                });

            } else if (result.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Error: " + result.getMessage());
            }
        });
    }

    private void populateUI(LoanAppraisalDetailResponse data) {
        // 1. Application Details
        LoanAppraisalDetailResponse.Application app = data.getApplication();
        binding.tvAppId.setText(String.format("APP ID: #%s", app.getApplicationNo()));
        binding.tvAppStatus.setText(app.getStatus());
        binding.tvClientName.setText(data.getClient().getFullName());
        
        // Add click listener to name to view full profile
        binding.tvClientName.setOnClickListener(v -> {
            ClientDetailsBottomSheetFragment fragment = ClientDetailsBottomSheetFragment.newInstance(
                    data.getClient().getId(), 
                    data.getClient().getFullName(), 
                    data.getClient().getClientNo(), 
                    "N/A", // Phone not in appraisal detail yet
                    "N/A", 
                    "ACTIVE"
            );
            fragment.show(getParentFragmentManager(), fragment.getTag());
        });

        binding.tvLoanAmount.setText(String.format(Locale.getDefault(), "KES %,.2f", app.getRequestedAmount()));
        binding.tvLoanDuration.setText(String.format(Locale.getDefault(), "%d Months", app.getDurationMonths()));
        binding.tvLoanPurpose.setText(app.getPurpose());

        // 2. Credit Score
        LoanAppraisalDetailResponse.CreditScore score = data.getCreditScore();
        if (score != null) {
            currentCreditScore = score.getScore();
        } else {
            currentCreditScore = 0;
        }
        binding.creditGaugeView.setScore(currentCreditScore);
        binding.clientStatus.setText(score != null ? score.getRating() : "N/A");
        
        if (score != null) {
            populateBreakdown(score);
        }

        // Fetch full assets and income to check for PENDING verifications
        String clientIdStr = String.valueOf(data.getClient().getId());
        
        viewModel.getClientAssets(clientIdStr).observe(getViewLifecycleOwner(), assetResource -> {
            if (assetResource.getStatus() == LoginStatus.SUCCESS && assetResource.getData() != null) {
                boolean pendingAssets = false;
                for (AssetResponse asset : assetResource.getData()) {
                    if ("PENDING".equalsIgnoreCase(asset.getVerificationStatus())) {
                        pendingAssets = true;
                        break;
                    }
                }
                
                final boolean finalHasPendingAssets = pendingAssets;
                
                viewModel.getIncomeAssessments(clientIdStr).observe(getViewLifecycleOwner(), incomeResource -> {
                    boolean hasPendingIncome = false;
                    if (incomeResource.getStatus() == LoginStatus.SUCCESS && incomeResource.getData() != null) {
                        for (IncomeAssessmentResponse income : incomeResource.getData()) {
                            if ("PENDING".equalsIgnoreCase(income.getVerificationStatus())) {
                                hasPendingIncome = true;
                                break;
                            }
                        }
                    }
                    
                    updateVerificationBanner(data, finalHasPendingAssets, hasPendingIncome);
                });
            }
        });

        // 4. Asset Inventory
        populateAssets(data.getAssets());

        // 5. Repayment History (Concise Loan List)
        fetchAndPopulateLoans(String.valueOf(data.getClient().getId()));

       }

    private void updateBreakdownFromLive(CreditScoreResponse score) {
        binding.tvRepaymentHistoryScore.setText(score.getRepaymentHistoryScore() + " / 40");
        binding.pbRepaymentHistory.setProgress(score.getRepaymentHistoryScore());

        binding.tvRepaymentCapacityScore.setText(score.getRepaymentCapacityScore() + " / 30");
        binding.pbRepaymentCapacity.setProgress(score.getRepaymentCapacityScore());

        binding.tvFinancialStabilityScore.setText(score.getFinancialStabilityScore() + " / 20");
        binding.pbFinancialStability.setProgress(score.getFinancialStabilityScore());

        binding.tvVerifiedAssetsScore.setText(score.getVerifiedAssetsScore() + " / 10");
        binding.pbVerifiedAssets.setProgress(score.getVerifiedAssetsScore());
    }

    private void populateBreakdown(LoanAppraisalDetailResponse.CreditScore score) {
        binding.tvRepaymentHistoryScore.setText(score.getRepaymentHistoryScore() + " / 40");
        binding.pbRepaymentHistory.setProgress(score.getRepaymentHistoryScore());

        binding.tvRepaymentCapacityScore.setText(score.getRepaymentCapacityScore() + " / 30");
        binding.pbRepaymentCapacity.setProgress(score.getRepaymentCapacityScore());

        binding.tvFinancialStabilityScore.setText(score.getFinancialStabilityScore() + " / 20");
        binding.pbFinancialStability.setProgress(score.getFinancialStabilityScore());

        binding.tvVerifiedAssetsScore.setText(score.getVerifiedAssetsScore() + " / 10");
        binding.pbVerifiedAssets.setProgress(score.getVerifiedAssetsScore());
    }

    private void fetchAndPopulateLoans(String clientId) {
        viewModel.getClientLoans(clientId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                populateConciseLoanList(result.getData());
            }
        });
    }

    private void populateConciseLoanList(List<com.terralink.data.model.ClientLoansResponse> loans) {
        binding.repaymentHistoryContainer.removeAllViews();
        if (loans == null) return;
        
        int count = 0;
        for (com.terralink.data.model.ClientLoansResponse loan : loans) {
            // Only include actual loans in Repayment History, exclude applications
            if (!"Loan".equalsIgnoreCase(loan.getType())) continue;
            
            if (count >= 5) break;
            
            ItemLoanProgressCardBinding itemBinding = ItemLoanProgressCardBinding.inflate(LayoutInflater.from(requireContext()), binding.repaymentHistoryContainer, false);
            
            itemBinding.tvApplicantName.setText(getArguments() != null ? getArguments().getString("name") : "Client");
            itemBinding.tvLoanDetails.setText(String.format("%s • %s", loan.getReferenceNo(), loan.getLoanProductName()));
            itemBinding.tvLoanAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", loan.getRepaymentAmount()));
            itemBinding.tvStatus.setText(loan.getStatus());
            
            // Calculate Progress
            double totalRepayable = loan.getRepaymentAmount();
            double balance = loan.getBalance();
            int progress = 0;
            if (totalRepayable > 0) {
                progress = (int) (((totalRepayable - balance) / totalRepayable) * 100);
            }
            progress = Math.max(0, Math.min(100, progress));
            
            itemBinding.tvProgressPercent.setText(progress + "%");
            itemBinding.progressRepayment.setProgress(progress);
            
            // Status Styling
            int statusColor = R.color.status_green;
            int statusBg = R.drawable.bg_status_badge_green;
            
            if ("ARREARS".equalsIgnoreCase(loan.getStatus())) {
                statusColor = R.color.status_red;
                statusBg = R.drawable.bg_status_badge_red;
            } else if ("PENDING_DISBURSEMENT".equalsIgnoreCase(loan.getStatus())) {
                statusColor = R.color.status_amber;
                statusBg = R.drawable.bg_status_badge_amber;
            }
            
            itemBinding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), statusColor));
            itemBinding.tvStatus.setBackgroundResource(statusBg);
            
            binding.repaymentHistoryContainer.addView(itemBinding.getRoot());
            count++;
        }
        
        if (count == 0) {
            android.widget.TextView tv = new android.widget.TextView(requireContext());
            tv.setText("No previous loan history found.");
            tv.setPadding(0, 20, 0, 0);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            binding.repaymentHistoryContainer.addView(tv);
        }
    }

    private void updateVerificationBanner(LoanAppraisalDetailResponse data, boolean hasPendingAssets, boolean hasPendingIncome) {
        if (hasPendingAssets || hasPendingIncome) {
            binding.cardPendingVerification.setVisibility(View.VISIBLE);
            binding.btnGoToScoring.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ClientScoringActivity.class);
                intent.putExtra("clientId", (long) data.getClient().getId());
                intent.putExtra("clientName", data.getClient().getFullName());
                startActivity(intent);
            });
            
            // Allow approval even if scoring is pending (User request)
            binding.btnSubmitApproval.setEnabled(true);
            binding.btnSubmitApproval.setAlpha(1.0f);
            
            String msg = "Verification pending for ";
            if (hasPendingAssets && hasPendingIncome) msg += "assets & income.";
            else if (hasPendingAssets) msg += "assets.";
            else msg += "income.";
            
            binding.tvPendingVerificationMsg.setText(msg);
        } else {
            binding.cardPendingVerification.setVisibility(View.GONE);
            binding.btnSubmitApproval.setEnabled(true);
            binding.btnSubmitApproval.setAlpha(1.0f);
        }
    }

    private void populateAssets(List<AssetResponse> assets) {
        binding.assetFlexbox.removeAllViews();
        if (assets == null) return;
        
        boolean foundVerified = false;
        for (AssetResponse asset : assets) {
            if ("VERIFIED".equalsIgnoreCase(asset.getVerificationStatus())) {
                foundVerified = true;
                android.widget.TextView tv = new android.widget.TextView(requireContext());
                tv.setText(String.format("● %s (KES %,.0f)", asset.getAssetType(), asset.getEstimatedValue()));
                tv.setPadding(20, 10, 20, 10);
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_green));
                binding.assetFlexbox.addView(tv);
            }
        }
        
        if (!foundVerified) {
            android.widget.TextView tv = new android.widget.TextView(requireContext());
            tv.setText("No verified assets");
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
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
                    SnackbarUtils.showSuccess(binding.getRoot(), "Decision submitted successfully");
                    binding.btnSubmitApproval.postDelayed(this::dismiss, 2000);
                    break;
                case ERROR:
                    binding.btnSubmitApproval.setEnabled(true);
                    SnackbarUtils.showError(binding.getRoot(), "Failed to submit decision: " + result.getMessage());
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
