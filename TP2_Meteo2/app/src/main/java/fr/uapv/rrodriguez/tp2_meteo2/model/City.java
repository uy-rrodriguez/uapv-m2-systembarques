package fr.uapv.rrodriguez.tp2_meteo2.model;

import java.io.Serializable;

/**
 * Une ville devra correspondre à un objet City ayant les attributs suivants :
 *
 * - _ID dans la table "city"
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

public class City implements Serializable {
    private int _ID = 0;
    private String nom;
    private String pays;
    private String dernierReleve;
    //private int vent;
    //private String ventDir;
    private String vent;
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


    /* Getter et Setter */

    public int getId() {
        return _ID;
    }

    public void setId(int id) {
        this._ID = id;
    }

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

    /*
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
    */

    public String getVent() {
        return vent;
    }

    public void setVent(String vent) {
        this.vent = vent;
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
