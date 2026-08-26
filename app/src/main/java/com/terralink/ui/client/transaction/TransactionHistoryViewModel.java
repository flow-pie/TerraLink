package com.terralink.ui.client.transaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.PaymentHistoryResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.PaymentRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionHistoryViewModel extends ViewModel {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Inject
    public TransactionHistoryViewModel(PaymentRepository paymentRepository, UserRepository userRepository){
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    public LiveData<Resource<UserProfileResponse>> getActiveUser(){
        return userRepository.getMe();
    }

    public LiveData<Resource<List<PaymentHistoryResponse>>> getTransactions(String clientId){
        return paymentRepository.getClientPayments(clientId);
    }
}
