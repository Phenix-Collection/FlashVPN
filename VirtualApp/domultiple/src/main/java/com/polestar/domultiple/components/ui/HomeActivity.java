package com.polestar.domultiple.components.ui;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.support.v7.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.utils.AnimatorHelper;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.widget.DropableLinearLayout;
import com.polestar.domultiple.widget.ExplosionField;
import com.polestar.domultiple.widget.HomeGridAdapter;
import com.polestar.domultiple.widget.NarrowPromotionCard;
import com.polestar.domultiple.widget.UpDownDialog;
import com.polestar.domultiple.widget.dragdrop.DragController;
import com.polestar.domultiple.widget.dragdrop.DragImageView;
import com.polestar.domultiple.widget.dragdrop.DragLayer;
import com.polestar.domultiple.widget.dragdrop.DragSource;

import java.io.File;
import java.util.List;

/**
 * Created by guojia on 2017/7/15.
 */

public class HomeActivity extends BaseActivity implements CloneManager.OnClonedAppChangListener, DragController.DragListener{
    private List<CloneModel> mClonedList;
    private CloneManager cm;
    private GridView cloneGridView;
    private HomeGridAdapter gridAdapter;
    boolean showLucky;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private FrameLayout mTitleBar;
    private LinearLayout mActionBar;
    private DropableLinearLayout createShortcutArea;
    private DropableLinearLayout deleteArea;
    private LinearLayout createDropButton;
    private LinearLayout deleteDropButton;
    private TextView deleteDropTxt;
    private TextView createDropTxt;
    private ExplosionField mExplosionField;
    private PopupMenu homeMenuPopup;
    private NarrowPromotionCard functionCard;
    private View mProgressBar;
    private static final int REQUEST_UNLOCK_SETTINGS = 100;

