package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.PaymentApi;
import com.terralink.data.model.InitiatePaymentRequest;
import com.terralink.data.model.InitiatePaymentResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.PaymentHistoryResponse;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {
    private final PaymentApi paymentApi;

    @Inject
    public PaymentRepository(PaymentApi paymentApi){
        this.paymentApi = paymentApi;
    }

    // allow Hilt to inject this repository

    public LiveData<Resource<InitiatePaymentResponse>> initiatePayment(InitiatePaymentRequest request){
        MutableLiveData<Resource<InitiatePaymentResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        paymentApi.initiatePayment(request).enqueue(new Callback<InitiatePaymentResponse>() {
            @Override
            public void onResponse(Call<InitiatePaymentResponse> call, Response<InitiatePaymentResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    result.postValue(Resource.success(response.body()));
                }else{
                    result.postValue(Resource.error("Request Failed: "+response.code()));
                }
            }

            @Override
            public void onFailure(Call<InitiatePaymentResponse> call, Throwable t) {
                result.postValue(Resource.error("Network Error: "+t.getMessage()));
            }
        });

        return result;
    }
    public LiveData<Resource<InitiatePaymentResponse>> getPaymentStatus(long paymentId){
        MutableLiveData<Resource<InitiatePaymentResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        paymentApi.getPaymentStatus(paymentId).enqueue(new Callback<InitiatePaymentResponse>() {
            @Override
            public void onResponse(Call<InitiatePaymentResponse> call, Response<InitiatePaymentResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    result.postValue(Resource.success(response.body()));
                }else{
                    result.postValue(Resource.error("Request Failed: "+response.code()));
                }
            }

            @Override
            public void onFailure(Call<InitiatePaymentResponse> call, Throwable t) {
                result.postValue(Resource.error("Network Error: "+t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<List<PaymentHistoryResponse>>> getClientPayments(String clientId){
        MutableLiveData<Resource<List<PaymentHistoryResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        paymentApi.getClientPayments(clientId, 1, 20).enqueue(
                new Callback<PaginatedResponse<PaymentHistoryResponse>>() {
                    @Override
                    public void onResponse(Call<PaginatedResponse<PaymentHistoryResponse>> call, Response<PaginatedResponse<PaymentHistoryResponse>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(Resource.success(response.body().getItems()));
                        }else{
                            result.postValue(Resource.error("Request Failed for client Id : "+clientId));
                        }
                    }

                    @Override
                    public void onFailure(Call<PaginatedResponse<PaymentHistoryResponse>> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }

}
