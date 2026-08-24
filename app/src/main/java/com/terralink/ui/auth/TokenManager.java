package com.terralink.ui.auth;
import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "terralink_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private final SharedPreferences preferences;

    public TokenManager(Context context) {
        preferences =context.getApplicationContext().getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }
    public void saveTokens(String accessToken, String refreshToken){
        preferences.edit().putString(
                KEY_ACCESS_TOKEN,
                accessToken
        ).putString(
                KEY_REFRESH_TOKEN,
                refreshToken
        ).apply();
    }

    public String getAccessToken(){
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken(){
        return preferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean hasRefreshToken(){
        String token = getRefreshToken();

        return token !=null && !token.isEmpty();
    }

    public void clearTokens(){
        preferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }

    public boolean hasSession() {
        return hasRefreshToken();
    }
}
