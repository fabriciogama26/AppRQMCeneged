package com.example.rqm.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Classe responsável por criar e gerenciar o banco de dados SQLite.
 * Define a tabela de requisições e fornece métodos utilitários para manutenção.
 */
public class DBHelper extends SQLiteOpenHelper {

    // Nome do banco de dados
    public static final String DB_NAME = "rqm.db";

    // Versão do banco de dados (incrementar quando houver mudanças na estrutura) Se você mudar a estrutura do banco (ex: adicionar coluna, tipo novo, etc.), aumente a versão
    public static final int DB_VERSION = 1;

    // Nome da tabela principal
    public static final String TABLE_REQUISICOES = "requisicoes";

    // Instância Singleton para evitar múltiplas conexões
    private static DBHelper instance;

    // Nomes das colunas da tabela de requisições
    public static final class RequisicaoColumns {
        public static final String ID = "id";
        public static final String REQUISITOR = "requisitor";
        public static final String PROJETO = "projeto";
        public static final String USUARIO = "usuario";
        public static final String DATA = "data";
        public static final String TIPO_OPERACAO = "tipo_operacao";
        public static final String OBSERVACAO = "observacao";
        public static final String MATERIAIS_JSON = "materiais_json";
    }

    /**
     * Método para obter uma instância única do DBHelper (padrão Singleton).
     */
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Construtor privado para forçar o uso do Singleton
    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Método chamado automaticamente quando o banco é criado pela primeira vez.
     * Define a estrutura da tabela e índices para melhorar performance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_REQUISICOES_TABLE = "CREATE TABLE " + TABLE_REQUISICOES + " (" +
                RequisicaoColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RequisicaoColumns.REQUISITOR + " TEXT NOT NULL, " +
                RequisicaoColumns.PROJETO + " TEXT NOT NULL, " +
                RequisicaoColumns.USUARIO + " TEXT NOT NULL, " +
                RequisicaoColumns.DATA + " TEXT NOT NULL, " +
                RequisicaoColumns.TIPO_OPERACAO + " TEXT NOT NULL, " +
                RequisicaoColumns.OBSERVACAO + " TEXT, " +
                RequisicaoColumns.MATERIAIS_JSON + " TEXT NOT NULL" +
                ");";

        // Executa a criação da tabela
        db.execSQL(SQL_CREATE_REQUISICOES_TABLE);

        // Cria índices para acelerar consultas por projeto e por data
        db.execSQL("CREATE INDEX idx_requisicao_projeto ON " + TABLE_REQUISICOES +
                "(" + RequisicaoColumns.PROJETO + ")");
        db.execSQL("CREATE INDEX idx_requisicao_data ON " + TABLE_REQUISICOES +
                "(" + RequisicaoColumns.DATA + ")");
    }

    /**
     * Chamado automaticamente quando a versão do banco de dados muda.
     * Ideal para realizar migrações de estrutura sem perder dados.
     */

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Exemplo: upgrade para a versão 2 com uma nova estrutura
        if (oldVersion < 2) {
            // 1. Renomeia a tabela antiga
            db.execSQL("ALTER TABLE " + TABLE_REQUISICOES + " RENAME TO requisicoes_backup");

            // 2. Cria a nova estrutura (modifique conforme necessidade futura)
            final String SQL_CREATE_REQUISICOES_TABLE = "CREATE TABLE " + TABLE_REQUISICOES + " (" +
                    RequisicaoColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RequisicaoColumns.REQUISITOR + " TEXT NOT NULL, " +
                    RequisicaoColumns.PROJETO + " TEXT NOT NULL, " +
                    RequisicaoColumns.USUARIO + " TEXT NOT NULL, " +
                    RequisicaoColumns.DATA + " TEXT NOT NULL, " +
                    RequisicaoColumns.TIPO_OPERACAO + " TEXT NOT NULL, " +
                    RequisicaoColumns.OBSERVACAO + " TEXT, " +
                    RequisicaoColumns.MATERIAIS_JSON + " TEXT NOT NULL" +
                    ");";
            db.execSQL(SQL_CREATE_REQUISICOES_TABLE);

            // 3. Copia os dados da tabela antiga para a nova
            db.execSQL("INSERT INTO " + TABLE_REQUISICOES + " (" +
                    RequisicaoColumns.ID + ", " +
                    RequisicaoColumns.REQUISITOR + ", " +
                    RequisicaoColumns.PROJETO + ", " +
                    RequisicaoColumns.USUARIO + ", " +
                    RequisicaoColumns.DATA + ", " +
                    RequisicaoColumns.TIPO_OPERACAO + ", " +
                    RequisicaoColumns.OBSERVACAO + ", " +
                    RequisicaoColumns.MATERIAIS_JSON +
                    ") SELECT " +
                    RequisicaoColumns.ID + ", " +
                    RequisicaoColumns.REQUISITOR + ", " +
                    RequisicaoColumns.PROJETO + ", " +
                    RequisicaoColumns.USUARIO + ", " +
                    RequisicaoColumns.DATA + ", " +
                    RequisicaoColumns.TIPO_OPERACAO + ", " +
                    RequisicaoColumns.OBSERVACAO + ", " +
                    RequisicaoColumns.MATERIAIS_JSON +
                    " FROM requisicoes_backup");

            // 4. Remove a tabela de backup
            db.execSQL("DROP TABLE requisicoes_backup");

            // 5. Recria os índices
            db.execSQL("CREATE INDEX idx_requisicao_projeto ON " + TABLE_REQUISICOES +
                    "(" + RequisicaoColumns.PROJETO + ")");
            db.execSQL("CREATE INDEX idx_requisicao_data ON " + TABLE_REQUISICOES +
                    "(" + RequisicaoColumns.DATA + ")");
        }
    }


    /**
     * Remove todos os dados da tabela de requisições.
     * Útil para testes ou limpeza completa.
     */
    public void clearAllData() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TABLE_REQUISICOES, null, null);
        } finally {
            db.close();
        }
    }

    /**
     * Retorna a quantidade de registros na tabela de requisições.
     */
    public long getRequisicoesCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REQUISICOES, null)) {
            return cursor.moveToFirst() ? cursor.getLong(0) : 0;
        }
    }
}
