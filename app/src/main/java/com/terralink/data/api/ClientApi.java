package com.terralink.data.api;

import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.PaginatedResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ClientApi {

    @Multipart
    @POST("api/clients/register")
    Call<Void> registerClient(
            @Part("fullName") RequestBody fullName,
            @Part("nationalId") RequestBody nationalId,
            @Part("phone") RequestBody phone,
            @Part("dateOfBirth") RequestBody dateOfBirth,
            @Part("gender") RequestBody gender,
            @Part("address") RequestBody address,
            @Part MultipartBody.Part nationalIdFront,
            @Part MultipartBody.Part nationalIdBack,
            @Part MultipartBody.Part passportPhoto
    );

    @GET("api/clients")
    Call<PaginatedResponse<ClientListItemResponse>> getClients(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("search") String search
    );

    @POST("api/clients/{id}/verify")
    Call<Void> verifyClient(@retrofit2.http.Path("id") int id);

    @POST("api/clients/{id}/reject")
    Call<Void> rejectClient(
            @retrofit2.http.Path("id") int id,
            @retrofit2.http.Body com.terralink.data.model.VerificationRejectRequest request
    );

    @GET("api/clients/{id}/kyc-documents")
    Call<java.util.List<com.terralink.data.model.KycDocumentResponse>> getKycDocuments(@retrofit2.http.Path("id") int id);
}
