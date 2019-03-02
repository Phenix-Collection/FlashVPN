package com.polestar.clone.client.hook.proxies.shortcut;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.ArraySet;

import com.polestar.clone.BitmapUtils;
import com.polestar.clone.CustomizeAppData;
import com.polestar.clone.client.VClientImpl;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.env.SpecialComponentList;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.helper.compat.ParceledListSliceCompat;
import com.polestar.clone.helper.utils.Reflect;
import com.polestar.clone.helper.utils.VLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mirror.android.content.pm.IShortcutService;
import mirror.android.content.pm.ParceledListSlice;

/**
 * @author Lody
 */
@TargetApi(25)

public class ShortcutServiceStub extends BinderInvocationProxy {


    private final static String TAG = "shortcut";
    public ShortcutServiceStub() {
        super(IShortcutService.Stub.TYPE, "shortcut");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();

        addMethodProxy(new WrapperShortcutInfo("createShortcutResultIntent",1, null));
        addMethodProxy(new WrapperShortcutInfo("updateShortcuts" ,1, false));
        addMethodProxy(new WrapperShortcutInfo("requestPinShortcut", 1, false));
        addMethodProxy(new ReplaceCallingPkgMethodProxyNoException("getManifestShortcuts", ParceledListSliceCompat.create(new ArrayList())));

        addMethodProxy(new UnWrapperShortcutInfo("getDynamicShortcuts"));
        addMethodProxy(new WrapperShortcutInfo("setDynamicShortcuts",1, true));
        addMethodProxy(new WrapperShortcutInfo("addDynamicShortcuts", 1,true));
        addMethodProxy(new ReplaceCallingPkgMethodProxyNoException("removeDynamicShortcuts", 0));
        addMethodProxy(new ReplaceCallingPkgMethodProxyNoException("removeAllDynamicShortcuts", 0));

        addMethodProxy(new UnWrapperShortcutInfo("getPinnedShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("disableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRemainingCallCount"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRateLimitResetTime"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getIconMaxDimensions"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getMaxShortcutCountPerActivity"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("reportShortcutUsed"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("onApplicationActive"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("hasShortcutHostPermission"));
    }

    private static final String EXTRA_CLONED_APP_PACKAGENAME = "app_packagename";
    private static final String EXTRA_FROM = "From where";
    private static final String VALUE_FROM_SHORTCUT = "cloned_shortcut";
    private static final String EXTRA_CLONED_APP_USERID = "app_userid";
    private static final String EXTRA_INTENT = "va|_shortcut_intent_";
    private static final String EXTRA_ACTIVITY = "va|_shortcut_comp_";
    private static final String EXTRA_CAT = "va|_shortcut_cat_";
    private static final String EXTRA_ID = "va|_shortcut_id_";


    private static String setToString(Set arg4) {
        if(arg4 == null) {
            return null;
        }
        else {
            StringBuilder v1 = new StringBuilder();
            Iterator v2 = arg4.iterator();
            int v0_1 = 1;
            while(v2.hasNext()) {
                if(v0_1 != 0) {
                    v0_1 = 0;
                }
                else {
                    v1.append(",");
                }

                v1.append(v2.next());
            }

            return  v1.toString();
        }
    }

    private static Set toSet(String arg5) {
        if(arg5 == null) {
            return null;
        }
        else {
            String[] v2 = arg5.split(",");
            ArraySet v0_1 = new ArraySet();
            int v3 = v2.length;
            int v1;
            for(v1 = 0; v1 < v3; ++v1) {
                ((Set)v0_1).add(v2[v1]);
            }
            return  v0_1;
        }
    }

    private Intent wrapperShortcutIntent(ShortcutInfo info, String pkg, int userId) {
        Intent actionIntent = new Intent(Intent.ACTION_DEFAULT);
        actionIntent.setClassName(VirtualCore.get().getHostPkg(), SpecialComponentList.APP_LOADING_ACTIVITY);
        actionIntent.putExtra(EXTRA_CLONED_APP_PACKAGENAME, pkg);
        actionIntent.putExtra(EXTRA_FROM , VALUE_FROM_SHORTCUT);
        actionIntent.putExtra(EXTRA_CLONED_APP_USERID, userId);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        actionIntent.putExtra(EXTRA_CAT, setToString(info.getCategories()));
        actionIntent.putExtra(EXTRA_ACTIVITY, info.getActivity());
        actionIntent.putExtra(EXTRA_INTENT, info.getIntent().toUri(0));
        actionIntent.putExtra(EXTRA_ID, info.getId());
        return actionIntent;
    }

    ShortcutInfo wrapShortcutInfo(Context arg6, ShortcutInfo shortcutInfo, String pkg, int userId) {
//        Object v0 = Reflect.on(shortcutInfo).opt("mIcon");
//        Bitmap v0_1 = v0 != null ?
//                BitmapUtils.drawableToBitmap(((Icon)v0).loadDrawable(arg6)) :
//                BitmapUtils.drawableToBitmap(arg6.getApplicationInfo().loadIcon(VirtualCore.get().getPackageManager()));
        Intent v1 = wrapperShortcutIntent(shortcutInfo, pkg, userId);
        ShortcutInfo.Builder v2 = new ShortcutInfo.Builder(VirtualCore.get().getContext(),
                pkg + "_" + userId+"_"+shortcutInfo.getId());
//        if(shortcutInfo.getLongLabel() != null) {
//            v2.setLongLabel(shortcutInfo.getLongLabel());
//        }
//
//        if(shortcutInfo.getShortLabel() != null) {
//            v2.setShortLabel(shortcutInfo.getShortLabel());
//        }

        CustomizeAppData appData = CustomizeAppData.loadFromPref(pkg, userId);
        v2.setIcon(Icon.createWithBitmap(appData.getCustomIcon()));
        v2.setShortLabel(appData.label);
        v2.setLongLabel(appData.label);
        v2.setIntent(v1);
        return v2.build();
    }

    @TargetApi(25)
    ShortcutInfo unwrapShortcutInfo(Context arg7, ShortcutInfo shortcutInfo, String pkg, int userId)  throws Throwable{
        ShortcutInfo v1 = null;
        Intent v3 = shortcutInfo.getIntent();
        if(v3 != null) {
            String v0 = v3.getStringExtra(EXTRA_CLONED_APP_PACKAGENAME);
            int v2 = v3.getIntExtra(EXTRA_CLONED_APP_USERID, 0);
            if(TextUtils.equals(v0, pkg) && v2 == userId) {
                v0 = shortcutInfo.getId();
                String v4 = v3.getStringExtra(EXTRA_ID);
                Object v0_1 = Reflect.on(shortcutInfo).opt("mIcon");
                String v2_1 = v3.getStringExtra(EXTRA_INTENT);
                Intent v2_2 = Intent.parseUri(v2_1, 0) ;
                Parcelable v1_1 = v3.getParcelableExtra(EXTRA_ACTIVITY);
                String v3_1 = v3.getStringExtra(EXTRA_CAT);
                ShortcutInfo.Builder v5 = new ShortcutInfo.Builder(arg7, v4);
                if(v0_1 != null) {
                    v5.setIcon(((Icon)v0_1));
                }

                if(shortcutInfo.getLongLabel() != null) {
                    v5.setLongLabel(shortcutInfo.getLongLabel());
                }

                if(shortcutInfo.getShortLabel() != null) {
                    v5.setShortLabel(shortcutInfo.getShortLabel());
                }

                if(v1_1 != null) {
                    v5.setActivity(((ComponentName)v1_1));
                }

                if(v2_2 != null) {
                    v5.setIntent(v2_2);
                }

                Set v0_2 = ShortcutServiceStub.toSet(v3_1);
                if(v0_2 != null) {
                    v5.setCategories(v0_2);
                }

                v1 = v5.build();
            }
        }

        return v1;
    }

    class ReplaceCallingPkgMethodProxyNoException extends ReplaceCallingPkgMethodProxy {
        private Object defValue;

        public ReplaceCallingPkgMethodProxyNoException(String arg1, Object arg2) {
            super(arg1);
            this.defValue = arg2;
        }

        public Object call(Object arg4, Method arg5, Object[] arg6) {
            Object v0_1;
            try {
                v0_1 = super.call(arg4, arg5, arg6);
            }
            catch(Throwable v0) {
                VLog.e("Caught", v0.getMessage());
                v0.printStackTrace();
                v0_1 = this.defValue;
            }

            return v0_1;
        }
    }

    class UnWrapperShortcutInfo extends ReplaceCallingPkgMethodProxy {
        public UnWrapperShortcutInfo(String arg1) {
            super(arg1);
        }

        @Override
        public Object call(Object arg8, Method arg9, Object[] arg10) {
            Object v0;
            try {
                v0 = super.call(arg8, arg9, arg10);
                if(v0 != null) {
                    ArrayList v3 = new ArrayList();
                    v0 = ParceledListSlice.getList.call(v0, new Object[0]);
                    if(v0 != null) {
                        int v2;
                        for(v2 = ((List)v0).size() - 1; v2 >= 0; --v2) {
                            Object v1 = ((List)v0).get(v2);
                            if((v1 instanceof ShortcutInfo)) {
                                ShortcutInfo v1_1 = unwrapShortcutInfo(VClientImpl.get().getCurrentApplication(), ((ShortcutInfo)v1), getAppPkg(), getAppUserId());
                                if(v1_1 != null) {
                                    ((List)v3).add(v1_1);
                                }
                            }
                        }
                    }
                    v0 = ParceledListSliceCompat.create(((List)v3));
                }
            }catch (Throwable ex) {
                v0 = null;
            }
            return v0;
        }
    }

    class WrapperShortcutInfo extends ReplaceCallingPkgMethodProxy {
        private Object defValue;
        private int infoIndex;

        public WrapperShortcutInfo(String arg1, int arg2, Object arg3) {
            super(arg1);
            this.infoIndex = arg2;
            this.defValue = arg3;
        }

        public Object call(Object arg8, Method arg9, Object[] arg10) {
            VLog.d("Shortcut",getMethodName() + " app " + getAppPkg() + " id; " + getAppUserId());
            try {
                Object v0;
                v0 = arg10[this.infoIndex];
                if (v0 != null) {
                    if ((v0 instanceof ShortcutInfo)) {
                        arg10[this.infoIndex] = wrapShortcutInfo(VClientImpl.get().getCurrentApplication(), ((ShortcutInfo) v0), getAppPkg(), getAppUserId());
                    } else {
                        ArrayList v3 = new ArrayList();
                        v0 = ParceledListSlice.getList.call(v0, new Object[0]);
                        if (v0 != null) {
                            int v2;
                            for (v2 = ((List) v0).size() - 1; v2 >= 0; --v2) {
                                Object v1 = ((List) v0).get(v2);
                                if ((v1 instanceof ShortcutInfo)) {
                                    ShortcutInfo v1_1 = wrapShortcutInfo(VClientImpl.get().getCurrentApplication(), ((ShortcutInfo) v1), getAppPkg(), getAppUserId());
                                    if (v1_1 != null) {
                                        ((List) v3).add(v1_1);
                                    }
                                }
                            }
                        }
                        arg10[this.infoIndex] = ParceledListSliceCompat.create(((List) v3));
                    }

                    return arg9.invoke(arg8, arg10);
                } else {
                    return defValue;
                }
            } catch (Throwable ex) {
                return defValue;
            }
        }
    }
}
