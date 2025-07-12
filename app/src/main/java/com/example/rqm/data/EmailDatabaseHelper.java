// EmailDatabaseHelper.java
package com.example.rqm.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por gerenciar o banco de dados SQLite nativo para os e-mails de destino.
 */
public class EmailDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "requisicoes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EMAILS = "emails";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ENDERECO = "endereco";

    public EmailDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_EMAILS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ENDERECO + " TEXT NOT NULL)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMAILS);
        onCreate(db);
    }

    /**
     * Insere um novo e-mail no banco.
     */
    public void adicionarEmail(String endereco) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ENDERECO, endereco);
        db.insert(TABLE_EMAILS, null, values);
        db.close();
    }

    /**
     * Retorna todos os e-mails armazenados.
     */
    public List<String> listarEmails() {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMAILS, new String[]{COLUMN_ENDERECO},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    /**
     * Remove todos os e-mails (opcional).
     */
    public void limparEmails() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EMAILS, null, null);
        db.close();
    }
}
