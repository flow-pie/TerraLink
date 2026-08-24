package com.terralink.ui.client.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

// Receive UserRepository through Hilt.
// Request required info.
// Expose the result to the Activity.

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private final LiveData<Resource<UserProfileResponse>> userProfile;
    private final LoanRepository loanRepository;

    @Inject
    public HomeViewModel(UserRepository userRepository, LoanRepository loanRepository){
        this.userProfile = userRepository.getMe();
        this.loanRepository = loanRepository;
    }

    public LiveData<Resource<UserProfileResponse>> getActiveUser(){
        return userProfile;
    }

    public LiveData<Resource< List<ClientLoansResponse>>> getClientLoans(String clientId){
        return loanRepository.getClientLoans(clientId);
    }

    public LiveData<Resource< LoanDetailsResponse>> getClientDetails(String clientId){
        return loanRepository.getClientLoanDetails(clientId);
    }
}
