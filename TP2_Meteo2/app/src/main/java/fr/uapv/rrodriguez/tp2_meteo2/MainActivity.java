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
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import fr.uapv.rrodriguez.tp2_meteo2.model.City;
import fr.uapv.rrodriguez.tp2_meteo2.model.CityDAO;
import fr.uapv.rrodriguez.tp2_meteo2.util.JSONResponseHandler;
import fr.uapv.rrodriguez.tp2_meteo2.util.MyUtils;
import fr.uapv.rrodriguez.tp2_meteo2.util.WSUtil;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifiant du loader à utiliser. L'id d'un Loader est spécifique à chaque Activity ou Fragment
    // où le Loader est utilisé
    private static final int CITY_LOADER_ID = 1;

    // URI du ContentProvider à contacter (WeatherContentProvider)
    private static final String DB_WEATHER_PROVIDER_AUTHORITY = "fr.uapv.rrodriguez.tp2_meteo2.provider";
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
    
    // Pour accéder au ContentProvider
    private ContentResolver contentResolver;

    // Observer qui va écouter le Provider
    private WeatherObserver observer;
    
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

        // Récupération d'une instance de ContentResolver
        contentResolver = getContentResolver();
        
        // Création de l'Observer pour réagir aux changements dans la BDD
        observer = new WeatherObserver(new Handler());
        
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
        int[] idViewsVille = {android.R.id.text1,
                                android.R.id.text2};

        // Création de l'adapter pour afficher la liste de villes
        adapterListeVilles = new SimpleCursorAdapter(
                MainActivity.this,
                android.R.layout.simple_list_item_2,
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
                //final City item = (City) parent.getItemAtPosition(position);

                // Récupération du cursor
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                City item = CityDAO.cursorToCity(c);

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
    
    @Override
    public void onResume(){
        super.onResume();
        
        // Enregistrement de l'Observer pour réagir aux changements dans la BDD
        contentResolver.registerContentObserver(Uri.parse(DB_WEATHER_PROVIDER_URI), true, observer);
    }
    
    @Override
    public void onPause(){
        super.onPause();
        
        // "Desenregistrement" de l'Observer
        contentResolver.unregisterContentObserver(observer);
    }
    

    // Menu contextuel pour supprimer une ville
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.listViewVilles) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Récupération du cursor
            Cursor c = (Cursor) adapterListeVilles.getItem(info.position);
            City city = CityDAO.cursorToCity(c);
            Log.d("TP2_Meteo", "Main : Menu contextuel pour ville " + city.getId());

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
                // L'adaptateur qui permet d'afficher la liste est ensuite mis-a-jour
                String where = "_id = ?";
                String[] whereValues = {"" + info.id};
                contentResolver.delete(
                        Uri.parse(DB_WEATHER_PROVIDER_URI),
                        where,
                        whereValues);

                // Lancer une recharge du contenu dans la liste
                //getLoaderManager().restartLoader(CITY_LOADER_ID, null, this);

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
                // L'adaptateur qui permet d'afficher la liste est ensuite mis-a-jour
                ContentValues values = new ContentValues();
                values.put("nom", ville.getNom());
                values.put("pays", ville.getPays());
                contentResolver.insert(Uri.parse(DB_WEATHER_PROVIDER_URI), values);

                // Lancer une recharge du contenu dans la liste
                getLoaderManager().restartLoader(CITY_LOADER_ID, null, this);
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
        Log.d("TP2_Meteo", "Main.onLoadFinished : " + Arrays.asList(data.getColumnNames()).toString());

        // Le switch suivant est utile pour gérer plusieurs Loaders différents
        switch (loader.getId()) {
            case CITY_LOADER_ID:
                adapterListeVilles.swapCursor(data);
                adapterListeVilles.notifyDataSetChanged();
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
                adapterListeVilles.notifyDataSetChanged();
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

        /*
        if (accountManager.addAccountExplicitly(compte, null, null)) {
        }
        else {
            Log.e("TP2_Meteo", "Il y a eu une erreur pour créer le compte Yahoo Weather");
        }
        */

        accountManager.addAccountExplicitly(compte, null, null);

        return compte;
    }

    
    /* ************************************************************************************** 
     * Observer pour réagir aux changements dans la BDD
    /* ************************************************************************************** */
    public class WeatherObserver extends ContentObserver {
        
        public WeatherObserver(Handler handler) {
            super(handler);
            Log.d("TP2_Meteo", "WeatherObserver.constructor");
        }

        /**
         * Signature pour compatibilité avec versions anciennes du SDK
         */
        @Override
        public void onChange(boolean selfChange) {
            Log.d("TP2_Meteo", "WeatherObserver.onChange(" + selfChange + ")");
            
            onChange(selfChange, null);
        }
        
        @Override
        public void onChange(boolean selfChange, Uri changeUri) {
            Log.d("TP2_Meteo", "WeatherObserver.onChange(" + selfChange + ", " + changeUri + ")");
            
            // Lancer une recharge du contenu dans la liste
            activity.getLoaderManager().restartLoader(activity.CITY_LOADER_ID, null, activity);
        }
    }
}
