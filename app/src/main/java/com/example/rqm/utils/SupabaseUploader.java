package com.example.rqm.utils;

import android.content.Context;
import android.text.TextUtils;

import com.example.rqm.models.Material;
import com.example.rqm.models.Requisicao;
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
import java.util.List;

public class SupabaseUploader {

    public static class UploadResult {
        public final boolean success;
        public final String status;
        public final String message;

        public UploadResult(boolean success, String status, String message) {
            this.success = success;
            this.status = status;
            this.message = message;
        }
    }

    public static UploadResult enviarRequisicao(Context context, Requisicao req) {
        SupabaseConfigLoader.ensureConfig(context);
        String baseUrl = SupabasePrefs.getUrl(context);
        String anonKey = SupabasePrefs.getAnonKey(context);
        String accessToken = AuthPrefs.getAccessToken(context);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(anonKey)) {
            return new UploadResult(false, "ERROR", "Configuracao do Supabase ausente.");
        }
        if (TextUtils.isEmpty(accessToken) || AuthPrefs.isTestSession(context)) {
            return new UploadResult(false, "ERROR", "Sessao invalida para envio ao servidor.");
        }

        JsonObject payload = buildSubmitPayload(context, req);
        JsonElement response = postJson(
                baseUrl + "/functions/v1/submit_material_request",
                anonKey,
                accessToken,
                payload
        );
        if (response == null) {
            return new UploadResult(false, "ERROR", "Falha ao atualizar base de dados.");
        }

        JsonObject obj = toObject(response);
        if (obj == null) {
            return new UploadResult(false, "ERROR", "Resposta invalida do servidor.");
        }

        boolean success = getBoolean(obj, "success");
        JsonObject resultObj = obj.has("result") && obj.get("result").isJsonObject()
                ? obj.getAsJsonObject("result")
                : obj;

        String status = getString(resultObj, "status");
        String reason = getString(obj, "reason");
        if (TextUtils.isEmpty(reason)) {
            reason = getString(resultObj, "reason");
        }

        if ("APPLIED".equalsIgnoreCase(status) || "ALREADY_APPLIED".equalsIgnoreCase(status)) {
            String msg = "Base de dados atualizada.";
            if ("ALREADY_APPLIED".equalsIgnoreCase(status)) {
                msg = "Requisicao ja aplicada no servidor.";
            }
            return new UploadResult(true, status, msg);
        }

        if ("REJECTED".equalsIgnoreCase(status)) {
            String msg;
            if ("INSUFFICIENT_STOCK".equalsIgnoreCase(reason)) {
                msg = "Conflito: saldo insuficiente para aplicar a requisicao.";
            } else if ("MATERIAL_NOT_FOUND".equalsIgnoreCase(reason)) {
                msg = "Material nao encontrado no estoque.";
            } else if ("PROJECT_REQUIRED".equalsIgnoreCase(reason)) {
                msg = "Projeto obrigatorio para movimentar material.";
            } else if ("PROJECT_RETURN_EXCEEDS_ISSUED".equalsIgnoreCase(reason)) {
                msg = "Devolucao maior que o saldo liquido do projeto.";
            } else {
                msg = getString(obj, "message");
                if (TextUtils.isEmpty(msg)) {
                    msg = "Requisicao rejeitada pelo servidor.";
                }
            }
            return new UploadResult(false, status, msg);
        }

        if (success) {
            String message = getString(obj, "message");
            if (TextUtils.isEmpty(message)) {
                message = "Base de dados atualizada.";
            }
            return new UploadResult(true, TextUtils.isEmpty(status) ? "OK" : status, message);
        }

        String message = getString(obj, "message");
        if (TextUtils.isEmpty(message)) {
            message = "Falha ao atualizar base de dados.";
        }
        return new UploadResult(false, TextUtils.isEmpty(status) ? "ERROR" : status, message);
    }

    private static JsonObject buildSubmitPayload(Context context, Requisicao req) {
        JsonObject obj = new JsonObject();
        obj.addProperty("client_request_id", safe(req.clientRequestId));
        obj.addProperty("requisitor", safe(req.requisitor));
        obj.addProperty("projeto", safe(req.projeto));
        obj.addProperty("usuario", safe(req.usuario));
        obj.addProperty("data", DateUtils.toIsoWithNow(req.data));
        obj.addProperty("tipo_operacao", safe(req.tipoOperacao));
        obj.addProperty("observacao", safe(req.observacao));
        obj.addProperty("origem", safe(req.origem));
        obj.addProperty("device_id", safe(req.deviceId));
        obj.addProperty("tenant_id", safe(AuthPrefs.getTenantId(context)));
        obj.add("itens", buildItensPayload(req.getMateriaisSelecionados()));
        return obj;
    }

    private static JsonArray buildItensPayload(List<Material> materiais) {
        JsonArray array = new JsonArray();
        if (materiais == null) return array;
        for (Material mat : materiais) {
            JsonObject obj = new JsonObject();
            obj.addProperty("codigo", safe(mat.codigo));
            obj.addProperty("descricao", safe(mat.descricao));
            obj.addProperty("umb", safe(mat.umb));
            obj.addProperty("tipo", safe(mat.tipo));
            obj.addProperty("lp", safe(mat.lp));
            obj.addProperty("serial", safe(mat.serial));
            obj.addProperty("quantidade", mat.quantidade);
            obj.addProperty("valor_unitario", safeToDouble(mat.valor_unitario));
            array.add(obj);
        }
        return array;
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static double safeToDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private static boolean getBoolean(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).getAsBoolean();
    }

    private static JsonObject toObject(JsonElement element) {
        if (element == null) return null;
        if (element.isJsonObject()) return element.getAsJsonObject();
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            if (!arr.isEmpty() && arr.get(0).isJsonObject()) {
                return arr.get(0).getAsJsonObject();
            }
        }
        return null;
    }

    private static JsonElement postJson(String urlStr, String anonKey, String accessToken, JsonObject payload) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setDoOutput(true);
            connection.setRequestProperty("apikey", anonKey);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/json");

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
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
            return JsonParser.parseString(body);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
