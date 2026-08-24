package com.terralink.data.model;

public class LoanDetailsResponse {

    private long id;
    private String loanNo;
    private String clientFullName;
    private double approvedAmount;
    private double principalBalance;
    private double outStandingAmount;
    private double totalRepayment;
    private int installmentsPaid;
    private int installmentsTotal;
    private String nextDueDate;
    private double nextInstallmentAmount;
    private double intrestRate;
    private String status;

    public long getId() {
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
    public double getPrincipalBalance() {
        return principalBalance;
    }

    public double getOutStandingAmount() {
        return outStandingAmount;
    }

    public double getNextInstallmentAmount() {
        return nextInstallmentAmount;
    }

    public double getIntrestRate() {
        return intrestRate;
    }

    public double getTotalRepayment() {
        return totalRepayment;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public int getInstallmentsTotal() {
        return installmentsTotal;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public String getStatus() {
        return status;
    }
}