package com.sothikdor.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SothikDorSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_PREFERRED_MARKET_ID = "preferred_market_id";
    private static final String KEY_PREFERRED_MARKET_NAME = "preferred_market_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private static SessionManager instance;

    private SessionManager(Context context) {
        pref = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveLogin(String userId, String name, String phone, boolean isAdmin) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }

    public void savePreferredMarket(String marketId, String marketName) {
        editor.putString(KEY_PREFERRED_MARKET_ID, marketId);
        editor.putString(KEY_PREFERRED_MARKET_NAME, marketName);
        editor.apply();
    }

    public void setDarkMode(boolean isDark) {
        editor.putBoolean(KEY_DARK_MODE, isDark);
        editor.apply();
    }

    public boolean isLoggedIn() { return pref.getBoolean(KEY_IS_LOGGED_IN, false); }
    public String getUserId() { return pref.getString(KEY_USER_ID, ""); }
    public String getUserName() { return pref.getString(KEY_USER_NAME, ""); }
    public String getUserPhone() { return pref.getString(KEY_USER_PHONE, ""); }
    public String getPreferredMarketId() { return pref.getString(KEY_PREFERRED_MARKET_ID, ""); }
    public String getPreferredMarketName() { return pref.getString(KEY_PREFERRED_MARKET_NAME, "কারওয়ান বাজার"); }
    public boolean isAdmin() { return pref.getBoolean(KEY_IS_ADMIN, false); }
    public boolean isDarkMode() { return pref.getBoolean(KEY_DARK_MODE, false); }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
