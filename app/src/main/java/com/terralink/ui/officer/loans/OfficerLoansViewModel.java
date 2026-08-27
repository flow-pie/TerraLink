package com.terralink.ui.officer.loans;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.LoanListItemResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficerLoansViewModel extends ViewModel {
    private final LoanRepository loanRepository;

    @Inject
    public OfficerLoansViewModel(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public LiveData<Resource<List<LoanListItemResponse>>> getLoans(String status, String search) {
        return loanRepository.getLoans(status, search, 1, 100);
    }
}
