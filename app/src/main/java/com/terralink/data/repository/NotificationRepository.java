package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.NotificationApi;
import com.terralink.data.model.NotificationResponse;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {
    private final NotificationApi notificationApi;

    @Inject
    public NotificationRepository(NotificationApi notificationApi){
        this.notificationApi = notificationApi;
    }

    public LiveData<Resource<List<NotificationResponse>>> getNotifications(){
        MutableLiveData<Resource<List<NotificationResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        notificationApi.getNotifications(false, 1, 20).enqueue(
                new Callback<List<NotificationResponse>>() {
                    @Override
                    public void onResponse(Call<List<NotificationResponse>> call, Response<List<NotificationResponse>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(Resource.success(response.body()));
                        }else{
                            result.postValue(Resource.error("Request Failed: "+response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }

    public LiveData<Resource<Void>> markAllAsRead(){
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        notificationApi.markAllAsRead().enqueue(
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(response.isSuccessful()){
                            result.postValue(Resource.success(null));
                        }else{
                            result.postValue(Resource.error("Request Failed: "+response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }
}
