package com.example.rqm.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rqm.models.Requisicao;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO (Data Access Object) para manipular registros de Requisição no banco de dados SQLite.
 */
public class RequisicaoDAO {

    private final SQLiteDatabase db;

    /**
     * Construtor que abre o banco em modo leitura e escrita, utilizando o Singleton do DBHelper.
     */
    public RequisicaoDAO(Context context) {
        DBHelper helper = DBHelper.getInstance(context);  // Usa o Singleton do DBHelper
        db = helper.getWritableDatabase();
    }

    /**
     * Insere uma nova requisição no banco.
     *
     * @param req Objeto Requisicao a ser salvo.
     * @return ID da nova requisição inserida.
     */
    public long salvar(Requisicao req) {
        ContentValues values = new ContentValues();
        values.put("requisitor", req.requisitor);
        values.put("projeto", req.projeto);
        values.put("usuario", req.usuario);
        values.put("data", req.data); // Ex: 2025-06-13
        values.put("tipo_operacao", req.tipoOperacao); // Ex: "Requisição" ou "Devolução"
        values.put("observacao", req.observacao);
        values.put("materiais_json", req.materiaisJson); // Lista serializada em JSON

        return db.insert(DBHelper.TABLE_REQUISICOES, null, values);
    }

    /**
     * Retorna todas as requisições cadastradas, ordenadas da mais recente para a mais antiga.
     *
     * @return Lista com todas as requisições.
     */
    public List<Requisicao> listarTodas() {
        List<Requisicao> lista = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_REQUISICOES, null, null, null, null, null, "id DESC");

        while (cursor.moveToNext()) {
            lista.add(converterCursorParaRequisicao(cursor));
        }

        cursor.close();
        return lista;
    }

    /**
     * Filtra as requisições com base em qualquer combinação de:
     * - data (parcial ou completa),
     * - prefixo (parte inicial do código do projeto),
     * - projeto (número),
     * - tipo de operação ("Requisição", "Devolução", etc).
     *
     * Todos os filtros são opcionais e funcionam de forma independente.
     *
     * @param data Ex: "2025-06"
     * @param projeto Ex: "6001"
     * @param prefixo Ex: "OII-24"
     * @param tipoOperacao Ex: "Requisição"
     * @return Lista de requisições que atendem aos critérios.
     */
    public List<Requisicao> filtrarRequisicoes(String data, String projeto, String prefixo, String tipoOperacao) {
        List<Requisicao> lista = new ArrayList<>();

        // Monta a base da query dinâmica
        String query = "SELECT * FROM " + DBHelper.TABLE_REQUISICOES + " WHERE 1=1";
        List<String> args = new ArrayList<>();

        // Filtro por data (formato: yyyy-MM-dd ou parte dele, como "2025-06")
        if (data != null && !data.isEmpty()) {
            query += " AND data LIKE ?";
            args.add("%" + data + "%");
        }

        // Filtro combinando prefixo + projeto, ou apenas um dos dois
        if (projeto != null && !projeto.isEmpty() && prefixo != null && !prefixo.equals("Todos")) {
            query += " AND projeto LIKE ?";
            args.add("%" + prefixo + projeto + "%");
        } else if (projeto != null && !projeto.isEmpty()) {
            query += " AND projeto LIKE ?";
            args.add("%" + projeto + "%");
        } else if (prefixo != null && !prefixo.equals("Todos")) {
            query += " AND projeto LIKE ?";
            args.add("%" + prefixo + "%");
        }

        // Filtro por tipo de operação, ignorando o valor "Todos"
        if (tipoOperacao != null && !tipoOperacao.equals("Todos")) {
            query += " AND tipo_operacao = ?";
            args.add(tipoOperacao);
        }

        // Executa a consulta final
        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                lista.add(converterCursorParaRequisicao(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    /**
     * Converte um registro do banco (cursor) para o objeto Requisicao.
     *
     * @param cursor Posição atual da linha retornada pelo SELECT
     * @return Objeto Requisicao preenchido com os dados da linha.
     */
    public static Requisicao converterCursorParaRequisicao(Cursor cursor) {
        Requisicao r = new Requisicao();
        r.id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        r.requisitor = cursor.getString(cursor.getColumnIndexOrThrow("requisitor"));
        r.projeto = cursor.getString(cursor.getColumnIndexOrThrow("projeto"));
        r.usuario = cursor.getString(cursor.getColumnIndexOrThrow("usuario"));
        r.data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
        r.tipoOperacao = cursor.getString(cursor.getColumnIndexOrThrow("tipo_operacao"));
        r.observacao = cursor.getString(cursor.getColumnIndexOrThrow("observacao"));
        r.materiaisJson = cursor.getString(cursor.getColumnIndexOrThrow("materiais_json"));
        return r;
    }
}
