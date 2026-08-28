package com.terralink.ui.client.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.RepaymentInstallments;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

// Receive UserRepository through Hilt.
// Request required info.
// Expose the result to the Activity.

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private final MediatorLiveData<Resource<UserProfileResponse>> userProfile = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<ClientLoansResponse>>> clientLoans = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<LoanDetailsResponse>> loanDetails = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<RepaymentInstallments>>> repaymentInstallments = new MediatorLiveData<>();

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Inject
    public HomeViewModel(UserRepository userRepository, LoanRepository loanRepository){
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        refreshProfile();
    }

    public void refreshProfile() {
        fetchSource(userRepository.getMe(), userProfile);
    }

    public void refreshLoans(String clientId) {
        fetchSource(loanRepository.getClientLoans(clientId), clientLoans);
    }

    public void refreshLoanDetails(String loanId) {
        fetchSource(loanRepository.getClientLoanDetails(loanId), loanDetails);
    }

    public void refreshRepayments(String loanId) {
        fetchSource(loanRepository.getRepaymentSchedule(loanId), repaymentInstallments);
    }

    private <T> void fetchSource(LiveData<Resource<T>> source, MediatorLiveData<Resource<T>> target) {
        target.setValue(Resource.loading());
        target.addSource(source, resource -> {
            target.setValue(resource);
            if (resource.getStatus() != LoginStatus.LOADING) {
                target.removeSource(source);
            }
        });
    }

    public LiveData<Resource<UserProfileResponse>> getActiveUser(){
        return userProfile;
    }

    public LiveData<Resource<List<ClientLoansResponse>>> getClientLoansStream(){
        return clientLoans;
    }

    public LiveData<Resource<LoanDetailsResponse>> getLoanDetailsStream(){
        return loanDetails;
    }

    public LiveData<Resource<List<RepaymentInstallments>>> getRepaymentInstallmentsStream(){
        return repaymentInstallments;
    }
    
    // Legacy methods for compatibility if needed, but better to use streams
    public LiveData<Resource< List<ClientLoansResponse>>> getClientLoans(String clientId){
        refreshLoans(clientId);
        return clientLoans;
    }

    public LiveData<Resource< LoanDetailsResponse>> getClientDetails(String loanId){
        refreshLoanDetails(loanId);
        return loanDetails;
    }

    public LiveData< Resource<List<RepaymentInstallments>>> getRepaymentInstallments(String loanId){
        refreshRepayments(loanId);
        return repaymentInstallments;
    }
}
