package fr.uapv.rrodriguez.tp2_meteo2;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.uapv.rrodriguez.tp2_meteo2.model.City;
import fr.uapv.rrodriguez.tp2_meteo2.model.CityDAO;
import fr.uapv.rrodriguez.tp2_meteo2.util.JSONResponseHandler;
import fr.uapv.rrodriguez.tp2_meteo2.util.MyUtils;
import fr.uapv.rrodriguez.tp2_meteo2.util.WSData;
import fr.uapv.rrodriguez.tp2_meteo2.util.WSUtil;

public class MainActivity extends AppCompatActivity {

    MainActivity activity;
    ListView listViewVilles;
    FloatingActionButton fabAjouterVille;

    List<City> listeVillesEnBDD;
    ArrayAdapter<City> adapterListeVilles;

    // Options du menu contextuel de villes
    final static int CITY_CONTEXT_MENU_DEL = 1;

    // Identifiants pour les activites qui retournent des donnees
    final static int ADD_CITY_REQUEST = 1;


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

        // On récupère la ListView
        listViewVilles = (ListView) findViewById(R.id.listViewVilles);

        // On récupère le FAB
        fabAjouterVille = (FloatingActionButton) findViewById(R.id.fabAjouterVille);

        // Charge des villes dans la liste
        listeVillesEnBDD = CityDAO.list(activity);
        adapterListeVilles = new ArrayAdapter<City>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                listeVillesEnBDD);
        listViewVilles.setAdapter(adapterListeVilles);


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
                City supprimer = CityDAO.get(activity, (int) info.id);

                // Suppression de la ville en BDD
                // L'adaptateur qui permet d'afficher la liste est ensuite mis-a-jour
                CityDAO.delete(activity, supprimer);
                listeVillesEnBDD.clear();
                listeVillesEnBDD.addAll(CityDAO.list(activity));
                adapterListeVilles.notifyDataSetChanged();

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
                new WSRequestTask().execute();
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
                CityDAO.insert(activity, ville);
                listeVillesEnBDD.clear();
                listeVillesEnBDD.addAll(CityDAO.list(activity));
                adapterListeVilles.notifyDataSetChanged();
            }
        }
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
            List<City> listeVilles = CityDAO.list(activity);
            Iterator<City> it = listeVilles.iterator();
            while (it.hasNext()) {
                City c = it.next();
                wsdata.setRetour(c.toString(), new ArrayList<String>());
            }

            // Ensuite, pour chaque ville on va faire appel au WS
            it = listeVilles.iterator();
            while (it.hasNext()) {
                try {
                    City c = it.next();
                    String url = WSUtil.getURL(c.getNom(), c.getPays());

                    // Creation de la requete HTTP
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");

                    // Récupération de la réponse
                    List<String> reponse = jsonHandler.handleResponse(con.getInputStream(), "UTF-8");
                    wsdata.setRetour(c.toString(), reponse);
                }
                catch (Exception e) {
                    Log.e("TP1 Meteo : WS", e.getMessage(), e);
                }
            }

            /*
            try {
                Thread.sleep(5000);
            }
            catch(InterruptedException iex) {}
            */

            return wsdata;
        }

        @Override
        protected void onPostExecute(WSData wsdata) {
            // Recuperation des retours du WS et actualisation de la liste de villes
            List<City> listeVilles = CityDAO.list(activity);
            Iterator<City> it = listeVilles.iterator();
            while (it.hasNext()) {
                City c = it.next();
                List<String> donnees = wsdata.getRetour(c.toString());

                // Actualisation des villes en mémoire
                if (donnees != null && ! donnees.isEmpty()) {
                    //(wind, temperature, pressure, time)
                    c.setVent(donnees.get(0));
                    c.setTemp(Float.parseFloat(donnees.get(1)));
                    c.setPression((int) Float.parseFloat(donnees.get(2)));
                    c.setDernierReleve(donnees.get(3));
                }

                // Actualisation des donnees en BDD
                CityDAO.update(activity, it.next());
            }

            // Apres d'avoir traite toutes les villes, on va mettre a jour l'adapter
            listeVillesEnBDD.clear();
            listeVillesEnBDD.addAll(CityDAO.list(activity));
            adapterListeVilles.notifyDataSetChanged();

            MyUtils.showSnackBar(activity, "Actualisation de villes finalisée !");
        }

    }

}
