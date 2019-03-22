package com.polestar.clone.client.hook.proxies.phonesubinfo;

import android.Manifest;
import android.content.pm.PackageManager;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.Inject;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;

import java.lang.reflect.Method;

import mirror.com.android.internal.telephony.IPhoneSubInfo;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class PhoneSubInfoStub extends BinderInvocationProxy {
	public PhoneSubInfoStub() {
		super(IPhoneSubInfo.Stub.asInterface, "iphonesubinfo");
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceLastPkgMethodProxy("getNaiForSubscriber"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getImeiForSubscriber") {
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				return getDeviceInfo().deviceId;
			}
		});
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getDeviceSvn"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getDeviceSvnUsingSubId"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getSubscriberId"){
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				if (VirtualCore.get().getContext().checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)
						!= PackageManager.PERMISSION_GRANTED) {
					return "";
				}
				return super.call(who, method, args);
			}
		});
		addMethodProxy(new ReplaceLastPkgMethodProxy("getSubscriberIdForSubscriber"){
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				if (VirtualCore.get().getContext().checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)
						!= PackageManager.PERMISSION_GRANTED) {
					return "";
				}
				return super.call(who, method, args);
			}
		});
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getGroupIdLevel1"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getGroupIdLevel1ForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getLine1Number"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getLine1NumberForDisplay"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getLine1NumberForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getLine1AlphaTag"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getLine1AlphaTagForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getMsisdn"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getMsisdnForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getVoiceMailNumber"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getVoiceMailNumberForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getVoiceMailAlphaTag"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getVoiceMailAlphaTagForSubscriber"));
	}

}
