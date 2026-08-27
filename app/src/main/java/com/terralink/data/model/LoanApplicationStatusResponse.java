package com.terralink.data.model;

import java.util.List;

public class LoanApplicationStatusResponse {
    private String status;
    private String decisionNotes;
    private String decidedAt;
    private List<TimelineStage> timeline;
    private AssignedOfficer assignedOfficer;

    public String getStatus() { return status; }
    public String getDecisionNotes() { return decisionNotes; }
    public String getDecidedAt() { return decidedAt; }
    public List<TimelineStage> getTimeline() { return timeline; }
    public AssignedOfficer getAssignedOfficer() { return assignedOfficer; }

    public static class TimelineStage {
        private String stage;
        private String completedAt;

        public String getStage() { return stage; }
        public String getCompletedAt() { return completedAt; }
    }

    public static class AssignedOfficer {
        private String fullName;
        private String employeeNo;

        public String getFullName() { return fullName; }
        public String getEmployeeNo() { return employeeNo; }
    }
}
