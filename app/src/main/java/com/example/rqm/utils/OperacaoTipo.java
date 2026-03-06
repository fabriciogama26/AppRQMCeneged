package com.example.rqm.utils;

import com.example.rqm.R;

public class OperacaoTipo {

    public static final String REQ = "REQ";
    public static final String DEV = "DEV";

    public static String normalize(String value) {
        if (value == null) return REQ;
        String v = value.trim().toLowerCase();
        if (v.isEmpty()) return REQ;
        if (v.startsWith("dev") || v.startsWith("devol")) return DEV;
        if (v.startsWith("req") || v.startsWith("requi")) return REQ;
        if ("devolucao".equalsIgnoreCase(value)) return DEV;
        if ("requisicao".equalsIgnoreCase(value)) return REQ;
        return value.toUpperCase();
    }

    public static boolean isDevolucao(String value) {
        return DEV.equalsIgnoreCase(normalize(value));
    }

    public static String toLabel(String value) {
        return isDevolucao(value) ? "Devolu\u00E7\u00E3o" : "Requisi\u00E7\u00E3o";
    }

    public static int toLabelResId(String value) {
        return isDevolucao(value) ? R.string.home_action_devolucao : R.string.home_action_requisicao;
    }
}