package com.example.rqm.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rqm.models.SyncRun;

import java.util.ArrayList;
import java.util.List;

public class SyncRunDao {

    private final SQLiteDatabase db;

    public SyncRunDao(Context context) {
        db = DBHelper.getInstance(context).getWritableDatabase();
    }

    public long salvar(SyncRun run) {
        ContentValues values = toValues(run);
        long id = db.insertWithOnConflict(DBHelper.TABLE_SYNC_RUNS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        run.id = id;
        return id;
    }

    public void marcarComoEnviado(long id) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.SyncRunColumns.UPLOADED_TO_SERVER, 1);
        db.update(DBHelper.TABLE_SYNC_RUNS, values, DBHelper.SyncRunColumns.ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<SyncRun> listarTodos() {
        List<SyncRun> lista = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_SYNC_RUNS, null, null, null, null, null, DBHelper.SyncRunColumns.ID + " DESC");
        while (cursor.moveToNext()) {
            lista.add(fromCursor(cursor));
        }
        cursor.close();
        return lista;
    }

    public List<SyncRun> listarPendentesUpload() {
        List<SyncRun> lista = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_SYNC_RUNS, null,
                DBHelper.SyncRunColumns.UPLOADED_TO_SERVER + " = 0",
                null, null, null, DBHelper.SyncRunColumns.ID + " ASC");
        while (cursor.moveToNext()) {
            lista.add(fromCursor(cursor));
        }
        cursor.close();
        return lista;
    }

    public SyncRun obterUltimo() {
        Cursor cursor = db.query(DBHelper.TABLE_SYNC_RUNS, null, null, null, null, null,
                DBHelper.SyncRunColumns.ID + " DESC", "1");
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private ContentValues toValues(SyncRun run) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.SyncRunColumns.SYNC_UUID, run.syncUuid);
        values.put(DBHelper.SyncRunColumns.STARTED_AT, run.startedAt);
        values.put(DBHelper.SyncRunColumns.FINISHED_AT, run.finishedAt);
        values.put(DBHelper.SyncRunColumns.STATUS, run.status);
        values.put(DBHelper.SyncRunColumns.PENDING_TOTAL, run.pendingTotal);
        values.put(DBHelper.SyncRunColumns.PENDING_SENT, run.pendingSent);
        values.put(DBHelper.SyncRunColumns.MATERIALS_UPDATED, run.materialsUpdated);
        values.put(DBHelper.SyncRunColumns.CONFLICTS_FOUND, run.conflictsFound);
        values.put(DBHelper.SyncRunColumns.ERRORS_COUNT, run.errorsCount);
        values.put(DBHelper.SyncRunColumns.MESSAGE, run.message);
        values.put(DBHelper.SyncRunColumns.DEVICE_ID, run.deviceId);
        values.put(DBHelper.SyncRunColumns.USER_ID, run.userId);
        values.put(DBHelper.SyncRunColumns.TENANT_ID, run.tenantId);
        values.put(DBHelper.SyncRunColumns.SOURCE, run.source);
        values.put(DBHelper.SyncRunColumns.UPLOADED_TO_SERVER, run.uploadedToServer ? 1 : 0);
        values.put(DBHelper.SyncRunColumns.CREATED_AT, run.createdAt);
        return values;
    }

    private SyncRun fromCursor(Cursor cursor) {
        SyncRun run = new SyncRun();
        run.id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.ID));
        run.syncUuid = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.SYNC_UUID));
        run.startedAt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.STARTED_AT));
        run.finishedAt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.FINISHED_AT));
        run.status = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.STATUS));
        run.pendingTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.PENDING_TOTAL));
        run.pendingSent = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.PENDING_SENT));
        run.materialsUpdated = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.MATERIALS_UPDATED));
        run.conflictsFound = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.CONFLICTS_FOUND));
        run.errorsCount = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.ERRORS_COUNT));
        run.message = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.MESSAGE));
        run.deviceId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.DEVICE_ID));
        run.userId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.USER_ID));
        run.tenantId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.TENANT_ID));
        run.source = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.SOURCE));
        run.uploadedToServer = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.UPLOADED_TO_SERVER)) == 1;
        run.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SyncRunColumns.CREATED_AT));
        return run;
    }
}