package com.terralink.data.model;

public class PortfolioSummaryResponse {
    private int activeLoans;
    private int totalClients;
    private double disbursedMtd;
    private double outstandingPortfolio;
    private int pendingApplications;
    private int overdueLoans;
    private double arrearsAmount;

    public int getActiveLoansCount() {
        return activeLoans;
    }

    public double getDisbursedAmountMtd() {
        return disbursedMtd;
    }

    public int getTotalClients() {
        return totalClients;
    }

    public double getOutstandingPortfolio() {
        return outstandingPortfolio;
    }

    public int getPendingApplications() {
        return pendingApplications;
    }

    public int getOverdueLoans() {
        return overdueLoans;
    }

    public double getArrearsAmount() {
        return arrearsAmount;
    }
}
