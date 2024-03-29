package in.dualspace.cloner.components.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.clone.CloneAgent64;
import com.polestar.clone.CustomizeAppData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;
import in.dualspace.cloner.clone.CloneManager;
import in.dualspace.cloner.components.AppMonitorService;
import in.dualspace.cloner.db.CloneModel;
import in.dualspace.cloner.db.DBManager;
import in.dualspace.cloner.notification.QuickSwitchNotification;
import in.dualspace.cloner.utils.CommonUtils;
import in.dualspace.cloner.utils.DisplayUtils;
import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;
import in.dualspace.cloner.widget.AddClonePopup;
import in.dualspace.cloner.widget.BottomProgressPopup;
import in.dualspace.cloner.widget.ExplosionField;
import in.dualspace.cloner.widget.PageIndicatorView;
import in.dualspace.cloner.widget.PageRecyclerView;
import in.dualspace.cloner.widget.RateDialog;
import in.dualspace.cloner.widget.UpDownDialog;

/**
 * Created by guojia on 2019/2/16.
 */

public class MainActivity extends Activity implements CloneManager.OnClonedAppChangListener{

    private PageRecyclerView pageRecyclerView = null;
    private PageRecyclerView.PageAdapter listAdapter = null;

    private CloneManager cm;
    private List<CustomizedCloneItem> mItemList = new ArrayList<>();

    private String startingPkg;

    private static final String CONFIG_FORCE_REQUESTED_PERMISSIONS = "force_requested_permission";
    private static final int REQUEST_APPLY_PERMISSION = 101;


    private static final String CONFIG_CLONE_RATE_PACKAGE = "clone_rate_package";
    private static final String CONFIG_CLONE_RATE_INTERVAL = "clone_rate_interval";
    private boolean rateDialogShowed = false;
    private static final String RATE_FROM_QUIT = "quit";
    private static final String RATE_AFTER_CLONE = "clone";
    private static final String RATE_FROM_MENU = "menu";

    private int luckyPos;
    private int adTaskPos;

    private ExplosionField mExplosionField;
    private static final int PAGE_ITEM_SIZE = 3*3;
    private BottomProgressPopup bottomProgress;
    private AddClonePopup addClonePopup;

    private CustomizedCloneItem pendingStartItem;

    private LinearLayout nativeAdContainer;
    public static final String SLOT_HOME_NATIVE = "slot_home_native";
    private FuseAdLoader adLoader ;
    private long adShowTime = 0;

    private int lastCloneIdx = -1;
    private int longClickPos = -1;
    private int pendingUnlockPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesUtils.setLiteMode(true);

