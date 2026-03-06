package com.example.rqm.utils;

import android.content.Context;
import android.text.TextUtils;

import com.example.rqm.models.EstoqueItem;
import com.example.rqm.models.Material;
import com.example.rqm.models.ResponsavelItem;
import com.example.rqm.models.SyncRun;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SupabaseEdgeClient {

    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final String accessToken;
        public final String userId;
        public final String role;
        public final String tenantId;
        public final String loginAuditId;

        public LoginResult(boolean success, String message, String accessToken, String userId,
                           String role, String tenantId, String loginAuditId) {
            this.success = success;
            this.message = message;
            this.accessToken = accessToken;
            this.userId = userId;
            this.role = role;
            this.tenantId = tenantId;
            this.loginAuditId = loginAuditId;
        }
    }

    public static class SyncRunResult {
        public final boolean success;
        public final String message;
        public final int retryAfterSeconds;

        public SyncRunResult(boolean success, String message, int retryAfterSeconds) {
            this.success = success;
            this.message = message;
            this.retryAfterSeconds = retryAfterSeconds;
        }
    }

    public static class EstoqueResult {
        public final boolean success;
        public final String message;
        public final List<EstoqueItem> items;

        public EstoqueResult(boolean success, String message, List<EstoqueItem> items) {
            this.success = success;
            this.message = message;
            this.items = items;
        }
    }

    public static class MaterialCatalogResult {
        public final boolean success;
        public final String message;
        public final List<Material> items;

        public MaterialCatalogResult(boolean success, String message, List<Material> items) {
            this.success = success;
            this.message = message;
            this.items = items;
        }
    }

    public static class ResponsavelResult {
        public final boolean success;
        public final String message;
        public final List<ResponsavelItem> items;

        public ResponsavelResult(boolean success, String message, List<ResponsavelItem> items) {
            this.success = success;
            this.message = message;
            this.items = items;
        }
    }

    public static LoginResult login(Context context, String matricula, String senha, String imei, boolean skipImeiCheck) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon)) {
            return new LoginResult(false, "Configuracao do Supabase ausente.", "", "", "", "", "");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("matricula", matricula);
        payload.addProperty("senha", senha);
        payload.addProperty("imei", imei);
        payload.addProperty("source", "APP");
        payload.addProperty("skip_imei_check", skipImeiCheck);

        JsonObject response = postJson(baseUrl + "/functions/v1/login_matricula", anon, "", payload);
        if (response == null) {
            return new LoginResult(false, "Falha de conexao.", "", "", "", "", "");
        }

        boolean success = response.has("success") && response.get("success").getAsBoolean();
        String message = response.has("message") ? response.get("message").getAsString() : "";
        if (!success) {
            return new LoginResult(false, TextUtils.isEmpty(message) ? "Login invalido." : message,
                    "", "", "", "", "");
        }

        String token = response.has("access_token") ? response.get("access_token").getAsString() : "";
        String userId = response.has("user_id") ? response.get("user_id").getAsString() : "";
        String role = response.has("role") ? response.get("role").getAsString() : "";
        String tenantId = response.has("tenant_id") ? response.get("tenant_id").getAsString() : "";
        String auditId = response.has("login_audit_id") ? response.get("login_audit_id").getAsString() : "";
        return new LoginResult(true, message, token, userId, role, tenantId, auditId);
    }

    public static EstoqueResult getEstoque(Context context, String projeto, String codigo, String descricao,
                                           String qtyExact, String qtyMin, String qtyMax, int limit, int offset) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        String accessToken = AuthPrefs.getAccessToken(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon) || TextUtils.isEmpty(accessToken)) {
            return new EstoqueResult(false, "Supabase nao configurado para consultar estoque.", new ArrayList<>());
        }

        JsonObject payload = new JsonObject();
        if (!TextUtils.isEmpty(projeto)) payload.addProperty("projeto", projeto);
        if (!TextUtils.isEmpty(codigo)) payload.addProperty("codigo", codigo);
        if (!TextUtils.isEmpty(descricao)) payload.addProperty("descricao", descricao);
        if (!TextUtils.isEmpty(qtyExact)) payload.addProperty("qty_exact", qtyExact);
        if (!TextUtils.isEmpty(qtyMin)) payload.addProperty("qty_min", qtyMin);
        if (!TextUtils.isEmpty(qtyMax)) payload.addProperty("qty_max", qtyMax);
        payload.addProperty("limit", limit);
        payload.addProperty("offset", offset);

        String endpoint = TextUtils.isEmpty(projeto)
                ? "/functions/v1/get_inventory_balance"
                : "/functions/v1/get_project_material_balance";

        JsonObject response = postJson(baseUrl + endpoint, anon, accessToken, payload);
        if (response == null) {
            return new EstoqueResult(false, "Falha ao consultar estoque.", new ArrayList<>());
        }

        boolean success = response.has("success") && response.get("success").getAsBoolean();
        String message = response.has("message") ? response.get("message").getAsString() : "";
        if (!success) {
            return new EstoqueResult(false, TextUtils.isEmpty(message) ? "Falha ao consultar estoque." : message, new ArrayList<>());
        }

        List<EstoqueItem> items = new ArrayList<>();
        JsonArray array = response.has("items") && response.get("items").isJsonArray()
                ? response.getAsJsonArray("items") : new JsonArray();

        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            JsonObject material = obj.has("materials") && obj.get("materials").isJsonObject()
                    ? obj.getAsJsonObject("materials") : new JsonObject();

            EstoqueItem item = new EstoqueItem();
            item.projectScope = !TextUtils.isEmpty(projeto);
            item.projeto = getString(obj, "projeto");
            item.codigo = getString(material, "codigo");
            item.descricao = getString(material, "descricao");
            item.umb = getString(material, "umb");
            item.tipo = getString(material, "tipo");
            item.updatedAt = getString(obj, "updated_at");
            if (item.projectScope) {
                item.quantidade = getDouble(obj, "qty_net");
                item.qtyIssued = getDouble(obj, "qty_issued");
                item.qtyReturned = getDouble(obj, "qty_returned");
            } else {
                item.quantidade = getDouble(obj, "qty_on_hand");
            }
            items.add(item);
        }

        return new EstoqueResult(true, message, items);
    }

    public static MaterialCatalogResult getMateriais(Context context) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        String accessToken = AuthPrefs.getAccessToken(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon) || TextUtils.isEmpty(accessToken)) {
            return new MaterialCatalogResult(false, "Supabase nao configurado para consultar materiais.", new ArrayList<>());
        }

        JsonObject response = postJson(baseUrl + "/functions/v1/get_materials", anon, accessToken, new JsonObject());
        if (response == null) {
            return new MaterialCatalogResult(false, "Falha ao consultar materiais.", new ArrayList<>());
        }

        boolean success = response.has("success") && response.get("success").getAsBoolean();
        String message = response.has("message") ? response.get("message").getAsString() : "";
        if (!success) {
            return new MaterialCatalogResult(false, TextUtils.isEmpty(message) ? "Falha ao consultar materiais." : message, new ArrayList<>());
        }

        List<Material> items = new ArrayList<>();
        JsonArray array = response.has("items") && response.get("items").isJsonArray()
                ? response.getAsJsonArray("items") : new JsonArray();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            Material item = new Material();
            item.codigo = getString(obj, "codigo");
            item.descricao = getString(obj, "descricao");
            item.umb = getString(obj, "umb");
            item.tipo = getString(obj, "tipo");
            item.lp = getString(obj, "lp");
            item.serial = getString(obj, "serial");
            item.valor_unitario = getString(obj, "valor_unitario");
            items.add(item);
        }
        return new MaterialCatalogResult(true, message, items);
    }

    public static ResponsavelResult getResponsaveis(Context context) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        String accessToken = AuthPrefs.getAccessToken(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon) || TextUtils.isEmpty(accessToken)) {
            return new ResponsavelResult(false, "Supabase nao configurado para consultar responsaveis.", new ArrayList<>());
        }

        JsonObject response = postJson(baseUrl + "/functions/v1/get_responsaveis", anon, accessToken, new JsonObject());
        if (response == null) {
            return new ResponsavelResult(false, "Falha ao consultar responsaveis.", new ArrayList<>());
        }

        boolean success = response.has("success") && response.get("success").getAsBoolean();
        String message = response.has("message") ? response.get("message").getAsString() : "";
        if (!success) {
            return new ResponsavelResult(false, TextUtils.isEmpty(message) ? "Falha ao consultar responsaveis." : message, new ArrayList<>());
        }

        List<ResponsavelItem> items = new ArrayList<>();
        JsonArray array = response.has("items") && response.get("items").isJsonArray()
                ? response.getAsJsonArray("items") : new JsonArray();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            ResponsavelItem item = new ResponsavelItem();
            item.id = getString(obj, "id");
            item.nome = getString(obj, "nome");
            item.cargo = getString(obj, "cargo");
            items.add(item);
        }
        return new ResponsavelResult(true, message, items);
    }

    public static SyncRunResult sendSyncRun(Context context, SyncRun run) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        String accessToken = AuthPrefs.getAccessToken(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon) || TextUtils.isEmpty(accessToken)) {
            return new SyncRunResult(false, "Supabase nao configurado para sincronizacao.", 0);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("sync_uuid", run.syncUuid);
        payload.addProperty("tenant_id", safe(run.tenantId));
        payload.addProperty("user_id", safe(run.userId));
        payload.addProperty("device_id", safe(run.deviceId));
        payload.addProperty("source", safe(run.source));
        payload.addProperty("trigger_type", "MANUAL");
        payload.addProperty("started_at", safe(run.startedAt));
        payload.addProperty("finished_at", safe(run.finishedAt));
        payload.addProperty("status", safe(run.status));
        payload.addProperty("pending_total", run.pendingTotal);
        payload.addProperty("pending_sent", run.pendingSent);
        payload.addProperty("materials_updated", run.materialsUpdated);
        payload.addProperty("projects_updated", 0);
        payload.addProperty("balances_updated", 0);
        payload.addProperty("conflicts_found", run.conflictsFound);
        payload.addProperty("warnings_count", 0);
        payload.addProperty("errors_count", run.errorsCount);
        payload.addProperty("message", safe(run.message));
        payload.addProperty("app_version", obterVersaoApp(context));

        JsonObject response = postJson(baseUrl + "/functions/v1/sync_run", anon, accessToken, payload);
        if (response == null) {
            return new SyncRunResult(false, "Falha ao registrar sincronizacao no servidor.", 0);
        }

        boolean success = response.has("success") && response.get("success").getAsBoolean();
        String message = response.has("message") ? response.get("message").getAsString() : "";
        int retryAfter = response.has("retry_after") ? response.get("retry_after").getAsInt() : 0;
        return new SyncRunResult(success, message, retryAfter);
    }

    public static boolean verifyAdminPin(Context context, String userId, String pin) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon)) return false;

        JsonObject payload = new JsonObject();
        payload.addProperty("user_id", userId);
        payload.addProperty("pin", pin);
        payload.addProperty("source", "APP");

        JsonObject response = postJson(baseUrl + "/functions/v1/verify_admin_pin", anon,
                AuthPrefs.getAccessToken(context), payload);
        return response != null && response.has("success") && response.get("success").getAsBoolean();
    }

    public static void logout(Context context, String auditId, String reason) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon)) return;

        JsonObject payload = new JsonObject();
        payload.addProperty("login_audit_id", auditId);
        payload.addProperty("reason", reason);
        payload.addProperty("source", "APP");

        postJson(baseUrl + "/functions/v1/logout", anon, AuthPrefs.getAccessToken(context), payload);
    }

    public static void logError(Context context, JsonObject payload) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anon = SupabasePrefs.getAnonKey(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anon)) return;

        postJson(baseUrl + "/functions/v1/log_error", anon, AuthPrefs.getAccessToken(context), payload);
    }

    private static JsonObject postJson(String urlStr, String anonKey, String accessToken, JsonObject payload) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            connection.setDoOutput(true);
            connection.setRequestProperty("apikey", anonKey);
            if (!TextUtils.isEmpty(accessToken)) {
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }
            connection.setRequestProperty("Content-Type", "application/json");

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(payload.toString());
                writer.flush();
            }

            int code = connection.getResponseCode();
            InputStream in = (code >= 200 && code < 300) ? connection.getInputStream() : connection.getErrorStream();
            if (in == null) return null;
            byte[] bytes = new byte[4096];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = in.read(bytes)) > 0) {
                sb.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
            }
            String body = sb.toString();
            if (TextUtils.isEmpty(body)) return null;
            return JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static String getString(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsString() : "";
    }

    private static double getDouble(JsonObject object, String key) {
        try {
            return object != null && object.has(key) && !object.get(key).isJsonNull()
                    ? object.get(key).getAsDouble() : 0d;
        } catch (Exception ignored) {
            return 0d;
        }
    }

    private static String obterVersaoApp(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
            return "unknown";
        }
    }
}
