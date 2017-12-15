package fr.uapv.rrodriguez.tp2_meteo2.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.util.List;

import fr.uapv.rrodriguez.tp2_meteo2.model.City;
import fr.uapv.rrodriguez.tp2_meteo2.model.CityDAO;
import fr.uapv.rrodriguez.tp2_meteo2.util.JSONResponseHandler;
import fr.uapv.rrodriguez.tp2_meteo2.util.WSUtil;

/**
 * Created by uy.rrodriguez on 06/11/2017.
 *
 * Basé sur
 *  https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public class YahooWeatherSyncAdapter extends AbstractThreadedSyncAdapter {

    ContentResolver mContentResolver;
    JSONResponseHandler jsonHandler;

    public YahooWeatherSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        jsonHandler = new JSONResponseHandler();
    }

    /**
     * Constructeur pour la ompatibilité avec Android 3.0+
     */
    public YahooWeatherSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        jsonHandler = new JSONResponseHandler();
    }

    /**
     * Réalise la syncrhonisation de données entre le WS Yahoo Weather et l'application.
     *
     * @param account       Ignoré, pas nécessaire pour Yahoo Weather
     * @param extras        Objet contenant le pays et la ville concernés par la requête
     * @param authority     Autorité du provider à contacter pour stocker les données reçues du WS
     * @param provider      Provider associé au paramètre authority
     * @param syncResult    Objet pour communiquer avec le sync adapter framework
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d("TP2_Meteo", "SyncAdapter.onPerformSync");

        // URI du provider à contacter pour stocker les données en BDD
        String uri = "content://" + authority + "/weather";
        
        // Récupération des villes en BDD
        try {
            Cursor c = provider.query(Uri.parse(uri), null, "", null, "");
            c.moveToFirst();

            // Ensuite, on va faire appel au WS pour chaque ville
            while (! c.isAfterLast()) {
                City ville = CityDAO.cursorToCity(c);
                Log.d("TP2_Meteo", "SyncAdapter.onPerformSync : Ville objet = " + ville);
                
                HttpURLConnection con = null;
                try {
                    String urlYahoo = WSUtil.getURL(ville.getNom(), ville.getPays());
                    Log.d("TP2_Meteo", "SyncAdapter.onPerformSync : URI Yahoo " + urlYahoo);
                    
                    URL url = new URL(urlYahoo);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    // Récupération de la réponse
                    List<String> reponse = jsonHandler.handleResponse(con.getInputStream(), "UTF-8");

                    // Si la réponse est vide, on arrête le traitement. Sinon on continue.
                    if (reponse == null || reponse.isEmpty()) {
                        Log.d("TP2_Meteo", "SyncAdapter.onPerformSync : Le WS à retourné une réponse vide");
                        continue;
                    }

                    // (wind, temperature, pressure, time)
                    String vent             = reponse.get(0);
                    float temp              = Float.parseFloat(reponse.get(1));
                    int pression            = (int) Float.parseFloat(reponse.get(2));
                    String dernierReleve    = reponse.get(3);


                    /* *** Stockage de la réponse en BDD *** */

                    // Si tout va bien, actualisation de la ville en BDD
                    ContentValues values = new ContentValues();
                    values.put("vent",          vent);
                    values.put("temp",          temp);
                    values.put("pression",      pression);
                    values.put("dernierReleve", dernierReleve);

                    String where = "_id = ?";
                    String[] whereValues = {"" + ville.getId()};
                    
                    provider.update(Uri.parse(uri), values, where, whereValues);
                }

                catch (Exception e) {
                    Log.e("TP2_Meteo", "SyncAdapter.onPerformSync : " + e.getMessage(), e);
                }

                finally {
                    c.moveToNext();
                    
                    if (con != null)
                        con.disconnect();
                }
            }
            
        } // try de BDD

        catch (Exception e) {
            Log.e("TP2_Meteo", "SyncAdapter.onPerformSync : " + e.getMessage(), e);
        }
    }
}
