package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.AuthApi;
import com.terralink.data.api.UserApi;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.ui.common.Resource;

import com.terralink.data.model.RefreshTokenRequest;
import com.terralink.ui.auth.TokenManager;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;
    private final AuthApi authApi;
    private final TokenManager tokenManager;

    @Inject
    public UserRepository(UserApi userApi, AuthApi authApi, TokenManager tokenManager){
        this.userApi = userApi;
        this.authApi = authApi;
        this.tokenManager = tokenManager;
    }

    public LiveData<Resource<Void>> logout() {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) {
            result.postValue(Resource.success(null));
            return result;
        }

        authApi.logout(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.postValue(Resource.success(null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.success(null)); // Success even on failure to clear local
            }
        });
        return result;
    }
    public LiveData<Resource<UserProfileResponse >> getMe(){
        MutableLiveData<Resource<UserProfileResponse>> result = new MutableLiveData<>();

        result.setValue(Resource.loading());

        userApi.getMe().enqueue(
                new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(
                                    Resource.success(
                                            response.body()
                                    )
                            );
                        }else {
                            result.postValue(
                                    Resource.error("Request failed: "+response.code())
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        result.postValue(
                                Resource.error("Network Error: "+ t.getMessage())
                        );
                    }
                }
        );

        return result;

    }
}
