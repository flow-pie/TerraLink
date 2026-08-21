package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.AuthApi;
import com.terralink.data.api.UserApi;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;

    @Inject
    public UserRepository(UserApi userApi){
        this.userApi = userApi;
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
