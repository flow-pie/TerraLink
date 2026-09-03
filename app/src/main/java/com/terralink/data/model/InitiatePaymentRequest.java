package com.terralink.data.model;

public class InitiatePaymentRequest {
    private long loanId;
    private long scheduleId;
    private double amount;
    private String phone;

    public InitiatePaymentRequest(long loanId, long scheduleId, double amount, String phone) {
        this.loanId = loanId;
        this.scheduleId = scheduleId;
        this.amount = amount;
        this.phone = phone;
    }

    public long getLoanId() { return loanId; }
    public long getScheduleId() { return scheduleId; }
    public double getAmount() { return amount; }
    public String getPhone() { return phone; }
}
