package com.example.rqm.utils;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ErrorLogger {

    private static final String PREFS = "error_logs";
    private static final String KEY_PENDING = "pending";

    public static void logCrash(Context context, Thread thread, Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        JsonObject payload = basePayload(context);
        payload.addProperty("severity", "FATAL");
        payload.addProperty("message", throwable.getMessage() != null ? throwable.getMessage() : "Crash");
        payload.addProperty("stacktrace", sw.toString());
        payload.addProperty("thread", thread.getName());
        enqueue(context, payload);
    }

    public static void logError(Context context, String message, String stack, String severity, String screen) {
        JsonObject payload = basePayload(context);
        payload.addProperty("severity", severity);
        payload.addProperty("message", message);
        payload.addProperty("stacktrace", stack);
        payload.addProperty("screen", screen);
        enqueue(context, payload);
    }

    public static void flushPending(Context context) {
        List<JsonObject> pending = getPending(context);
        if (pending.isEmpty()) return;

        for (JsonObject obj : pending) {
            SupabaseEdgeClient.logError(context, obj);
        }

        clearPending(context);
    }

    private static JsonObject basePayload(Context context) {
        JsonObject payload = new JsonObject();
        payload.addProperty("user_id", AuthPrefs.getUserId(context));
        payload.addProperty("matricula", AuthPrefs.getMatricula(context));
        payload.addProperty("tenant_id", AuthPrefs.getTenantId(context));
        payload.addProperty("source", "APP");
        payload.addProperty("device_imei", AuthPrefs.getImei(context));
        return payload;
    }

    private static void enqueue(Context context, JsonObject payload) {
        List<JsonObject> pending = getPending(context);
        pending.add(payload);
        JsonArray arr = new JsonArray();
        for (JsonObject obj : pending) {
            arr.add(obj);
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PENDING, arr.toString())
                .apply();
    }

    private static List<JsonObject> getPending(Context context) {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PENDING, "");
        List<JsonObject> list = new ArrayList<>();
        if (TextUtils.isEmpty(raw)) return list;
        try {
            JsonArray arr = JsonParser.parseString(raw).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                list.add(arr.get(i).getAsJsonObject());
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private static void clearPending(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_PENDING)
                .apply();
    }
}
