package com.terralink.data.model;

public class ClientLoansResponse {

    private String id;
    private String loanNo;
    private double approvedAmount;
    private double balance;
    private double repaymentAmount;
    private String status;

    public String getLoanId() {
        return id;
    }

    public String getLoanNo() {
        return loanNo;
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

    public String getStatus() {
        return status;
    }
}