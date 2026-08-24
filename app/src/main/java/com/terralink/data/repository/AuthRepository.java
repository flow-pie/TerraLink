package com.terralink.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.AuthApi;
import com.terralink.data.model.LoginRequest;
import com.terralink.data.model.LoginResponse;
import com.terralink.ui.auth.LoginResult;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.auth.TokenManager;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final AuthApi authApi;
    private final TokenManager tokenManager;

    //@inject tell hilt how to create AuthRepository object
    @Inject
    public AuthRepository(AuthApi authApi, TokenManager tokenManager){

        this.authApi = authApi;
        this.tokenManager = tokenManager;
    }

    public LiveData< Resource<LoginResponse> > login(
            LoginRequest request
    ){
        MutableLiveData< Resource<LoginResponse> > result = new MutableLiveData<>();

        result.setValue(Resource.loading());

        Log.d("LOGIN", "about to enqueue request");

        //use asynchronous execution to avoid performing a network request directly on main thread
        authApi.login(request).enqueue(
                new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(
                            Call<LoginResponse> call,
                            Response<LoginResponse> response
                    ) {
                        //unlike setValue which must called on Main Thread, postValue execute on background thread
                        if(response.isSuccessful() && response.body() != null){

                            LoginResponse loginResponse = response.body();
                            tokenManager.saveTokens(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
                            result.postValue( Resource
                                    .success(loginResponse)
                            );

                        }else {

                            result.postValue(Resource.error("Login Failed"+ response.code()));
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<LoginResponse> call,
                            Throwable t
                    ) {
                        Log.e("LOGIN", "onFailure", t);
                        result.postValue(Resource.error("Network Error: "+t.getMessage()) );
                    }
                }
        );

        return  result;
    }
}