        setContentView(R.layout.main_activity_layout);
        nativeAdContainer = findViewById(R.id.ad_container);
        mExplosionField = ExplosionField.attachToWindow(this);
        pageRecyclerView = (PageRecyclerView) findViewById(R.id.cusom_swipe_view);
        // 设置指示器
        pageRecyclerView.setIndicator((PageIndicatorView) findViewById(R.id.page_indicator));
        // 设置行数和列数
        pageRecyclerView.setPageSize(3, 3);
        // 设置页间距
        pageRecyclerView.setPageMargin(30);
        // 设置数据
        pageRecyclerView.setAdapter(listAdapter = pageRecyclerView.new PageAdapter(mItemList, new PageRecyclerView.CallBack() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.grid_app_item, parent, false);
                return new MyHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//                if (position < mItemList.size()) {
//                    MLogs.e("Error item out of size");
//                    return;
//                }
                int idx = convertDisplayPosAndIdx(position);
                CustomizedCloneItem item;
                if (idx >=0 && idx < mItemList.size()) {
                    item = mItemList.get(idx);
                } else {
                    item = new CustomizedCloneItem(CustomizedCloneItem.TYPE_EMPTY);
                }
                MyHolder myHolder = (MyHolder) holder;
                MLogs.d("onBindViewHolder " + position + " type: " + item.type);
                if (item != null) {
                    holder.itemView.setTag(item);
                    item.fillViewHolder(myHolder);
                    myHolder.icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.onClick(v);
                        }
                    });
                    myHolder.icon.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            longClickPos = position;
                            item.onLongClick(v);
                            return true;
                        }
                    });
                }

            }

        }));

        bottomProgress = new BottomProgressPopup(this);
        addClonePopup = new AddClonePopup(this);
        addClonePopup.loadAppListAsync();

        initData();

        adLoader = FuseAdLoader.get(SLOT_HOME_NATIVE, this);

        String from = getIntent().getStringExtra(AppConstants.EXTRA_FROM);
        if (TextUtils.isEmpty(from)) {
            from = "main";
        }
        EventReporter.reportWake(this, "user_launch_" + from);
        if (!PreferencesUtils.isShortCutCreated()) {
            PreferencesUtils.setShortCutCreated();
            CommonUtils.createLaunchShortcut(this);
        }

        if (needUpdate()) {
            MLogs.d("need update");
            pageRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showUpdateDialog();
                }
            }, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingStartItem != null) {
            startAppItem(pendingStartItem);
        } else {
            if (!guideRateIfNeeded()) {
                guideQuickSwitchIfNeeded();
            }
            //保证每次弹框尽可能只会在启动分身后，回到主界面时做一次判断，而不包含从后台切到主界面的时候
            startingPkg = null;
        }
        if (pendingUnlockPos >= 0 ) {
            int idx = convertDisplayPosAndIdx(pendingUnlockPos);
            MLogs.d("update pos for unlock click: " + pendingUnlockPos + " idx: " + idx);
            CustomizedCloneItem item = mItemList.get(idx);
            if (AppMonitorService.isUnlocked(getPackageName(), 0)
                    || AppMonitorService.isUnlocked(item.clone.getPackageName(), item.clone.getPkgUserId())) {
                item.clone.setLockerState(AppConstants.AppLockState.DISABLED);
                DBManager.updateCloneModel(MainActivity.this, item.clone);
                CloneManager.reloadLockerSetting();
                Toast.makeText(this, getString(R.string.been_unlocked, item.title.getText()), Toast.LENGTH_SHORT).show();
            }
            listAdapter.notifyItemChanged(pendingUnlockPos);
            pendingUnlockPos = -1;
        }
        if (longClickPos >= 0 ) {
            MLogs.d("update pos for long click: " + longClickPos);
            listAdapter.notifyItemChanged(longClickPos);
            longClickPos = -1;
        }
        long current = System.currentTimeMillis();
        if (current - adShowTime > RemoteConfig.getLong("home_ad_refresh_interval_s")*1000) {
            loadAd();
        }
    }

    private void showRateDialog(String from, String pkg){
        if (RATE_AFTER_CLONE.equals(from) || RATE_FROM_QUIT.equals(from)){
            if (rateDialogShowed ) {
                MLogs.d("Already showed dialog this time");
                return;
            }
            rateDialogShowed= true;
        }
        PreferencesUtils.updateRateDialogTime(this);
        String s = from+"_"+pkg;
        RateDialog rateDialog = new RateDialog(this, s);
        rateDialog.show().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                EventReporter.reportRate(s+"_cancel", s);
                PreferencesUtils.setLoveApp(false);
            }
        });
    }

    private boolean guideRateIfNeeded() {
        if (startingPkg != null) {
            String pkg = startingPkg;
            //startingPkg = null;
            MLogs.d("Cloning package: " + pkg);
            if (PreferencesUtils.isRated()) {
                return false;
            }
            String config = RemoteConfig.getString(CONFIG_CLONE_RATE_PACKAGE);
            if ("off".equalsIgnoreCase(config)) {
                MLogs.d("Clone rate off");
                return false;
            }
            if(PreferencesUtils.getLoveApp() == -1) {
                // not love, should wait for interval
                long interval = RemoteConfig.getLong(CONFIG_CLONE_RATE_INTERVAL) * 60 * 60 * 1000;
                if ((System.currentTimeMillis() - PreferencesUtils.getRateDialogTime(this)) < interval) {
                    MLogs.d("Not love, need wait longer");
                    return false;
                }
            }
            boolean match = "*".equals(config);
            if (!match) {
                String[] pkgList = config.split(":");
                if (pkgList != null && pkgList.length > 0) {
                    for (String s: pkgList) {
                        if(s.equalsIgnoreCase(pkg)) {
                            match = true;
                            break;
                        }
                    }
                }
            }
            if (match) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showRateDialog(RATE_AFTER_CLONE, pkg);
                    }
                }, 800);
                return true;
            } else {
                MLogs.d("No matching package for clone rate");
            }
        }
        return false;
    }

    private void startAppItem(CustomizedCloneItem item) {
        if (item == null || item.clone == null) {
            MLogs.d("Invalid pending start item");
            return;
        }
        startingPkg = item.clone.getPackageName();
        if (item.clone.getLaunched() == 0) {
            item.clone.setLaunched(1);
            if (item.newDot != null) {
                item.newDot.setVisibility(View.INVISIBLE);
            }
            DBManager.updateCloneModel(MainActivity.this, item.clone);
        }
        doLaunchItem(item);
        pendingStartItem = null;
    }

    private void doLaunchItem(CustomizedCloneItem item) {
        if (!RemoteConfig.getBoolean("conf_start_by_bottom_bar")) {
            AppLoadingActivity.startAppStartActivity(MainActivity.this, item.clone);
            return;
        }
        boolean needDoUpGrade = CloneManager.needUpgrade(item.clone.getPackageName());
        if (needDoUpGrade) {
            CloneManager.killApp(item.clone.getPackageName());
        }
        boolean isAppRunning = CloneManager.isAppLaunched(item.clone);
        boolean isFirstStart = PreferencesUtils.isFirstStart(item.clone.getName());
        if (isFirstStart) {
            PreferencesUtils.setStarted(item.clone.getName());
        }
        bottomProgress.setIconBitmap(item.customizeAppData != null? item.customizeAppData.getCustomIcon() : item.clone.getCustomIcon());
        bottomProgress.setTitle(getString(R.string.app_starting_tips,
                item.customizeAppData != null && item.customizeAppData.customized ? item.customizeAppData.label: item.clone.getName()));
        if (isFirstStart) {
            bottomProgress.setTips(getString(R.string.first_start_tips));
        } else if(needDoUpGrade) {
            bottomProgress.setTips(getString(R.string.upgrade_start_tips));
        } else {
            bottomProgress.setTips(null);
        }
        long delay = 0;
        if (isFirstStart || needDoUpGrade) {
            delay = 4000;
            bottomProgress.setAutoDismissDuration(10*1000);
        } else if (!isAppRunning) {
            delay = 2500;
            bottomProgress.setAutoDismissDuration(5*1000);
        }
        if (delay > 0) {
            bottomProgress.popup();
            pageRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CloneManager.launchApp(MainActivity.this, item.clone,isFirstStart);
                }
            }, delay);
        }  else {
            bottomProgress.cancel();
            CloneManager.launchApp(MainActivity.this, item.clone, isFirstStart);
        }
        EventReporter.appStart(!isAppRunning,
                item.clone.getLockerState() != AppConstants.AppLockState.DISABLED,
                "home", item.clone.getPackageName(), item.clone.getPkgUserId());

    }
    
    @Override
    protected void onPause() {
        super.onPause();
        bottomProgress.cancel();
    }

    private void initData() {
        cm = CloneManager.getInstance(this);
        cm.loadClonedApps(this, this);
        lastCloneIdx = -1;
        luckyPos = (int)RemoteConfig.getLong("conf_home_lucky_pos");
        initItemList(null);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public TextView title = null;
        public ImageView icon = null;
        public ImageView newdot = null;
        public ImageView lockIcon = null;
        public TextView adFlag = null;

        public MyHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.app_name);
            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            newdot = (ImageView) itemView.findViewById(R.id.new_dot);
            lockIcon = (ImageView) itemView.findViewById(R.id.lock_icon_badge);
            adFlag = (TextView) itemView.findViewById(R.id.ad_flag);

        }
    }

    private boolean applyPermissionIfNeeded(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String conf = RemoteConfig.getString(CONFIG_FORCE_REQUESTED_PERMISSIONS);
            if (TextUtils.isEmpty(conf)) {
                return false;
            }
            String[] perms = conf.split(";");
            if (perms == null || perms.length == 0) {
                return false;
            }
            ArrayList<String> requestPerms = new ArrayList<>();
            for (String s : perms) {
                if (checkCallingOrSelfPermission(s) != PackageManager.PERMISSION_GRANTED) {
                    requestPerms.add(s);
                }
            }
            if (requestPerms.size() == 0) {
                EventReporter.setUserProperty(EventReporter.PROP_PERMISSION, "granted");
                return false;
            } else {
                EventReporter.setUserProperty(EventReporter.PROP_PERMISSION, "not_granted");
                String[] toRequest = requestPerms.toArray(new String[0]);
                boolean showRequestRational = false;
                for (String s: toRequest) {
                    if (shouldShowRequestPermissionRationale(s)){
                        showRequestRational = true;
                    }
                }
                if (showRequestRational
                        || !PreferencesUtils.hasShownPermissionGuide()) {
                    showPermissionGuideDialog(toRequest);
                } else {
                    requestPermissions(toRequest, REQUEST_APPLY_PERMISSION);
                }
                return true;
            }
        }
        return  false;
    }

    @TargetApi(23)
    private void showPermissionGuideDialog(String[] perms) {
        EventReporter.generalEvent("show_permission_guide");
        UpDownDialog.show(this, getString(R.string.dialog_permission_title),
                getString(R.string.dialog_permission_content), getString(R.string.disagree), getString(R.string.agree),
                -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case UpDownDialog.POSITIVE_BUTTON:
                                EventReporter.generalEvent("ok_permission_guide");
                                PreferencesUtils.setShownPermissionGuide(true);
                                requestPermissions(perms, REQUEST_APPLY_PERMISSION);
                                break;
                            case UpDownDialog.NEGATIVE_BUTTON:
                                EventReporter.generalEvent("disagree_permission_guide");
                                break;

                        }

                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                EventReporter.generalEvent("cancel_permission_guide");
//                requestPermissions(perms, REQUEST_APPLY_PERMISSION);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        /* callback - no nothing */
        switch (requestCode){
            case REQUEST_APPLY_PERMISSION:
                int i = 0;
                boolean success = true;
                for(String p: permissions){
                    if(grantResults[i++] != PackageManager.PERMISSION_GRANTED) {
                        success = false;
                        EventReporter.generalEvent("fail_"+p);
                    }
                }
                EventReporter.generalEvent("apply_permission_" + success);
                MLogs.d("Apply permission result: " + success);
                break;
        }
    }

    @Override
    public void onInstalled(CloneModel clonedApp, boolean result) {
        if (result) {
            addCloneItem(clonedApp);
            bottomProgress.setIconBitmap(clonedApp.getCustomIcon());
            bottomProgress.setTips(getString(R.string.clone_success_tips, clonedApp.getName()));

        }
        if (!CloneManager.getInstance(this).hasPendingClones()) {
            pageRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomProgress.dismiss();
                    MLogs.d("nofity on no pending delayed");
                    listAdapter.notifyDataSetChanged();
                    listAdapter.updatePage();
                }
            }, 500);

        }
    }

    @Override
    public void onUnstalled(CloneModel clonedApp, boolean result) {
        if (result) {
            CommonUtils.removeShortCut(this, clonedApp);
            // remove customized data
            CustomizeAppData.removePerf(clonedApp.getPackageName(), clonedApp.getPkgUserId());
            if (QuickSwitchNotification.isEnable()) {
                QuickSwitchNotification.disable();
                QuickSwitchNotification.enable();
            }
            deleteCloneItem(clonedApp);
        }
    }

    @Override
    public void onLoaded(List<CloneModel> clonedApp) {
        initItemList(cm.getClonedApps());
        if (clonedApp.size() <= RemoteConfig.getLong("add_clone_preload_gate")
                && !PreferencesUtils.isAdFree()) {
//            FuseAdLoader.get(AddCloneActivity.SLOT_ADD_CLONE_AD, DualApp.getApp())
//                    .setBannerAdSize(AddCloneActivity.getBannerAdSize()).preloadAd(this);
        }
    }

    private void addCloneItem(CloneModel model) {
        int lastCloneIdx = -1;
        int samePackageIdx = -1;
        int i = 0;
        for(CustomizedCloneItem item: mItemList) {
            if (item.type == CustomizedCloneItem.TYPE_CLONE) {
                lastCloneIdx = i;
                if (item.clone.getPackageName().equals(model.getPackageName()) ) {
                    samePackageIdx = i;
                }
            }
            i ++;
        }
        int insertIdx = samePackageIdx == -1 ? lastCloneIdx + 1: samePackageIdx + 1;
        mItemList.add(insertIdx, new CustomizedCloneItem(model));
        if (!CloneManager.getInstance(this).hasPendingClones()) {
            this.lastCloneIdx = insertIdx;
        }
    }

    private void deleteCloneItem(CloneModel model) {
        int idx = -1;
        int i = 0;
        for(CustomizedCloneItem item: mItemList) {
            if (item.type == CustomizedCloneItem.TYPE_CLONE) {
                if (item.clone.getPackageName().equals(model.getPackageName())
                        && item.clone.getPkgUserId() == model.getPkgUserId()) {
                    idx = i;
                    break;
                }
            }
            i ++;
        }
        if (idx != -1) {
            mItemList.remove(idx);
        }
        listAdapter.notifyDataSetChanged();
        listAdapter.updatePage();

    }
    private void initItemList(List<CloneModel> cloneModels) {
        mItemList.clear();
        HashMap<String, ArrayList<CloneModel>> sortMap = new HashMap<>();
        if (cloneModels != null) {
            for (CloneModel model: cloneModels) {
                ArrayList list = sortMap.get(model.getPackageName());
                if (list == null) {
                    list = new ArrayList();
                }
                list.add(model);
                sortMap.put(model.getPackageName(), list);
            }
        }
        for (ArrayList<CloneModel> list: sortMap.values()) {
            if (list != null && list.size() > 0) {
                for(CloneModel model: list) {
                    mItemList.add(new CustomizedCloneItem(model));
                }
            }
        }
        int pos = luckyPos > mItemList.size() ? mItemList.size(): luckyPos;
        if (cloneModels !=null && cloneModels.size() > RemoteConfig.getLong("conf_home_lucky_gate")) {
            mItemList.add(pos, new CustomizedCloneItem(CustomizedCloneItem.TYPE_LUCKY));
        }
        mItemList.add(new CustomizedCloneItem(CustomizedCloneItem.TYPE_ADD));
        listAdapter.notifyDataSetChanged();
        listAdapter.updatePage();

    }

    private class CustomizedCloneItem {
        public final static int TYPE_CLONE = 0;
        public final static int TYPE_ADD = 1;
        public final static int TYPE_LUCKY = 2;
        public final static int TYPE_BOOSTER = 3;
        public final static int TYPE_ICON_ADTASK= 4;
        public final static int TYPE_EMPTY= 5;
        public int type;

        public CloneModel clone = null;
        private CustomizeAppData customizeAppData = null;

        public ImageView icon;
        public TextView title;
        public ImageView newDot;

        public CustomizedCloneItem(CloneModel model) {
            type = TYPE_CLONE;
            this.clone = model;
            customizeAppData = CustomizeAppData.loadFromPref(model.getPackageName(),
                    model.getPkgUserId());
//            if (appModel.getCustomIcon() == null) {
//            Bitmap bmp =
//            appIcon.setImageBitmap(bmp);
            model.setCustomIcon(customizeAppData.getCustomIcon());

//                appModel.setCustomIcon(CommonUtils.createCustomIcon(mContext, appModel.getIconDrawable(mContext)));
//            }
        }

        public CustomizedCloneItem(int type) {
            this.type = type;

        }

        public void onClick(View view) {
            if (bottomProgress.isShowing()) {
                return;
            }
            switch (type) {
                case TYPE_CLONE:
                    if(!applyPermissionIfNeeded()) {
                        startAppItem(this);
                    } else {
                        pendingStartItem = this;
                    }
                    break;
                case TYPE_ADD:
                    addClonePopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            if (CloneManager.getInstance(MainActivity.this).hasPendingClones()) {
                                MLogs.d("Has pending clones");
                                bottomProgress.setTitle(getString(R.string.creating_clones));
                                bottomProgress.setTips(null);
                                bottomProgress.setIconRes(R.mipmap.ic_launcher);
                                bottomProgress.setMinDuration(2000);
                                bottomProgress.setAutoDismissDuration(80*1000);
                                bottomProgress.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                    @Override
                                    public void onDismiss() {
                                        listAdapter.notifyDataSetChanged();
                                        listAdapter.updatePage();
                                        MLogs.d("notify on dismiss");
                                        if (lastCloneIdx > 0 && lastCloneIdx < mItemList.size()) {
                                            listAdapter.scrollToPage((lastCloneIdx) / (PAGE_ITEM_SIZE));
                                            CustomizedCloneItem item = mItemList.get(lastCloneIdx);
                                            MLogs.d("lastCloneIdx : " + lastCloneIdx + " type: "  + item.type);
                                            pageRecyclerView.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (item.icon != null) {
                                                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(item.icon, "scaleX", 0.7f, 1.2f, 1.0f);
                                                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(item.icon, "scaleY", 0.7f, 1.2f, 1.0f);
                                                        AnimatorSet animSet = new AnimatorSet();
                                                        animSet.play(scaleX).with(scaleY);
                                                        animSet.setInterpolator(new BounceInterpolator());
                                                        animSet.setDuration(3000).start();
                                                    }
                                                }
                                            }, 500);


                                        }
                                    }
                                });
                                bottomProgress.popup();
                            }
                        }
                    });
                    addClonePopup.popup();
                    break;
                case TYPE_LUCKY:
                    Intent intent = new Intent(MainActivity.this, NativeInterstitialActivity.class);
                    startActivity(intent);
                    break;
            }
        }

        public void onLongClick(View view) {
            if (bottomProgress.isShowing()) {
                return;
            }
            MLogs.d("onLongClick");
            switch (type) {
                case TYPE_EMPTY:
                    break;
                case TYPE_ADD:
                    addClonePopup.popup();
                    break;
                default:
                    showItemMenu(view);
                    break;
            }
        }

        public void fillViewHolder(MyHolder holder) {
            fillIconView(holder.icon);
            holder.icon.setTag(holder);
            fillTitleView(holder.title);
            fillNewDotView(holder.newdot);
            if (clone != null && PreferencesUtils.isLockerEnabled(MainActivity.this)
                    && clone.getLockerState() != AppConstants.AppLockState.DISABLED) {
                holder.lockIcon.setVisibility(View.VISIBLE);
            } else {
                holder.lockIcon.setVisibility(View.INVISIBLE);
            }
            if(type == TYPE_ICON_ADTASK || type == TYPE_LUCKY)  {
                holder.adFlag.setVisibility(View.VISIBLE);
            } else {
                holder.adFlag.setVisibility(View.INVISIBLE);
            }
        }

        private void fillTitleView(TextView textView) {
            title = textView;
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            textView.setTextColor(getResources().getColor(R.color.white));
            switch (type) {
                case TYPE_CLONE:
                    String title = customizeAppData.customized? customizeAppData.label: clone.getName();
                    textView.setText(title);
                    break;
                case TYPE_ADD:
                    textView.setText(R.string.add_clone_title);
                    break;
                case TYPE_LUCKY:
                    textView.setText(R.string.feel_lucky);
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    textView.setTextColor(getResources().getColor(R.color.lucky_red));
                    break;
                default:
                    textView.setVisibility(View.INVISIBLE);
                    break;

            }
        }

        private void fillIconView(ImageView icon) {
            this.icon = icon;
            icon.setVisibility(View.VISIBLE);
            switch (type){
                case TYPE_CLONE:
                    icon.setImageBitmap(customizeAppData.getCustomIcon());
                    break;
                case TYPE_ADD:
                    icon.setImageResource(R.drawable.icon_add);
                    break;
                case TYPE_LUCKY:
                    icon.setImageResource(R.drawable.icon_feel_lucky);
                    break;
                default:
                    icon.setVisibility(View.INVISIBLE);
                    break;
            }
        }

        private void fillNewDotView(ImageView newdot) {
            newDot = newdot;
            newdot.setVisibility(View.INVISIBLE);
//            switch (type){
//                case TYPE_CLONE:
//                    if (clone.getLaunched() == 0) {
//                        newdot.setImageResource(R.drawable.shape_new_dot);
//                        newdot.setVisibility(View.VISIBLE);
//                    } else {
//                        newdot.setVisibility(View.INVISIBLE);
//                    }
//                    break;
//                default:
//                    newdot.setVisibility(View.INVISIBLE);
//                    break;
//            }
        }

        private void showItemMenu(View view) {
            PopupMenu itemMenuPopup = new PopupMenu(MainActivity.this, view);
            itemMenuPopup.inflate(R.menu.item_menu_popup);
            MLogs.d("showItemMenu " + type);
            switch(type) {
                case CustomizedCloneItem.TYPE_LUCKY:
                case CustomizedCloneItem.TYPE_BOOSTER:
                case CustomizedCloneItem.TYPE_ICON_ADTASK:
                    itemMenuPopup.getMenu().removeItem(R.id.item_customize);
                    itemMenuPopup.getMenu().removeItem(R.id.item_locker_enable);
                    itemMenuPopup.getMenu().removeItem(R.id.item_locker_disable);
                    itemMenuPopup.getMenu().removeItem(R.id.item_shortcut);
                    break;
                case CustomizedCloneItem.TYPE_CLONE:
                    if (!PreferencesUtils.isLockerEnabled(MainActivity.this)) {
                        itemMenuPopup.getMenu().removeItem(R.id.item_locker_disable);
                    } else {
                        if (clone.getLockerState() == AppConstants.AppLockState.DISABLED) {
                            itemMenuPopup.getMenu().removeItem(R.id.item_locker_disable);
                        } else {
                            itemMenuPopup.getMenu().removeItem(R.id.item_locker_enable);
                        }
                    }
                    break;
            }
            //菜单项的监听
            itemMenuPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.item_locker_disable:
                        case R.id.item_locker_enable:
                            if (PreferencesUtils.isLockerEnabled(MainActivity.this) ) {
                                boolean changed = false;
                                if(clone.getLockerState() != AppConstants.AppLockState.ENABLED_FOR_CLONE) {
                                    clone.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                                    DBManager.updateCloneModel(MainActivity.this, clone);
                                    CloneManager.reloadLockerSetting();
                                    changed  = true;
                                } else {
                                    boolean isUnlocked  = AppMonitorService.isUnlocked(getPackageName(), 0)
                                                || AppMonitorService.isUnlocked(clone.getPackageName(), clone.getPkgUserId());

                                    MLogs.d("isUnlocked: " + isUnlocked);
                                    if (isUnlocked) {
                                        clone.setLockerState(AppConstants.AppLockState.DISABLED);
                                        DBManager.updateCloneModel(MainActivity.this, clone);
                                        CloneManager.reloadLockerSetting();
                                        changed = true;
                                    } else {
                                        pendingUnlockPos = longClickPos;
                                        AppLockActivity.start(MainActivity.this, getPackageName(), 0, getString(R.string.unlock_before_disable), false, true);
                                    }
                                }
                                if (longClickPos >= 0 && changed) {
                                    MLogs.d("update pos for long click: " + longClickPos);
                                    if (clone.getLockerState() == AppConstants.AppLockState.DISABLED) {
                                        Toast.makeText(MainActivity.this, getString(R.string.been_unlocked, title.getText()), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, getString(R.string.been_locked, title.getText()), Toast.LENGTH_SHORT).show();
                                    }
                                    listAdapter.notifyItemChanged(longClickPos);
                                    longClickPos = -1;
                                }
                            } else {
                                LockSettingsActivity.start(MainActivity.this,"home");
                            }
                            break;
                        case R.id.item_customize:
                            if (clone != null) {
                                CustomizeIconActivity.start(MainActivity.this, clone.getPackageName(),
                                        clone.getPkgUserId());
                            }
                            break;
                        case R.id.item_delete:
                            if (clone != null) {
                                if (PreferencesUtils.getDeleteDialogTimes() < RemoteConfig.getLong("conf_delete_dialog_times")) {
                                    showDeleteDialog(clone, view);
                                } else {
                                    deleteWithAnimation(clone, view);
                                }
                            }
                            break;
                        case R.id.item_shortcut:
                            if (clone != null) {
                                int result =  getPackageManager().checkPermission("com.android.launcher.permission.INSTALL_SHORTCUT",  getPackageName());
                                MLogs.d("permission result: "+result);
                                CommonUtils.createShortCut(MainActivity.this, ((CloneModel) clone));
                                view.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
                                        //CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                                    }
                                }, 200);
                            }
                            break;
                    }
                    return true;
                }
            });
            itemMenuPopup.setGravity(Gravity.END|Gravity.BOTTOM);
            itemMenuPopup.show();
