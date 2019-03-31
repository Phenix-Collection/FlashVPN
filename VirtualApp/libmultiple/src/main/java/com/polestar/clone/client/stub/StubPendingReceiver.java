package com.polestar.clone.client.stub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.polestar.clone.client.env.Constants;
import com.polestar.clone.helper.utils.ComponentUtils;
import com.polestar.clone.os.VUserHandle;

/**
 * @author Lody
 */

public class StubPendingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent realIntent = intent.getParcelableExtra(Constants.VA_INTENT_KEY_INTENT);
        int userId = intent.getIntExtra(Constants.VA_INTENT_KEY_USERID, VUserHandle.USER_ALL);
        if (realIntent != null) {
            Intent newIntent = ComponentUtils.redirectBroadcastIntent(realIntent, userId);
            if (newIntent != null) {
                context.sendBroadcast(newIntent);
            }
        }
    }
}
