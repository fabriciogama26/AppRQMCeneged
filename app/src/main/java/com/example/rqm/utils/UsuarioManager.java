package com.example.rqm.utils;

import android.content.Context;

import com.example.rqm.models.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class UsuarioManager {

    private List<Usuario> usuarios;

    public UsuarioManager(Context context) {
        try {
            InputStream is = context.getAssets().open("usuarios.json");
            InputStreamReader reader = new InputStreamReader(is);
            Type listType = new TypeToken<List<Usuario>>() {}.getType();
            usuarios = new Gson().fromJson(reader, listType);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validarLogin(String login, String senha) {
        if (usuarios == null) return false;
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(login) && u.getSenha().equals(senha)) {
                return true;
            }
        }
        return false;
    }
}
