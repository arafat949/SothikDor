package com.sothikdor.app;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;

public class SothikDorApp extends Application {

    private static final String TAG = "SothikDorApp";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseDatabase.getInstance(
                "https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app"
            ).setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            // অফলাইন cache চালু করা যায়নি — অ্যাপ অনলাইনে কাজ করবে
            Log.w(TAG, "Could not enable Firebase offline persistence", e);
        }
    }
}
