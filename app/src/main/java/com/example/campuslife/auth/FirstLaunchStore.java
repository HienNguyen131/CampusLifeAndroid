package com.example.campuslife.auth;

import android.content.Context;

public class FirstLaunchStore {

    private static final String PREF = "app_pref";
    private static final String KEY_FIRST = "first_launch";

    public static boolean isFirstLaunch(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_FIRST, true);
    }

    public static void setFirstLaunchDone(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_FIRST, false).apply();
    }
}
