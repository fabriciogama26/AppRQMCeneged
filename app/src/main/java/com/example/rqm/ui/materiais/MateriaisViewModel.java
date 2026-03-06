package com.example.rqm.ui.materiais;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rqm.data.MaterialCacheDao;
import com.example.rqm.models.Material;
import com.example.rqm.utils.OperacaoTipo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MateriaisViewModel extends AndroidViewModel {

    private String tipoOperacao = "";
    private final List<Material> listaCompleta = new ArrayList<>();
    private final MutableLiveData<List<Material>> materiaisSelecionados = new MutableLiveData<>(new ArrayList<>());
    private final Set<String> codigosComLP = new HashSet<>();

    public MateriaisViewModel(@NonNull Application application) {
        super(application);
        carregarMateriais();
        carregarCodigosComLP(application);
    }

    private void carregarMateriais() {
        MaterialCacheDao cacheDao = new MaterialCacheDao(getApplication());
        List<Material> cache = cacheDao.listarTodos();
        if (!cache.isEmpty()) {
            listaCompleta.clear();
            listaCompleta.addAll(cache);
            return;
        }

        try (InputStream is = getApplication().getAssets().open("materiais_completos.json")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            if (bytesRead != size) {
                throw new RuntimeException("Erro ao ler o arquivo de materiais.");
            }
            String json = new String(buffer, StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<Material>>() {}.getType();
            List<Material> materiais = new Gson().fromJson(json, listType);

            listaCompleta.clear();
            listaCompleta.addAll(materiais);
            cacheDao.replaceAll(materiais);

        } catch (Exception e) {
            Log.e("MateriaisViewModel", "Erro ao carregar materiais", e);
        }
    }

    private void carregarCodigosComLP(Context context) {
        try {
            InputStream is = context.getAssets().open("trafo_codigo.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                codigosComLP.add(obj.get("codigo").getAsString());
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            Log.e("MateriaisViewModel", "Erro ao carregar trafo_codigo.json", e);
        }
    }

    public Set<String> getCodigosComLP() {
        return codigosComLP;
    }

    public List<Material> getListaCompleta() {
        return listaCompleta;
    }

    public LiveData<List<Material>> getMateriaisSelecionados() {
        return materiaisSelecionados;
    }

    public void adicionarMaterial(String codigo) {
        for (Material material : materiaisSelecionados.getValue()) {
            if (material.codigo.equals(codigo)) return;
        }

        for (Material material : listaCompleta) {
            if (material.codigo.equals(codigo)) {
                List<Material> listaAtual = new ArrayList<>(materiaisSelecionados.getValue());
                listaAtual.add(material);
                materiaisSelecionados.setValue(listaAtual);
                return;
            }
        }
    }

    public void removerMaterial(Material material) {
        List<Material> atuais = new ArrayList<>(materiaisSelecionados.getValue());
        atuais.removeIf(item -> item.codigo.equalsIgnoreCase(material.codigo));
        materiaisSelecionados.setValue(atuais);
    }

    public void setTipoOperacao(String tipo) {
        this.tipoOperacao = tipo;
    }

    public String getMensagemConfirmacao() {
        return OperacaoTipo.isDevolucao(tipoOperacao)
                ? "Deseja finalizar a devolucao?"
                : "Deseja finalizar a requisicao?";
    }
}
