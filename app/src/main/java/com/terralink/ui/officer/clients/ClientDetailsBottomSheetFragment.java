package com.terralink.ui.officer.clients;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.RepaymentInstallments;
import com.terralink.databinding.FragmentClientDetailsBinding;
import com.terralink.databinding.ItemScheduleRowBinding;
import com.terralink.ui.auth.LoginStatus;

import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientDetailsBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentClientDetailsBinding binding;
    private OfficerClientsViewModel viewModel;
    private int clientId;

    public static ClientDetailsBottomSheetFragment newInstance(ClientListItemResponse client) {
        ClientDetailsBottomSheetFragment fragment = new ClientDetailsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt("id", client.getId());
        args.putString("name", client.getFullName());
        args.putString("no", client.getClientNo());
        args.putString("phone", client.getPhone());
        args.putString("date", client.getCreatedAt());
        args.putString("status", client.getStatus());
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
        viewModel = new ViewModelProvider(this).get(OfficerClientsViewModel.class);
        
        if (getArguments() != null) {
            clientId = getArguments().getInt("id");
            String clientNo = getArguments().getString("no");
            
            binding.tvClientName.setText(getArguments().getString("name"));
            binding.tvGridClientName.setText(getArguments().getString("name"));
            
            // Set client number with prefix to ensure visibility
            binding.tvClientNo.setText("Account: " + (clientNo != null ? clientNo : "N/A"));
            
            binding.tvPhone.setText(getArguments().getString("phone"));
            binding.tvCreatedAt.setText(getArguments().getString("date"));
            binding.tvStatus.setText(getArguments().getString("status"));
        }

        fetchClientData();

        binding.btnViewLoans.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void fetchClientData() {
        String idToUse = String.valueOf(clientId);
        String clientNo = getArguments() != null ? getArguments().getString("no") : null;
        
        Log.d("ClientDetails", "Fetching loans. Numeric ID: " + idToUse + ", ClientNo: " + clientNo);
        
        viewModel.getClientLoans(idToUse).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS) {
                List<ClientLoansResponse> loans = result.getData();
                if (loans != null && !loans.isEmpty()) {
                    Log.d("ClientDetails", "Loans found: " + loans.size());
                    fetchLoanDetails(loans.get(0).getLoanId());
                } else {
                    Log.d("ClientDetails", "No loans found with Numeric ID. Trying ClientNo...");
                    if (clientNo != null) fetchLoansWithId(clientNo);
                }
            } else if (result.getStatus() == LoginStatus.ERROR) {
                Log.e("ClientDetails", "Error: " + result.getMessage());
                if (clientNo != null) fetchLoansWithId(clientNo);
            }
        });
    }

    private void fetchLoansWithId(String identifier) {
        viewModel.getClientLoans(identifier).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null && !result.getData().isEmpty()) {
                fetchLoanDetails(result.getData().get(0).getLoanId());
            }
        });
    }

    private void fetchLoanDetails(String loanId) {
        Log.d("ClientDetails", "Fetching details for loanId: " + loanId);
        viewModel.getLoanDetails(loanId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                populateLoanUI(result.getData());
            }
        });

        viewModel.getRepaymentSchedule(loanId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == LoginStatus.SUCCESS && result.getData() != null) {
                populateScheduleUI(result.getData());
            }
        });
    }

    private void populateLoanUI(LoanDetailsResponse data) {
        binding.tvBalance.setText(String.format(Locale.getDefault(), "KES %,.2f", data.getOutStandingAmount()));
        binding.tvOriginalAmount.setText(String.format(Locale.getDefault(), "KES %,.2f", data.getApprovedAmount()));
        binding.tvInstallmentsPaid.setText(String.format(Locale.getDefault(), "%d / %d", data.getInstallmentsPaid(), data.getInstallmentsTotal()));
        binding.tvNextDueDate.setText(data.getNextDueDate());
    }

    private void populateScheduleUI(List<RepaymentInstallments> schedules) {
        int childCount = binding.repaymentScheduleContainer.getChildCount();
        if (childCount > 1) {
            binding.repaymentScheduleContainer.removeViews(1, childCount - 1);
        }

        for (RepaymentInstallments item : schedules) {
            ItemScheduleRowBinding rowBinding = ItemScheduleRowBinding.inflate(LayoutInflater.from(requireContext()), binding.repaymentScheduleContainer, false);
            rowBinding.tvId.setText(String.valueOf(item.getInstallmentNumber()));
            rowBinding.tvDueDate.setText(item.getDueDate());
            rowBinding.tvAmount.setText(String.format(Locale.getDefault(), "KES %,.0f", item.getTotalDue()));
            rowBinding.tvPrincipal.setText(String.format(Locale.getDefault(), "KES %,.0f", item.getPrincipal()));
            rowBinding.tvInterest.setText(String.format(Locale.getDefault(), "KES %,.0f", item.getInterest()));
            rowBinding.tvStatus.setText(item.getStatus());
            binding.repaymentScheduleContainer.addView(rowBinding.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
