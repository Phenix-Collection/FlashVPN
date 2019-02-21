package in.dualspace.cloner.widget;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polestar.clone.client.core.VirtualCore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.R;
import in.dualspace.cloner.clone.CloneManager;
import in.dualspace.cloner.db.CloneModel;
import in.dualspace.cloner.utils.CommonUtils;
import in.dualspace.cloner.utils.DisplayUtils;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;

/**
 * Created by guojia on 2019/2/20.
 */

public class AddClonePopup implements AdapterView.OnItemClickListener, View.OnClickListener{
    private PopupWindow popupWindow;
    private View popupView;
    private Activity activity;
    private Handler mainHandler;
    private GridView hotAppGridView;
    private GridView moreAppGridView;
    private TextView cloneButton;
    private ProgressBar progressBar;
    private boolean appListReady;

    private static final String CONFIG_HOT_CLONE_LIST = "hot_clone_list";
    private static final String CONFIG_ADD_CLONE_AD_POS = "conf_add_clone_ad_pos";
    public static final String SLOT_ADD_CLONE_AD = "slot_add_clone_native";
    private ArrayList<SelectGridAppItem> hotAppList = new ArrayList<>();
    private ArrayList<SelectGridAppItem> otherAppList = new ArrayList<>();
    private int selected;

    private SelectPkgGridAdapter hotAdapter;
    private SelectPkgGridAdapter moreAdapter;

    private LinearLayout appGridLayout;

    public AddClonePopup(Activity context) {
        activity = context;
        popupView = View.inflate(context, R.layout.add_clone_popup_layout, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);

        FrameLayout root = popupView.findViewById(R.id.root);
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtils.getScreenHeight(context)*2/3, Gravity.BOTTOM));
        popupWindow.setOutsideTouchable(false);
