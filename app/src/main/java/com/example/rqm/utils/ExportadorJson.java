package com.example.rqm.utils;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.rqm.models.Requisicao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Classe utilitária responsável por exportar e compartilhar uma lista de requisições em formato JSON.
 */
public class ExportadorJson {

    /**
     * Exporta e compartilha a lista de requisições em um arquivo JSON.
     *
     * @param context     Contexto da aplicação.
     * @param requisicoes Lista de objetos Requisicao a serem exportados.
     * @return true se o compartilhamento foi iniciado com sucesso, false caso contrário.
     */
    public static boolean exportarECompartilhar(Context context, List<Requisicao> requisicoes) {
        File arquivo = exportarParaJson(context, requisicoes);

        if (arquivo != null && arquivo.exists()) {
            try {
                // Gera a URI segura usando FileProvider
                Uri uri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        arquivo
                );

                // Intenção de compartilhamento
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Inicia o compartilhamento
                context.startActivity(Intent.createChooser(intent, "Compartilhar JSON"));
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Erro ao compartilhar arquivo JSON", Toast.LENGTH_SHORT).show();
                return false;
            }

        } else {
            Toast.makeText(context, "Erro ao exportar arquivo JSON", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Exporta a lista de requisições para um arquivo JSON na pasta privada do app.
     *
     * @param context      Contexto da aplicação.
     * @param requisicoes  Lista de objetos Requisicao.
     * @return Arquivo gerado, ou null em caso de erro.
     */
    private static File exportarParaJson(Context context, List<Requisicao> requisicoes) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(requisicoes);

        // Cria diretório de exportação privado do app
        File diretorio = new File(context.getExternalFilesDir(null), "export_json");
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        // Gera a data atual no formato ddMMyyyy
        String dataAtual = new SimpleDateFormat("ddMMyyyy").format(new Date());

        // Define o nome do arquivo com a data
        File arquivo = new File(diretorio, "historico_" + dataAtual + ".json");

        // Grava o conteúdo JSON no arquivo
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write(json);
            return arquivo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
