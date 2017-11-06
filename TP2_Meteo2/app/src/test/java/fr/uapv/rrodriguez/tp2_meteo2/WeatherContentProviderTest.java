package fr.uapv.rrodriguez.tp2_meteo2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.uapv.rrodriguez.tp2_meteo2.provider.WeatherContentProvider;

/**
 * Created by uy.rrodriguez on 23/10/2017.
 */

public class WeatherContentProviderTest extends ProviderTestCase2 {

    private static final String AUTHORITY = "fr.uapv.rrodriguez.tp2_meteo2.provider";
    private static final String URI_BASE = "content://" + AUTHORITY + "/weather";

    public WeatherContentProviderTest() {
        super(WeatherContentProvider.class, AUTHORITY);
    }

    public void testCreate() throws Exception {
        MockContentResolver mock = this.getMockContentResolver();
        mock.addProvider("weather", this.getProvider());

        // MIME pour l'ensemble de la BDD
        String mime = mock.getType(Uri.parse(URI_BASE));
        assertEquals(mime, ContentResolver.CURSOR_DIR_BASE_TYPE);

        // MIME pour une ligne de la table weather
        mime = mock.getType(Uri.parse(URI_BASE + "/Montevideo/Uruguay"));
        assertEquals(mime, ContentResolver.CURSOR_ITEM_BASE_TYPE);
    }

    /**
     * Le test de query teste en quelque sorte une insertion basique d'une ligne, puisqu'il faut
     * d'abord créer la ligne en BDD avant de pouvoir récupérer ses données.
     *
     * @throws Exception
     */
    public void testQuery() throws Exception {
        MockContentResolver mock = this.getMockContentResolver();
        mock.addProvider("weather", this.getProvider());

        ContentValues values = new ContentValues();
        values.put("nom", "Londres");
        values.put("pays", "Angleterre");
        Uri uri = mock.insert(Uri.parse(URI_BASE), values);

        // Test URI ".../weather/country/city"
        Cursor cursor = mock.query(uri, null, "", null, "");
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getString(cursor.getColumnIndex("nom")), "Londres");
        assertEquals(cursor.getString(cursor.getColumnIndex("pays")), "Angleterre");

