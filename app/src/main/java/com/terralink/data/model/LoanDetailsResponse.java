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
    private double nextInstallmentPaid;
    private double interestRate;
    private double interestPaid;
    private double principalPaid;
    private double actualAmountPaid;
    private double penaltiesAccrued;
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

    public double getNextInstallmentPaid() {
        return nextInstallmentPaid;
    }

    public String getInterestRate() {
        return interestRate+"%";
    }

    public double getTotalRepayment() {
        return totalRepayment;
    }

    public double getActualAmountPaid() {
        return actualAmountPaid;
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

    public double getInterestPaid() {
        return interestPaid;
    }

    public double getPrincipalPaid() {
        return principalPaid;
    }

    public double getPenaltiesAccrued() {
        return penaltiesAccrued;
    }

    public String getStatus() {
        return status;
    }

    public long getDaysUntilNextDueDate() {

        if (nextDueDate == null || nextDueDate.isBlank()) {
            return 0;
        }

        try {
            LocalDate dueDate = LocalDate.parse(nextDueDate);
            return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        } catch (Exception e) {
            return 0;
        }
    }
}