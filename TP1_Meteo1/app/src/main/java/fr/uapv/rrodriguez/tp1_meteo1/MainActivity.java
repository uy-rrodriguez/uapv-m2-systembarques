package fr.uapv.rrodriguez.tp1_meteo1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import fr.uapv.rrodriguez.tp1_meteo1.metier.City;
import fr.uapv.rrodriguez.tp1_meteo1.util.MyUtils;
import res.layout.CityView;

public class MainActivity extends AppCompatActivity {

    MainActivity activity;
    ListView listViewVilles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        // On récupère la ListView
        listViewVilles = (ListView) findViewById(R.id.listViewVilles);

        // Charge des villes dans la liste
        final ArrayAdapter<City> adapter = new ArrayAdapter<City>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                City.getVilles());
        listViewVilles.setAdapter(adapter);

        // OnClick
        listViewVilles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                final City item = (City) parent.getItemAtPosition(position);
                MyUtils.showSnackBar(activity, item.toString());

                Intent intent = new Intent(activity, CityView.class);
                TextView cityName = (TextView) findViewById(R.id.cityName);
                TextView cityCountry = (TextView) findViewById(R.id.cityName);
                TextView cityWind = (TextView) findViewById(R.id.cityName);
                TextView cityPresure = (TextView) findViewById(R.id.cityPresure);
                TextView cityTemp = (TextView) findViewById(R.id.cityTemp);
                TextView cityDate = (TextView) findViewById(R.id.cityDate);

                startActivity(intent);

            }

        });
    }
}
