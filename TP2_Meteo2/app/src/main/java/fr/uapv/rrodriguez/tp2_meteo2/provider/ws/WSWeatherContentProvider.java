package fr.uapv.rrodriguez.tp2_meteo2.provider.ws;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import fr.uapv.rrodriguez.tp2_meteo2.util.JSONResponseHandler;
import fr.uapv.rrodriguez.tp2_meteo2.util.WSUtil;

/**
 * Created by uy.rrodriguez on 06/11/2017.
 */
public class WSWeatherContentProvider extends ContentProvider {

    private JSONResponseHandler jsonHandler;
    private static final String AUTHORITY = "fr.uapv.rrodriguez.tp2_meteo2.provider.ws";
    private static final int URI_TYPE_ITEM = 1;

    // Objet UriMatcher pour vérifier le bon format de l'URI.
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * URI pour accéder à une ville en particulier.
         * Ex. : content://<authority>/weather/Uruguay/Montevideo
         */
        URI_MATCHER.addURI(AUTHORITY, "weather/*/*", URI_TYPE_ITEM);
    }

    @Override
    public boolean onCreate() {
        jsonHandler = new JSONResponseHandler();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case URI_TYPE_ITEM:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE;

            default:
                return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        switch (URI_MATCHER.match(uri)) {
            case URI_TYPE_ITEM:
                // Creation de l'objet Cursor pour envoyer les resultats de cette requête
                String[] colonnes = {"vent", "temp", "pression", "dernierReleve"};
                MatrixCursor cursor = new MatrixCursor(colonnes, 0);

                // Traitement de la requête, tous les paramètres sont ignorés
                // On prend le pays et le nom de la ville de l'URI qui doit avoir le format
                //  content://<authority>/weather/pays/ville
                //
                List segments = uri.getPathSegments();
                String pays = (String) segments.get(segments.size() - 2);
                String ville = (String) segments.get(segments.size() - 1);

                // Ensuite, on va faire appel au WS
                try {
                    String url = WSUtil.getURL(ville, pays);
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");

                    // Récupération de la réponse
                    List<String> reponse = jsonHandler.handleResponse(con.getInputStream(), "UTF-8");

                    if (reponse != null && ! reponse.isEmpty()) {
                        // (wind, temperature, pressure, time)
                        Object[] donnees = {
                                reponse.get(0),
                                Float.parseFloat(reponse.get(1)),
                                (int) Float.parseFloat(reponse.get(2)),
                                reponse.get(3)
                        };
                        cursor.addRow(donnees);
                    }
                }
                catch (Exception e) {
                    Log.e("TP2 Meteo : WSWeather", e.getMessage(), e);
                }

                return cursor;

            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
