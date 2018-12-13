package com.polestar.welive;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.polestar.booster.BoosterLog;

/**
 * Created by guojia on 2018/12/12.
 */

public class AuthService extends Service {
    private MyAuthenticator a;

    public AuthService() {
        super();

    }

    public IBinder onBind(Intent arg2) {
        return this.a.getIBinder();
    }

    public void onCreate() {
        BoosterLog.log("AuthService onCreate");
        a = new MyAuthenticator(this);
    }


    public final class MyAuthenticator extends AbstractAccountAuthenticator {
        public MyAuthenticator(Context arg1) {
            super(arg1);
        }

        public final Bundle addAccount(AccountAuthenticatorResponse arg2, String arg3, String arg4, String[] arg5, Bundle arg6) {
            return null;
        }

        public final Bundle confirmCredentials(AccountAuthenticatorResponse arg2, Account arg3, Bundle arg4) {
            return null;
        }

        public final Bundle editProperties(AccountAuthenticatorResponse arg2, String arg3) {
            throw new UnsupportedOperationException();
        }

        public final Bundle getAuthToken(AccountAuthenticatorResponse arg2, Account arg3, String arg4, Bundle arg5) {
            throw new UnsupportedOperationException();
        }

        public final String getAuthTokenLabel(String arg2) {
            throw new UnsupportedOperationException();
        }

        public final Bundle hasFeatures(AccountAuthenticatorResponse arg2, Account arg3, String[] arg4) {
            throw new UnsupportedOperationException();
        }

        public final Bundle updateCredentials(AccountAuthenticatorResponse arg2, Account arg3, String arg4, Bundle arg5) {
            throw new UnsupportedOperationException();
        }
    }
}
