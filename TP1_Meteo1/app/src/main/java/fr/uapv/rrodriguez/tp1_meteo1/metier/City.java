package fr.uapv.rrodriguez.tp1_meteo1.metier;

import java.util.ArrayList;
import java.util.List;

/**
 * Une ville devra correspondre à un objet City ayant les attributs suivants :
 *
 * - nom de la ville,
 * - pays d'appartenance,
 * - date du dernier relevé météo,
 * - vitesse du vent (en km/h),
 * - direction du vent,
 * - pression (en hPa),
 * - température de l'air (en degrés Celsius).
 *
 * Created by uapv1601663 on 09/10/17.
 */

public class City {
    private String nom;
    private String pays;
    private String dernierReleve;
    private int vent;
    private String ventDir;
    private int pression;
    private float temp;

    public City(String nom, String pays) {
        this.nom = nom;
        this.pays = pays;
    }

    @Override
    public String toString() {
        return nom + " (" + pays + ")";
    }

    public static List getVilles() {
        List<City> villes = new ArrayList<>();
        villes.add(new City("Avignon", "France"));
        villes.add(new City("Paris", "France"));
        villes.add(new City("Montevideo", "Uruguay"));
        return villes;
    }


    /* Getter et Setter */

    public String getNom() { return nom; }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getDernierReleve() {
        return dernierReleve;
    }

    public void setDernierReleve(String dernierReleve) {
        this.dernierReleve = dernierReleve;
    }

    public int getVent() {
        return vent;
    }

    public void setVent(int vent) {
        this.vent = vent;
    }

    public String getVentDir() {
        return ventDir;
    }

    public void setVentDir(String ventDir) {
        this.ventDir = ventDir;
    }

    public int getPression() {
        return pression;
    }

    public void setPression(int pression) {
        this.pression = pression;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }
}
