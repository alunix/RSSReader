package com.igordotsenko.dotsenkorssreader;

import android.app.Application;
import android.content.Context;

public class ReaderApplication extends Application {
    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }
}
