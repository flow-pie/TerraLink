package com.terralink.data.model;

public class CreditHistoryResponse {
    private String loanType;
    private String completionDate;
    private String status;

    public String getLoanType() {
        return loanType;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public String getStatus() {
        return status;
    }
}
