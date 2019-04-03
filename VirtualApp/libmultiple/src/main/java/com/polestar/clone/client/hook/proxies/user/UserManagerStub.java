package com.polestar.clone.client.hook.proxies.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;

import java.util.Collections;

import mirror.android.content.pm.UserInfo;
import mirror.android.os.IUserManager;

/**
 * @author Lody
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
        addMethodProxy(new ResultStaticMethodProxy("isUserUnlocked", true));
        addMethodProxy(new ResultStaticMethodProxy("isUserUnlockingOrUnlocked", true));
        addMethodProxy(new ResultStaticMethodProxy("getUserIcon", null));
        addMethodProxy(new ResultStaticMethodProxy("getUserInfo", UserInfo.ctor.newInstance(0, "Admin", UserInfo.FLAG_PRIMARY.get())));
        addMethodProxy(new ResultStaticMethodProxy("getDefaultGuestRestrictions", null));
        addMethodProxy(new ResultStaticMethodProxy("setDefaultGuestRestrictions", null));
        addMethodProxy(new ResultStaticMethodProxy("removeRestrictions", null));
//        addMethodProxy(new ResultStaticMethodProxy("getUsers", Collections.EMPTY_LIST));
        addMethodProxy(new ResultStaticMethodProxy("createUser", null));
        addMethodProxy(new ResultStaticMethodProxy("createProfileForUser", null));
        addMethodProxy(new ResultStaticMethodProxy("getProfiles", Collections.EMPTY_LIST));
        addMethodProxy(new ResultStaticMethodProxy("isManagedProfile", false));


        addMethodProxy(new ResultStaticMethodProxy("getUsers",
                Collections.singletonList(UserInfo.ctor.newInstance(0, "Admin", Integer.valueOf(UserInfo.FLAG_PRIMARY.get())))));
    }
}
