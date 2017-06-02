package com.lody.virtual.client.hook.proxies.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.view.ViewGroup;

import com.lody.virtual.R;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;

import mirror.android.os.IUserManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class UserManagerStub extends BinderInvocationProxy {
	private final static String TAG = "UserManager";

	public UserManagerStub() {
		super(IUserManager.Stub.asInterface, Context.USER_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("setApplicationRestrictions"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getApplicationRestrictions"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getApplicationRestrictionsForUser"));
		addMethodProxy(new ResultStaticMethodProxy("getProfileParent", null));
		addMethodProxy(new ResultStaticMethodProxy("getUserIcon", null));
		addMethodProxy(new GetUserInfo());
		addMethodProxy(new ResultStaticMethodProxy("getDefaultGuestRestrictions", null));
		addMethodProxy(new ResultStaticMethodProxy("setDefaultGuestRestrictions", null));
		addMethodProxy(new ResultStaticMethodProxy("removeRestrictions", null));
		addMethodProxy(new ResultStaticMethodProxy("getUsers", Collections.EMPTY_LIST));
		addMethodProxy(new ResultStaticMethodProxy("createUser", null));
		addMethodProxy(new ResultStaticMethodProxy("createProfileForUser", null));
		addMethodProxy(new ResultStaticMethodProxy("getProfiles", Collections.EMPTY_LIST));
	}

	private class GetUserInfo extends StaticMethodProxy{

		public GetUserInfo( ) {
			super("getUserInfo");
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			VLog.d(TAG, "getUserInfo hooked");
			VUserInfo primary = new VUserInfo(0,
					VirtualCore.get().getContext().getResources().getString(R.string.owner_name), null,
					VUserInfo.FLAG_ADMIN | VUserInfo.FLAG_PRIMARY | VUserInfo.FLAG_INITIALIZED);
			Parcel p = Parcel.obtain();
			primary.writeToParcel(p,0);
			//hack android 7.0
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				p.writeInt(0);
			}
			Object ret = null;
			try {
				ret = Reflect.on("android.content.pm.UserInfo").create(p).get();
			} catch (Throwable e) {
				VLog.logbug(TAG, VLog.getStackTraceString(e));
			}
//			Class c = Class.forName("android.content.pm.UserInfo");
//			Constructor constructor = c.getConstructor(Parcel.class);
//			constructor.setAccessible(true);
//			VLog.d("UserManager", "getUserInfo hooked");
//			return constructor.newInstance(p);
			return ret;
		}
	}
}
