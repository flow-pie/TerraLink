package com.terralink.di;

import android.content.Context;

import com.google.gson.Gson;
import com.terralink.data.api.AuthApi;
import com.terralink.data.api.LoanApi;
import com.terralink.data.api.RefreshApi;
import com.terralink.data.api.UserApi;
import com.terralink.data.auth.TokenAuthenticator;
import com.terralink.ui.auth.AuthInterceptor;
import com.terralink.ui.auth.TokenManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//create a module container for our instructions which belong to singleton service lifetime in our dependancy graph
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

//    Someone needs TokenManager
//        ↓
//    Hilt calls provideTokenManager()
//        ↓
//    new TokenManager(context)
//        ↓
//    TokenManager
    @Provides
    @Singleton
    public TokenManager provideTokenManager(
            @ApplicationContext Context context
    ){
        return new TokenManager(context);
    }

    //Auth interceptor object
    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(
            TokenManager tokenManager
    ){
        return new AuthInterceptor(tokenManager);
    }

    //provide okhttp
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(
            AuthInterceptor authInterceptor,
            TokenAuthenticator tokenAuthenticator
    ){
        return  new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .authenticator(tokenAuthenticator)
                .build();
    }
    //provide retrofit
    @Provides
    @Singleton
    public Retrofit provideRetrofit(
            OkHttpClient okHttpClient
    ){
        return new Retrofit.Builder()
                .baseUrl("http://192.168.0.104:5031/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    //refresh token retrofit object
    @Provides
    @Singleton
    @AuthRetrofit
    public Retrofit provideAuthRetrofit(
            Gson gson
    ) {

        return new Retrofit.Builder()
                .baseUrl("http://192.168.0.104:5031/")
                .addConverterFactory(
                        GsonConverterFactory.create(gson)
                )
                .build();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    //provide AuthAPi
    @Provides
    @Singleton
    public AuthApi provideAuthApi(
            Retrofit retrofit
    ){
        return retrofit.create(AuthApi.class);
    }

    @Provides
    @Singleton
    public RefreshApi provideRefreshApi(
            @AuthRetrofit Retrofit retrofit
    ) {

        return retrofit.create(RefreshApi.class);
    }

    @Provides
    @Singleton
    public UserApi provideUserApi(
            Retrofit retrofit
    ){
        return retrofit.create(UserApi.class);
    }

    @Provides
    @Singleton
    public LoanApi provideLoanApi(
            Retrofit retrofit
    ){
        return retrofit.create(LoanApi.class);
    }
}
