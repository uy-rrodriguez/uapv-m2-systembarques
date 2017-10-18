package fr.uapv.rrodriguez.tp1_meteo1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import fr.uapv.rrodriguez.tp1_meteo1.util.MyUtils;
import fr.uapv.rrodriguez.tp1_meteo1.util.WSData;

public class CityView extends AppCompatActivity {

    private CityView activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_view);

        activity = this;

        // Récupération des paramètres depuis l'Intent
        Bundle b = getIntent().getExtras();

        // Elements à modifier avec les paramètres reçus
        TextView cityName = (TextView) findViewById(R.id.cityName);
        TextView cityCountry = (TextView) findViewById(R.id.cityCountry);
        TextView cityDate = (TextView) findViewById(R.id.cityDate);
        TextView cityPresure = (TextView) findViewById(R.id.cityPresure);
        TextView cityTemp = (TextView) findViewById(R.id.cityTemp);
        TextView cityWind = (TextView) findViewById(R.id.cityWind);

        // Modification des TextView
        cityName.setText(b.getCharSequence("cityName"));
        cityCountry.setText(b.getCharSequence("cityCountry"));
        cityDate.setText(b.getCharSequence("cityDate"));
        cityPresure.setText(b.getInt("cityPresure") + " hPa");
        cityTemp.setText(b.getFloat("cityTemp") + " C");
        //cityWind.setText(b.getInt("cityWindSpeed") + " km/h (" + b.getCharSequence("cityWindDir") + ")");
        cityWind.setText(b.getString("cityWind"));
    }
}
