package com.terralink.ui.client.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.InitiatePaymentRequest;
import com.terralink.data.model.InitiatePaymentResponse;
import com.terralink.data.repository.PaymentRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PaymentViewModel extends ViewModel {
    private final PaymentRepository paymentRepository;

    @Inject
    public PaymentViewModel(PaymentRepository paymentRepository){
        this.paymentRepository = paymentRepository;
    }

    public LiveData<Resource<InitiatePaymentResponse>> initiatePayment(InitiatePaymentRequest request){
        return paymentRepository.initiatePayment(request);
    }

    public LiveData<Resource<InitiatePaymentResponse>> getPaymentStatus(long paymentId){
        return paymentRepository.getPaymentStatus(paymentId);
    }
}