    private boolean rateDialogShowed = false;
    private static final String RATE_FROM_QUIT = "quit";
    private static final String RATE_AFTER_CLONE = "clone";
    private static final String RATE_FROM_MENU = "menu";

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        EventReporter.homeShow();
    }

    private void initData() {
        cm = CloneManager.getInstance(this);
        cm.loadClonedApps(this, this);
        showLucky = PreferencesUtils.hasCloned() && !PreferencesUtils.isAdFree();
        gridAdapter.setShowLucky(showLucky );
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (CloneManager.getInstance(this).hasPendingClones()) {
            MLogs.d("Has pending clones");
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                }
            }, 60*1000);
        }
    }

    private void initView() {
        setContentView(R.layout.home_activity_layout);
        cloneGridView = (GridView) findViewById(R.id.clone_grid_view);
        mProgressBar = findViewById(R.id.progressBar);
        gridAdapter = new HomeGridAdapter(this);
        cloneGridView.setAdapter(gridAdapter);
        cloneGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int luckyIdx = mClonedList.size();
                int addIdx = showLucky? luckyIdx + 1 : luckyIdx;
                if (i < mClonedList.size()) {
                    CloneModel model = (CloneModel)gridAdapter.getItem(i);
                    AppLoadingActivity.startAppStartActivity(HomeActivity.this, model);
                    if (model.getLaunched() == 0) {
                        model.setLaunched(1);
                        DBManager.updateCloneModel(HomeActivity.this, model);
                        gridAdapter.notifyDataSetChanged();
                    }
                } else if (showLucky && i == luckyIdx) {
                    MLogs.d("lucky clicked");
                } else if (i == addIdx) {
                    MLogs.d("to add more clone");
                    startActivity(new Intent(HomeActivity.this, AddCloneActivity.class));
                }
            }
        });
        cloneGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= mClonedList.size()) {
                    return false;
                }
                DragImageView iv = (DragImageView) view.findViewById(R.id.app_icon);
                mDragController.startDrag(iv, iv, gridAdapter.getItem(i), DragController.DRAG_ACTION_COPY);
                return true;
            }
        });

        mActionBar = (LinearLayout) findViewById(R.id.action_bar) ;
        mTitleBar = (FrameLayout) findViewById(R.id.title_bar);

        mDragLayer = (DragLayer)findViewById(R.id.drag_layer);
        mDragController = new DragController(this);
        mDragController.setWindowToken(mDragLayer.getWindowToken());
        mDragController.setDragListener(this);
        mDragLayer.setDragController(mDragController);

        createShortcutArea = (DropableLinearLayout) findViewById(R.id.create_shortcut_area);
        createDropButton = (LinearLayout) findViewById(R.id.shortcut_drop_button);
        deleteDropButton = (LinearLayout)findViewById(R.id.delete_drop_button);
        deleteDropTxt = (TextView)findViewById(R.id.delete_drop_text);
        createDropTxt = (TextView)findViewById(R.id.shortcut_drop_text);
        createShortcutArea.setOnEnterListener(new DropableLinearLayout.IDragListener() {
            @Override
            public void onEnter() {
                createDropButton.setBackgroundResource(R.drawable.shape_create_shortcut);
                createDropTxt.setTextColor(getResources().getColor(R.color.shortcut_text_color));
            }

            @Override
            public void onExit() {
                createDropButton.setBackgroundColor(0);
                createDropTxt.setTextColor(getResources().getColor(R.color.white));
            }
        });
        deleteArea = (DropableLinearLayout) findViewById(R.id.delete_app_area);
        deleteArea.setOnEnterListener(new DropableLinearLayout.IDragListener() {
            @Override
            public void onEnter() {
                deleteDropButton.setBackgroundResource(R.drawable.shape_delete);
                deleteDropTxt.setTextColor(getResources().getColor(R.color.delete_text_color));
            }

            @Override
            public void onExit() {
                deleteDropButton.setBackgroundColor(0);
                deleteDropTxt.setTextColor(getResources().getColor(R.color.white));
            }
        });

        mExplosionField = ExplosionField.attachToWindow(this);
        functionCard = (NarrowPromotionCard) findViewById(R.id.narrow_function_card);
        functionCard.init(R.drawable.icon_locker_small, R.string.privacy_locker, new Intent(this, LockSettingsActivity.class));
    }

    @Override
    public void onInstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        if (result && PreferencesUtils.getBoolean(this, AppConstants.KEY_AUTO_CREATE_SHORTCUT, false)) {
            CommonUtils.createShortCut(this, clonedApp);
        }
        if (!CloneManager.getInstance(this).hasPendingClones()) {
            mProgressBar.setVisibility(View.GONE);
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onUnstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        if (result) {
            CommonUtils.removeShortCut(this, clonedApp);
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onLoaded(List<CloneModel> clonedApp) {
        mClonedList = cm.getClonedApps();
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    public static boolean isDebugMode(){
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "polestarunlocktest");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            MLogs.e(e);
        }
        return false;
    }
    public void onMenuClick(View view) {
        View more = findViewById(R.id.menu_more);
        if (homeMenuPopup == null) {
            homeMenuPopup = new PopupMenu(this, more);
            homeMenuPopup.inflate(R.menu.home_menu_popup);
        }
        //菜单项的监听
        homeMenuPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_notification:
                        startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
                        break;
                    case R.id.item_faq:
                        startActivity(new Intent(HomeActivity.this, FaqActivity.class));
                        break;
                    case R.id.item_setting:
                        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                        break;
                    case R.id.item_locker:
                        if (PreferencesUtils.isLockerEnabled(HomeActivity.this) || isDebugMode()) {
                            LockPasswordSettingActivity.start(HomeActivity.this, false, getString(R.string.lock_settings_title), REQUEST_UNLOCK_SETTINGS);
                        } else {
                            LockSettingsActivity.start(HomeActivity.this,"home");
                        }
                        break;
                    case R.id.item_rate:
                        showRateDialog("menu", "");
                        break;
                    case R.id.item_feedback:
                        Intent feedback = new Intent(HomeActivity.this, FeedbackActivity.class);
                        startActivity(feedback);
                        break;
                    case R.id.item_share:
                        CommonUtils.shareWithFriends(HomeActivity.this);
                        break;
                }
                return true;
            }
        });
        try {
            MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) homeMenuPopup.getMenu(), more);
            menuHelper.setForceShowIcon(true);
            menuHelper.show();
