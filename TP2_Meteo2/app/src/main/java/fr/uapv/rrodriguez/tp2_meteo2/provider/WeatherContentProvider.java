package fr.uapv.rrodriguez.tp2_meteo2.provider;

import android.content.ContentProvider;
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
    private static final String MIME = "vnd.android.cursor.item/vnd.fr.uapv.rrodriguez.tp2_meteo2.provider." + TABLE_NAME;
    private static final int URI_WEATHER = 1;
    private static final int URI_WEATHER_ROW = 2;

    // Objet UriMatcher pour gérer les diiférentes URI.
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * URI pour accéder à l'ensemble de la BDD.
         */
        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME, URI_WEATHER);

        /*
         * URI pour accéder à une ville en particulier.
         * Ex. : content://<authority>/weather/Uruguay/Montevideo
         */
        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME + "/*/*", URI_WEATHER_ROW);
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
        return MIME;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
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

        //return ContentUris.withAppendedId(uri, id);
        String nom = values.getAsString("nom");
        String pays = values.getAsString("pays");
        return Uri.parse(uri.toString() + "/" + pays + "/" + nom);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        int rows = db.update(TABLE_NAME, values, selection, selectionArgs);
        db.close();
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows = 0;
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        Cursor cursor = this.query(uri, null, selection, selectionArgs, "");

        List<City> liste = new ArrayList<>();
        cursor.moveToFirst();
        while (! cursor.isAfterLast()) {
            String[] whereValues = {"" + cursor.getInt(cursor.getColumnIndex("_ID"))};
            rows += db.delete(TABLE_NAME, "id = ?", whereValues);
            cursor.moveToNext();
        }

        db.close();
        return rows;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Initialisation des paramètres de la requête par rapport à l'URI donnée
        switch (URI_MATCHER.match(uri)) {
            case URI_WEATHER:
                // Aucun filtre en particulier
                break;

            case URI_WEATHER_ROW:
                if (selection.trim().isEmpty()) {
                    selection = "pays=? AND nom=?";
                }
                else {
                    selection += " AND pays=? AND nom=?";
                }

                // Extraction du pays et ville de l'URI
                List<String> seg = uri.getPathSegments();
                String[] args = Arrays.copyOf(selectionArgs, selectionArgs.length+2);
                args[args.length-2] = seg.get(seg.size()-2);
                args[args.length-1] = seg.get(seg.size()-1);
                selectionArgs = args;

                break;
        }

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, "", "", sortOrder);
        return cursor;
    }
}
