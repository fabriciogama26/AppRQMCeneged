package com.example.rqm.utils;

import android.content.Context;
import android.text.TextUtils;

import com.example.rqm.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SupabaseConfigLoader {

    public static void ensureConfig(Context context) {
        if (!TextUtils.isEmpty(SupabasePrefs.getUrl(context)) &&
                !TextUtils.isEmpty(SupabasePrefs.getAnonKey(context))) {
            return;
        }

        try (InputStream in = context.getResources().openRawResource(R.raw.supabase_config)) {
            byte[] bytes = new byte[in.available()];
            int read = in.read(bytes);
            if (read <= 0) return;
            String json = new String(bytes, StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String url = obj.has("url") ? obj.get("url").getAsString() : "";
            String anon = obj.has("anon_key") ? obj.get("anon_key").getAsString() : "";
            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(anon)) {
                SupabasePrefs.saveConfig(context, url, anon);
            }
        } catch (Exception ignored) {
        }
    }
}