        // Test URI ".../weather"
        String[] whereValues1 = {"Angleterre", "Londres"};
        cursor = mock.query(Uri.parse(URI_BASE), null, "pays=? AND nom=?", whereValues1, "");
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getString(cursor.getColumnIndex("nom")), "Londres");
        assertEquals(cursor.getString(cursor.getColumnIndex("pays")), "Angleterre");

        // Test URI ".../weather/country/city" avec d'autres filtres
        String[] whereValues2 = {"Espagne"};
        cursor = mock.query(uri, null, "pays=?", whereValues2, "");
        assertFalse(cursor.moveToFirst());
    }


    /**
     * Méthode utilitaire pour comparer l'information dans un Cursor retournée par une query,
     * avec le ContentValues utilisé pour modifier la BDD.
     *
     * @param cursor
     * @param values
     */
    private void comparerCursorAvecContentValues(Cursor cursor, ContentValues values) {
        assertEquals(values.getAsString("nom"),             cursor.getString(cursor.getColumnIndexOrThrow("nom")));
        assertEquals(values.getAsString("pays"),            cursor.getString(cursor.getColumnIndexOrThrow("pays")));
        assertEquals(values.getAsString("dernierReleve"),   cursor.getString(cursor.getColumnIndexOrThrow("dernierReleve")));
        assertEquals(values.getAsString("vent"),            cursor.getString(cursor.getColumnIndexOrThrow("vent")));
        assertEquals((int) values.getAsInteger("pression"), cursor.getInt(cursor.getColumnIndexOrThrow("pression")));
        assertEquals(values.getAsFloat("temp"),             cursor.getFloat(cursor.getColumnIndexOrThrow("temp")));
    }

    public void testInsert() throws Exception {
        MockContentResolver mock = this.getMockContentResolver();
        mock.addProvider("weather", this.getProvider());

        // Création des données de test
        List<ContentValues> testValues = new ArrayList<ContentValues>();

        // Première ville, Nantes
        ContentValues values = new ContentValues();
        values.put("nom", "Nantes");
        values.put("pays", "France");
        values.put("dernierReleve", "23 Oct 2017 1:26");
        values.put("vent", "10 (NE)");
        values.put("pression", 100);
        values.put("temp", 15.0);
        testValues.add(values);

        // Deuxième ville, Paysandú, Uruguay
        values = new ContentValues();
        values.put("nom", "Paysandú");
        values.put("pays", "Uruguay");
        values.put("dernierReleve", "23 Oct 2017 1:26");
        values.put("vent", "25 (SW)");
        values.put("pression", 100);
        values.put("temp", 25.3);
        testValues.add(values);

        // Tests en boucle
        Iterator<ContentValues> it = testValues.iterator();
        while (it.hasNext()) {
            values = it.next();

            Uri uri = mock.insert(Uri.parse(URI_BASE), values);

            // Vérification URI
            assertEquals(uri.toString(), URI_BASE + "/" + values.getAsString("pays") + "/" + values.getAsString("nom"));

            // Vérification données
            Cursor cursor = mock.query(uri, null, "", null, "");
            assertTrue(cursor.moveToFirst());
            comparerCursorAvecContentValues(cursor, values);
        }
    }

    public void testUpdate() throws Exception {
        MockContentResolver mock = this.getMockContentResolver();
        mock.addProvider("weather", this.getProvider());

        // Insertion initiale de la ville de test
        ContentValues values = new ContentValues();
        values.put("nom", "Nantes");
        values.put("pays", "France");
        values.put("dernierReleve", "23 Oct 2017 1:26");
        values.put("vent", "10 (NE)");
        values.put("pression", 100);
        values.put("temp", 15.0);
        Uri uri = mock.insert(Uri.parse(URI_BASE), values);

        // Test URI ".../weather/country/city"
        values.put("dernierReleve", "25 Oct 2017 10:00");
        values.put("vent", "2 (NW)");
        values.put("pression", 200);
        values.put("temp", 22.0);

        int rows = mock.update(uri, values, "", null);
        assertEquals(rows, 1);

        // Recherche de l'objet modifié et comparaison des données
        Cursor cursor = mock.query(uri, null, "", null, "");
        assertTrue(cursor.moveToFirst());
        comparerCursorAvecContentValues(cursor, values);


        // Test URI ".../weather"
        values.put("dernierReleve", "26 Oct 2017 20:18");
        values.put("vent", "5 (W)");
        values.put("pression", 150);
        values.put("temp", 12.0);

        String[] whereValues1 = {"France", "Nantes"};
        rows = mock.update(Uri.parse(URI_BASE), values, "pays=? AND nom=?", whereValues1);
        assertEquals(rows, 1);

        cursor = mock.query(uri, null, "", null, "");
        assertTrue(cursor.moveToFirst());
        comparerCursorAvecContentValues(cursor, values);


        // Test URI ".../weather/country/city" avec d'autres filtres
        values.put("dernierReleve", "27 Oct 2017 13:30");
        values.put("vent", "8 (SW)");
        values.put("pression", 100);
        values.put("temp", 25.0);

        String[] whereValues2 = {"Espagne"};
        rows = mock.update(uri, values, "pays=?", whereValues2);
        assertEquals(rows, 0);
    }

    public void testDelete() throws Exception {
        MockContentResolver mock = this.getMockContentResolver();
        mock.addProvider("weather", this.getProvider());

        ContentValues values = new ContentValues();
        values.put("nom", "Marseille");
        values.put("pays", "France");
        Uri uri = mock.insert(Uri.parse(URI_BASE), values);

        // Test URI ".../weather/country/city"
        int rows = mock.delete(uri, "", null);
        assertEquals(rows, 1);

        // Recherche du même objet, un curseur vide doit être retourné
        Cursor cursor = mock.query(uri, null, "", null, "");
        assertFalse(cursor.moveToFirst());


        // Test URI ".../weather"
        mock.insert(Uri.parse(URI_BASE), values);
        String[] whereValues1 = {"France", "Marseille"};
        rows = mock.delete(Uri.parse(URI_BASE), "pays=? AND nom=?", whereValues1);
        assertEquals(rows, 1);

        cursor = mock.query(uri, null, "", null, "");
        assertFalse(cursor.moveToFirst());


        // Test URI ".../weather/country/city" avec d'autres filtres
        uri = mock.insert(Uri.parse(URI_BASE), values);
        String[] whereValues2 = {"Espagne"};
        rows = mock.delete(uri, "pays=?", whereValues2);
        assertEquals(rows, 0);
    }
}
