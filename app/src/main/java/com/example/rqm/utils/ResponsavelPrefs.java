package com.example.rqm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ResponsavelPrefs {

    private static final String PREFS = "responsavel_prefs";
    private static final String KEY_NAMES = "names";
    private static final int MAX_ITEMS = 20;

    private ResponsavelPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static List<String> getResponsaveis(Context context) {
        Set<String> set = prefs(context).getStringSet(KEY_NAMES, Collections.emptySet());
        List<String> items = new ArrayList<>(set);
        Collections.sort(items, String.CASE_INSENSITIVE_ORDER);
        return items;
    }

    public static void addResponsavel(Context context, String nome) {
        String clean = nome != null ? nome.trim().toUpperCase() : "";
        if (TextUtils.isEmpty(clean)) {
            return;
        }

        List<String> current = new ArrayList<>(getResponsaveis(context));
        current.remove(clean);
        current.add(0, clean);
        if (current.size() > MAX_ITEMS) {
            current = current.subList(0, MAX_ITEMS);
        }

        Set<String> ordered = new LinkedHashSet<>(current);
        prefs(context).edit().putStringSet(KEY_NAMES, ordered).apply();
    }
}
