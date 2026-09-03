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
import com.terralink.databinding.ItemLoanScheduleRowBinding;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.client.notification.NotificationStatusViewModel;
import com.terralink.ui.common.FileUtils;
import com.terralink.ui.common.SnackbarUtils;

import java.text.NumberFormat;
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
            String typeStr = getArguments().getString("type");
            referenceNo = getArguments().getString("ref");
            String status = getArguments().getString("status");

            binding.tvLoanTitle.setText(referenceNo);
            binding.tvStatusBadge.setText(status);
            
            int badgeBg;
            int badgeText;

            if ("REJECTED".equals(status)) {
                badgeBg = R.drawable.bg_status_badge_red;
                badgeText = R.color.status_red;
            } else if ("ACTIVE".equals(status) || "CLOSED".equals(status) || "COMPLETED".equals(status)) {
                badgeBg = R.drawable.bg_status_badge_green;
                badgeText = R.color.status_green;
            } else {
                badgeBg = R.drawable.bg_status_badge_amber;
                badgeText = R.color.status_amber;
            }
            binding.tvStatusBadge.setBackgroundResource(badgeBg);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), badgeText));
            
            if ("Application".equals(typeStr)) {
                setupApplicationView(Integer.parseInt(loanId));
            } else {
                setupLoanView(loanId, status);
            }
        }
    }

    private void setupApplicationView(int id) {
        binding.layoutActiveLoanDetails.setVisibility(View.GONE);
        binding.layoutApplicationTimeline.setVisibility(View.VISIBLE);
        binding.btnMainAction.setVisibility(View.GONE);
        binding.btnDownloadCertificate.setVisibility(View.GONE);
        
        applicationViewModel.getLoanStatus(id).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                populateTimeline(result.getData());
            }
        });
    }

    private void setupLoanView(String id, String status) {
        binding.layoutActiveLoanDetails.setVisibility(View.VISIBLE);
        binding.layoutApplicationTimeline.setVisibility(View.GONE);
        
        if ("CLOSED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            binding.btnMainAction.setVisibility(View.GONE);
            binding.btnDownloadCertificate.setVisibility(View.VISIBLE);
            binding.btnDownloadCertificate.setOnClickListener(v -> downloadCertificate());
        } else {
            binding.btnMainAction.setVisibility(View.VISIBLE);
            binding.btnMainAction.setText("Make Payment");
            binding.btnDownloadCertificate.setVisibility(View.GONE);
        }

        loanViewModel.getClientLoans(loanId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                for (ClientLoansResponse loan : result.getData()) {
                    if (loan.getLoanId().equals(loanId)) {
                        updateOverview(loan);
                        break;
                    }
                }
            }
        });

        loanViewModel.getRepaymentSchedule(loanId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                populateSchedule(result.getData());
            }
        });
    }

    private void downloadCertificate() {
        loanViewModel.getClosureCertificate(loanId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                FileUtils.saveAndOpenPdf(requireContext(), result.getData(), "Completion_Certificate_" + referenceNo);
            } else if (result.getStatus() == LoginStatus.ERROR) {
                SnackbarUtils.showError(binding.getRoot(), "Failed to download certificate");
            }
        });
    }

    private void updateOverview(ClientLoansResponse loan) {
        double total = loan.getRepaymentAmount();
        double balance = loan.getBalance();
        double repaid = total - balance;
        
        int progress = 0;
        if (total > 0) {
            progress = (int) ((repaid / total) * 100);
        }
        progress = Math.max(0, Math.min(100, progress));

        binding.cpProgress.setProgress(progress);
        binding.tvProgressText.setText(String.format(Locale.getDefault(), "%d%%", progress));
        
        NumberFormat ksh = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));
        binding.tvRepaidValue.setText(ksh.format(repaid));
        binding.tvTotalValue.setText("out of " + ksh.format(total));
        binding.tvBalanceValue.setText(ksh.format(balance));
        
        binding.tvLoanSubtitle.setText(loan.getLoanProductName());
    }

    private void populateSchedule(List<RepaymentInstallments> schedule) {
        binding.repaymentScheduleContainer.removeAllViews();
        NumberFormat ksh = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));
        
        for (RepaymentInstallments item : schedule) {
            ItemLoanScheduleRowBinding row = ItemLoanScheduleRowBinding.inflate(
                    LayoutInflater.from(requireContext()), binding.repaymentScheduleContainer, false);
            
            row.tvDueDate.setText(item.getDueDate());
            row.tvInstallmentLabel.setText("Installment #" + item.getInstallmentNumber());
            
            double balanceDue = item.getTotalDue() - item.getPaidAmount();
            row.tvAmount.setText(ksh.format(balanceDue > 0 ? balanceDue : item.getTotalDue()));
            
            int color;
            int icon;
            String statusText = item.getStatus();

            if ("PAID".equalsIgnoreCase(item.getStatus())) {
                color = R.color.status_green;
                icon = R.drawable.ic_check_circle;
            } else if ("OVERDUE".equalsIgnoreCase(item.getStatus())) {
                color = R.color.status_red;
                icon = R.drawable.ic_warning;
                if (item.getPaidAmount() > 0) {
                    statusText = String.format(Locale.getDefault(), "OVERDUE (Paid %s)", ksh.format(item.getPaidAmount()));
                }
            } else {
                color = R.color.status_amber;
                icon = R.drawable.ic_help_circle;
                if (item.getPaidAmount() > 0) {
                    statusText = String.format(Locale.getDefault(), "PARTIAL (Paid %s)", ksh.format(item.getPaidAmount()));
                }
            }
            
            row.tvStatus.setText(statusText);
            row.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), color));
            row.ivStatusIcon.setImageResource(icon);
            row.ivStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), color));
            
            if (item.getPaidAmount() > 0 && !"PAID".equalsIgnoreCase(item.getStatus())) {
                row.installmentProgress.setVisibility(View.VISIBLE);
                int progress = (int) ((item.getPaidAmount() / item.getTotalDue()) * 100);
                row.installmentProgress.setProgress(progress);
            } else {
                row.installmentProgress.setVisibility(View.GONE);
            }

            binding.repaymentScheduleContainer.addView(row.getRoot());
        }
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
