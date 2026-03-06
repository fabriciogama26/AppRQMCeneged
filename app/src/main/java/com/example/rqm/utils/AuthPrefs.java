package com.example.rqm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthPrefs {

    public static final String TEST_MODE_TOKEN = "test-mode-token";
    private static final String PREFS = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_MATRICULA = "matricula";
    private static final String KEY_ROLE = "role";
    private static final String KEY_TENANT_ID = "tenant_id";
    private static final String KEY_LOGIN_AUDIT_ID = "login_audit_id";
    private static final String KEY_LOGIN_DATE = "login_date";
    private static final String KEY_IMEI = "imei";
    private static final String KEY_IMEI_VALIDATED = "imei_validated";
    private static final String KEY_PENDING_LOGOUT_REASON = "pending_logout_reason";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void saveLogin(Context context, String accessToken, String userId, String matricula,
                                 String role, String tenantId, String loginAuditId) {
        prefs(context).edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_MATRICULA, matricula)
                .putString(KEY_ROLE, role)
                .putString(KEY_TENANT_ID, tenantId)
                .putString(KEY_LOGIN_AUDIT_ID, loginAuditId)
                .putString(KEY_LOGIN_DATE, todayDate())
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        return !TextUtils.isEmpty(getAccessToken(context));
    }

    public static String getAccessToken(Context context) {
        return prefs(context).getString(KEY_ACCESS_TOKEN, "");
    }

    public static boolean isTestSession(Context context) {
        return TEST_MODE_TOKEN.equals(getAccessToken(context));
    }

    public static String getUserId(Context context) {
        return prefs(context).getString(KEY_USER_ID, "");
    }

    public static String getMatricula(Context context) {
        return prefs(context).getString(KEY_MATRICULA, "");
    }

    public static String getRole(Context context) {
        return prefs(context).getString(KEY_ROLE, "");
    }

    public static boolean isAdmin(Context context) {
        return "admin".equalsIgnoreCase(getRole(context));
    }

    public static String getTenantId(Context context) {
        return prefs(context).getString(KEY_TENANT_ID, "");
    }

    public static String getLoginAuditId(Context context) {
        return prefs(context).getString(KEY_LOGIN_AUDIT_ID, "");
    }

    public static String getLoginDate(Context context) {
        return prefs(context).getString(KEY_LOGIN_DATE, "");
    }

    public static boolean shouldExpireByDate(Context context) {
        String loginDate = getLoginDate(context);
        if (TextUtils.isEmpty(loginDate)) return false;
        return !loginDate.equals(todayDate());
    }

    public static void clearSession(Context context) {
        String imei = getImei(context);
        boolean imeiValidated = isImeiValidated(context);
        prefs(context).edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_MATRICULA)
                .remove(KEY_ROLE)
                .remove(KEY_TENANT_ID)
                .remove(KEY_LOGIN_AUDIT_ID)
                .remove(KEY_LOGIN_DATE)
                .apply();
        setImei(context, imei, imeiValidated);
    }

    public static void setImei(Context context, String imei, boolean validated) {
        prefs(context).edit()
                .putString(KEY_IMEI, imei)
                .putBoolean(KEY_IMEI_VALIDATED, validated)
                .apply();
    }

    public static String getImei(Context context) {
        return prefs(context).getString(KEY_IMEI, "");
    }

    public static boolean isImeiValidated(Context context) {
        return prefs(context).getBoolean(KEY_IMEI_VALIDATED, false);
    }

    public static void markPendingLogout(Context context, String reason) {
        prefs(context).edit()
                .putString(KEY_PENDING_LOGOUT_REASON, reason)
                .apply();
    }

    public static String consumePendingLogout(Context context) {
        String reason = prefs(context).getString(KEY_PENDING_LOGOUT_REASON, "");
        if (!TextUtils.isEmpty(reason)) {
            prefs(context).edit().remove(KEY_PENDING_LOGOUT_REASON).apply();
        }
        return reason;
    }

    private static String todayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