//        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.setAnimationStyle(R.style.anim_bottombar);


        hotAppGridView = popupView.findViewById(R.id.hot_clone_grid);
        moreAppGridView = popupView.findViewById(R.id.more_clone_grid);
        cloneButton = popupView.findViewById(R.id.clone_button);
        cloneButton.setOnClickListener(this);

        progressBar = popupView.findViewById(R.id.progressBar);
        appGridLayout = popupView.findViewById(R.id.app_grid_layout);

        hotAdapter = new SelectPkgGridAdapter(activity,hotAppList);
        hotAppGridView.setAdapter(hotAdapter);
        hotAppGridView.setOnItemClickListener(this);

        moreAdapter = new SelectPkgGridAdapter(activity,otherAppList);
        moreAppGridView.setAdapter(moreAdapter);
        moreAppGridView.setOnItemClickListener(this);

        popupView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        selected = 0;
        cloneButton.setText(String.format(activity.getString(R.string.clone_action_txt), ""));
    }

    public void loadAppListAsync() {
        // NOT include host APP itself, already cloned APP in core and popular APP.
        appListReady = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String hotCloneConf = RemoteConfig.getString(CONFIG_HOT_CLONE_LIST);
                HashSet<String> hotCloneSet = new HashSet<>();
                if(!TextUtils.isEmpty(hotCloneConf)) {
                    String[] arr = hotCloneConf.split(":");
                    for (String s: arr) {
                        hotCloneSet.add(s);
                    }
                }
                PackageManager pm = activity.getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
                String hostPkg = activity.getPackageName();

                int adPos = (int)RemoteConfig.getLong(CONFIG_ADD_CLONE_AD_POS);
                adPos = adPos % 3 == 0 ? 9 : adPos;
                for (ResolveInfo resolveInfo : resolveInfos) {
                    String pkgName = resolveInfo.activityInfo.packageName;
                    if (hostPkg.equals(pkgName)) {
                        continue;
                    }
                    if (!CloneManager.getInstance(activity).isAllowedToClone(pkgName)) {
                        continue;
                    }
                    if (!CloneManager.getInstance(activity).isClonable(pkgName)) {
                        MLogs.d("package: " + pkgName + " not clonable!");
                        continue;
                    }
                    SelectGridAppItem item = new SelectGridAppItem();
                    item.icon = resolveInfo.activityInfo.loadIcon(pm);
                    item.name = resolveInfo.activityInfo.loadLabel(pm);
                    item.selected = false;
                    item.pkg = pkgName;
                    if (hotCloneSet.contains(pkgName)) {
                        hotAppList.add(item);
                    } else{
                        otherAppList.add(item);
                    }
                }
                if ( hotAppList.size() < adPos ) {
                    int pad = adPos - hotAppList.size();
                    for (int i = 0; i < pad && otherAppList.size() > 0; i++) {
                        hotAppList.add(otherAppList.get(otherAppList.size() - 1));
                        otherAppList.remove(otherAppList.size() - 1);
                    }
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appListReady = true;
                        updateView();
                    }
                });
            }
        }).start();
    }

    private void updateView() {
        if (appListReady) {
            appGridLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            hotAdapter.notifyDataSetChanged();
            moreAdapter.notifyDataSetChanged();
            cloneButton.setClickable(true);
        } else  {
            appGridLayout.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            cloneButton.setClickable(false);
        }
        //setGrideViewHeightBasedOnChildren(hotAppGridView);
    }

    public void cancel() {
        MLogs.d("cancel");
        doDismiss();
    }

    public void popup() {
        MLogs.d("popup");
        if (popupWindow != null) {
            View root = activity.findViewById(android.R.id.content);
            if (root != null) {

                popupWindow.showAtLocation(root, Gravity.BOTTOM, DisplayUtils.dip2px(activity, 10), DisplayUtils.dip2px(activity, 10));

            }
            updateView();
//            if(!mIsShowing){
//                params.alpha= 0.3f;
//                getWindow().setAttributes(params);
//
//                mIsShowing = true;
//            }
        }

    }

    private void reset() {
        selected = 0;
        cloneButton.setText(String.format(activity.getString(R.string.clone_action_txt), ""));
        for (SelectGridAppItem item: hotAppList) {
            item.selected = false;
        }
        for (SelectGridAppItem item: otherAppList) {
            item.selected = false;
        }
        if (popupWindow != null) {
            popupWindow.setOnDismissListener(null);
        }
    }

    public void dismiss() {
        MLogs.d("dismiss");
        doDismiss();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        if (popupWindow != null) {
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    listener.onDismiss();
                }
            });
        }
    }

    private void doDismiss() {
        MLogs.d("do dismiss");
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        reset();
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SelectGridAppItem item = (SelectGridAppItem) view.getTag();
        if (item != null) {
            ImageView cbox = (ImageView) view.findViewById(R.id.select_cb_img);
            View cover = view.findViewById(R.id.cover);
            if (cbox != null) {
                item.selected = !item.selected;
                if (item.selected) {
                    selected ++;
                    cbox.setImageResource(R.drawable.selectd);
                    cover.setVisibility(View.INVISIBLE);
                } else {
                    selected --;
                    cbox.setImageResource(R.drawable.not_select);
                    cover.setVisibility(View.VISIBLE);
                }
                if (selected > 0) {
                    cloneButton.setText(String.format(activity.getString(R.string.clone_action_txt), "(" + selected + ")"));
                    cloneButton.setEnabled(true);

                } else {
                    cloneButton.setText(String.format(activity.getString(R.string.clone_action_txt), ""));
                    cloneButton.setEnabled(false);

                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clone_button:
                boolean hasLock = false;
                CloneManager cm = CloneManager.getInstance(activity);
                cm.clearPendingClones();
                for (SelectGridAppItem item: hotAppList) {
                    if( item.selected) {
                        CloneModel model = new CloneModel(item.pkg, activity);
                        if (CommonUtils.isSocialApp(item.pkg)) {
                            model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                            model.setNotificationEnable(true);
                        }
                        //model.setName(cm.getDefaultName(item.pkg));
                        int userId = cm.getNextAvailableUserId(item.pkg);
                        PackageManager pm = activity.getPackageManager();
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(item.pkg, 0);
                            CharSequence label = pm.getApplicationLabel(ai);
                            model.setName(VirtualCore.getCompatibleName(""+label, userId));
                            cm.createClone(activity, model, userId);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (SelectGridAppItem item: otherAppList) {
                    if( item.selected) {
                        CloneModel model = new CloneModel(item.pkg, activity);
                        if (CommonUtils.isSocialApp(item.pkg)) {
                            model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                        }
                        int userId = cm.getNextAvailableUserId(item.pkg);
                        PackageManager pm = activity.getPackageManager();
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(item.pkg, 0);
                            CharSequence label = pm.getApplicationLabel(ai);
                            model.setName(VirtualCore.getCompatibleName(""+label, userId));
                            cm.createClone(activity, model, userId);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                MLogs.d("on clone button click");

                PreferencesUtils.setHasCloned();
                cancel();

                break;
        }
    }
}
