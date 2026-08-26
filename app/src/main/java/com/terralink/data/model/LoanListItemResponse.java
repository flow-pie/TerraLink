package com.terralink.data.model;

public class LoanListItemResponse {
    private String id;
    private String loanNo;
    private String clientFullName;
    private double approvedAmount;
    private double outstandingAmount;
    private String status;
    private String nextDueDate;

    public String getId() {
        return id;
    }

    public String getLoanNo() {
        return loanNo;
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }

    public double getOutstandingAmount() {
        return outstandingAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }
}
