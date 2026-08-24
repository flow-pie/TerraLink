package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.LoanApi;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoanRepository {
    private final LoanApi loanApi;

    @Inject
    public LoanRepository(LoanApi loanApi){
        this.loanApi = loanApi;
    }

    public LiveData<Resource <List<ClientLoansResponse>> > getClientLoans(String clientId){

        MutableLiveData<Resource< List<ClientLoansResponse>>> result = new MutableLiveData<>();

        result.setValue(Resource.loading());

        loanApi.getClientLoans(clientId).enqueue(
                new Callback<List<ClientLoansResponse>>() {
                    @Override
                    public void onResponse(Call<List<ClientLoansResponse>> call, Response<List<ClientLoansResponse>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(
                                    Resource.success(response.body())
                            );
                        }else{
                            result.postValue(
                                    Resource.error("Request Failed: "+response.code())
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ClientLoansResponse>> call, Throwable t) {
                            result.postValue(
                                    Resource.error("Network Error: "+t.getMessage())
                            );
                    }
                }
        );

        return result;
    }

    public LiveData<Resource<LoanDetailsResponse>> getClientLoanDetails(String loanId){

        MutableLiveData< Resource<LoanDetailsResponse>> results = new MutableLiveData<>();

        results.setValue(Resource.loading());

        loanApi.getloanDetails(loanId).enqueue(
                new Callback<LoanDetailsResponse>() {
                    @Override
                    public void onResponse(Call<LoanDetailsResponse> call, Response<LoanDetailsResponse> response) {
                        if(response.isSuccessful() && response.body() != null){
                            results.postValue(
                                    Resource.success(response.body())
                            );
                        }else{
                            results.postValue(
                                    Resource.error("Request Failed: "+response.code())
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<LoanDetailsResponse> call, Throwable t) {
                        results.postValue(
                                Resource.error("Network Error: "+t.getMessage())
                        );
                    }
                }
        );
        return results;
    }


}

