package com.terralink.ui.auth;
import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "terralink_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private final SharedPreferences preferences;

    public TokenManager(Context context) {
        preferences =context.getApplicationContext().getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }
    public void saveAccessToken(String token){
        preferences.edit().putString(
                KEY_ACCESS_TOKEN,
                token
        ).apply();
    }

    public String getAccessToken(){
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public void clearAccessToken(){
        preferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .apply();
    }
}
