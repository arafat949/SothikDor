package com.sothikdor.app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class AuthUtils {

    private static final String ADMIN_EMAIL = "mdarafatmiah949@gmail.com";

    private AuthUtils() {
    }

    public static boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static boolean isAdmin() {
        return isAdmin(FirebaseAuth.getInstance().getCurrentUser());
    }

    public static boolean isAdmin(FirebaseUser user) {
        if (user == null || user.isAnonymous()) {
            return false;
        }
        String email = user.getEmail();
        return email != null && email.equalsIgnoreCase(ADMIN_EMAIL);
    }
}
