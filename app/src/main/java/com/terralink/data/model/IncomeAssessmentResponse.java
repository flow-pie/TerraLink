package com.terralink.data.model;

import java.util.Date;

public class IncomeAssessmentResponse {
    private long id;
    private long clientId;
    private Long loanApplicationId;
    private double businessRevenue;
    private double otherIncome;
    private double householdExpenses;
    private double disposableIncome;
    private String verificationStatus;
    private Date verifiedAt;
    private Long verifiedBy;
    private Date assessedAt;

    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public Long getLoanApplicationId() {
        return loanApplicationId;
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

    public double getDisposableIncome() {
        return disposableIncome;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public Date getAssessedAt() {
        return assessedAt;
    }
}
