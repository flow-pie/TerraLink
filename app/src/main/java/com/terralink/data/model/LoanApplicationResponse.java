package com.terralink.data.model;

public class LoanApplicationResponse {
    private int id;
    private String applicationNo;
    private String clientFullName;
    private String loanProductName;
    private double requestedAmount;
    private int durationMonths;
    private String status;
    private String submittedAt;

    public int getId() {
        return id;
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public String getLoanProductName() {
        return loanProductName;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
}
