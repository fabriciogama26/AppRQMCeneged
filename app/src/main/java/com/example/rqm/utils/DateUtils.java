package com.example.rqm.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String PATTERN_DISPLAY = "dd/MM/yyyy";
    private static final String PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String PATTERN_ISO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final String PATTERN_ISO_NO_TZ = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String PATTERN_DATE = "yyyy-MM-dd";

    public static String toIsoWithNow(String value) {
        if (TextUtils.isEmpty(value)) {
            return formatIso(new Date());
        }

        if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Date date = parse(value, PATTERN_DISPLAY);
            if (date != null) {
                Calendar base = Calendar.getInstance();
                base.setTime(date);
                Calendar now = Calendar.getInstance();
                base.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                base.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                base.set(Calendar.SECOND, now.get(Calendar.SECOND));
                base.set(Calendar.MILLISECOND, 0);
                return formatIso(base.getTime());
            }
        }

        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Date date = parse(value, PATTERN_DATE);
            if (date != null) {
                Calendar base = Calendar.getInstance();
                base.setTime(date);
                Calendar now = Calendar.getInstance();
                base.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                base.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                base.set(Calendar.SECOND, now.get(Calendar.SECOND));
                base.set(Calendar.MILLISECOND, 0);
                return formatIso(base.getTime());
            }
        }

        if (value.contains("T")) {
            return value;
        }

        return formatIso(new Date());
    }

    public static String toDisplayDate(String value) {
        if (TextUtils.isEmpty(value)) return "";
        if (value.matches("\\d{2}/\\d{2}/\\d{4}")) return value;

        Date parsed = parse(value, PATTERN_ISO_MILLIS);
        if (parsed == null) parsed = parse(value, PATTERN_ISO);
        if (parsed == null) parsed = parse(value, PATTERN_ISO_NO_TZ);
        if (parsed == null && value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            parsed = parse(value, PATTERN_DATE);
        }

        if (parsed != null) {
            return new SimpleDateFormat(PATTERN_DISPLAY, Locale.getDefault()).format(parsed);
        }

        return value;
    }

    public static String toIsoDatePrefix(String value) {
        if (TextUtils.isEmpty(value)) return "";
        if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Date date = parse(value, PATTERN_DISPLAY);
            if (date != null) {
                return new SimpleDateFormat(PATTERN_DATE, Locale.getDefault()).format(date);
            }
        }
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return value;
        }
        if (value.length() >= 10 && value.contains("T")) {
            return value.substring(0, 10);
        }
        return value;
    }

    private static Date parse(String value, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            sdf.setLenient(false);
            return sdf.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String formatIso(Date date) {
        return new SimpleDateFormat(PATTERN_ISO, Locale.getDefault()).format(date);
    }
}
