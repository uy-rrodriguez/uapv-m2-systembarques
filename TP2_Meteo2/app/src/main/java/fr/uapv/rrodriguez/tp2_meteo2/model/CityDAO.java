package fr.uapv.rrodriguez.tp2_meteo2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricci on 22/10/2017.
 */

public class CityDAO {
    private static final String TABLE_NAME = "weather";

    private static ContentValues cityToContentValues(City city) {
        ContentValues values = new ContentValues();
        values.put("nom", city.getNom());
        values.put("pays", city.getPays());
        values.put("dernierReleve", city.getDernierReleve());
        values.put("vent", city.getVent());
        values.put("pression", city.getPression());
        values.put("temp", city.getTemp());
        return values;
    }

    private static City cursorToCity(Cursor cursor) {
        City city = new City("", "");
        city.setNom(cursor.getString(cursor.getColumnIndex("nom")));
        city.setPays(cursor.getString(cursor.getColumnIndex("pays")));
        city.setDernierReleve(cursor.getString(cursor.getColumnIndex("dernierReleve")));
        city.setVent(cursor.getString(cursor.getColumnIndex("vent")));
        city.setPression(cursor.getInt(cursor.getColumnIndex("pression")));
        city.setTemp(cursor.getFloat(cursor.getColumnIndex("temp")));
        return city;
    }

    /**
     * Insère une ville dans la BDD.
     *
     * @param context
     * @param city
     * @return Retourne l'id de la ville créée
     */
    public static int insert(Context context, City city) {
        int id = -1;
        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getWritableDatabase();

        try {
            id = (int) conn.insertOrThrow(TABLE_NAME, null, cityToContentValues(city));
        }
        catch (SQLException sqlex) {
            Log.e("TP2-Meteo2", "Erreur pour insérer la ville " + city + " : " + sqlex.getMessage());
        }
        finally {
            conn.close();
        }
        return id;
    }

    /**
     * Modifie une ville en BDD.
     *
     * @param context
     * @param city
     * @return true si la ville a été bien modifiée, false dans le cas contraire.
     */
    public static boolean update(Context context, City city) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getWritableDatabase();

        String[] whereValues = {"" + city.getId()};
        int rows = conn.update(TABLE_NAME, cityToContentValues(city), "id = ?", whereValues);

        conn.close();

        // Verification des lignes modifiees
        if (rows != 1) {
            Log.e("TP2-Meteo2", "Erreur pour actualiser la ville " + city + " : Lignes modifiées " + rows);
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Supprime une ville en BDD.
     *
     * @param context
     * @param city
     * @return true si la ville a été bien supprimée, false dans le cas cotraire.
     */
    public static boolean delete(Context context, City city) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getWritableDatabase();

        String[] whereValues = {"" + city.getId()};
        int rows = conn.delete(TABLE_NAME, "id = ?", whereValues);

        conn.close();

        // Verification des lignes modifiees
        if (rows != 0) {
            Log.e("TP2-Meteo2", "Erreur pour supprimer la ville " + city + " : Lignes modifiées " + rows);
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Retourne la liste complète de villes en BDD.
     *
     * @param context
     * @return
     */
    public static List<City> list(Context context) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getReadableDatabase();
        Cursor cursor = conn.query(TABLE_NAME, null, "", null, "", "", "");

        conn.close();

        List<City> liste = new ArrayList<>();
        cursor.moveToFirst();
        while (! cursor.isAfterLast()) {
            liste.add(cursorToCity(cursor));
            cursor.moveToNext();
        }

        return liste;
    }

    /**
     * Retourne une ville cherchée par son id.
     *
     * @param context
     * @param id
     * @return
     */
    public static City get(Context context, int id) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getReadableDatabase();
        String[] whereValues = {"" + id};
        Cursor cursor = conn.query(TABLE_NAME, null, "id = ?", whereValues, "", "", "");

        conn.close();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursorToCity(cursor);
        }
        else {
            return null;
        }
    }

    /**
     * Retourne une liste de villes en BDD filtrée d'après les paramètres donnés.
     *
     * @param context
     * @param selection Filtres à appliquer, écrits sous forme d'un WHERE en SQL, sans le mot WHERE.
     * @param selectionArgs Valeurs des colonnes faisant partie du filtre. Les ? dans selection
     *                      seront remplacées par les valeurs dans ce tableau.
     * @return
     */
    public static List<City> select(Context context, String selection, String[] selectionArgs) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getReadableDatabase();
        Cursor cursor = conn.query(TABLE_NAME, null, selection, selectionArgs, "", "", "");

        conn.close();

        List<City> liste = new ArrayList<>();
        cursor.moveToFirst();
        while (! cursor.isAfterLast()) {
            liste.add(cursorToCity(cursor));
            cursor.moveToNext();
        }

        return liste;
    }
}
