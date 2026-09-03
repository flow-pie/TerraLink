package com.terralink.data.model;

public class CloseLoanResponse {
    private long id;
    private long loanId;
    private String closureDate;
    private String certificateNumber;
    private long closedBy;

    public CloseLoanResponse() {}

    public CloseLoanResponse(long id, String certificateNumber) {
        this.id = id;
        this.certificateNumber = certificateNumber;
    }

    public long getId() {
        return id;
    }

    public long getLoanId() {
        return loanId;
    }

    public String getClosureDate() {
        return closureDate;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public long getClosedBy() {
        return closedBy;
    }
}
