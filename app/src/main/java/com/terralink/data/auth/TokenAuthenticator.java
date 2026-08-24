package com.terralink.data.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.terralink.data.api.AuthApi;
import com.terralink.data.api.RefreshApi;
import com.terralink.data.model.RefreshTokenRequest;
import com.terralink.data.model.RefreshTokenResponse;
import com.terralink.ui.auth.TokenManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import retrofit2.Call;
import retrofit2.Retrofit;

public class TokenAuthenticator implements Authenticator {
    private final TokenManager tokenManager;
    private final RefreshApi refreshApi;

    @Inject
    public TokenAuthenticator(TokenManager tokenManager, RefreshApi refreshApi){
        this.tokenManager = tokenManager;
        this.refreshApi = refreshApi;
    }

    @Nullable
    @Override
    public Request authenticate(
            @Nullable Route route,
            @NonNull Response response) throws IOException {

        if (responseCount(response) >= 2)
            return null;


        String refreshToken = tokenManager.getRefreshToken();
        if(refreshToken ==null || refreshToken.isEmpty()){
            tokenManager.clearTokens();
            return null;
        }

        Call<RefreshTokenResponse> call = refreshApi.refreshToken(
                new RefreshTokenRequest(refreshToken)
        );

        retrofit2.Response<RefreshTokenResponse> refreshResponse = call.execute(); // its okay to make a network request since we are inside Authenticator not main thread

        if(!refreshResponse.isSuccessful() || refreshResponse.body() == null){
            tokenManager.clearTokens();
            return null;
        }

        RefreshTokenResponse tokens = refreshResponse.body();
        tokenManager.saveTokens(
                tokens.getAccessToken(),
                tokens.getRefreshToken()
        );

        return response.request()
                .newBuilder()
                .header(
                        "Authorization",
                        "Bearer "+tokens.getAccessToken()
                ).build();
    }
    private int responseCount(Response response) {

        int count = 1;

        while ((response = response.priorResponse()) != null) {
            count++;
        }

        return count;
    }
}
