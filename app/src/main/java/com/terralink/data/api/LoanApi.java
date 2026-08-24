package com.terralink.data.api;

import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.RepaymentInstallments;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LoanApi {
    @GET("api/loans/clients/{clientId}/loans")
    Call<List<ClientLoansResponse>> getClientLoans(
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
}
