package com.terralink.data.model;

public class RepaymentInstallments {

    private long id;
    private int installmentNumber;
    private String dueDate;
    private double principal;
    private double interest;
    private double totalDue;
    private String status;

    public long getRepaymentScheduleId() {
        return id;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public String getDueDate() {
        return dueDate;
    }

    public double getPrincipal() {
        return principal;
    }

    public double getInterest() {
        return interest;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public String getStatus() {
        return status;
    }
}