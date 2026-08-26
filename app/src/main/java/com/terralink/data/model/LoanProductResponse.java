package com.terralink.data.model;

public class LoanProductResponse {
    private String id;
    private String name;
    private double minimumAmount;
    private double maximumAmount;
    private double interestRate;
    private int minimumDuration;
    private int maximumDuration;
    private String repaymentFrequency;
    private String status;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getMinimumAmount() {
        return minimumAmount;
    }

    public double getMaximumAmount() {
        return maximumAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public int getMinimumDuration() {
        return minimumDuration;
    }

    public int getMaximumDuration() {
        return maximumDuration;
    }

    public String getRepaymentFrequency() {
        return repaymentFrequency;
    }

    public String getStatus() {
        return status;
    }
}
