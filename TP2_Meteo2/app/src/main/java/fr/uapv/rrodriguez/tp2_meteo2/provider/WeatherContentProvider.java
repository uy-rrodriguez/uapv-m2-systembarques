package fr.uapv.rrodriguez.tp2_meteo2.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import fr.uapv.rrodriguez.tp2_meteo2.model.City;
import fr.uapv.rrodriguez.tp2_meteo2.model.DBHelper;

public class WeatherContentProvider extends ContentProvider {
    private DBHelper dbhelper;

    private static final String TABLE_NAME = "weather";
    private static final String AUTHORITY = "fr.uapv.rrodriguez.tp2_meteo2.provider";
    private static final int URI_TYPE_DIR = 1;
    private static final int URI_TYPE_ITEM = 2;

    // Objet UriMatcher pour gérer les diiférentes URI.
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * URI pour accéder à l'ensemble de la BDD.
         */
        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME, URI_TYPE_DIR);

        /*
         * URI pour accéder à une ville en particulier.
         * Ex. : content://<authority>/weather/Uruguay/Montevideo
         */
        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME + "/*/*", URI_TYPE_ITEM);
    }

    public WeatherContentProvider() {
    }

    @Override
    public boolean onCreate() {
        dbhelper = new DBHelper(this.getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case URI_TYPE_DIR:
                return ContentResolver.CURSOR_DIR_BASE_TYPE;

            case URI_TYPE_ITEM:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE;

            default:
                return ContentResolver.CURSOR_DIR_BASE_TYPE;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Modification des filtres par rapport à l'URI donnée
        switch (URI_MATCHER.match(uri)) {
            case URI_TYPE_DIR:
                // Aucun changement aux filtres
                break;

            case URI_TYPE_ITEM:
                // On ajoute des filtres pour le pays et la ville à la fin du string de séléction
                if (selection.trim().isEmpty()) {
                    selection = "pays=? AND nom=?";
                }
                else {
                    selection += " AND pays=? AND nom=?";
                }

                // Extraction du pays et de la ville depuis l'URI
                List<String> seg = uri.getPathSegments();
                String pays = seg.get(seg.size()-2);
                String nom = seg.get(seg.size()-1);

                String[] args;
                if (selectionArgs == null) {
                    args = new String[2];
                }
                else {
                    args = Arrays.copyOf(selectionArgs, selectionArgs.length+2);
                }

                args[args.length-2] = pays;
                args[args.length-1] = nom;
                selectionArgs = args;

                break;
        }

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, "", "", sortOrder);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        /*
         * Pour l'insertion on va ignorer le type d'URI donnée, on va toujours aller chercher
         * les données dans le ContentValues.
         */

        int id = -1;
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        try {
            id = (int) db.insertOrThrow(TABLE_NAME, null, values);
        }
        catch (SQLException sqlex) {
            throw sqlex;
        }
        finally {
            db.close();
        }

        String nom = values.getAsString("nom");
        String pays = values.getAsString("pays");
        return Uri.parse(uri.toString() + "/" + pays + "/" + nom);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rows = 0;

        // Récupération des lignes à modifier
        Cursor cursor = this.query(uri, null, selection, selectionArgs, "");

        // Modification des éléments trouvés
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        cursor.moveToFirst();

        while (! cursor.isAfterLast()) {
            String[] whereValues = {"" + cursor.getInt(cursor.getColumnIndex("_ID"))};
            rows += db.update(TABLE_NAME, values, "_ID = ?", whereValues);
            cursor.moveToNext();
        }

        db.close();
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows = 0;

        // Récupération des lignes à supprimer
        Cursor cursor = this.query(uri, null, selection, selectionArgs, "");

        // Suppression des éléments trouvés
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        cursor.moveToFirst();

        while (! cursor.isAfterLast()) {
            String[] whereValues = {"" + cursor.getInt(cursor.getColumnIndex("_ID"))};
            rows += db.delete(TABLE_NAME, "_ID = ?", whereValues);
            cursor.moveToNext();
        }

        db.close();
        return rows;
    }
}
