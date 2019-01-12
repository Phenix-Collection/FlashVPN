package com.polestar.clone.client.hook.proxies.telephony;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.Inject;

import mirror.com.android.internal.telephony.IHwTelephony;

/**
 * Created by guojia on 2019/1/12.
 */

@Inject(MethodProxies.class)
public class HwTelephonyStub extends BinderInvocationProxy {
    class GetUniqueDeviceId extends MethodProxies.GetDeviceId {
//        GetUniqueDeviceId() {
//            this();
//        }

        public GetUniqueDeviceId() {
            super();
        }

        public String getMethodName() {
            return "getUniqueDeviceId";
        }
    }

    public HwTelephonyStub() {
        super(IHwTelephony.Stub.TYPE, "phone_huawei");
    }

    protected void onBindMethods() {
        this.addMethodProxy(new GetUniqueDeviceId());
    }
}
