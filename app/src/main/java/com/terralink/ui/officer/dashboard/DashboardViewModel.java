package com.terralink.ui.officer.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.PortfolioSummaryResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.ReportRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DashboardViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final LoanRepository loanRepository;

    @Inject
    public DashboardViewModel(UserRepository userRepository, ReportRepository reportRepository, LoanRepository loanRepository) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.loanRepository = loanRepository;
    }

    public LiveData<Resource<UserProfileResponse>> getProfile() {
        return userRepository.getMe();
    }

    public LiveData<Resource<PortfolioSummaryResponse>> getPortfolioSummary() {
        return reportRepository.getPortfolioSummary();
    }

    public LiveData<Resource<PaginatedResponse<LoanApplicationResponse>>> getPendingAppraisals() {
        return loanRepository.getLoanApplications(null, 1, 20);
    }
}
