package com.terralink.data.model;

public class LoanApplicationRequest {
    private String loanProductId;
    private double requestedAmount;
    private int durationMonths;
    private String purpose;

    public LoanApplicationRequest(String loanProductId, double requestedAmount, int tenureMonths, String purpose) {
        this.loanProductId = loanProductId;
        this.requestedAmount = requestedAmount;
        this.durationMonths = tenureMonths;
        this.purpose = purpose;
    }

    public String getProductId() {
        return loanProductId;
    }

    public double getAmount() {
        return requestedAmount;
    }

    public int getTenureMonths() {
        return durationMonths;
    }

    public String getPurpose() {
        return purpose;
    }
}
