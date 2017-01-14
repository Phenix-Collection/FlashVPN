package com.lody.virtual.client.hook.patchs.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.proto.StubActivityRecord;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * Created by guojia on 2017/1/14.
 */

class StartActivities extends BaseStartActivity {
    @Override
    public String getName() {
        return "startActivities";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        //filter
        super.call(who, method, args);

        int intentArrayIndex = ArrayUtils.indexOfFirst(args, Intent[].class);
        if (intentArrayIndex == -1) {
            return null;
        }
        int userId = VUserHandle.myUserId();
        Intent[] intents = (Intent[]) args[intentArrayIndex];
        int resultToIndex = ArrayUtils.indexOfObject(args, IBinder.class, 2);
        ComponentName caller = null;
        if (resultToIndex != -1) {
            IBinder token = (IBinder) args[resultToIndex];
            caller = VActivityManager.get().getActivityForToken(token);
        }
        for (int i = 0; i < intents.length; i++) {
            Intent intent = intents[i];
            if (ComponentUtils.isStubComponent(intent)) {
                continue;
            }
            ActivityInfo ai = VirtualCore.get().resolveActivityInfo(intent, userId);
            if (ai == null) {
                continue;
            }

            int vid = VActivityManager.get().initProcess(ai.packageName, ai.processName, userId);
            Intent targetIntent = new Intent();
            targetIntent.setClassName(VirtualCore.get().getHostPkg(), fetchStubActivity(vid, ai));
            ComponentName component = intent.getComponent();
            if (component == null) {
                component = ComponentUtils.toComponentName(ai);
            }
            targetIntent.setType(component.flattenToString());

            StubActivityRecord saveInstance = new StubActivityRecord(intent, ai,caller, userId);
            saveInstance.saveToIntent(targetIntent);
            VLog.d("StartActivities", "replace intent from " + intent.toString() + " to: " + targetIntent.toString());
            intents[i] = targetIntent;

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            args[intentArrayIndex - 1] = getHostPkg();
        }
        return method.invoke(who,args);
    }

    private String fetchStubActivity(int vpid, ActivityInfo targetInfo) {
        return StubManifest.getStubActivityName(vpid);
//        boolean isFloating = false;
//        boolean isTranslucent = false;
//        boolean showWallpaper = false;
//        try {
//            int[] R_Styleable_Window = R_Hide.styleable.Window.get();
//            int R_Styleable_Window_windowIsTranslucent = R_Hide.styleable.Window_windowIsTranslucent.get();
//            int R_Styleable_Window_windowIsFloating = R_Hide.styleable.Window_windowIsFloating.get();
//            int R_Styleable_Window_windowShowWallpaper = R_Hide.styleable.Window_windowShowWallpaper.get();
//
//            if (AttributeCache.instance() == null) {
//                AttributeCache.init(VirtualCore.get().getContext());
//            }
//            AttributeCache.Entry ent = AttributeCache.instance().get(targetInfo.packageName, targetInfo.theme,
//                    R_Styleable_Window);
//            if (ent != null && ent.array != null) {
//                showWallpaper = ent.array.getBoolean(R_Styleable_Window_windowShowWallpaper, false);
//                isTranslucent = ent.array.getBoolean(R_Styleable_Window_windowIsTranslucent, false);
//                isFloating = ent.array.getBoolean(R_Styleable_Window_windowIsFloating, false);
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//        boolean isDialogStyle = isFloating || isTranslucent || showWallpaper;
//        if (isDialogStyle) {
//            return StubManifest.getStubDialogName(vpid);
//        } else {
//            return StubManifest.getStubActivityName(vpid);
//        }
    }
    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
