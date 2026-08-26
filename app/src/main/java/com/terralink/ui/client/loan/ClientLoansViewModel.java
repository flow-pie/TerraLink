package com.terralink.ui.client.loan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanListItemResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ClientLoansViewModel extends ViewModel {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Inject
    public ClientLoansViewModel(LoanRepository loanRepository, UserRepository userRepository){
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    public LiveData<Resource<UserProfileResponse>> getActiveUser(){
        return userRepository.getMe();
    }

    public LiveData<Resource<List<LoanListItemResponse>>> getLoans(){
        return loanRepository.getLoans("ACTIVE", null, 1, 20);
    }

    public LiveData<Resource <List<ClientLoansResponse>> > getClientLoans(String clientId){
        return  loanRepository.getClientLoans(clientId);
    }
}
