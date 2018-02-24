package com.polestar.domultiple.components.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;


import com.lody.virtual.os.VUserHandle;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.CustomizeAppData;
import com.polestar.domultiple.utils.BitmapUtils;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;

import java.io.File;
import java.util.List;

/**
 * Created by PolestarApp on 2017/7/29.
 */

public class CustomizeIconActivity extends Activity implements SeekBar.OnSeekBarChangeListener{

    private SeekBar seekBarHue;
    private SeekBar seekBarSat;
    private SeekBar seekBarLight;
    private EditText labelText;
    private CheckBox badgeCheckBox;
    private String pkg;
    private CustomizeAppData mData;
    private CloneModel appModel;
    private Drawable defaultIcon;
    private Bitmap customIcon;
    private ImageView iconImg;
    private int userId;
    private static int MID_VALUE = 127;

    public static void start(Activity activity, String pkg, int userId) {
        Intent intent = new Intent(activity, CustomizeIconActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, pkg);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, userId);

        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_dialog_layout);
        EventReporter.generalClickEvent("customize_icon_enter");
        seekBarHue = (SeekBar) findViewById(R.id.seek_bar_hue);
        seekBarSat = (SeekBar) findViewById(R.id.seek_bar_sat);
        seekBarLight = (SeekBar) findViewById(R.id.seek_bar_light);
        seekBarHue.setOnSeekBarChangeListener(this);
        seekBarLight.setOnSeekBarChangeListener(this);
        seekBarSat.setOnSeekBarChangeListener(this);
        labelText = (EditText) findViewById(R.id.app_label);
        badgeCheckBox = (CheckBox) findViewById(R.id.badge_checkbox);
        badgeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mData.badge = b;
                updateView();
            }
        });
        iconImg = (ImageView) findViewById(R.id.app_icon);
        pkg = getIntent().getStringExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME);
        userId = getIntent().getIntExtra(AppConstants.EXTRA_CLONED_APP_USERID, VUserHandle.myUserId());
        if (pkg != null) {
            appModel = CloneManager.getInstance(this).getCloneModel(pkg, userId);
        }
        if (appModel == null) {
            finish();
            return;
        }
        try {
            defaultIcon = getPackageManager().getApplicationIcon(pkg);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }
        initData();
        labelText.setText(mData.label);
        labelText.setSelection(Math.min(mData.label.length(),36));
        updateView();
    }

    private void updateView() {
        seekBarLight.setProgress(mData.light );
        seekBarSat.setProgress(mData.sat );
        seekBarHue.setProgress(mData.hue );
        badgeCheckBox.setChecked(mData.badge);
        updateIcon();
    }

    private void updateIcon() {
        float hue = (mData.hue - MID_VALUE) * 1.0F / MID_VALUE * 180;
        float sat = mData.sat * 1.0F / MID_VALUE ;
        float lum = mData.light * 1.0F / MID_VALUE ;
        MLogs.d("hue: " + hue  + " sat: " + sat + " lum: " + lum);
        customIcon = BitmapUtils.handleImageEffect(BitmapUtils.drawableToBitmap(defaultIcon), hue, sat,lum );
        if (mData.badge) {
            customIcon = BitmapUtils.createBadgeIcon(this, new BitmapDrawable(customIcon), appModel.getPkgUserId());
        }
        iconImg.setImageBitmap(customIcon);

    }

    public void doReset() {
        mData.badge = true;
        mData.hue = MID_VALUE;
        mData.sat = MID_VALUE;
        mData.light = MID_VALUE;
        mData.label = String.format(getString(R.string.clone_label_tag),  appModel.getName());
        updateView();
    }

    public void onReset(View view){
        doReset();
    }

    public void onConfirm(View view) {
        saveData();
    }

    public void onCancel(View view) {
        finish();
    }

    public void initData() {
        mData = CustomizeAppData.loadFromPref(pkg, userId);
        if (mData.label == null) {
            mData.label = String.format(getString(R.string.clone_label_tag),  appModel.getName());
        }
    }

    public void saveData() {
        String text =  labelText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        EventReporter.generalClickEvent("customize_icon_save");
        mData.label = text;
        mData.saveToPref();
        try{
            File dir = new File(getFilesDir() + AppConstants.ICON_FILE_PATH );
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String pathname = BitmapUtils.getCustomIconPath(this, pkg, userId);
            BitmapUtils.saveBitmapToPNG(customIcon, pathname);
        } catch (Exception e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        if (appModel.getLockerState() != AppConstants.AppLockState.DISABLED) {
            CloneManager.reloadLockerSetting();
        }
        finish();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        switch (seekBar.getId()) {
            case R.id.seek_bar_hue:
                mData.hue = progress;
                break;
            case R.id.seek_bar_light:
                mData.light = progress;
                break;
            case R.id.seek_bar_sat:
                mData.sat = progress;
                break;
        }
        updateView();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
