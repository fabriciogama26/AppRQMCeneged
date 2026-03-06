package com.example.rqm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class SupabasePrefs {

    public static final int STATUS_DISCONNECTED = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_CONNECTED = 2;

    private static final String PREFS = "supabase_prefs";
    private static final String KEY_URL = "url";
    private static final String KEY_ANON = "anon_key";
    private static final String KEY_STATUS = "status";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void saveConfig(Context context, String url, String anonKey) {
        prefs(context).edit()
                .putString(KEY_URL, url)
                .putString(KEY_ANON, anonKey)
                .apply();
    }

    public static String getUrl(Context context) {
        return prefs(context).getString(KEY_URL, "");
    }

    public static String getAnonKey(Context context) {
        return prefs(context).getString(KEY_ANON, "");
    }

    public static void clearConfig(Context context) {
        prefs(context).edit()
                .remove(KEY_URL)
                .remove(KEY_ANON)
                .apply();
    }

    public static boolean hasConfig(Context context) {
        return !TextUtils.isEmpty(getUrl(context)) && !TextUtils.isEmpty(getAnonKey(context));
    }

    public static void setStatus(Context context, int status) {
        prefs(context).edit()
                .putInt(KEY_STATUS, status)
                .apply();
    }

    public static int getStatus(Context context) {
        return prefs(context).getInt(KEY_STATUS, STATUS_DISCONNECTED);
    }
}
