package com.lody.virtual.server.net;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.RemoteException;

import com.lody.virtual.helper.utils.Singleton;
import com.lody.virtual.server.INetworkScoreManager;


/**
 * @author weishu
 */
@TargetApi(Build.VERSION_CODES.M)
public class VNetworkScoreManagerService extends INetworkScoreManager.Stub {

    private static final String TAG = VNetworkScoreManagerService.class.getSimpleName();


    private VNetworkScoreManagerService() {
    }

    private static final Singleton<VNetworkScoreManagerService> gDefault = new Singleton<VNetworkScoreManagerService>() {
        @Override
        protected VNetworkScoreManagerService create() {
            return new VNetworkScoreManagerService();
        }
    };

    public static VNetworkScoreManagerService get() {
        return gDefault.get();
    }

    @Override
    public boolean setActiveScorer(String packageName) throws RemoteException {
        return true;
    }
}
