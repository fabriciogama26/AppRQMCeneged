package com.example.rqm;

import android.app.Application;

import com.example.rqm.utils.AuthPrefs;
import com.example.rqm.utils.ErrorLogger;

public class RQMApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                ErrorLogger.logCrash(getApplicationContext(), thread, throwable);
                AuthPrefs.markPendingLogout(getApplicationContext(), "CRASH");
            } catch (Exception ignored) {
            }

            Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
