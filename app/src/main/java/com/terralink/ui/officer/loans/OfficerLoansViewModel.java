package com.terralink.ui.officer.loans;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.LoanListItemResponse;
import com.terralink.data.model.LoanProductResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.PortfolioSummaryResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.ReportRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficerLoansViewModel extends ViewModel {
    private final LoanRepository loanRepository;
    private final ReportRepository reportRepository;

    @Inject
    public OfficerLoansViewModel(LoanRepository loanRepository, ReportRepository reportRepository) {
        this.loanRepository = loanRepository;
        this.reportRepository = reportRepository;
    }

    public LiveData<Resource<PaginatedResponse<LoanListItemResponse>>> getLoans(String status, String search) {
        return loanRepository.getLoans(status, search, 1, 100);
    }

    public LiveData<Resource<List<LoanProductResponse>>> getLoanProducts() {
        return loanRepository.getLoanProducts();
    }

    public LiveData<Resource<PortfolioSummaryResponse>> getPortfolioSummary() {
        return reportRepository.getPortfolioSummary();
    }
}
