package fr.uapv.rrodriguez.tp2_meteo2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by uy.rrodriguez on 22/10/2017.
 */

public class CityDAO {
    private static final String TABLE_NAME = "weather";

    public static ContentValues cityToContentValues(City city) {
        ContentValues values = new ContentValues();
        //values.put("_ID", city.getId());
        values.put("nom", city.getNom());
        values.put("pays", city.getPays());
        values.put("dernierReleve", city.getDernierReleve());
        values.put("vent", city.getVent());
        values.put("pression", city.getPression());
        values.put("temp", city.getTemp());
        return values;
    }

    public static City cursorToCity(Cursor cursor) {
        City city = new City("", "");
        city.setId(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
        city.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
        city.setPays(cursor.getString(cursor.getColumnIndexOrThrow("pays")));
        city.setDernierReleve(cursor.getString(cursor.getColumnIndexOrThrow("dernierReleve")));
        city.setVent(cursor.getString(cursor.getColumnIndexOrThrow("vent")));
        city.setPression(cursor.getInt(cursor.getColumnIndexOrThrow("pression")));
        city.setTemp(cursor.getFloat(cursor.getColumnIndexOrThrow("temp")));

        Log.d("TP2-Meteo2", "CityDAO.cursorToCity " + city);

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
            city.setId(id);
        }
        catch (SQLException sqlex) {
            Log.e("TP2-Meteo2", "Erreur pour insérer la ville " + city + " : " + sqlex.getMessage());
        }
        finally {
            conn.close();
        }

        Log.d("TP2-Meteo2", "CityDAO.insert " + city);
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
        Log.d("TP2-Meteo2", "CityDAO.update " + city);

        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getWritableDatabase();

        String[] whereValues = {"" + city.getId()};
        int rows = conn.update(TABLE_NAME, cityToContentValues(city), "_ID = ?", whereValues);

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
        Log.d("TP2-Meteo2", "CityDAO.delete " + city);

        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getWritableDatabase();

        String[] whereValues = {"" + city.getId()};
        int rows = conn.delete(TABLE_NAME, "_ID = ?", whereValues);

        conn.close();

        // Verification des lignes modifiees
        if (rows != 1) {
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
        Log.d("TP2-Meteo2", "CityDAO.list");

        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getReadableDatabase();
        Cursor cursor = conn.query(TABLE_NAME, null, "", null, "", "", "");

        List<City> liste = new ArrayList<>();
        cursor.moveToFirst();
        while (! cursor.isAfterLast()) {
            liste.add(cursorToCity(cursor));
            cursor.moveToNext();
        }

        conn.close();
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
        City city = null;

        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getReadableDatabase();
        String[] whereValues = {"" + id};
        Cursor cursor = conn.query(TABLE_NAME, null, "_ID = ?", whereValues, "", "", "");

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            city = cursorToCity(cursor);
        }

        conn.close();
        return city;
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
        List<City> liste = new ArrayList<>();

        DBHelper db = new DBHelper(context);
        SQLiteDatabase conn = db.getReadableDatabase();
        Cursor cursor = conn.query(TABLE_NAME, null, selection, selectionArgs, "", "", "");

        cursor.moveToFirst();
        while (! cursor.isAfterLast()) {
            liste.add(cursorToCity(cursor));
            cursor.moveToNext();
        }

        conn.close();
        return liste;
    }
}
