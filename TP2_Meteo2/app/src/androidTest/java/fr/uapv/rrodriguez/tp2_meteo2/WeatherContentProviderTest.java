package fr.uapv.rrodriguez.tp2_meteo2;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import fr.uapv.rrodriguez.tp2_meteo2.model.City;
import fr.uapv.rrodriguez.tp2_meteo2.provider.WeatherContentProvider;

/**
 * Created by Ricci on 23/10/2017.
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
        String mime = mock.getType(Uri.parse(URI_BASE));
        assertEquals(mime, "vnd.android.cursor.item/vnd.fr.uapv.rrodriguez.tp2_meteo2.provider.weather");
    }

    public void testInsert() throws Exception {
        MockContentResolver mock = this.getMockContentResolver();
        mock.addProvider("weather", this.getProvider());

        ContentValues values = new ContentValues();
        values.put("nom", "Nantes");
        values.put("pays", "France");
        values.put("date", "23 Oct 2017 1:26");
        values.put("vent", "10 (NE)");
        values.put("pression", 100);
        values.put("temp", 15.0);
        Uri uri = mock.insert(Uri.parse(URI_BASE), values);
        assertEquals(uri.toString(), URI_BASE + "/France/Nantes");

        values = new ContentValues();
        values.put("nom", "Paysandú");
        values.put("pays", "Uruguay");
        values.put("date", "23 Oct 2017 1:26");
        values.put("vent", "25 (SW)");
        values.put("pression", 100);
        values.put("temp", 25.3);
        uri = mock.insert(Uri.parse(URI_BASE), values);
        assertEquals(uri.toString(), URI_BASE + "/Uruguay/Paysandú");
    }

    public void testUpdate() throws Exception {
        MockContentResolver mock = this.getMockContentResolver();
        mock.addProvider("weather", this.getProvider());

        ContentValues values = new ContentValues();
        values.put("nom", "Nantes");
        values.put("pays", "France");
        values.put("date", "23 Oct 2017 1:26");
        values.put("vent", "10 (NE)");
        values.put("pression", 100);
        values.put("temp", 15.0);
        Uri uri = mock.insert(Uri.parse(URI_BASE), values);

        // Test URI ".../weather/country/city"
        values.put("date", "25 Oct 2017 10:00");
        values.put("vent", "2 (NW)");
        values.put("pression", 200);
        values.put("temp", 22.0);
        int rows = mock.update(uri, values, "", null);
        assertEquals(rows, 1);

        // Test URI ".../weather"
        values.put("date", "26 Oct 2017 20:18");
        values.put("vent", "5 (W)");
        values.put("pression", 150);
        values.put("temp", 12.0);

        String[] whereValues1 = {"France", "Nantes"};
        rows = mock.update(Uri.parse(URI_BASE), values, "pays=? AND nom=?", whereValues1);
        assertEquals(rows, 1);

        // Test URI ".../weather/country/city" avec d'autres filtres
        values.put("date", "27 Oct 2017 13:30");
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

        // Test URI ".../weather"
        mock.insert(Uri.parse(URI_BASE), values);
        String[] whereValues1 = {"France", "Marseille"};
        rows = mock.delete(Uri.parse(URI_BASE), "pays=? AND nom=?", whereValues1);
        assertEquals(rows, 1);

        // Test URI ".../weather/country/city" avec d'autres filtres
        uri = mock.insert(Uri.parse(URI_BASE), values);
        String[] whereValues2 = {"Espagne"};
        rows = mock.delete(uri, "pays=?", whereValues2);
        assertEquals(rows, 0);
    }

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

        // Test URI ".../weather"
        String[] whereValues1 = {"Angleterre", "Londres"};
        cursor = mock.query(Uri.parse(URI_BASE), null, "pays=? AND nom=?", whereValues1, "");
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getString(cursor.getColumnIndex("nom")), "Londres");

        // Test URI ".../weather/country/city" avec d'autres filtres
        String[] whereValues2 = {"Espagne"};
        cursor = mock.query(uri, null, "pays=?", whereValues2, "");
        assertFalse(cursor.moveToFirst());
    }
}
