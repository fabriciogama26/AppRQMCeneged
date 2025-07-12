package com.example.rqm.models;

import java.io.Serializable;

public class Material implements Serializable {
    public String codigo;
    public String descricao;
    public String umb;
    public String lp;
    public String serial;
    public String valor_unitario;
    public String tipo;
    public int quantidade = 0;

    // ✅ Construtor padrão (necessário para Firebase e serialização)
    public Material() {

    }

    // ✅ Construtor com os campos usados na interface
    public Material(String codigo, String descricao, String umb, String valor_unitario, String tipo, int quantidade, String lp, String serial) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.umb = umb;
        this.valor_unitario = valor_unitario;
        this.tipo = tipo;
        this.lp = lp;
        this.serial = serial;
        this.quantidade = quantidade;
    }
}
