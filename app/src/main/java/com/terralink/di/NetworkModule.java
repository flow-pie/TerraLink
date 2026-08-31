package com.terralink.di;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.terralink.data.api.AuthApi;
import com.terralink.data.api.LoanApi;
import com.terralink.data.api.RefreshApi;
import com.terralink.data.api.UserApi;
import com.terralink.data.auth.TokenAuthenticator;
import com.terralink.ui.auth.AuthInterceptor;
import com.terralink.ui.auth.TokenManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            OkHttpClient okHttpClient,
            Gson gson
    ){
        return new Retrofit.Builder()
                .baseUrl("http://192.168.0.104:5031/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
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
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                    String dateStr = json.getAsString();
                    if (dateStr.contains(".")) {
                        // Handle ISO 8601 with fractional seconds
                        // SimpleDateFormat's S is milliseconds, so we truncate to 3 digits
                        int dotIndex = dateStr.lastIndexOf(".");
                        String base = dateStr.substring(0, dotIndex);
                        String frac = dateStr.substring(dotIndex + 1);
                        if (frac.length() > 3) frac = frac.substring(0, 3);
                        String formattedDate = base + "." + frac;
                        try {
                            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).parse(formattedDate);
                        } catch (ParseException e) {
                            // Fallback
                        }
                    }
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(dateStr);
                    } catch (ParseException e) {
                        try {
                            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr);
                        } catch (ParseException e2) {
                            return null;
                        }
                    }
                })
                .create();
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

    @Provides
    @Singleton
    public com.terralink.data.api.ClientApi provideClientApi(
            Retrofit retrofit
    ){
        return retrofit.create(com.terralink.data.api.ClientApi.class);
    }

    @Provides
    @Singleton
    public com.terralink.data.api.PaymentApi providePaymentApi(
            Retrofit retrofit
    ){
        return retrofit.create(com.terralink.data.api.PaymentApi.class);
    }

    @Provides
    @Singleton
    public com.terralink.data.api.NotificationApi provideNotificationApi(
            Retrofit retrofit
    ){
        return retrofit.create(com.terralink.data.api.NotificationApi.class);
    }

    @Provides
    @Singleton
    public com.terralink.data.api.ReportApi provideReportApi(
            Retrofit retrofit
    ){
        return retrofit.create(com.terralink.data.api.ReportApi.class);
    }
}
