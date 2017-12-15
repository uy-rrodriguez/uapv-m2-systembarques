package fr.uapv.rrodriguez.tp2_meteo2.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by uy.rrodriguez on 20/10/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_FILE_NAME = "db-tp2-meteo2.sql";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION, null);
    }

    /**
     * Méthode appelée lors de la création d'un fichier de BDD SQLite.
     * On va créer les tables correspondantes.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;
        SQLiteStatement st;

        // Table weather
        sql = "CREATE TABLE weather(" +
                "   _id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   nom            VARCHAR(20)," +
                "   pays           VARCHAR(20)," +
                "   dernierReleve  DATE," +
                "   vent           VARCHAR(10)," +
                "   pression       INTEGER," +
                "   temp           FLOAT," +
                "   UNIQUE         (nom, pays)" +
                ")";
        st = db.compileStatement(sql);
        st.execute();
    }

    /**
     * Méthode appelée lors d'un upgrade de la BDD (changement de version).
     * Pour ce TP 2 cette méthode ne fait rien, on ne va pas s'amuser a changer la version de la BDD :)
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Rien
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        
        // Hack pour recréer la table weather
        boolean debug = false;
        if (debug) {
            String sql;
            SQLiteStatement st;

            // Table weather
            sql = "DROP TABLE weather;";
            st = db.compileStatement(sql);
            st.execute();

            onCreate(db);
        }
    }
}
