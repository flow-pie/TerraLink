package com.terralink.data.api;

import com.terralink.data.model.InitiatePaymentRequest;
import com.terralink.data.model.InitiatePaymentResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.PaymentHistoryResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentApi {

    @POST("api/payments/initiate")
    Call<InitiatePaymentResponse> initiatePayment(@Body InitiatePaymentRequest request);

    @GET("api/payments/{id}")
    Call<InitiatePaymentResponse> getPaymentStatus(@Path("id") long paymentId);

    @GET("api/payments/clients/{clientId}/payments")
    Call<PaginatedResponse<PaymentHistoryResponse>> getClientPayments(
            @Path("clientId") String clientId,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );
}
