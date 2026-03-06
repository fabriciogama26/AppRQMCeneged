package com.example.rqm.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "rqm.db";
    public static final int DB_VERSION = 7;

    public static final String TABLE_REQUISICOES = "requisicoes";
    public static final String TABLE_SYNC_RUNS = "sync_runs";
    public static final String TABLE_MATERIAL_CACHE = "material_cache";
    public static final String TABLE_RESPONSAVEIS_CACHE = "responsaveis_cache";

    private static DBHelper instance;

    public static final class RequisicaoColumns {
        public static final String ID = "id";
        public static final String REQUISITOR = "requisitor";
        public static final String PROJETO = "projeto";
        public static final String USUARIO = "usuario";
        public static final String DATA = "data";
        public static final String TIPO_OPERACAO = "tipo_operacao";
        public static final String OBSERVACAO = "observacao";
        public static final String MATERIAIS_JSON = "materiais_json";
        public static final String ORIGEM = "origem";
        public static final String DEVICE_ID = "device_id";
        public static final String CLIENT_REQUEST_ID = "client_request_id";
        public static final String SYNC_STATUS = "sync_status";
        public static final String LAST_SYNC_AT = "last_sync_at";
    }

    public static final class SyncRunColumns {
        public static final String ID = "id";
        public static final String SYNC_UUID = "sync_uuid";
        public static final String STARTED_AT = "started_at";
        public static final String FINISHED_AT = "finished_at";
        public static final String STATUS = "status";
        public static final String PENDING_TOTAL = "pending_total";
        public static final String PENDING_SENT = "pending_sent";
        public static final String MATERIALS_UPDATED = "materials_updated";
        public static final String CONFLICTS_FOUND = "conflicts_found";
        public static final String ERRORS_COUNT = "errors_count";
        public static final String MESSAGE = "message";
        public static final String DEVICE_ID = "device_id";
        public static final String USER_ID = "user_id";
        public static final String TENANT_ID = "tenant_id";
        public static final String SOURCE = "source";
        public static final String UPLOADED_TO_SERVER = "uploaded_to_server";
        public static final String CREATED_AT = "created_at";
    }

    public static final class MaterialCacheColumns {
        public static final String CODIGO = "codigo";
        public static final String DESCRICAO = "descricao";
        public static final String UMB = "umb";
        public static final String TIPO = "tipo";
        public static final String LP = "lp";
        public static final String SERIAL = "serial";
        public static final String VALOR_UNITARIO = "valor_unitario";
    }

    public static final class ResponsavelColumns {
        public static final String REMOTE_ID = "remote_id";
        public static final String NOME = "nome";
        public static final String CARGO = "cargo";
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_REQUISICOES + " (" +
                RequisicaoColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RequisicaoColumns.REQUISITOR + " TEXT NOT NULL, " +
                RequisicaoColumns.PROJETO + " TEXT NOT NULL, " +
                RequisicaoColumns.USUARIO + " TEXT NOT NULL, " +
                RequisicaoColumns.DATA + " TEXT NOT NULL, " +
                RequisicaoColumns.TIPO_OPERACAO + " TEXT NOT NULL, " +
                RequisicaoColumns.OBSERVACAO + " TEXT, " +
                RequisicaoColumns.MATERIAIS_JSON + " TEXT NOT NULL, " +
                RequisicaoColumns.ORIGEM + " TEXT, " +
                RequisicaoColumns.DEVICE_ID + " TEXT, " +
                RequisicaoColumns.CLIENT_REQUEST_ID + " TEXT, " +
                RequisicaoColumns.SYNC_STATUS + " TEXT DEFAULT 'PENDING', " +
                RequisicaoColumns.LAST_SYNC_AT + " TEXT" +
                ");");

        db.execSQL("CREATE INDEX idx_requisicao_projeto ON " + TABLE_REQUISICOES +
                "(" + RequisicaoColumns.PROJETO + ")");
        db.execSQL("CREATE INDEX idx_requisicao_data ON " + TABLE_REQUISICOES +
                "(" + RequisicaoColumns.DATA + ")");
        db.execSQL("CREATE INDEX idx_requisicao_sync_status ON " + TABLE_REQUISICOES +
                "(" + RequisicaoColumns.SYNC_STATUS + ")");

        db.execSQL("CREATE TABLE " + TABLE_SYNC_RUNS + " (" +
                SyncRunColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SyncRunColumns.SYNC_UUID + " TEXT NOT NULL UNIQUE, " +
                SyncRunColumns.STARTED_AT + " TEXT NOT NULL, " +
                SyncRunColumns.FINISHED_AT + " TEXT, " +
                SyncRunColumns.STATUS + " TEXT NOT NULL, " +
                SyncRunColumns.PENDING_TOTAL + " INTEGER DEFAULT 0, " +
                SyncRunColumns.PENDING_SENT + " INTEGER DEFAULT 0, " +
                SyncRunColumns.MATERIALS_UPDATED + " INTEGER DEFAULT 0, " +
                SyncRunColumns.CONFLICTS_FOUND + " INTEGER DEFAULT 0, " +
                SyncRunColumns.ERRORS_COUNT + " INTEGER DEFAULT 0, " +
                SyncRunColumns.MESSAGE + " TEXT, " +
                SyncRunColumns.DEVICE_ID + " TEXT, " +
                SyncRunColumns.USER_ID + " TEXT, " +
                SyncRunColumns.TENANT_ID + " TEXT, " +
                SyncRunColumns.SOURCE + " TEXT, " +
                SyncRunColumns.UPLOADED_TO_SERVER + " INTEGER DEFAULT 0, " +
                SyncRunColumns.CREATED_AT + " TEXT NOT NULL" +
                ");");

        db.execSQL("CREATE INDEX idx_sync_runs_created_at ON " + TABLE_SYNC_RUNS +
                "(" + SyncRunColumns.CREATED_AT + " DESC)");
        db.execSQL("CREATE INDEX idx_sync_runs_uploaded ON " + TABLE_SYNC_RUNS +
                "(" + SyncRunColumns.UPLOADED_TO_SERVER + ")");

        criarTabelasCache(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_REQUISICOES +
                    " ADD COLUMN " + RequisicaoColumns.ORIGEM + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_REQUISICOES +
                    " ADD COLUMN " + RequisicaoColumns.DEVICE_ID + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_REQUISICOES +
                    " ADD COLUMN " + RequisicaoColumns.CLIENT_REQUEST_ID + " TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_REQUISICOES +
                    " ADD COLUMN " + RequisicaoColumns.SYNC_STATUS + " TEXT DEFAULT 'PENDING'");
            db.execSQL("ALTER TABLE " + TABLE_REQUISICOES +
                    " ADD COLUMN " + RequisicaoColumns.LAST_SYNC_AT + " TEXT");
            db.execSQL("UPDATE " + TABLE_REQUISICOES +
                    " SET " + RequisicaoColumns.SYNC_STATUS + " = 'PENDING' WHERE " + RequisicaoColumns.SYNC_STATUS + " IS NULL");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_requisicao_sync_status ON " + TABLE_REQUISICOES +
                    "(" + RequisicaoColumns.SYNC_STATUS + ")");
        }
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SYNC_RUNS + " (" +
                    SyncRunColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SyncRunColumns.SYNC_UUID + " TEXT NOT NULL UNIQUE, " +
                    SyncRunColumns.STARTED_AT + " TEXT NOT NULL, " +
                    SyncRunColumns.FINISHED_AT + " TEXT, " +
                    SyncRunColumns.STATUS + " TEXT NOT NULL, " +
                    SyncRunColumns.PENDING_TOTAL + " INTEGER DEFAULT 0, " +
                    SyncRunColumns.PENDING_SENT + " INTEGER DEFAULT 0, " +
                    SyncRunColumns.MATERIALS_UPDATED + " INTEGER DEFAULT 0, " +
                    SyncRunColumns.CONFLICTS_FOUND + " INTEGER DEFAULT 0, " +
                    SyncRunColumns.ERRORS_COUNT + " INTEGER DEFAULT 0, " +
                    SyncRunColumns.MESSAGE + " TEXT, " +
                    SyncRunColumns.DEVICE_ID + " TEXT, " +
                    SyncRunColumns.USER_ID + " TEXT, " +
                    SyncRunColumns.TENANT_ID + " TEXT, " +
                    SyncRunColumns.SOURCE + " TEXT, " +
                    SyncRunColumns.UPLOADED_TO_SERVER + " INTEGER DEFAULT 0, " +
                    SyncRunColumns.CREATED_AT + " TEXT NOT NULL" +
                    ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sync_runs_created_at ON " + TABLE_SYNC_RUNS +
                    "(" + SyncRunColumns.CREATED_AT + " DESC)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sync_runs_uploaded ON " + TABLE_SYNC_RUNS +
                    "(" + SyncRunColumns.UPLOADED_TO_SERVER + ")");
        }
        if (oldVersion < 6) {
            criarTabelasCache(db);
        }
        if (oldVersion < 7) {
            criarTabelasCache(db);
        }
    }

    public void clearAllData() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TABLE_REQUISICOES, null, null);
            db.delete(TABLE_SYNC_RUNS, null, null);
            db.delete(TABLE_MATERIAL_CACHE, null, null);
            db.delete(TABLE_RESPONSAVEIS_CACHE, null, null);
        } finally {
            db.close();
        }
    }

    public long getRequisicoesCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REQUISICOES, null)) {
            return cursor.moveToFirst() ? cursor.getLong(0) : 0;
        }
    }

    private void criarTabelasCache(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MATERIAL_CACHE + " (" +
                MaterialCacheColumns.CODIGO + " TEXT PRIMARY KEY, " +
                MaterialCacheColumns.DESCRICAO + " TEXT NOT NULL, " +
                MaterialCacheColumns.UMB + " TEXT, " +
                MaterialCacheColumns.TIPO + " TEXT, " +
                MaterialCacheColumns.LP + " TEXT, " +
                MaterialCacheColumns.SERIAL + " TEXT, " +
                MaterialCacheColumns.VALOR_UNITARIO + " TEXT" +
                ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_material_cache_descricao ON " + TABLE_MATERIAL_CACHE +
                "(" + MaterialCacheColumns.DESCRICAO + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RESPONSAVEIS_CACHE + " (" +
                ResponsavelColumns.REMOTE_ID + " TEXT PRIMARY KEY, " +
                ResponsavelColumns.NOME + " TEXT NOT NULL, " +
                ResponsavelColumns.CARGO + " TEXT" +
                ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_responsaveis_cache_nome ON " + TABLE_RESPONSAVEIS_CACHE +
                "(" + ResponsavelColumns.NOME + ")");
    }
}
