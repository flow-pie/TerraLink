package com.terralink.ui.officer.appraisal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.CreditHistoryResponse;
import com.terralink.data.model.CreditScoreResponse;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.data.model.LoanAppraisalDetailResponse;
import com.terralink.data.model.LoanAppraisalRequest;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoanAppraisalViewModel extends ViewModel {
    private final LoanRepository loanRepository;

    @Inject
    public LoanAppraisalViewModel(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public LiveData<Resource<LoanAppraisalDetailResponse>> getApplicationDetail(int id) {
        return loanRepository.getLoanApplicationDetail(id);
    }

    public LiveData<Resource<LoanDetailsResponse>> getLoanDetails(String loanId) {
        return loanRepository.getClientLoanDetails(loanId);
    }

    public LiveData<Resource<CreditScoreResponse>> getCreditScore(String clientId) {
        return loanRepository.getCreditScore(clientId);
    }

    public LiveData<Resource<CreditScoreResponse>> calculateCreditScore(String clientId, Double amount) {
        return loanRepository.calculateCreditScore(clientId, amount);
    }

    public LiveData<Resource<List<CreditHistoryResponse>>> getCreditHistory(String clientId) {
        return loanRepository.getCreditHistory(clientId);
    }

    public LiveData<Resource<List<AssetResponse>>> getClientAssets(String clientId) {
        return loanRepository.getClientAssets(clientId);
    }

    public LiveData<Resource<List<ClientLoansResponse>>> getClientLoans(String clientId) {
        return loanRepository.getClientLoans(clientId);
    }

    public LiveData<Resource<List<IncomeAssessmentResponse>>> getIncomeAssessments(String clientId) {
        return loanRepository.getIncomeAssessments(clientId);
    }

    public LiveData<Resource<Void>> submitAppraisal(int applicationId, String decision, String notes, int score) {
        LoanAppraisalRequest request = new LoanAppraisalRequest(decision, notes, score);
        return loanRepository.appraiseLoan(applicationId, request);
    }
}
