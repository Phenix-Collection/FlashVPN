package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.statusbar.IStatusBarService;

/**
 * Created by guojia on 2016/12/18.
 */

public class StatusBarBinderDelegate extends HookBinderDelegate {

    @Override
    protected IInterface createInterface() {
        IBinder binder = ServiceManager.getService.call(Context.ALARM_SERVICE);
        return IStatusBarService.Stub.asInterface.call(binder);
    }
}
