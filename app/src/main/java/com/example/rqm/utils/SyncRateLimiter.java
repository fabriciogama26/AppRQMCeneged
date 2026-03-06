package com.example.rqm.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SyncRateLimiter {

    private static final String PREFS_NAME = "sync_rate_limiter";
    private static final String KEY_LAST_MANUAL_SYNC_AT = "last_manual_sync_at";
    private static final long MIN_INTERVAL_MS = 30000L;

    private SyncRateLimiter() {
    }

    public static boolean canStartManualSync(Context context) {
        return getRemainingMs(context) <= 0L;
    }

    public static long getRemainingMs(Context context) {
        long lastAttemptAt = getPrefs(context).getLong(KEY_LAST_MANUAL_SYNC_AT, 0L);
        long elapsed = System.currentTimeMillis() - lastAttemptAt;
        return Math.max(0L, MIN_INTERVAL_MS - elapsed);
    }

    public static int getRemainingSeconds(Context context) {
        long remainingMs = getRemainingMs(context);
        return (int) Math.ceil(remainingMs / 1000.0);
    }

    public static void markManualSyncStarted(Context context) {
        getPrefs(context).edit().putLong(KEY_LAST_MANUAL_SYNC_AT, System.currentTimeMillis()).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
