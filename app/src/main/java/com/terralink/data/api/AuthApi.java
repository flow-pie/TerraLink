package com.terralink.data.api;

import com.terralink.data.model.LoginRequest;
import com.terralink.data.model.LoginResponse;
import com.terralink.data.model.RefreshTokenRequest;
import com.terralink.data.model.RefreshTokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/logout")
    Call<Void> logout(@Body RefreshTokenRequest request);
}
