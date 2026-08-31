package com.terralink.data.model;

import java.util.List;

public class LoanAppraisalDetailResponse {
    private Application application;
    private Client client;
    private CreditScore creditScore;
    private List<CreditHistoryResponse> creditHistory;
    private IncomeAssessment incomeAssessment;
    private List<AssetResponse> assets;

    public Application getApplication() { return application; }
    public Client getClient() { return client; }
    public CreditScore getCreditScore() { return creditScore; }
    public List<CreditHistoryResponse> getCreditHistory() { return creditHistory; }
    public IncomeAssessment getIncomeAssessment() { return incomeAssessment; }
    public List<AssetResponse> getAssets() { return assets; }

    public static class Application {
        private int id;
        private String applicationNo;
        private double requestedAmount;
        private int durationMonths;
        private String purpose;
        private String status;

        public int getId() { return id; }
        public String getApplicationNo() { return applicationNo; }
        public double getRequestedAmount() { return requestedAmount; }
        public int getDurationMonths() { return durationMonths; }
        public String getPurpose() { return purpose; }
        public String getStatus() { return status; }
    }

    public static class Client {
        private int id;
        private String fullName;
        private String clientNo;

        public int getId() { return id; }
        public String getFullName() { return fullName; }
        public String getClientNo() { return clientNo; }
    }

    public static class CreditScore {
        private int score;
        private String rating;
        private int repaymentHistoryScore;
        private int repaymentCapacityScore;
        private int financialStabilityScore;
        private int verifiedAssetsScore;

        public int getScore() { return score; }
        public String getRating() { return rating; }
        public int getRepaymentHistoryScore() { return repaymentHistoryScore; }
        public int getRepaymentCapacityScore() { return repaymentCapacityScore; }
        public int getFinancialStabilityScore() { return financialStabilityScore; }
        public int getVerifiedAssetsScore() { return verifiedAssetsScore; }
    }

    public static class IncomeAssessment {
        private long id;
        private double businessRevenue;
        private double otherIncome;
        private double householdExpenses;
        private double disposableIncome;
        private String verificationStatus;

        public long getId() { return id; }
        public double getBusinessRevenue() { return businessRevenue; }
        public double getOtherIncome() { return otherIncome; }
        public double getHouseholdExpenses() { return householdExpenses; }
        public double getDisposableIncome() { return disposableIncome; }
        public String getVerificationStatus() { return verificationStatus; }
    }
}
