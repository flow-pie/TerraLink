package com.terralink.ui.client.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.LoanApplicationStatusResponse;
import com.terralink.data.model.NotificationResponse;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.NotificationRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotificationStatusViewModel extends ViewModel {
    private final NotificationRepository notificationRepository;
    private final LoanRepository loanRepository;

    @Inject
    public NotificationStatusViewModel(NotificationRepository notificationRepository, LoanRepository loanRepository){
        this.notificationRepository = notificationRepository;
        this.loanRepository = loanRepository;
    }

    public LiveData<Resource<List<NotificationResponse>>> getNotifications(){
        return notificationRepository.getNotifications();
    }

    public LiveData<Resource<Void>> markAllAsRead(){
        return notificationRepository.markAllAsRead();
    }

    public LiveData<Resource<LoanApplicationStatusResponse>> getLoanStatus(int applicationId) {
        return loanRepository.getLoanApplicationStatus(applicationId);
    }
}
