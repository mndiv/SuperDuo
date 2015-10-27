package barqsoft.footballscores.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by DivyaM on 10/22/2015.
 */
public class ScoresSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ScoresSyncAdapter sScoresSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ScoresSyncService", "onCreate-ScoresSyncService");
        synchronized (sSyncAdapterLock){
            if(sScoresSyncAdapter == null){
                sScoresSyncAdapter = new ScoresSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sScoresSyncAdapter.getSyncAdapterBinder();
    }
}
