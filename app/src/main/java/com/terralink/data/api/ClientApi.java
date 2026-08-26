package com.terralink.data.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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
}
