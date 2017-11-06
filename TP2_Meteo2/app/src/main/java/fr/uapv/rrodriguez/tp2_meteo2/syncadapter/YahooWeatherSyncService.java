package fr.uapv.rrodriguez.tp2_meteo2.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by uy.rrodriguez on 06/11/2017.
 *
 * Basé sur
 *  https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 *
 */
public class YahooWeatherSyncService extends Service {

    // Instance unique du SyncAdapter
    private static YahooWeatherSyncAdapter sSyncAdapter = null;

    // Lock pour faire les opérations thread-safe
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        // Création du SyncAdapter comme Singleton, de manière thread-safe
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new YahooWeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * La méthode retourne un objet IBinder qui permet aux processus externes de faire appel à
     * onPerfomrSync(). L'objet est crée par la super classe du {@link YahooWeatherSyncAdapter}
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
