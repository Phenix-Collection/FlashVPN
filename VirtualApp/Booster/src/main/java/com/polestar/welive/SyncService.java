package com.polestar.welive;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.polestar.booster.Booster;
import com.polestar.booster.BoosterLog;

/**
 * Created by guojia on 2018/12/13.
 */

public class SyncService extends Service {
    private static MySyncAdapter a;
    static {
        a = null;
    }

    public SyncService() {
        super();
    }

    public IBinder onBind(Intent arg2) {
        return SyncService.a.getSyncAdapterBinder();
    }

    public void onCreate() {
        try {
            BoosterLog.log("SyncService onCreate");
            if(SyncService.a == null) {
                SyncService.a = new MySyncAdapter(this.getApplicationContext());
            }
        }
        catch(Throwable v0) {
        }

    }

    public final class MySyncAdapter extends AbstractThreadedSyncAdapter {
        ContentResolver a;

        public MySyncAdapter(Context arg2) {
            super(arg2, true);
            this.a = arg2.getContentResolver();
        }

        public final void onPerformSync(Account arg6, Bundle arg7, String arg8, ContentProviderClient arg9, SyncResult arg10) {
            Context v0 = this.getContext();
            Booster.wake(v0, "sync");
        }
    }
}
