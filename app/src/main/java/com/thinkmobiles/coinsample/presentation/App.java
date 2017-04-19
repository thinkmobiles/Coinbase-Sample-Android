package com.thinkmobiles.coinsample.presentation;

import android.app.Application;


public class App extends Application {

    private static  App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SharedPref.init(this);
    }
}
