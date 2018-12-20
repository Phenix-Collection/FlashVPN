package com.polestar.clone.client.hook.proxies.fingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;

import mirror.android.hardware.fingerprint.IFingerprintService;

/**
 * Created by natsuki on 12/10/2017.
 */

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintManagerStub extends BinderInvocationProxy {
    public FingerprintManagerStub() {
        super(IFingerprintService.Stub.asInterface, Context.FINGERPRINT_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        addMethodProxy(new ReplaceLastPkgMethodProxy("isHardwareDetected"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("hasEnrolledFingerprints"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("authenticate"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("cancelAuthentication"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getEnrolledFingerprints"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getAuthenticatorId"));
    }
}
