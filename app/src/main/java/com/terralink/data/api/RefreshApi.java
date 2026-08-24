package com.terralink.data.api;

import com.terralink.data.model.RefreshTokenRequest;
import com.terralink.data.model.RefreshTokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RefreshApi {

    @POST("api/auth/refresh")
    Call<RefreshTokenResponse> refreshToken(
            @Body RefreshTokenRequest request
    );
}