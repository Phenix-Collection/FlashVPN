package com.lody.virtual.client.hook.patchs.am;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.BitmapUtils;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class BroadcastIntent extends Hook {

    private static boolean filterFBOnce = false;
    private static final String TAG = "BroadcastIntent";
    @Override
    public String getName() {
        return "broadcastIntent";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        Intent intent = (Intent) args[1];
        VLog.logbug(TAG, "enter call for : " + intent.toString());
        if (intent == null || intent.getAction() == null) {
//                || intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")
//                || intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")
//                || intent.getAction().equals("com.facebook.GET_UNIQUE_ID")
//                || intent.getAction().equals("com.facebook.GET_PHONE_ID")) {
            return 0;
        }
        if (intent.getAction().equals("appclone.intent.action.SHOW_CRASH_DIALOG")) {
            return method.invoke(who, args);
        }
        if (!filterFBOnce && intent.getAction().equals("com.facebook.zero.ACTION_ZERO_REFRESH_TOKEN")) {
            VLog.logbug(TAG, "filter fb onece");
            return 0;
        }
        String type = (String) args[2];
        intent.setDataAndType(intent.getData(), type);
        if (VirtualCore.get().getComponentDelegate() != null) {
            VirtualCore.get().getComponentDelegate().onSendBroadcast(intent);
        }
        Intent newIntent = handleIntent(intent);
        if (newIntent != null) {
            args[1] = newIntent;
        } else {
            return 0;
        }
        if (args[7] instanceof String || args[7] instanceof String[]) {
            // clear the permission
            args[7] = null;
        }
        int resultToIdx = ArrayUtils.indexOfFirst(args, IIntentReceiver.class);
        if (resultToIdx != -1) {
            IIntentReceiver resultTo = (IIntentReceiver)args[resultToIdx];
            IIntentReceiver proxy = new RegisterReceiver.ProxyIIntentReceiver(resultTo);
            args[resultToIdx] = proxy;
        }

        Object ret = method.invoke(who, args);
        if(newIntent != null) {
            VLog.d("BroadcastIntent", "x call for : " + newIntent.toString());
        }
        return ret;
    }


    private Intent handleIntent(final Intent intent) {
        final String action = intent.getAction();
        if ("android.intent.action.CREATE_SHORTCUT".equals(action)
                || "com.android.launcher.action.INSTALL_SHORTCUT".equals(action)) {
            handleInstallShortcutIntent(intent);
        } else if ("com.android.launcher.action.UNINSTALL_SHORTCUT".equals(action)) {
            handleUninstallShortcutIntent(intent);
        } else {
            return ComponentUtils.redirectBroadcastIntent(intent, VUserHandle.myUserId());
        }
        return intent;
    }

    private void handleInstallShortcutIntent(Intent intent) {
        Intent shortcut = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String pkg;
        if (shortcut != null) {
            ComponentName component = shortcut.resolveActivity(VirtualCore.getPM());
            if (component != null) {
                pkg = component.getPackageName();
                Intent newShortcutIntent = new Intent();
                newShortcutIntent.setClassName(getHostPkg(), Constants.SHORTCUT_PROXY_ACTIVITY_NAME);
                newShortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
                newShortcutIntent.putExtra("_VA_|_intent_", shortcut);
                newShortcutIntent.putExtra("_VA_|_uri_", shortcut.toUri(0));
                newShortcutIntent.putExtra("_VA_|_user_id_", VUserHandle.myUserId());
                intent.removeExtra(Intent.EXTRA_SHORTCUT_INTENT);
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, newShortcutIntent);
                if (VirtualCore.get().getAppApiDelegate() != null) {
                    String label = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                    label = VirtualCore.get().getAppApiDelegate().getCloneTagedLabel(label);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
                }
                Intent.ShortcutIconResource icon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                if (icon != null && !TextUtils.equals(icon.packageName, getHostPkg())) {
                    try {
                        Resources resources = VirtualCore.get().getResources(pkg);
                        if (resources != null) {
                            int resId = resources.getIdentifier(icon.resourceName, "drawable", pkg);
                            if (resId > 0) {
                                Drawable iconDrawable = resources.getDrawable(resId);
                                Bitmap newIcon = BitmapUtils.drawableToBitmap(iconDrawable);
                                newIcon = VirtualCore.get().getAppApiDelegate().createCloneTagedBitmap(icon.packageName, newIcon);
                                if (newIcon != null) {
                                    intent.removeExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, newIcon);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Bitmap origIcon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
                        if (origIcon != null && pkg != null && !TextUtils.equals(pkg, getHostPkg())){
                            Bitmap newIcon = VirtualCore.get().getAppApiDelegate().createCloneTagedBitmap(pkg, origIcon);
                            if (newIcon != null) {
                                intent.removeExtra(Intent.EXTRA_SHORTCUT_ICON);
                                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, newIcon);
                            }
                        }
                    }catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void handleUninstallShortcutIntent(Intent intent) {
        Intent shortcut = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (shortcut != null) {
            ComponentName componentName = shortcut.resolveActivity(getPM());
            if (componentName != null) {
                Intent newShortcutIntent = new Intent();
                newShortcutIntent.putExtra("_VA_|_uri_", shortcut);
                newShortcutIntent.setClassName(getHostPkg(), Constants.SHORTCUT_PROXY_ACTIVITY_NAME);
                newShortcutIntent.removeExtra(Intent.EXTRA_SHORTCUT_INTENT);
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, newShortcutIntent);
            }
        }
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
