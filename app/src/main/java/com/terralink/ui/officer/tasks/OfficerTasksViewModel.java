package com.terralink.ui.officer.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficerTasksViewModel extends ViewModel {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Inject
    public OfficerTasksViewModel(LoanRepository loanRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    public LiveData<Resource<PaginatedResponse<LoanApplicationResponse>>> getPendingTasks() {
        return loanRepository.getLoanApplications(null, 1, 50);
    }

    public LiveData<Resource<UserProfileResponse>> getProfile() {
        return userRepository.getMe();
    }

    public LiveData<Resource<Void>> logout() {
        return userRepository.logout();
    }
}
