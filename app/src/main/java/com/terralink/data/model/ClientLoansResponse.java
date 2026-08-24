package com.terralink.data.model;

public class ClientLoansResponse {

    private long id;
    private String loanNo;
    private double approvedAmount;
    private double balance;
    private String status;

    public long getId() {
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

    public String getStatus() {
        return status;
    }
}