//            Field field = homeMenuPopup.getClass().getDeclaredField("mPopup");
//            field.setAccessible(true);
//            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(homeMenuPopup);
//            mHelper.setForceShowIcon(true);
        } catch (Exception e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        //homeMenuPopup.show();
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        mTitleBar.setVisibility(View.INVISIBLE);
        mActionBar.setVisibility(View.VISIBLE);
        AnimatorHelper.verticalShowFromBottom(mActionBar);
        mDragController.addDropTarget(createShortcutArea);
        createShortcutArea.clearState();
        createDropButton.setBackgroundColor(0);
        createDropTxt.setTextColor(getResources().getColor(R.color.white));
        deleteDropButton.setBackgroundColor(0);
        deleteDropTxt.setTextColor(getResources().getColor(R.color.white));
        mDragController.addDropTarget(deleteArea);
        deleteArea.clearState();
    }

    @Override
    public void onDragEnd(DragSource source, Object info, int dragAction) {
        mTitleBar.setVisibility(View.VISIBLE);
        mActionBar.setVisibility(View.INVISIBLE);

        if (createShortcutArea.isSelected()) {
            CommonUtils.createShortCut(this,((CloneModel) info));
            mActionBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                        Toast.makeText(HomeActivity.this, R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
                    //CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                }
            },500);
        } else if (deleteArea.isSelected()) {
            mActionBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MLogs.d("Delete clone model!");
                    showDeleteDialog((CloneModel) info);
                }
            },500);
        }
        AnimatorHelper.hideToBottom(mActionBar);
        mActionBar.setVisibility(View.GONE);
        AnimatorHelper.verticalShowFromBottom(mTitleBar);
        mTitleBar.setVisibility(View.VISIBLE);
        mDragController.removeDropTarget(createShortcutArea);
        mDragController.removeDropTarget(deleteArea);
    }

    private int getPosForModel(final CloneModel model) {
        int i = 0;
        for (CloneModel c : mClonedList) {
            if (c.getPackageName().equals(model.getPackageName())) {
                return i;
            }
            i ++;
        }
        return  -1;
    }

    private void showDeleteDialog(CloneModel info) {
        UpDownDialog.show(HomeActivity.this, getString(R.string.delete_dialog_title), getString(R.string.delete_dialog_content),
                getString(R.string.no_thanks), getString(R.string.yes), -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                int pos = getPosForModel(info);
                                if (pos == -1) {
                                    MLogs.logBug("Unkown package");
                                }
                                View view = cloneGridView.getChildAt(pos);
                                if(view != null) {
                                    mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                                        @Override
                                        public void onExplodeFinish(View v) {
                                        }
                                    });
                                }
                                view.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        CloneManager.getInstance(HomeActivity.this).deleteClone(HomeActivity.this, info.getPackageName());
                                    }
                                }, 1000);
                                break;
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UNLOCK_SETTINGS) {
            switch (resultCode) {
                case RESULT_OK:
                    LockSettingsActivity.start(this, "home");
                    break;
                case RESULT_CANCELED:
                    break;
            }
            return;
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
        String title = RATE_AFTER_CLONE.equals(from) ? getString(R.string.congratulations) : getString(R.string.rate_us);
        UpDownDialog.show(this, title,
                getString(R.string.dialog_rating_us_content), getString(R.string.not_really),
                getString(R.string.yes), R.drawable.dialog_tag_congratulations,
                R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                PreferencesUtils.setLoveApp(false);
                                UpDownDialog.show(HomeActivity.this, getString(R.string.feedback),
                                        getString(R.string.dialog_feedback_content),
                                        getString(R.string.no_thanks),
                                        getString(R.string.ok), R.drawable.dialog_tag_comment,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        Intent feedback = new Intent(HomeActivity.this, FeedbackActivity.class);
                                                        startActivity(feedback);
                                                        EventReporter.reportRate("not_love_go_fb", from);
                                                        break;
                                                    case UpDownDialog.NEGATIVE_BUTTON:
                                                        EventReporter.reportRate("not_love_not_fb", from);
                                                        break;
                                                }
                                            }
                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        EventReporter.reportRate("not_love_cancel_fb", from);
                                    }
                                });
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                PreferencesUtils.setLoveApp(true);
                                UpDownDialog.show(HomeActivity.this, getString(R.string.dialog_love_title),
                                        getString(R.string.dialog_love_content),
                                        getString(R.string.remind_me_later),
                                        getString(R.string.star_rating), R.drawable.dialog_tag_love,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        PreferencesUtils.setRated(true);
                                                        CommonUtils.jumpToMarket(HomeActivity.this, getPackageName());
                                                        EventReporter.reportRate("love_rate", from);
                                                        break;
                                                    case UpDownDialog.NEGATIVE_BUTTON:
                                                        EventReporter.reportRate("love_not_rate", from);
                                                        break;
                                                }
                                            }
                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        EventReporter.reportRate("love_cancel_rate", from);
                                    }
                                });
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                EventReporter.reportRate("cancel_rate", from);
            }
        });

    }
}
