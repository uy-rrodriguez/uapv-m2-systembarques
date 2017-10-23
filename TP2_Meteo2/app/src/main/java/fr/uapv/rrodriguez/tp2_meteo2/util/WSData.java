package fr.uapv.rrodriguez.tp2_meteo2.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WSData {
    // Objet Map qui va stocker, pour chaque ville, le résultat de l'appel au WS
    // Une liste de 4 Strings (wind, temperature, pressure, time) si l'appel a été bien réalisé,
    // une liste vide dans le cas contraire.
    private Map<String, List<String>> retour;

    public WSData() {
        this.retour = new HashMap<>();
    }

    public Map<String, List<String>> getRetour() {
        return this.retour;
    }

    public List<String> getRetour(String cleVille) {
        return this.retour.get(cleVille);
    }

    public void setRetour(String cleVille, List<String> donnees) {
        this.retour.put(cleVille, donnees);
    }
}