//            try {
//                MenuPopupHelper menuHelper = new MenuPopupHelper(MainActivity.this, (MenuBuilder) itemMenuPopup.getMenu(), view);
////                menuHelper.setForceShowIcon(true);
//                menuHelper.show();
////            Field field = homeMenuPopup.getClass().getDeclaredField("mPopup");
////            field.setAccessible(true);
////            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(homeMenuPopup);
////            mHelper.setForceShowIcon(true);
//            } catch (Exception e) {
//                MLogs.logBug(MLogs.getStackTraceString(e));
//            }
        }
    }


    private void deleteWithAnimation(CloneModel model, View view){
        MyHolder holder = (MyHolder)view.getTag();
        if (holder != null) {
            holder.itemView.setVisibility(View.INVISIBLE);
        }
        mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
            @Override
            public void onExplodeFinish(View v) {
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CloneManager.getInstance(MainActivity.this).deleteClone(MainActivity.this, model);
                        PreferencesUtils.resetStarted(model.getName());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CloneAgent64 agent64 = new CloneAgent64(MainActivity.this);
                                if(agent64.hasSupport() && agent64.isCloned(model.getPackageName(),model.getPkgUserId())) {
                                    agent64.deleteClone(model.getPackageName(), model.getPkgUserId());
                                }
                            }
                        }).start();
                    }
                }, 500);
            }
        });

    }

    private void showDeleteDialog(CloneModel info, View explosionView) {
        UpDownDialog.show(MainActivity.this, getString(R.string.delete_dialog_title), getString(R.string.delete_dialog_content),
                getString(R.string.no_thanks), getString(R.string.yes), -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                deleteWithAnimation(info, explosionView);
                                break;
                        }
                    }
                });
        PreferencesUtils.incDeleteDialogTimes();
    }

    int convertDisplayPosAndIdx(int displayPos) {
        int ax = displayPos % (PAGE_ITEM_SIZE);
        switch (ax) {
            case 0:
            case 4:
            case 8:
                return displayPos;
            case 1:
            case 5:
                return displayPos + 2;
            case 3:
            case 7:
                return displayPos - 2;
            case 2:
                return displayPos + 4;
            case 6:
                return displayPos - 4;
        }
        return -1;
//        return displayPos;
    }

    private final static String QUIT_RATE_RANDOM = "quit_rating_random";
    private final static String QUIT_RATE_INTERVAL = "quit_rating_interval";
    private final static String QUIT_RATE_CLONED_APP_GATE = "quit_rating_cloned_app_gate";

    @Override
    public void onBackPressed() {
        boolean showRate = false;
        if (! PreferencesUtils.isRated()) {
            MLogs.d("Quit Rate config:" +  RemoteConfig.getLong(QUIT_RATE_INTERVAL)+" , "
                    + RemoteConfig.getLong(QUIT_RATE_RANDOM) + " , gate " +RemoteConfig.getLong(QUIT_RATE_CLONED_APP_GATE));
            long interval = RemoteConfig.getLong(QUIT_RATE_INTERVAL) * 60 * 60 * 1000;
            long lastTime = PreferencesUtils.getRateDialogTime(this);
            if (PreferencesUtils.getLoveApp() != -1) {
                //Don't love app
                int random = new Random().nextInt(100);
                int clonedCnt = mItemList == null? 0 : mItemList.size();
                boolean isShowRateDialog = PreferencesUtils.getLoveApp() == 1 ||
                        ((random < RemoteConfig.getLong(QUIT_RATE_RANDOM)) && clonedCnt >= RemoteConfig.getLong(QUIT_RATE_CLONED_APP_GATE));
                if (isShowRateDialog && (System.currentTimeMillis() - lastTime) > interval) {
                    showRate = true;
                    showRateDialog(RATE_FROM_QUIT, null);
                }
            }
        }
        if (!showRate) {
            super.onBackPressed();
        }

    }

    public void onSettingsClick(View view) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    private boolean needUpdate() {
        try {
            PackageInfo vinfo = getPackageManager().getPackageInfo(getPackageName(),0);
            int versionCode = vinfo.versionCode;
            long pushVersion = RemoteConfig.getLong(AppConstants.CONF_UPDATE_VERSION);
            long latestVersion = RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION);
            long ignoreVersion = PreferencesUtils.getIgnoreVersion();
            MLogs.d("local: " + versionCode + " push: " + pushVersion + " latest: " + latestVersion + " ignore: "+ ignoreVersion);
            if (versionCode <= pushVersion
                    && ignoreVersion < latestVersion) {
                return true;
            }
        }catch (Exception e) {
            MLogs.e(e);
        }
        return false;
    }

    private void showUpdateDialog() {
        EventReporter.generalEvent("update_dialog");
        UpDownDialog.show(this, getString(R.string.update_dialog_title),
                getString(R.string.update_dialog_content, "" + RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION)),
                getString(R.string.update_dialog_left), getString(R.string.update_dialog_right),
                -1, R.layout.dialog_up_down,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                dialogInterface.dismiss();
                                PreferencesUtils.ignoreVersion(RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION));
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                dialogInterface.dismiss();
                                String forceUpdateUrl = RemoteConfig.getString("force_update_to");
                                if (!TextUtils.isEmpty(forceUpdateUrl)) {
                                    CommonUtils.jumpToUrl(MainActivity.this,forceUpdateUrl);
                                } else {
                                    CommonUtils.jumpToMarket(MainActivity.this, getPackageName());
                                }
                                EventReporter.generalEvent("update_go");
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                PreferencesUtils.ignoreVersion(RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION));
            }
        });
    }

    private void loadAd() {
        loadEmbedNative();
        if (RemoteConfig.getBoolean(AppLoadingActivity.CONFIG_NEED_PRELOAD_LOADING) && !PreferencesUtils.isAdFree()) {
            AppLoadingActivity.preloadAd(this);
        }
    }

    public static AdSize getBannerAdSize() {
        int dpWidth = DisplayUtils.px2dip(DualApp.getApp(), DisplayUtils.getScreenWidth(DualApp.getApp()));
        return new AdSize(dpWidth, 320);
    }
    private void inflateNativeAdView(IAdAdapter ad) {
        if (ad == null) {
            return;
        }
        final AdViewBinder viewBinder;
        switch (ad.getAdType()) {
            default:
                viewBinder =  new AdViewBinder.Builder(R.layout.home_native_ad_default)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .fbMediaId(R.id.ad_fb_mediaview)
                        .admMediaId(R.id.ad_adm_mediaview)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_container)
                        .adFlagId(R.id.ad_flag)
                        .build();
                break;
        }

        View adView = ad.getAdView(this, viewBinder);
        if (adView != null) {
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adView);
            nativeAdContainer.setVisibility(View.VISIBLE);
            adShowTime = System.currentTimeMillis();
        }
    }
    private void loadEmbedNative() {
        if (adLoader == null) {
            adLoader = FuseAdLoader.get(SLOT_HOME_NATIVE, MainActivity.this);
        }
        if (adLoader.hasValidAdSource()) {
            adLoader.setBannerAdSize(getBannerAdSize());
            adLoader.loadAd(this, 2, 2000, new IAdLoadListener() {
                @Override
                public void onRewarded(IAdAdapter ad) {

                }

                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    inflateNativeAdView(ad);
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {

                }

                @Override
                public void onError(String error) {
                    MLogs.e(SLOT_HOME_NATIVE + " load error: " + error);
                }
            });
        }
    }

    private static final String CONFIG_QUICK_SWITCH_INTERVAL = "guide_quick_switch_interval_s";
    private static final String CONFIG_QUICK_SWITCH_TIMES = "guide_quick_switch_times";
    private boolean guideQuickSwitchIfNeeded() {
        if (startingPkg == null) {
            MLogs.d("No starting package");
            return false;
        }
        if (QuickSwitchNotification.isEnable()) {
            MLogs.d("Already enabled quick switch");
            return false;
        }
        long allowCnt = RemoteConfig.getLong(CONFIG_QUICK_SWITCH_TIMES);
        int times = PreferencesUtils.getGuideQuickSwitchTimes();
        if (times >= allowCnt) {
            MLogs.d("Guide quick switch hit cnt");
            return false;
        }
        if( System.currentTimeMillis() - PreferencesUtils.getLastGuideQuickSwitchTime()
                < times*1000*RemoteConfig.getLong(CONFIG_QUICK_SWITCH_INTERVAL)) {
            MLogs.d("not guide quick switch too frequent");
            return false;
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    PreferencesUtils.updateLastGuideQuickSwitchTime();
                    PreferencesUtils.setGuideQuickSwitchTimes(times + 1);
                    showQuickSwitchDialog( );
                }
            }, 800);

        }
        return true;
    }

    private void showQuickSwitchDialog() {
        EventReporter.generalEvent("quick_switch_dialog");
        MLogs.d("showQuickSwitchDialog");
        UpDownDialog.show(this, this.getResources().getString(R.string.quick_switch_title),
                this.getResources().getString(R.string.quick_switch_dialog_content),
                this.getResources().getString(R.string.no_thanks), this.getResources().getString(R.string.ok),
                R.drawable.dialog_tag_congratulations, R.layout.dialog_up_down,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                dialogInterface.dismiss();
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                dialogInterface.dismiss();
                                QuickSwitchNotification.enable();
                                EventReporter.generalEvent("quick_switch_dialog_go");
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
    }

}
