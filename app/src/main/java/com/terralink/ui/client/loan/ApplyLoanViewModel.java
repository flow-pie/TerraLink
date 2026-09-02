package com.terralink.ui.client.loan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.CreditScoreResponse;
import com.terralink.data.model.LoanApplicationRequest;
import com.terralink.data.model.LoanProductResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ApplyLoanViewModel extends ViewModel {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Inject
    public ApplyLoanViewModel(LoanRepository loanRepository, UserRepository userRepository){
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    public LiveData<Resource<UserProfileResponse>> getActiveUser(){
        return userRepository.getMe();
    }

    public LiveData<Resource<CreditScoreResponse>> getCreditScore(String clientId){
        return loanRepository.getCreditScore(clientId);
    }

    public LiveData<Resource<CreditScoreResponse>> calculateCreditScore(String clientId, Double amount) {
        return loanRepository.calculateCreditScore(clientId, amount);
    }

    public LiveData<Resource<Void>> submitApplication(LoanApplicationRequest request){
        return loanRepository.createLoanApplication(request);
    }

    public LiveData<Resource<List<LoanProductResponse>>> getLoanProducts(){
        return loanRepository.getLoanProducts();
    }
}
