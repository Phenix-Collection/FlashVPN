package winterfell.flash.vpn.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import winterfell.flash.vpn.core.LocalVpnService;

public class SystemStatusReceiver extends BroadcastReceiver {
    private Context mContext;
    @Override
    public void onReceive(Context context, final Intent intent) {
        mContext = context;
        final PendingResult result = goAsync();
        new Thread(new Runnable() {
            @Override
            public void run() {
                handleIntent(intent);
                result.finish();
            }
        }).start();
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent prepare = LocalVpnService.prepare(mContext);
            if (prepare == null) {
                //TODO only allow to post notification
                //mContext.startService(new Intent(this, LocalVpnService.class));
            }
        }
    }
}
