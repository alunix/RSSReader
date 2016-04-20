package com.igordotsenko.dotsenkorssreader;

import android.app.Application;
import android.content.Context;

public class ReaderApplication extends Application {
    public static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = getApplicationContext();
    }
}
