package com.terralink.data.model;

public class InitiatePaymentResponse {
    private long paymentId;
    private String status;

    public InitiatePaymentResponse(long paymentId, String status) {
        this.paymentId = paymentId;
        this.status = status;
    }

    public long getPaymentId() { return paymentId; }
    public String getStatus() { return status; }
}
