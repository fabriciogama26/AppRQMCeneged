package com.example.rqm.ui.historico;

import androidx.lifecycle.ViewModel;

public class HistoricoViewModel extends ViewModel {

    public boolean validarCamposHistorico(String prefixo, String numeroProjeto) {
        if (numeroProjeto.isEmpty()) {
            return false;
        }

        String projetoCompleto = prefixo + "-" + numeroProjeto;
        String regexProjeto = "^(OMI|OII)-\\d{2}-\\d{4}$|^OS-\\d{8}$|^OM-\\d{10}$";
        return projetoCompleto.matches(regexProjeto);
    }

    public String formatarNumeroProjetoHistorico(String prefixo, String input) {
        String texto = input.replaceAll("[^\\d]", ""); // Remove não-dígitos

        if (prefixo.equals("OMI") || prefixo.equals("OII")) {
            if (texto.length() > 2) {
                texto = texto.substring(0, 2) + "-" + texto.substring(2);
            }
            if (texto.length() > 7) {
                texto = texto.substring(0, 7);
            }
        } else if (prefixo.equals("OS")) {
            if (texto.length() > 8) texto = texto.substring(0, 8);
        } else if (prefixo.equals("OM")) {
            if (texto.length() > 10) texto = texto.substring(0, 10);
        }

        return texto;
    }
}
