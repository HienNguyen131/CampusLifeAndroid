package com.example.campuslife.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {

    private static final String PREF = "auth_prefs";
    private static final String KEY_ACCESS = "jwt_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_STUDENT_ID = "student_id";

    public static void saveStudentId(Context context, long id) {
        getSP(context).edit().putLong(KEY_STUDENT_ID, id).apply();
    }

    public static long getStudentId(Context context) {
        return getSP(context).getLong(KEY_STUDENT_ID, -1);
    }

    private static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // Access Token
    public static void saveToken(Context context, String token) {
        getSP(context).edit().putString(KEY_ACCESS, token).apply();
    }

    public static String getToken(Context context) {
        String v = getSP(context).getString(KEY_ACCESS, null);
        return (v == null || v.isEmpty()) ? null : v;
    }

    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }

    public static void clearToken(Context context) {
        getSP(context).edit().remove(KEY_ACCESS).apply();
    }

    // Refresh Token
    public static void saveRefreshToken(Context context, String refresh) {
        getSP(context).edit().putString(KEY_REFRESH, refresh).apply();
    }

    public static String getRefreshToken(Context context) {
        return getSP(context).getString(KEY_REFRESH, null);
    }

    // Username
    public static void saveUsername(Context context, String username) {
        getSP(context).edit().putString(KEY_USERNAME, username).apply();
    }

    public static String getUsername(Context context) {
        return getSP(context).getString(KEY_USERNAME, null);
    }

    // Clear all
    public static void clearAll(Context context) {
        getSP(context).edit()
                .remove(KEY_ACCESS)
                .remove(KEY_REFRESH)
                .remove(KEY_USERNAME)
                .apply();
    }
}
