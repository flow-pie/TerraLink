package com.terralink.data.model;

public class PortfolioSummaryResponse {
    private int activeLoans;
    private int totalClients;
    private double disbursedMtd;
    private int outstandingPortfolio;

    public int getActiveLoansCount() {
        return activeLoans;
    }

    public double getDisbursedAmountMtd() {
        return disbursedMtd;
    }

    public int getTotalClients() {
        return totalClients;
    }

    public int getOutstandingPortfolio() {
        return outstandingPortfolio;
    }

}
