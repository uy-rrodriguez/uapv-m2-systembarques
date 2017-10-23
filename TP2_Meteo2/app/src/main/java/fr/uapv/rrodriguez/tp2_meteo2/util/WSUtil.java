package fr.uapv.rrodriguez.tp2_meteo2.util;

import android.net.Uri;

/**
 * Created by Ricci on 18/10/2017.
 */

public class WSUtil {
    public static final String WS_URL = "https://query.yahooapis.com/v1/public/yql?format=json&q=";
    public static final String BASE_QUERY = "select * from weather.forecast where woeid in" +
            " (select woeid from geo.places(1) where text=\":cityName, :cityCountry\")";

    public static String getURL(String cityName, String cityCountry) {
        String query = WSUtil.BASE_QUERY;
        query = query.replaceAll(":cityName", cityName);
        query = query.replaceAll(":cityCountry", cityCountry);
        return WSUtil.WS_URL + Uri.encode(query);
    }
}
