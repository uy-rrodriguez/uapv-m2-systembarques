package fr.uapv.rrodriguez.tp1_meteo1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricci on 18/10/2017.
 *
 * classe singleton qui stocke une liste de villes, accessible par toute l'application.
 */

public class CityList {
    private static CityList instance = null;
    private List<City> villes;

    private CityList() {
        this.villes = new ArrayList<>();
        this.villes.add(new City("Avignon", "France"));
        this.villes.add(new City("Paris", "France"));
        this.villes.add(new City("Montevideo", "Uruguay"));
    }

    public static CityList getInstance() {
        if (CityList.instance == null)
            CityList.instance = new CityList();
        return CityList.instance;
    }

    public static List<City> getVilles() {
        return CityList.getInstance().villes;
    }

    public static void addVille(City ville) {
        CityList.getInstance().villes.add(ville);
    }

    public static void deleteVille(City ville) {
        CityList.getInstance().villes.remove(ville);
    }
}
