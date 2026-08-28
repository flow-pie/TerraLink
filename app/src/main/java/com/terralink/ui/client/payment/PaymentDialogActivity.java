package com.terralink.ui.client.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.terralink.R;
import com.terralink.data.model.InitiatePaymentRequest;
import com.terralink.data.model.InitiatePaymentResponse;
import com.terralink.databinding.DialogInstallmentPaymentBinding;
import com.terralink.ui.common.SnackbarUtils;

import java.text.NumberFormat;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentDialogActivity extends AppCompatActivity {

    //intent keys
    public static final String EXTRA_LOAN_ID = "extra_loan_id";
    public static final String EXTRA_SCHEDULE_ID = "extra_schedule_id";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_INSTALLMENT = "extra_installment_amount";
    public static final String EXTRA_INSTALLMENT_DUE_DATE = "extra_installment_due_date";

    private PaymentViewModel viewModel;
    private long loanId;
    private long scheduleId;
    private double outstandingBalance;
    private double installmentAmount;
    private String installmentDueDate;
    private BottomSheetDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        loanId = getIntent().getLongExtra(EXTRA_LOAN_ID, -1);
        scheduleId = getIntent().getLongExtra(EXTRA_SCHEDULE_ID, -1);
        outstandingBalance = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        installmentAmount = getIntent().getDoubleExtra(EXTRA_INSTALLMENT, 0);
        installmentDueDate = getIntent().getStringExtra(EXTRA_INSTALLMENT_DUE_DATE);

        showPaymentDialog();
    }

    private void showPaymentDialog(){
        dialog = new BottomSheetDialog(this);
        DialogInstallmentPaymentBinding dialogBinding = DialogInstallmentPaymentBinding.inflate(LayoutInflater.from(this));
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.tvRemainingBalance.setText(formatCurrency(outstandingBalance));
        dialogBinding.etPaymentAmount.setText(formatCurrency(outstandingBalance));
        dialogBinding.tvInstallment.setText(formatCurrency(installmentAmount));
        dialogBinding.tvInstallmentDate.setText(installmentDueDate);

        dialogBinding.etMpesaPhone.setText("0712 345 678");

        dialogBinding.chipGroupAmount.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.chipFullBalance){
                dialogBinding.etPaymentAmount.setText(formatCurrency(outstandingBalance));
            } else if(checkedId == R.id.chipInstallment){
                dialogBinding.etPaymentAmount.setText(formatCurrency(installmentAmount));
            }
        });

        dialogBinding.btnPayMpesa.setOnClickListener(v -> {
            String amountStr = dialogBinding.etPaymentAmount.getText().toString().replaceAll("[^\\d.]", "");
            String phone = dialogBinding.etMpesaPhone.getText().toString().replaceAll("\\s", "");

            if(amountStr.isEmpty()){
                dialogBinding.etPaymentAmount.setError("Amount required");
                return;
            }

            if(phone.isEmpty() || phone.length() < 10){
                dialogBinding.etMpesaPhone.setError("Valid phone number required");
                return;
            }

            double amount;
            try{
                amount = Double.parseDouble(amountStr);
            }catch(NumberFormatException e){
                dialogBinding.etPaymentAmount.setError("Invalid amount");
                return;
            }

            dialogBinding.btnPayMpesa.setEnabled(false);
            dialogBinding.btnPayMpesa.setText("Processing...");

            InitiatePaymentRequest req = new InitiatePaymentRequest(loanId, scheduleId, phone);

            viewModel.initiatePayment(req).observe(this, result -> {
                switch (result.getStatus()){
                    case LOADING:
                        break;
                    case SUCCESS:
                        InitiatePaymentResponse resp = result.getData();
                        viewModel.getPaymentStatus(resp.getPaymentId()).observe(this, statusRes -> {
                            switch (statusRes.getStatus()){
                                case SUCCESS:
                                    SnackbarUtils.showSuccess((ViewGroup) findViewById(android.R.id.content), "Payment successful");
                                    dialog.dismiss();
                                    findViewById(android.R.id.content).postDelayed(this::finish, 2000);
                                    break;
                                case ERROR:
                                    dialogBinding.btnPayMpesa.setEnabled(true);
                                    dialogBinding.btnPayMpesa.setText("Pay Now");
                                    SnackbarUtils.showError((ViewGroup) findViewById(android.R.id.content), statusRes.getMessage());
                                    break;
                            }
                        });
                        break;
                    case ERROR:
                        dialogBinding.btnPayMpesa.setEnabled(true);
                        dialogBinding.btnPayMpesa.setText("Pay Now");
                        SnackbarUtils.showError((ViewGroup) findViewById(android.R.id.content), result.getMessage());
                        break;
                }
            });
        });

        dialog.setOnDismissListener(d -> finish());
        dialog.show();
    }

    private String formatCurrency(double amount){
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "KE"));
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
