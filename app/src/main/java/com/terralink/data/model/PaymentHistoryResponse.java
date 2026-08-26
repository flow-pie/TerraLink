package com.terralink.data.model;

public class PaymentHistoryResponse {
    private long id;
    private double amount;
    private String status;
    private String paymentDate;
    private String loanNo;
    private String receiptNumber;

    public long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public String getLoanNo() {
        return loanNo;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }
}
