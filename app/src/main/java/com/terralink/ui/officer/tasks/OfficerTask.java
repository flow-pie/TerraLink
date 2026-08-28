package com.terralink.ui.officer.tasks;

import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.LoanApplicationResponse;

public abstract class OfficerTask {
    public enum Type { APPRAISAL, VERIFICATION }

    public abstract Type getType();
    public abstract int getId();
    public abstract String getTitle();
    public abstract String getSubtitle();
    public abstract String getStatus();

    public static class AppraisalTask extends OfficerTask {
        private final LoanApplicationResponse loanApp;

        public AppraisalTask(LoanApplicationResponse loanApp) {
            this.loanApp = loanApp;
        }

        @Override public Type getType() { return Type.APPRAISAL; }
        @Override public int getId() { return loanApp.getId(); }
        @Override public String getTitle() { return loanApp.getClientFullName(); }
        @Override public String getSubtitle() { return "Loan ID: #" + loanApp.getApplicationNo(); }
        @Override public String getStatus() { return loanApp.getStatus(); }
        
        public LoanApplicationResponse getLoanApp() { return loanApp; }
    }

    public static class VerificationTask extends OfficerTask {
        private final ClientListItemResponse client;

        public VerificationTask(ClientListItemResponse client) {
            this.client = client;
        }

        @Override public Type getType() { return Type.VERIFICATION; }
        @Override public int getId() { return client.getId(); }
        @Override public String getTitle() { return client.getFullName(); }
        @Override public String getSubtitle() { 
            String sub = "Client No: " + client.getClientNo();
            if ("INACTIVE".equals(client.getUserStatus())) {
                sub += " (INACTIVE)";
            }
            return sub;
        }
        @Override public String getStatus() { return client.getStatus(); }
        
        public ClientListItemResponse getClient() { return client; }
    }
}
