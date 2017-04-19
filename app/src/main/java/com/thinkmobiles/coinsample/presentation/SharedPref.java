package com.thinkmobiles.coinsample.presentation;


import android.content.Context;
import android.content.SharedPreferences;

import com.coinbase.api.entity.OAuthTokensResponse;


public class SharedPref  {

    private static final String PREF_NAME = "Wallet";

    private static SharedPref instance;

    private SharedPreferences preferences;

    public static SharedPref getInstance() {
        return instance;
    }

    public static void init(Context context) {
        instance = new SharedPref(context);
    }

    private SharedPref(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveOAuthResponse(OAuthTokensResponse response) {
        preferences.edit()
                .putString("access", response.getAccessToken())
                .apply();
        preferences.edit()
                .putString("refresh", response.getRefreshToken())
                .apply();
    }

    public String getAccessToken() {
        return preferences.getString("access", null);
    }

    public String getRefreshToken() {
        return preferences.getString("refresh", null);
    }

    public void clearTokens() {
        preferences.edit()
                .putString("access", null)
                .apply();
        preferences.edit()
                .putString("refresh", null)
                .apply();
    }
}
