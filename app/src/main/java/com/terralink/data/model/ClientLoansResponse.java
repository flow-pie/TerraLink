package com.terralink.data.model;

public class ClientLoansResponse {

    private String id;
    private String referenceNo;
    private String loanProductName;
    private double approvedAmount;
    private double balance;
    private double repaymentAmount;
    private String type;
    private String status;
    private String submittedAt;

    public String getLoanId() {
        return id;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public String getLoanProductName() {
        return loanProductName;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }

    public double getBalance() {
        return balance;
    }

    public double getRepaymentAmount(){
        return repaymentAmount;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
    
    // Compatibility for existing code
    public String getLoanNo() {
        return referenceNo;
    }
}