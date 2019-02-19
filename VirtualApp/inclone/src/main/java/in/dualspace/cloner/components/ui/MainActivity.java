package in.dualspace.cloner.components.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.clone.CloneAgent64;
import com.polestar.clone.CustomizeAppData;

import java.util.ArrayList;
import java.util.List;

import in.dualspace.cloner.R;
import in.dualspace.cloner.clone.CloneManager;
import in.dualspace.cloner.db.CloneModel;
import in.dualspace.cloner.db.DBManager;
import in.dualspace.cloner.notification.QuickSwitchNotification;
import in.dualspace.cloner.utils.CommonUtils;
import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;
import in.dualspace.cloner.widget.ExplosionField;
import in.dualspace.cloner.widget.PageIndicatorView;
import in.dualspace.cloner.widget.PageRecyclerView;
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
    private static final int REQUEST_UNLOCK_SETTINGS = 102;

    private int luckyPos;
    private int adTaskPos;

    private PopupMenu itemMenuPopup;
    private ExplosionField mExplosionField;
    private static final int PAGE_ITEM_SIZE = 3*3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_layout);
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
                    item.fillIconView(myHolder.icon);
                    myHolder.icon.setTag(myHolder);
                    item.fillTitleView(myHolder.title);
                    item.fillNewDotView(myHolder.newdot);
                    myHolder.icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.onClick(v);
                        }
                    });
                    myHolder.icon.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            item.onLongClick(v);
                            return true;
                        }
                    });
                }

            }

        }));
        initData();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initData() {
        cm = CloneManager.getInstance(this);
        cm.loadClonedApps(this, this);
        initItemList(null);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public TextView title = null;
        public ImageView icon = null;
        public ImageView newdot = null;

        public MyHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.app_name);
            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            newdot = (ImageView) itemView.findViewById(R.id.new_dot);

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
        PreferencesUtils.setShownPermissionGuide(true);
        UpDownDialog.show(this, getString(R.string.dialog_permission_title),
                getString(R.string.dialog_permission_content), null, getString(R.string.ok),
                R.drawable.dialog_tag_comment, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventReporter.generalEvent("ok_permission_guide");
                        requestPermissions(perms, REQUEST_APPLY_PERMISSION);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                EventReporter.generalEvent("cancel_permission_guide");
                requestPermissions(perms, REQUEST_APPLY_PERMISSION);
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
    }

    private void addCloneItem(CloneModel model) {
        int idx = -1;
        int i = 0;
        for(CustomizedCloneItem item: mItemList) {
            if (item.type == CustomizedCloneItem.TYPE_CLONE) {
                idx = i;
            }
            i ++;
        }
        mItemList.add(idx + 1, new CustomizedCloneItem(model));
        listAdapter.notifyDataSetChanged();
        listAdapter.updatePage();
        listAdapter.scrollToPage((idx + 1)/(PAGE_ITEM_SIZE));
    }

    private void deleteCloneItem(CloneModel model) {
        int idx = -1;
        int i = 0;
        for(CustomizedCloneItem item: mItemList) {
            if (item.type == CustomizedCloneItem.TYPE_CLONE) {
                if (item.model.getPackageName().equals(model.getPackageName())
                        && item.model.getPkgUserId() == model.getPkgUserId()) {
                    idx = i;
                    break;
                }
            }
            i ++;
        }
        if (idx != -1) {
            mItemList.remove(idx);
            listAdapter.notifyDataSetChanged();
            listAdapter.updatePage();
        }

    }
    private void initItemList(List<CloneModel> cloneModels) {
        mItemList.clear();
        if (cloneModels != null) {
            for (CloneModel model: cloneModels) {
                mItemList.add(new CustomizedCloneItem(model));
            }
        }
        mItemList.add(new CustomizedCloneItem(CustomizedCloneItem.TYPE_LUCKY));
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

        public CloneModel model = null;
        private CustomizeAppData customizeAppData = null;

        public CustomizedCloneItem(CloneModel model) {
            type = TYPE_CLONE;
            this.model = model;
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
            switch (type) {
                case TYPE_CLONE:
                    AppLoadingActivity.startAppStartActivity(MainActivity.this, model);
                    startingPkg = model.getPackageName();
                    if (model.getLaunched() == 0) {
                        model.setLaunched(1);
                        DBManager.updateCloneModel(MainActivity.this, model);
                    }
                    break;
                case TYPE_ADD:
                    startActivity(new Intent(MainActivity.this, AddCloneActivity.class));
                    break;
                case TYPE_LUCKY:
                    Intent intent = new Intent(MainActivity.this, NativeInterstitialActivity.class);
                    startActivity(intent);
                    break;
            }
        }

        public void onLongClick(View view) {
            MLogs.d("onLongClick");
            switch (type) {
                case TYPE_EMPTY:
                    break;
                case TYPE_ADD:
                    break;
                default:
                    showItemMenu(view);
                    break;
            }

        }

        public void fillTitleView(TextView textView) {
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            textView.setTextColor(getResources().getColor(R.color.white));
            switch (type) {
                case TYPE_CLONE:
                    String title = customizeAppData.customized? customizeAppData.label: model.getName();
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

        public void fillIconView(ImageView icon) {
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

        public void fillNewDotView(ImageView newdot) {
            switch (type){
                case TYPE_CLONE:
                    if (model.getLaunched() == 0) {
                        newdot.setImageResource(R.drawable.shape_new_dot);
                        newdot.setVisibility(View.VISIBLE);
                    } else {
                        newdot.setVisibility(View.INVISIBLE);
                    }
                    break;
                default:
                    newdot.setVisibility(View.INVISIBLE);
                    break;
            }
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
                    itemMenuPopup.getMenu().removeItem(R.id.item_locker);
                    itemMenuPopup.getMenu().removeItem(R.id.item_shortcut);
                    break;
            }
            //菜单项的监听
            itemMenuPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
//                    case R.id.item_notification:
//                        startActivity(new Intent(MainActivity.this, NotificationActivity.class));
//                        break;
//                    case R.id.item_faq:
//                        startActivity(new Intent(MainActivity.this, FaqActivity.class));
//                        break;
                        case R.id.item_setting:
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            break;
                        case R.id.item_locker:
                            if (PreferencesUtils.isLockerEnabled(MainActivity.this) ) {
                                LockPasswordSettingActivity.start(MainActivity.this, false, getString(R.string.lock_settings_title), REQUEST_UNLOCK_SETTINGS);
                            } else {
                                LockSettingsActivity.start(MainActivity.this,"home");
                            }
                            break;
                        case R.id.item_customize:
                            if (model != null) {
                                CustomizeIconActivity.start(MainActivity.this, model.getPackageName(),
                                        model.getPkgUserId());
                            }
                            break;
                        case R.id.item_delete:
                            if (model != null) {
                                if (PreferencesUtils.getDeleteDialogTimes() < RemoteConfig.getLong("conf_delete_dialog_times")) {
                                    showDeleteDialog(model, view);
                                } else {
                                    deleteWithAnimation(model, view);
                                }
                            }
                            break;
                        case R.id.item_shortcut:
                            if (model != null) {
                                int result =  getPackageManager().checkPermission("com.android.launcher.permission.INSTALL_SHORTCUT",  getPackageName());
                                MLogs.d("permission result: "+result);
                                CommonUtils.createShortCut(MainActivity.this, ((CloneModel) model));
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
            }
        });
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                PreferencesUtils.resetStarted(model.getName());
                CloneManager.getInstance(MainActivity.this).deleteClone(MainActivity.this, model);
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
        }, 1000);
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
}
