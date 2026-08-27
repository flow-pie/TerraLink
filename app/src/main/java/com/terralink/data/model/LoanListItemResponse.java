package com.terralink.data.model;

public class LoanListItemResponse {
    private String id;
    private String loanNo;
    private String clientFullName;
    private double approvedAmount;
    private double balance;
    private String status;
    private String nextDueDate;
    private String sector;
    private int repaymentProgress;

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
        return balance;
    }

    public double getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public String getSector() {
        return sector;
    }

    public int getRepaymentProgress() {
        return repaymentProgress;
    }
}
