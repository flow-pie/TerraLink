package com.terralink.data.model;

public class CreateIncomeAssessmentRequest {
    private double businessRevenue;
    private double otherIncome;
    private double householdExpenses;

    public CreateIncomeAssessmentRequest(double businessRevenue, double otherIncome, double householdExpenses) {
        this.businessRevenue = businessRevenue;
        this.otherIncome = otherIncome;
        this.householdExpenses = householdExpenses;
    }

    public double getBusinessRevenue() {
        return businessRevenue;
    }

    public double getOtherIncome() {
        return otherIncome;
    }

    public double getHouseholdExpenses() {
        return householdExpenses;
    }
}
