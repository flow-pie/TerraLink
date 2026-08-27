package com.terralink.ui.client.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.terralink.R;
import com.terralink.data.model.InitiatePaymentRequest;
import com.terralink.data.model.InitiatePaymentResponse;
import com.terralink.databinding.DialogInstallmentPaymentBinding;

import java.text.NumberFormat;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_LOAN_ID = "arg_loan_id";
    private static final String ARG_SCHEDULE_ID = "arg_schedule_id";
    private static final String ARG_BALANCE = "arg_balance";
    private static final String ARG_INSTALLMENT = "arg_installment";
    private static final String ARG_DUE_DATE = "arg_due_date";

    private DialogInstallmentPaymentBinding binding;
    private PaymentViewModel viewModel;
    private long loanId;
    private long scheduleId;
    private double balance;
    private double installment;
    private String dueDate;

    public static PaymentBottomSheetFragment newInstance(long loanId, long scheduleId, double balance, double installment, String dueDate) {
        PaymentBottomSheetFragment fragment = new PaymentBottomSheetFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_LOAN_ID, loanId);
        args.putLong(ARG_SCHEDULE_ID, scheduleId);
        args.putDouble(ARG_BALANCE, balance);
        args.putDouble(ARG_INSTALLMENT, installment);
        args.putString(ARG_DUE_DATE, dueDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loanId = getArguments().getLong(ARG_LOAN_ID);
            scheduleId = getArguments().getLong(ARG_SCHEDULE_ID);
            balance = getArguments().getDouble(ARG_BALANCE);
            installment = getArguments().getDouble(ARG_INSTALLMENT);
            dueDate = getArguments().getString(ARG_DUE_DATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogInstallmentPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.tvRemainingBalance.setText(formatCurrency(balance));
        binding.etPaymentAmount.setText(formatCurrency(balance));
        binding.tvInstallment.setText(formatCurrency(installment));
        binding.tvInstallmentDate.setText(dueDate);
        binding.etMpesaPhone.setText("0712 345 678");
    }

    private void setupListeners() {
        binding.chipGroupAmount.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipFullBalance) {
                binding.etPaymentAmount.setText(formatCurrency(balance));
            } else if (id == R.id.chipInstallment) {
                binding.etPaymentAmount.setText(formatCurrency(installment));
            }
        });

        binding.btnPayMpesa.setOnClickListener(v -> initiatePayment());
    }

    private void initiatePayment() {
        String phone = binding.etMpesaPhone.getText().toString().replaceAll("\\s", "");
        if (phone.isEmpty() || phone.length() < 10) {
            binding.etMpesaPhone.setError("Valid phone number required");
            return;
        }

        binding.btnPayMpesa.setEnabled(false);
        binding.btnPayMpesa.setText("Processing...");

        InitiatePaymentRequest req = new InitiatePaymentRequest(loanId, scheduleId, phone);
        viewModel.initiatePayment(req).observe(getViewLifecycleOwner(), result -> {
            switch (result.getStatus()) {
                case SUCCESS:
                    checkPaymentStatus(result.getData().getPaymentId());
                    break;
                case ERROR:
                    handleError(result.getMessage());
                    break;
            }
        });
    }

    private void checkPaymentStatus(long paymentId) {
        viewModel.getPaymentStatus(paymentId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == com.terralink.ui.auth.LoginStatus.SUCCESS) {
                Toast.makeText(requireContext(), "Payment successful", Toast.LENGTH_LONG).show();
                dismiss();
            } else if (result.getStatus() == com.terralink.ui.auth.LoginStatus.ERROR) {
                handleError(result.getMessage());
            }
        });
    }

    private void handleError(String message) {
        binding.btnPayMpesa.setEnabled(true);
        binding.btnPayMpesa.setText("Pay Now");
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "KES %,.2f", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
