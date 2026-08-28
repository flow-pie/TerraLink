package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.NotificationApi;
import com.terralink.data.model.NotificationResponse;
import com.terralink.data.model.PaginatedResponse;
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

        // Pass null for isRead to fetch both read and unread notifications
        notificationApi.getNotifications(null, 1, 50).enqueue(
                new Callback<PaginatedResponse<NotificationResponse>>() {
                    @Override
                    public void onResponse(Call<PaginatedResponse<NotificationResponse>> call, Response<PaginatedResponse<NotificationResponse>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            result.postValue(Resource.success(response.body().getItems()));
                        }else{
                            result.postValue(Resource.error("Request Failed: "+response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<PaginatedResponse<NotificationResponse>> call, Throwable t) {
                        result.postValue(Resource.error("Network Error: "+t.getMessage()));
                    }
                }
        );

        return result;
    }

    public LiveData<Resource<Void>> markAsRead(long id) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        notificationApi.markAsRead(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Failed to mark as read"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
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
