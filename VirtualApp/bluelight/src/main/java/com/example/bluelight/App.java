package com.example.bluelight;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static App instance;

    public App() {
        super();
    }

    public static Context getContext() {
        return App.instance.getApplicationContext();
    }

    public void onCreate() {
        super.onCreate();
        App.instance = this;
    }
}

