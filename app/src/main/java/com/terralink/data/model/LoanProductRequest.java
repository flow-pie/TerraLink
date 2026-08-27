package com.terralink.data.model;

public class LoanProductRequest {
    private String name;
    private double minimumAmount;
    private double maximumAmount;
    private double interestRate;
    private double processingFee;
    private double lateFee;
    private int minimumDuration;
    private int maximumDuration;
    private String repaymentFrequency;

    public LoanProductRequest(String name, double minimumAmount, double maximumAmount, double interestRate, 
                               double processingFee, double lateFee, int minimumDuration, int maximumDuration, 
                               String repaymentFrequency) {
        this.name = name;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.interestRate = interestRate;
        this.processingFee = processingFee;
        this.lateFee = lateFee;
        this.minimumDuration = minimumDuration;
        this.maximumDuration = maximumDuration;
        this.repaymentFrequency = repaymentFrequency;
    }
}
