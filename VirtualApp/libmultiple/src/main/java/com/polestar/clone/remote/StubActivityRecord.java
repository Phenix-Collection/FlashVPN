package com.polestar.clone.remote;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.polestar.clone.client.env.Constants;

/**
 * @author Lody
 */

public class StubActivityRecord  {
        public Intent intent;
        public ActivityInfo info;
        public ComponentName caller;
        public int userId;

        public StubActivityRecord(Intent intent, ActivityInfo info, ComponentName caller, int userId) {
            this.intent = intent;
            this.info = info;
            this.caller = caller;
            this.userId = userId;
        }

        public StubActivityRecord(Intent stub) {
            this.intent = stub.getParcelableExtra(Constants.VA_INTENT_KEY_INTENT);
            this.info = stub.getParcelableExtra("_VA_|_info_");
            this.caller = stub.getParcelableExtra("_VA_|_caller_");
            this.userId = stub.getIntExtra(Constants.VA_INTENT_KEY_USERID, 0);
        }

    public void saveToIntent(Intent stub) {
        stub.putExtra(Constants.VA_INTENT_KEY_INTENT, intent);
        stub.putExtra("_VA_|_info_", info);
        stub.putExtra("_VA_|_caller_", caller);
        stub.putExtra(Constants.VA_INTENT_KEY_USERID, userId);
    }
}
