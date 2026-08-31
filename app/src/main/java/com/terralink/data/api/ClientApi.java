package com.terralink.data.api;

import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.CreateAssetRequest;
import com.terralink.data.model.CreateIncomeAssessmentRequest;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.VerifyAssetRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
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
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
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
    Call<Void> verifyClient(@Path("id") int id);

    @POST("api/clients/{id}/reject")
    Call<Void> rejectClient(
            @Path("id") int id,
            @Body com.terralink.data.model.VerificationRejectRequest request
    );

    @GET("api/clients/{id}/kyc-documents")
    Call<List<com.terralink.data.model.KycDocumentResponse>> getKycDocuments(@Path("id") int id);

    @GET("api/clients/{clientId}/assets")
    Call<List<AssetResponse>> getClientAssets(@Path("clientId") long clientId);

    @POST("api/clients/{clientId}/assets")
    Call<AssetResponse> createAsset(@Path("clientId") long clientId, @Body CreateAssetRequest request);

    @POST("api/clients/{clientId}/assets/{assetId}/verify")
    Call<Void> verifyAsset(@Path("clientId") long clientId, @Path("assetId") long assetId, @Body VerifyAssetRequest request);

    @POST("api/clients/{clientId}/assets/{assetId}/reject")
    Call<Void> rejectAsset(@Path("clientId") long clientId, @Path("assetId") long assetId);

    @GET("api/clients/{clientId}/income-assessments")
    Call<List<IncomeAssessmentResponse>> getIncomeAssessments(@Path("clientId") long clientId);

    @POST("api/clients/{clientId}/income-assessments")
    Call<IncomeAssessmentResponse> createIncomeAssessment(@Path("clientId") long clientId, @Body CreateIncomeAssessmentRequest request);

    @POST("api/clients/{clientId}/income-assessments/{assessmentId}/verify")
    Call<Void> verifyIncomeAssessment(@Path("clientId") long clientId, @Path("assessmentId") long assessmentId);

    @POST("api/clients/{clientId}/income-assessments/{assessmentId}/reject")
    Call<Void> rejectIncomeAssessment(@Path("clientId") long clientId, @Path("assessmentId") long assessmentId);
}
