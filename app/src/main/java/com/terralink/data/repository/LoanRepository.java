package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.LoanApi;
import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.CreditHistoryResponse;
import com.terralink.data.model.CreditScoreResponse;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.data.model.LoanAppraisalDetailResponse;
import com.terralink.data.model.LoanAppraisalRequest;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.LoanListItemResponse;
import com.terralink.data.model.LoanProductResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.RepaymentInstallments;
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

    public LiveData<Resource<CreditScoreResponse>> getCreditScore(String clientId){
        MutableLiveData<Resource<CreditScoreResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getCreditScore(clientId).enqueue(new Callback<CreditScoreResponse>() {
            @Override
            public void onResponse(Call<CreditScoreResponse> call, Response<CreditScoreResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    result.postValue(Resource.success(response.body()));
                }else{
                    result.postValue(Resource.error("Request Failed: "+response.code()));
                }
            }

            @Override
            public void onFailure(Call<CreditScoreResponse> call, Throwable t) {
                result.postValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

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

    public  LiveData<Resource <List<RepaymentInstallments>> >  getRepaymentSchedule(String loanId){
        MutableLiveData< Resource<List<RepaymentInstallments>> > results = new MutableLiveData<>();

        results.setValue(Resource.loading());

        loanApi.getRepaymentInstallments(loanId).enqueue(
                new Callback<List<RepaymentInstallments>>() {
                    @Override
                    public void onResponse(Call<List<RepaymentInstallments>> call,
                                           Response<List<RepaymentInstallments>> response) {
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
                    public void onFailure(Call<List<RepaymentInstallments>> call, Throwable t) {
                        results.postValue(
                                Resource.error("Network Error: "+t.getMessage())
                        );
                    }
                }
        );

        return results;

    }

    public LiveData<Resource<Void>> createLoanApplication(com.terralink.data.model.LoanApplicationRequest request){
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();

        result.setValue(Resource.loading());

        loanApi.createLoanApplication(request).enqueue(
                new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if(response.isSuccessful()){
                            result.postValue(Resource.success(null));
                        }else{
                            result.postValue(Resource.error("Request Failed: "+response.code()));
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }

    public LiveData<Resource<List<LoanProductResponse>>> getLoanProducts(){
        MutableLiveData<Resource<List<LoanProductResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getLoanProducts(true).enqueue(
                new Callback<List<LoanProductResponse>>() {
                    @Override
                    public void onResponse(Call<List<LoanProductResponse>> call, Response<List<LoanProductResponse>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(Resource.success(response.body()));
                        }else{
                            result.postValue(Resource.error("Request Failed: "+response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LoanProductResponse>> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }

    public LiveData<Resource<List<LoanListItemResponse>>> getLoans(String status, String search, int page, int pageSize){
        MutableLiveData<Resource<List<LoanListItemResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getLoans(status, search, page, pageSize).enqueue(
                new Callback<List<LoanListItemResponse>>() {
                    @Override
                    public void onResponse(Call<List<LoanListItemResponse>> call, Response<List<LoanListItemResponse>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(Resource.success(response.body()));
                        }else{
                            result.postValue(Resource.error("Request Failed: "+response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LoanListItemResponse>> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }

    public LiveData<Resource<PaginatedResponse<LoanApplicationResponse>>> getLoanApplications(String status, int page, int pageSize){
        MutableLiveData<Resource<PaginatedResponse<LoanApplicationResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getLoanApplications(status, page, pageSize).enqueue(
                new Callback<PaginatedResponse<LoanApplicationResponse>>() {
                    @Override
                    public void onResponse(Call<PaginatedResponse<LoanApplicationResponse>> call, Response<PaginatedResponse<LoanApplicationResponse>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(Resource.success(response.body()));
                        }else{
                            result.postValue(Resource.error("Request Failed: "+response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<PaginatedResponse<LoanApplicationResponse>> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }

    public LiveData<Resource<LoanAppraisalDetailResponse>> getLoanApplicationDetail(int id) {
        MutableLiveData<Resource<LoanAppraisalDetailResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getLoanApplicationDetail(id).enqueue(new Callback<LoanAppraisalDetailResponse>() {
            @Override
            public void onResponse(Call<LoanAppraisalDetailResponse> call, Response<LoanAppraisalDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load application detail"));
                }
            }

            @Override
            public void onFailure(Call<LoanAppraisalDetailResponse> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> appraiseLoan(int applicationId, LoanAppraisalRequest request) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.appraiseLoanApplication(applicationId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Appraisal Failed: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<List<CreditHistoryResponse>>> getCreditHistory(String clientId) {
        MutableLiveData<Resource<List<CreditHistoryResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getCreditHistory(clientId).enqueue(new Callback<List<CreditHistoryResponse>>() {
            @Override
            public void onResponse(Call<List<CreditHistoryResponse>> call, Response<List<CreditHistoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load credit history"));
                }
            }

            @Override
            public void onFailure(Call<List<CreditHistoryResponse>> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<List<AssetResponse>>> getClientAssets(String clientId) {
        MutableLiveData<Resource<List<AssetResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getClientAssets(clientId).enqueue(new Callback<List<AssetResponse>>() {
            @Override
            public void onResponse(Call<List<AssetResponse>> call, Response<List<AssetResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load assets"));
                }
            }

            @Override
            public void onFailure(Call<List<AssetResponse>> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<List<IncomeAssessmentResponse>>> getIncomeAssessments(String clientId) {
        MutableLiveData<Resource<List<IncomeAssessmentResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        loanApi.getIncomeAssessments(clientId).enqueue(new Callback<List<IncomeAssessmentResponse>>() {
            @Override
            public void onResponse(Call<List<IncomeAssessmentResponse>> call, Response<List<IncomeAssessmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load income assessments"));
                }
            }

            @Override
            public void onFailure(Call<List<IncomeAssessmentResponse>> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });

        return result;
    }


}

