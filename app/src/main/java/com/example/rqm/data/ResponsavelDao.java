package com.example.rqm.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rqm.models.ResponsavelItem;

import java.util.ArrayList;
import java.util.List;

public class ResponsavelDao {

    private final SQLiteDatabase db;

    public ResponsavelDao(Context context) {
        db = DBHelper.getInstance(context).getWritableDatabase();
    }

    public void replaceAll(List<ResponsavelItem> items) {
        db.beginTransaction();
        try {
            db.delete(DBHelper.TABLE_RESPONSAVEIS_CACHE, null, null);
            if (items != null) {
                for (ResponsavelItem item : items) {
                    db.insert(DBHelper.TABLE_RESPONSAVEIS_CACHE, null, toValues(item));
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<String> listarNomes() {
        List<String> nomes = new ArrayList<>();
        Cursor cursor = db.query(
                DBHelper.TABLE_RESPONSAVEIS_CACHE,
                new String[]{DBHelper.ResponsavelColumns.NOME},
                null,
                null,
                null,
                null,
                DBHelper.ResponsavelColumns.NOME + " ASC"
        );
        try {
            while (cursor.moveToNext()) {
                nomes.add(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ResponsavelColumns.NOME)));
            }
        } finally {
            cursor.close();
        }
        return nomes;
    }

    public boolean isEmpty() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_RESPONSAVEIS_CACHE, null);
        try {
            return !cursor.moveToFirst() || cursor.getLong(0) == 0;
        } finally {
            cursor.close();
        }
    }

    private ContentValues toValues(ResponsavelItem item) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.ResponsavelColumns.REMOTE_ID, item.id);
        values.put(DBHelper.ResponsavelColumns.NOME, item.nome);
        values.put(DBHelper.ResponsavelColumns.CARGO, item.cargo);
        return values;
    }
}
