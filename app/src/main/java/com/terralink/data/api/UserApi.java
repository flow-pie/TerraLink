package com.terralink.data.api;

import com.terralink.data.model.UserProfileResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApi {
    @GET("api/users/me")
    Call<UserProfileResponse> getMe();
}
