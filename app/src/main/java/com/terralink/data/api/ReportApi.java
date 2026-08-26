package com.terralink.data.api;

import com.terralink.data.model.PortfolioSummaryResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ReportApi {
    @GET("api/reports/portfolio-summary")
    Call<PortfolioSummaryResponse> getPortfolioSummary();
}
