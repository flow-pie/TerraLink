package com.terralink.ui.client.loan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.terralink.R;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanApplicationStatusResponse;
import com.terralink.data.model.RepaymentInstallments;
import com.terralink.databinding.LayoutClientLoanDetailsSheetBinding;
import com.terralink.databinding.LayoutTimelineItemBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.client.notification.NotificationStatusViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoanDetailsBottomSheetFragment extends BottomSheetDialogFragment {

    private LayoutClientLoanDetailsSheetBinding binding;
    private NotificationStatusViewModel applicationViewModel;
    private ClientLoansViewModel loanViewModel;
    private String loanId;
    private String type;
    private String referenceNo;

    public static LoanDetailsBottomSheetFragment newInstance(ClientLoansResponse loan) {
        LoanDetailsBottomSheetFragment fragment = new LoanDetailsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("loanId", loan.getLoanId());
        args.putString("type", loan.getType());
        args.putString("ref", loan.getReferenceNo());
        args.putString("status", loan.getStatus());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutClientLoanDetailsSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applicationViewModel = new ViewModelProvider(this).get(NotificationStatusViewModel.class);
        loanViewModel = new ViewModelProvider(this).get(ClientLoansViewModel.class);

        if (getArguments() != null) {
            loanId = getArguments().getString("loanId");
            type = getArguments().getString("type");
            referenceNo = getArguments().getString("ref");
            String status = getArguments().getString("status");

            binding.tvLoanTitle.setText(referenceNo);
            binding.tvStatusBadge.setText(status);
            
            // Set badge color based on status
            int badgeBg;
            int badgeText;

            if ("REJECTED".equals(status)) {
                badgeBg = R.drawable.bg_status_badge_red;
                badgeText = R.color.status_red;
            } else if ("ACTIVE".equals(status) || "CLOSED".equals(status)) {
                badgeBg = R.drawable.bg_status_badge_green;
                badgeText = R.color.status_green;
            } else {
                badgeBg = R.drawable.bg_status_badge_amber;
                badgeText = R.color.status_amber;
            }
            binding.tvStatusBadge.setBackgroundResource(badgeBg);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), badgeText));
            
            if ("Application".equals(type)) {
                setupApplicationView(Integer.parseInt(loanId));
            } else {
                setupLoanView(loanId);
            }
        }
    }

    private void setupApplicationView(int id) {
        binding.layoutApplicationTimeline.setVisibility(View.VISIBLE);
        binding.layoutActiveLoanDetails.setVisibility(View.GONE);
        binding.btnMainAction.setVisibility(View.GONE);
        
        applicationViewModel.getLoanStatus(id).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                populateTimeline(result.getData());
            }
        });
    }

    private void setupLoanView(String id) {
        binding.layoutApplicationTimeline.setVisibility(View.GONE);
        binding.layoutActiveLoanDetails.setVisibility(View.VISIBLE);
        binding.btnMainAction.setVisibility(View.VISIBLE);
        binding.btnMainAction.setText("Make Payment");

        loanViewModel.getClientLoans(loanId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                // Find this specific loan
                for (ClientLoansResponse loan : result.getData()) {
                    if (loan.getLoanId().equals(loanId)) {
                        binding.tvBalanceValue.setText(String.format(Locale.getDefault(), "KES %,.2f", loan.getBalance()));
                        binding.tvLoanSubtitle.setText(String.format(Locale.getDefault(), "Approved: KES %,.0f", loan.getApprovedAmount()));
                        break;
                    }
                }
            }
        });

        // We can add logic to fetch repayment schedule if needed
        // applicationViewModel.getRepaymentSchedule(id)...
    }

    private void populateTimeline(LoanApplicationStatusResponse data) {
        List<LoanApplicationStatusResponse.TimelineStage> timeline = data.getTimeline();
        if (timeline == null) return;

        for (LoanApplicationStatusResponse.TimelineStage stage : timeline) {
            switch (stage.getStage()) {
                case "SUBMITTED":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepSubmitted.getRoot()), "Application Submitted", stage.getCompletedAt(), data.getStatus());
                    break;
                case "UNDER_REVIEW":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepReview.getRoot()), "Under Review", stage.getCompletedAt(), data.getStatus());
                    break;
                case "APPROVAL":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepApproval.getRoot()), "Final Approval", stage.getCompletedAt(), data.getStatus());
                    break;
                case "DISBURSEMENT":
                    updateStep(LayoutTimelineItemBinding.bind(binding.stepDisbursement.getRoot()), "Disbursement", stage.getCompletedAt(), data.getStatus());
                    LayoutTimelineItemBinding.bind(binding.stepDisbursement.getRoot()).indicatorLine.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void updateStep(LayoutTimelineItemBinding step, String title, String time, String overallStatus) {
        step.tvTitle.setText(title);
        if (time != null) {
            step.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.navy_text_primary));
            step.tvTime.setText(formatDate(time));
            if ("REJECTED".equals(overallStatus)) {
                step.indicatorDot.setBackgroundResource(R.drawable.bg_status_badge_red);
                step.indicatorLine.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.status_red));
            } else {
                step.indicatorDot.setBackgroundResource(R.drawable.bg_stepper_active);
                step.indicatorLine.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.terracotta_primary));
            }
        } else {
            step.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            step.tvTime.setText("Pending");
            step.indicatorDot.setBackgroundResource(R.drawable.bg_stepper_inactive);
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            Date date = input.parse(dateStr);
            return output.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
