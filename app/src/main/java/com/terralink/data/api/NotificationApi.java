package com.terralink.data.api;

import com.terralink.data.model.NotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("api/notifications")
    Call<List<NotificationResponse>> getNotifications(
            @Query("isRead") Boolean isRead,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @PATCH("api/notifications/read-all")
    Call<Void> markAllAsRead();
}
