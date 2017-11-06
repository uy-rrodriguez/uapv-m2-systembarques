package fr.uapv.rrodriguez.tp2_meteo2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.uapv.rrodriguez.tp2_meteo2.model.City;
import fr.uapv.rrodriguez.tp2_meteo2.util.JSONResponseHandler;
import fr.uapv.rrodriguez.tp2_meteo2.util.MyUtils;
import fr.uapv.rrodriguez.tp2_meteo2.util.WSData;
import fr.uapv.rrodriguez.tp2_meteo2.util.WSUtil;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifiant du loader à utiliser. L'id d'un Loader est spécifique à chaque Activity ou Fragment
    // où le Loader est utilisé
    private static final int CITY_LOADER_ID = 1;

    // URI du ContentProvider à contacter (WeatherContentProvider)
    private static final String DB_WEATHER_PROVIDER_AUTHORITY = "fr.uapv.rrodriguez.tp2_meteo2.provider.db";
    private static final String DB_WEATHER_PROVIDER_URI = "content://" + DB_WEATHER_PROVIDER_AUTHORITY + "/weather";

    // Options du menu contextuel de villes
    final static int CITY_CONTEXT_MENU_DEL = 1;

    // Identifiants pour les activites qui retournent des donnees
    final static int ADD_CITY_REQUEST = 1;

    // Compte pour se conecter au WS Yahoo Weather
    public static final String ACCOUNT_TYPE = "tp2_meteo2.rrodriguez.uapv.fr";
    public static final String ACCOUNT = "dummyaccount";

    // Périodicité de synchronisation de villes (appel au WS Yahoo Weather)
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;


    // Variables d'instance
    private MainActivity activity;
    private ListView listViewVilles;
    private FloatingActionButton fabAjouterVille;
    private SimpleCursorAdapter adapterListeVilles;

    // Compte pour se conecter au WS Yahoo Weather
    private Account compteYahooWeather;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        // Création du compte pour Yahoo Weather
        compteYahooWeather = creerCompteYahooWeather(this);

        // Configuration de la périodicité du SyncAdapter
        ContentResolver.addPeriodicSync(
                compteYahooWeather,
                DB_WEATHER_PROVIDER_AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL);


        // On récupère le FAB
        fabAjouterVille = (FloatingActionButton) findViewById(R.id.fabAjouterVille);


        /*
         * Début création du LoaderManager pour une charges des villes asynchrone
         */

        // On récupère la ListView
        listViewVilles = (ListView) findViewById(R.id.listViewVilles);

        // Colonnes de la ville a afficher
        String[] colonnesVille = {"nom", "pays"};
        int[] idViewsVille = {android.R.layout.simple_list_item_1,
                                android.R.layout.simple_list_item_2};

        // Création de l'adapter pour afficher la liste de villes
        adapterListeVilles = new SimpleCursorAdapter(
                MainActivity.this,
                android.R.layout.two_line_list_item,
                null,
                colonnesVille,
                idViewsVille,
                0);
        listViewVilles.setAdapter(adapterListeVilles);

        // Initialisation du LoaderManager, on passe this pour lui indiquer que c'est à cette
        // instance que le manager doit renvoyer les résultats du Loader
        LoaderManager lm = getLoaderManager();
        lm.initLoader(CITY_LOADER_ID, null, this);

        /*
         * Fin création du LoaderManager
         */


        // Liste villes OnClick
        listViewVilles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                // Récupération de la ville
                final City item = (City) parent.getItemAtPosition(position);
                //MyUtils.showSnackBar(activity, item.toString());

                // Intent pour lancer une nouvelle Activity
                Intent intent = new Intent(activity, CityView.class);

                // Objet Bundle pour passer des paramètres
                Bundle b = new Bundle();

                // Chargement des paramètres à envoyer
                b.putString("cityName", item.getNom());
                b.putString("cityCountry", item.getPays());
                //b.putInt("cityWindSpeed", item.getVent());
                //b.putString("cityWindDir", item.getVentDir());
                b.putString("cityWind", item.getVent());
                b.putInt("cityPresure", item.getPression());
                b.putFloat("cityTemp", item.getTemp());
                b.putString("cityDate", item.getDernierReleve());

                // Ajout des paramètres dans l'Intent
                intent.putExtras(b);

                // Démarrage de l'Activity
                startActivity(intent);
            }

        });

        // Liste villes context menu (Suppression de villes)
        registerForContextMenu(listViewVilles);

        // FAB ajouter ville OnClick
        fabAjouterVille.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent pour lancer une nouvelle Activity
                Intent intent = new Intent(activity, AddCityActivity.class);

                // Démarrage de l'Activity
                startActivityForResult(intent, ADD_CITY_REQUEST);
            }
        });
    }

    // Menu contextuel pour supprimer une ville
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.listViewVilles) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            City city = (City) adapterListeVilles.getItem(info.position);
            menu.setHeaderTitle(city.toString());

            // On indique l'id de la ville
            info.id = city.getId();

            //menu.setHeaderTitle(R.string.city_context_menu_title);
            menu.add(0, CITY_CONTEXT_MENU_DEL, 0, R.string.city_context_menu_del);
        }
    }

    // Selection d'un element du menu contextuel
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CITY_CONTEXT_MENU_DEL:
                // Ville a supprimer
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                // Suppression de la ville en BDD
                // L'adaptateur qui permet d'afficher la liste est mis-a-jour automatiquement
                String where = "_ID";
                String[] whereValues = {"" + info.id};
                getContentResolver().delete(
                        Uri.parse(DB_WEATHER_PROVIDER_URI),
                        where,
                        whereValues);

                //adapterListeVilles.notifyDataSetChanged();

                MyUtils.showSnackBar(activity, "Ville supprimée");
                break;

            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_refresh:
                MyUtils.showSnackBar(activity, "Actualisation en cours...");
                //new WSRequestTask().execute();

                // Lancement d'un synchronisation des villes en background
                Bundle syncBundle = new Bundle();
                syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                ContentResolver.requestSync(
                        compteYahooWeather,
                        DB_WEATHER_PROVIDER_AUTHORITY,
                        syncBundle);

                break;

            default:
                break;
        }

        return true;
    }

    // Recevoir les retours des activites appelees
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_CITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                // On recupere la ville créée
                //City ville = new City(); //(City) data.getSerializableExtra("ville");

                // Récupération des paramètres depuis l'Intent associé a la réponse
                Bundle b = data.getExtras();
                String cityName = b.getCharSequence("cityName").toString();
                String cityCountry = b.getCharSequence("cityCountry").toString();
                City ville = new City(cityName, cityCountry);

                // Ajout de la ville dans la liste globale
                // L'adaptateur qui permet d'afficher la liste est automatiquement mis-a-jour
                ContentValues values = new ContentValues();
                values.put("nom", ville.getPays());
                values.put("pays", ville.getNom());
                getContentResolver().insert(Uri.parse(DB_WEATHER_PROVIDER_URI), values);

                //adapterListeVilles.notifyDataSetChanged();
            }
        }
    }


    /* ******************************************************************************* */
    /*    Utilisation de Loader                                                        */
    /* ******************************************************************************* */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(MainActivity.this, Uri.parse(DB_WEATHER_PROVIDER_URI),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Le switch suivant est utile pour gérer plusieurs Loaders différents
        switch (loader.getId()) {
            case CITY_LOADER_ID:
                adapterListeVilles.swapCursor(data);
                break;
        }

        // Et voilà, les villes sont affichées (on veut croire :) )
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Si jamais on fait reset du Loader, il va aller chercher à nouveau les villes.
        // Il faut donc commencer par supprimer les liens vers les éléments actuellement affichés
        switch (loader.getId()) {
            case CITY_LOADER_ID:
                adapterListeVilles.swapCursor(null);
                break;
        }
    }


    /**
     * Crée et retourne un nouveau compte pour se connecter à Yahoo Weather.
     *
     * @param context
     */
    public static Account creerCompteYahooWeather(Context context) {
        Account compte = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(compte, null, null)) {
        }
        else {
            Log.e("TP2 Meteo : Main", "Il y a eu une erreur pour créer le compte Yahoo Weather");
        }

        return compte;
    }


    /* ******************************************************************************* */
    /*    Inner class : WSRequestTask, faire appel à des WS.                           */
    /* ******************************************************************************* */

    public class WSRequestTask extends AsyncTask<Void, Void, WSData> {
        @Override
        protected WSData doInBackground(Void... params) {
            JSONResponseHandler jsonHandler = new JSONResponseHandler();

            // Creation de l'objet WSData pour envoyer les resultats de cette tache
            WSData wsdata = new WSData();

            // Creation de listes vides pour chaque ville
            Cursor cursor = getContentResolver().query(Uri.parse(MainActivity.DB_WEATHER_PROVIDER_URI),
                                                    null, "", null, "");
            while (cursor.moveToNext()) {
                wsdata.setRetour("" + cursor.getInt(cursor.getColumnIndex("_ID")), new ArrayList<String>());
            }

            // Ensuite, pour chaque ville on va faire appel au WS
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                try {
                    String url = WSUtil.getURL(cursor.getString(cursor.getColumnIndex("nom")),
                                                cursor.getString(cursor.getColumnIndex("pays")));

                    // Creation de la requete HTTP
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");

                    // Récupération de la réponse
                    List<String> reponse = jsonHandler.handleResponse(con.getInputStream(), "UTF-8");
                    wsdata.setRetour("" + cursor.getInt(cursor.getColumnIndex("_ID")), reponse);
                }
                catch (Exception e) {
                    Log.e("TP2 Meteo : WS", e.getMessage(), e);
                }
            }

            return wsdata;
        }

        @Override
        protected void onPostExecute(WSData wsdata) {
            // Recuperation des retours du WS et actualisation de la liste de villes
            Iterator<String> it = wsdata.getRetour().keySet().iterator();
            while (it.hasNext()) {
                String idVille = it.next();
                List<String> donnees = wsdata.getRetour(idVille);

                // Actualisation des donnees en BDD
                if (donnees != null && ! donnees.isEmpty()) {
                    // (wind, temperature, pressure, time)
                    ContentValues values = new ContentValues();
                    values.put("vent", donnees.get(0));
                    values.put("temp", Float.parseFloat(donnees.get(1)));
                    values.put("pression", (int) Float.parseFloat(donnees.get(2)));
                    values.put("dernierReleve", donnees.get(3));

                    String where = "_ID";
                    String[] whereValues = {idVille};
                    getContentResolver().update(Uri.parse(DB_WEATHER_PROVIDER_URI),
                                                values,
                                                where,
                                                whereValues);
                }
            }

            // Apres d'avoir traite toutes les villes, l'adapter se met à jour automatiquement
            //adapterListeVilles.notifyDataSetChanged();

            MyUtils.showSnackBar(activity, "Actualisation de villes finalisée !");
        }

    }

}
