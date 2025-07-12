package com.example.rqm.ui.materiais;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rqm.models.Material;
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

/**
 * ViewModel responsável por gerenciar os dados da tela de seleção de materiais.
 * - Carrega os materiais do JSON principal.
 * - Carrega a lista de códigos especiais que exigem campos LP/Serial.
 * - Mantém a lista de materiais selecionados pelo usuário.
 */
public class MateriaisViewModel extends AndroidViewModel {

    // Define se a operação atual é "requisicao" ou "devolucao"
    private String tipoOperacao = "";

    // Lista completa de materiais disponíveis (do JSON "materiais_completos.json")
    private final List<Material> listaCompleta = new ArrayList<>();

    // Lista observável com os materiais selecionados na interface
    private final MutableLiveData<List<Material>> materiaisSelecionados = new MutableLiveData<>(new ArrayList<>());

    // Lista de códigos que devem usar o layout com LP e Serial (item_material_lp.xml)
    private final Set<String> codigosComLP = new HashSet<>();

    /**
     * Construtor do ViewModel.
     * - Carrega os materiais.
     * - Carrega os códigos que têm LP/Serial.
     */
    public MateriaisViewModel(@NonNull Application application) {
        super(application);
        carregarMateriais();                // Carrega lista de materiais
        carregarCodigosComLP(application); // Carrega lista de códigos especiais
    }

    /**
     * Lê o JSON de materiais e converte em objetos.
     */
    private void carregarMateriais() {
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

        } catch (Exception e) {
            Log.e("MateriaisViewModel", "Erro ao carregar materiais", e);
        }
    }

    /**
     * Lê o arquivo trafo_codigo.json contendo os códigos que exigem LP e Serial.
     * Esses códigos usarão um layout personalizado com campos adicionais.
     */
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

    /**
     * Getter público para que o MaterialAdapter saiba quais códigos usam LP/Serial.
     */
    public Set<String> getCodigosComLP() {
        return codigosComLP;
    }

    /**
     * Retorna todos os materiais disponíveis para seleção.
     */
    public List<Material> getListaCompleta() {
        return listaCompleta;
    }

    /**
     * Retorna a lista observável dos materiais que o usuário já adicionou.
     */
    public LiveData<List<Material>> getMateriaisSelecionados() {
        return materiaisSelecionados;
    }

    /**
     * Adiciona um material à lista de selecionados com base no código.
     * Evita duplicatas.
     */
    public void adicionarMaterial(String codigo) {
        // Evita adicionar um material que já está na lista
        for (Material m : materiaisSelecionados.getValue()) {
            if (m.codigo.equals(codigo)) return;
        }

        // Busca o material na lista completa e adiciona
        for (Material m : listaCompleta) {
            if (m.codigo.equals(codigo)) {
                List<Material> listaAtual = new ArrayList<>(materiaisSelecionados.getValue());
                listaAtual.add(m);
                materiaisSelecionados.setValue(listaAtual);
                return;
            }
        }
    }

    /**
     * Remove um material da lista de selecionados.
     */
    public void removerMaterial(Material material) {
        List<Material> atuais = new ArrayList<>(materiaisSelecionados.getValue());
        atuais.removeIf(m -> m.codigo.equalsIgnoreCase(material.codigo));
        materiaisSelecionados.setValue(atuais);
    }

    /**
     * Define o tipo de operação atual ("requisicao" ou "devolucao").
     */
    public void setTipoOperacao(String tipo) {
        this.tipoOperacao = tipo;
    }

    /**
     * Retorna a mensagem de confirmação adequada com base no tipo da operação.
     */
    public String getMensagemConfirmacao() {
        return "devolucao".equalsIgnoreCase(tipoOperacao)
                ? "Deseja finalizar a devolução?"
                : "Deseja finalizar a requisição?";
    }
}
