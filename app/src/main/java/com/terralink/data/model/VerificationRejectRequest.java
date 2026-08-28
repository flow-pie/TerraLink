package com.terralink.data.model;

public class VerificationRejectRequest {
    private String reason;

    public VerificationRejectRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
