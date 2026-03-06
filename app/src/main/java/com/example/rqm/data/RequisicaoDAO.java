package com.example.rqm.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rqm.models.Requisicao;
import com.example.rqm.utils.DateUtils;
import com.example.rqm.utils.OperacaoTipo;

import java.util.ArrayList;
import java.util.List;

public class RequisicaoDAO {

    public static final String SYNC_PENDING = "PENDING";
    public static final String SYNC_SYNCED = "SYNCED";
    public static final String SYNC_CONFLICT = "CONFLICT";
    public static final String SYNC_ERROR = "ERROR";

    private final SQLiteDatabase db;

    public RequisicaoDAO(Context context) {
        DBHelper helper = DBHelper.getInstance(context);
        db = helper.getWritableDatabase();
    }

    public long salvar(Requisicao req) {
        ContentValues values = new ContentValues();
        values.put("requisitor", req.requisitor);
        values.put("projeto", req.projeto);
        values.put("usuario", req.usuario);
        values.put("data", req.data);
        values.put("tipo_operacao", OperacaoTipo.normalize(req.tipoOperacao));
        values.put("observacao", req.observacao);
        values.put("materiais_json", req.materiaisJson);
        values.put("origem", req.origem);
        values.put("device_id", req.deviceId);
        values.put("client_request_id", req.clientRequestId);
        values.put("sync_status", SYNC_PENDING);
        values.putNull("last_sync_at");
        return db.insert(DBHelper.TABLE_REQUISICOES, null, values);
    }

    public List<Requisicao> listarTodas() {
        List<Requisicao> lista = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_REQUISICOES, null, null, null, null, null, "id DESC");
        while (cursor.moveToNext()) {
            lista.add(converterCursorParaRequisicao(cursor));
        }
        cursor.close();
        return lista;
    }

    public List<Requisicao> listarUltimas(int limit) {
        List<Requisicao> lista = new ArrayList<>();
        String query = "SELECT * FROM " + DBHelper.TABLE_REQUISICOES +
                " ORDER BY id DESC LIMIT " + limit;
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            lista.add(converterCursorParaRequisicao(cursor));
        }
        cursor.close();
        return lista;
    }

    public List<Requisicao> listarPendentesSync() {
        List<Requisicao> lista = new ArrayList<>();
        String query = "SELECT * FROM " + DBHelper.TABLE_REQUISICOES +
                " WHERE sync_status IS NULL OR sync_status IN (?, ?) ORDER BY id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{SYNC_PENDING, SYNC_ERROR});
        while (cursor.moveToNext()) {
            lista.add(converterCursorParaRequisicao(cursor));
        }
        cursor.close();
        return lista;
    }

    public void atualizarSyncStatus(long id, String status, String lastSyncAt) {
        ContentValues values = new ContentValues();
        values.put("sync_status", status);
        values.put("last_sync_at", lastSyncAt);
        db.update(DBHelper.TABLE_REQUISICOES, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Requisicao> filtrarRequisicoes(String data, String projeto, String prefixo, String tipoOperacao) {
        List<Requisicao> lista = new ArrayList<>();

        String query = "SELECT * FROM " + DBHelper.TABLE_REQUISICOES + " WHERE 1=1";
        List<String> args = new ArrayList<>();

        if (data != null && !data.isEmpty()) {
            String isoPrefix = DateUtils.toIsoDatePrefix(data);
            query += " AND (data LIKE ? OR data LIKE ?)";
            args.add("%" + data + "%");
            args.add(isoPrefix + "%");
        }

        if (projeto != null && !projeto.isEmpty() && prefixo != null && !prefixo.equals("Todos")) {
            query += " AND projeto LIKE ?";
            args.add(prefixo + "-" + projeto + "%");
        } else if (projeto != null && !projeto.isEmpty()) {
            query += " AND projeto LIKE ?";
            args.add("%" + projeto + "%");
        } else if (prefixo != null && !prefixo.equals("Todos")) {
            query += " AND projeto LIKE ?";
            args.add(prefixo + "-%");
        }

        if (tipoOperacao != null && !tipoOperacao.equals("Todos")) {
            String code = OperacaoTipo.normalize(tipoOperacao);
            String label = OperacaoTipo.toLabel(code);
            query += " AND (tipo_operacao = ? OR tipo_operacao = ?)";
            args.add(code);
            args.add(label);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                lista.add(converterCursorParaRequisicao(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    public static Requisicao converterCursorParaRequisicao(Cursor cursor) {
        Requisicao r = new Requisicao();
        r.id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        r.requisitor = cursor.getString(cursor.getColumnIndexOrThrow("requisitor"));
        r.projeto = cursor.getString(cursor.getColumnIndexOrThrow("projeto"));
        r.usuario = cursor.getString(cursor.getColumnIndexOrThrow("usuario"));
        r.data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
        r.tipoOperacao = OperacaoTipo.normalize(cursor.getString(cursor.getColumnIndexOrThrow("tipo_operacao")));
        r.observacao = cursor.getString(cursor.getColumnIndexOrThrow("observacao"));
        r.materiaisJson = cursor.getString(cursor.getColumnIndexOrThrow("materiais_json"));
        int origemIndex = cursor.getColumnIndex("origem");
        if (origemIndex >= 0) r.origem = cursor.getString(origemIndex);
        int deviceIndex = cursor.getColumnIndex("device_id");
        if (deviceIndex >= 0) r.deviceId = cursor.getString(deviceIndex);
        int clientReqIndex = cursor.getColumnIndex("client_request_id");
        if (clientReqIndex >= 0) r.clientRequestId = cursor.getString(clientReqIndex);
        return r;
    }
}