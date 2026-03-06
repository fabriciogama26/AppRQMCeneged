package com.example.rqm.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rqm.models.Material;

import java.util.ArrayList;
import java.util.List;

public class MaterialCacheDao {

    private final SQLiteDatabase db;

    public MaterialCacheDao(Context context) {
        db = DBHelper.getInstance(context).getWritableDatabase();
    }

    public void replaceAll(List<Material> materiais) {
        db.beginTransaction();
        try {
            db.delete(DBHelper.TABLE_MATERIAL_CACHE, null, null);
            if (materiais != null) {
                for (Material material : materiais) {
                    db.insert(DBHelper.TABLE_MATERIAL_CACHE, null, toValues(material));
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Material> listarTodos() {
        List<Material> lista = new ArrayList<>();
        Cursor cursor = db.query(
                DBHelper.TABLE_MATERIAL_CACHE,
                null,
                null,
                null,
                null,
                null,
                DBHelper.MaterialCacheColumns.CODIGO + " ASC"
        );
        try {
            while (cursor.moveToNext()) {
                lista.add(fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    public boolean isEmpty() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_MATERIAL_CACHE, null);
        try {
            return !cursor.moveToFirst() || cursor.getLong(0) == 0;
        } finally {
            cursor.close();
        }
    }

    private ContentValues toValues(Material material) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.MaterialCacheColumns.CODIGO, material.codigo);
        values.put(DBHelper.MaterialCacheColumns.DESCRICAO, material.descricao);
        values.put(DBHelper.MaterialCacheColumns.UMB, material.umb);
        values.put(DBHelper.MaterialCacheColumns.TIPO, material.tipo);
        values.put(DBHelper.MaterialCacheColumns.LP, material.lp);
        values.put(DBHelper.MaterialCacheColumns.SERIAL, material.serial);
        values.put(DBHelper.MaterialCacheColumns.VALOR_UNITARIO, material.valor_unitario);
        return values;
    }

    private Material fromCursor(Cursor cursor) {
        Material material = new Material();
        material.codigo = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.MaterialCacheColumns.CODIGO));
        material.descricao = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.MaterialCacheColumns.DESCRICAO));
        material.umb = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.MaterialCacheColumns.UMB));
        material.tipo = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.MaterialCacheColumns.TIPO));
        material.lp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.MaterialCacheColumns.LP));
        material.serial = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.MaterialCacheColumns.SERIAL));
        material.valor_unitario = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.MaterialCacheColumns.VALOR_UNITARIO));
        return material;
    }
}
