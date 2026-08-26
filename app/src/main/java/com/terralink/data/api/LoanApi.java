package com.terralink.data.api;

import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.CreditHistoryResponse;
import com.terralink.data.model.CreditScoreResponse;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.data.model.LoanApplicationRequest;
import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.data.model.LoanAppraisalDetailResponse;
import com.terralink.data.model.LoanAppraisalRequest;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.LoanListItemResponse;
import com.terralink.data.model.LoanProductResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.RepaymentInstallments;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoanApi {
    @GET("api/loans/clients/{clientId}/loans")
    Call<List<ClientLoansResponse>> getClientLoans(
            @Path("clientId") String clientId
    );

    @GET("api/clients/{clientId}/credit-score")
    Call<CreditScoreResponse> getCreditScore(
            @Path("clientId") String clientId
    );

    @GET("api/loans/{loanId}")
    Call<LoanDetailsResponse> getloanDetails(
            @Path("loanId") String loanId
    );

    @GET("api/loans/{loanId}/repayment-schedule")
    Call<List<RepaymentInstallments>> getRepaymentInstallments(
            @Path("loanId") String loanId
    );

    @POST("api/loan-applications")
    Call<Void> createLoanApplication(
            @Body LoanApplicationRequest request
    );

    @GET("api/loan-applications")
    Call<PaginatedResponse<LoanApplicationResponse>> getLoanApplications(
            @Query("status") String status,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("api/loan-applications/{id}")
    Call<LoanAppraisalDetailResponse> getLoanApplicationDetail(
            @Path("id") int id
    );

    @POST("api/loan-applications/{id}/appraise")
    Call<Void> appraiseLoanApplication(
            @Path("id") int applicationId,
            @Body LoanAppraisalRequest request
    );

    @GET("api/clients/{clientId}/credit-history")
    Call<List<CreditHistoryResponse>> getCreditHistory(
            @Path("clientId") String clientId
    );

    @GET("api/clients/{clientId}/assets")
    Call<List<AssetResponse>> getClientAssets(
            @Path("clientId") String clientId
    );

    @GET("api/clients/{clientId}/income-assessments")
    Call<List<IncomeAssessmentResponse>> getIncomeAssessments(
            @Path("clientId") String clientId
    );

    @GET("api/loan-products")
    Call<List<LoanProductResponse>> getLoanProducts(
            @Query("includeInactive") boolean includeInactive
    );

    @GET("api/loans")
    Call<List<LoanListItemResponse>> getLoans(
            @Query("status") String status,
            @Query("search") String search,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );
}
