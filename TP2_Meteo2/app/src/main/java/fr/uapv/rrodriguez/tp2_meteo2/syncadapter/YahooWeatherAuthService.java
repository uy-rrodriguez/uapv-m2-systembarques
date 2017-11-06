package fr.uapv.rrodriguez.tp2_meteo2.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Created by uy.rrodriguez on 05/11/2017.
 *
 * Basé sur l'exemple :
 *      https://developer.android.com/training/sync-adapters/creating-authenticator.html#CreateAuthenticatorService
 */

public class YahooWeatherAuthService extends Service {

    // Instance field that stores the authenticator object
    private YahooWeatherAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new YahooWeatherAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
