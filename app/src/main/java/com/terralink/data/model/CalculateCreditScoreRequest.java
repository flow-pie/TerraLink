package com.terralink.data.model;

public class CalculateCreditScoreRequest {
    private Double proposedLoanAmount;

    public CalculateCreditScoreRequest(Double proposedLoanAmount) {
        this.proposedLoanAmount = proposedLoanAmount;
    }

    public Double getProposedLoanAmount() {
        return proposedLoanAmount;
    }

    public void setProposedLoanAmount(Double proposedLoanAmount) {
        this.proposedLoanAmount = proposedLoanAmount;
    }
}
