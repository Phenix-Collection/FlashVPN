package com.polestar.clone.server.net;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.RemoteException;

import com.polestar.clone.helper.utils.Singleton;
import com.polestar.clone.server.INetworkScoreManager;


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
