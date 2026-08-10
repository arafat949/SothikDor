package com.sothikdor.app;

import android.app.Application;
import com.sothikdor.app.utils.FirebaseHelper;

public class SothikDorApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseHelper.getDatabase().setPersistenceEnabled(true);
    }
}
