package com.terralink.ui.auth;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//Interceptor can inspect, modify and pass them along and should rarely stop a request.
public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager){
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = tokenManager.getAccessToken();

        Request request = chain.request();
        if(token == null || token.isEmpty())
            return chain.proceed(request);

        Request authenticatedUser = request.newBuilder()
                .addHeader(
                        "Authorization",
                        "Bearer " + token
                ).build();

        return chain.proceed(authenticatedUser);

    }
}
