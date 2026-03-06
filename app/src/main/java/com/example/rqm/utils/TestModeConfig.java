package com.example.rqm.utils;

import android.content.Context;

import com.example.rqm.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestModeConfig {

    public final boolean enabled;
    public final String matricula;
    public final String userId;
    public final String role;
    public final String tenantId;
    public final String imei;

    private TestModeConfig(boolean enabled, String matricula, String userId,
                           String role, String tenantId, String imei) {
        this.enabled = enabled;
        this.matricula = matricula;
        this.userId = userId;
        this.role = role;
        this.tenantId = tenantId;
        this.imei = imei;
    }

    public static TestModeConfig load(Context context) {
        try (InputStream in = context.getResources().openRawResource(R.raw.supabase_config)) {
            byte[] bytes = new byte[in.available()];
            int read = in.read(bytes);
            if (read <= 0) {
                return disabled();
            }

            String json = new String(bytes, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("test_mode") || !root.get("test_mode").isJsonObject()) {
                return disabled();
            }

            JsonObject test = root.getAsJsonObject("test_mode");
            return new TestModeConfig(
                    test.has("enabled") && test.get("enabled").getAsBoolean(),
                    getString(test, "matricula", "TESTE"),
                    getString(test, "user_id", "test-user"),
                    getString(test, "role", "admin"),
                    getString(test, "tenant_id", "tenant-teste"),
                    getString(test, "imei", "IMEI-TESTE")
            );
        } catch (Exception ignored) {
            return disabled();
        }
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        return obj.has(key) ? obj.get(key).getAsString() : fallback;
    }

    private static TestModeConfig disabled() {
        return new TestModeConfig(false, "", "", "", "", "");
    }
}
