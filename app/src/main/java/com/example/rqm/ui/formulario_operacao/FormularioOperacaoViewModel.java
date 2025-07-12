package com.example.rqm.ui.formulario_operacao;

import android.app.DatePickerDialog;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FormularioOperacaoViewModel extends ViewModel {
    private final Calendar calendar = Calendar.getInstance();

    public String getDataAtualFormatada() {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formato.format(calendar.getTime());
    }

    public void abrirDatePicker(Context context, DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(context, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void setData(int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
    }

    public boolean validarCampos(String prefixo, String numeroProjeto, String requisitor, String usuario) {
        if (numeroProjeto.isEmpty() || requisitor.isEmpty() || usuario.isEmpty()) {
            return false;
        }

        String projetoCompleto = prefixo + "-" + numeroProjeto;
        String regexProjeto = "^(OMI|OII)-\\d{2}-\\d{4}$|^OS-\\d{8}$|^OM-\\d{10}$";
        return projetoCompleto.matches(regexProjeto);
    }

    public String formatarNumeroProjeto(String prefixo, String input) {
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
