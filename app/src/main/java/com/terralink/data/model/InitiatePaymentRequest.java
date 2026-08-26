package com.terralink.data.model;

public class InitiatePaymentRequest {
    private long loanId;
    private long scheduleId;
    private String phone;

    public InitiatePaymentRequest(long loanId, long scheduleId, String phone) {
        this.loanId = loanId;
        this.scheduleId = scheduleId;
        this.phone = phone;
    }

    public long getLoanId() { return loanId; }
    public long getScheduleId() { return scheduleId; }
    public String getPhone() { return phone; }
}
