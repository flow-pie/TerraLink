package com.terralink.data.model;

public class CreditScoreResponse {
    private int creditScore;
    private String rating;
    private int repaymentHistoryScore;
    private int repaymentCapacityScore;
    private int financialStabilityScore;
    private int verifiedAssetsScore;

    public int getCreditScore() {
        return creditScore;
    }

    public String getRating() {
        return rating;
    }

    public int getRepaymentHistoryScore() {
        return repaymentHistoryScore;
    }

    public int getRepaymentCapacityScore() {
        return repaymentCapacityScore;
    }

    public int getFinancialStabilityScore() {
        return financialStabilityScore;
    }

    public int getVerifiedAssetsScore() {
        return verifiedAssetsScore;
    }
}
