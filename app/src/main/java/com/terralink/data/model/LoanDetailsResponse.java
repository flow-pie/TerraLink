package com.terralink.data.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LoanDetailsResponse {

    private String id;
    private String loanNo;
    private String clientFullName;
    private double approvedAmount;
    private double principalBalance;
    private double outstandingAmount;
    private double totalRepayment;
    private int installmentsPaid;
    private int installmentsTotal;
    private String nextDueDate;
    private double nextInstallmentAmount;
    private String interestRate;
    private String status;

    public String getLoanId() {
        return id;
    }

    public String getLoanNo() {
        return loanNo;
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }
    public double getPrincipalBalance() {
        return principalBalance;
    }

    public double getOutStandingAmount() {
        return outstandingAmount;
    }

    public double getNextInstallmentAmount() {
        return nextInstallmentAmount;
    }

    public String getInterestRate() {
        return interestRate+"%";
    }

    public double getTotalRepayment() {
        return totalRepayment;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public int getInstallmentsTotal() {
        return installmentsTotal;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public String getStatus() {
        return status;
    }

    public long getDaysUntilNextDueDate() {

        if (nextDueDate == null || nextDueDate.isBlank()) {
            return 0;
        }

        LocalDate dueDate = LocalDate.parse(nextDueDate);

        return ChronoUnit.DAYS.between(
                LocalDate.now(),
                dueDate
        );
    }
}