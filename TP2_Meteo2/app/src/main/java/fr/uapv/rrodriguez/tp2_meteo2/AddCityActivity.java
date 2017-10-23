package fr.uapv.rrodriguez.tp2_meteo2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fr.uapv.rrodriguez.tp2_meteo2.util.MyUtils;

public class AddCityActivity extends AppCompatActivity {

    private AddCityActivity activity;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_add);

        activity = this;

        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TextView contenant les données de la ville
                TextView cityName = (TextView) findViewById(R.id.cityName);
                TextView cityCountry = (TextView) findViewById(R.id.cityCountry);

                // Si les données ne sont pas vides, on va afficher une erreur
                if (cityName.getText().length() == 0
                        || cityCountry.getText().length() == 0) {
                    MyUtils.showSnackBar(activity, "Les données ne sont pas corectes");
                }

                else {
                    // Intent de réponse
                    Intent returnIntent = new Intent();

                    // Objet Bundle pour passer des paramètres
                    Bundle b = new Bundle();

                    // Chargement des paramètres à envoyer
                    b.putString("cityName", cityName.getText().toString());
                    b.putString("cityCountry", cityCountry.getText().toString());
                    returnIntent.putExtras(b);

                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }
}
