package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.ReportApi;
import com.terralink.data.model.PortfolioSummaryResponse;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportRepository {
    private final ReportApi reportApi;

    @Inject
    public ReportRepository(ReportApi reportApi) {
        this.reportApi = reportApi;
    }

    public LiveData<Resource<PortfolioSummaryResponse>> getPortfolioSummary() {
        MutableLiveData<Resource<PortfolioSummaryResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        reportApi.getPortfolioSummary().enqueue(new Callback<PortfolioSummaryResponse>() {
            @Override
            public void onResponse(Call<PortfolioSummaryResponse> call, Response<PortfolioSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load portfolio summary"));
                }
            }

            @Override
            public void onFailure(Call<PortfolioSummaryResponse> call, Throwable t) {
                result.postValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }
}
