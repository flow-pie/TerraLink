package com.terralink.data.model;

public class LoanAppraisalRequest {
    private String decision;
    private String decisionNotes;
    private int creditScoreSnapshot;

    public LoanAppraisalRequest(String decision, String decisionNotes, int creditScoreSnapshot) {
        this.decision = decision;
        this.decisionNotes = decisionNotes;
        this.creditScoreSnapshot = creditScoreSnapshot;
    }

    public String getDecision() {
        return decision;
    }

    public String getDecisionNotes() {
        return decisionNotes;
    }

    public int getCreditScoreSnapshot() {
        return creditScoreSnapshot;
    }
}
