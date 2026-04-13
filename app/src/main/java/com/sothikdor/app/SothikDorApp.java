package com.sothikdor.app;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class SothikDorApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance(
            "https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).setPersistenceEnabled(true);
    }
}
