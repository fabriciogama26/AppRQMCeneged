package com.example.rqm.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;


public class Requisicao {
    public long id;
    public String requisitor;
    public String projeto;
    public String usuario;
    public String data;
    public String tipoOperacao;
    public String observacao;
    public String materiaisJson;
    public String lp;
    public String serial;
    public String origem;
    public String deviceId;
    public String clientRequestId;

    private transient List<Material> materiaisSelecionados; // Cache não serializado
    public void setMateriaisSelecionados(List<Material> materiais) {
        this.materiaisSelecionados = materiais;
        // Aqui você serializa os materiais em JSON
        this.materiaisJson = new com.google.gson.Gson().toJson(materiais);
    }


    public List<Material> getMateriaisSelecionados() {
        if (materiaisSelecionados == null && materiaisJson != null) {
            materiaisSelecionados = new Gson().fromJson(materiaisJson,
                    new TypeToken<List<Material>>(){}.getType());
        }
        return materiaisSelecionados;
    }
}
