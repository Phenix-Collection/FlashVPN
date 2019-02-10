package com.polestar.minesweeperclassic.activity;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.minesweeperclassic.BuildConfig;
import com.polestar.minesweeperclassic.R;
import com.polestar.minesweeperclassic.utils.CommonUtils;
import com.polestar.minesweeperclassic.utils.DisplayUtils;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;
import com.polestar.minesweeperclassic.utils.PreferenceUtils;
import com.polestar.minesweeperclassic.widget.RateDialog;

import java.util.List;

public class SettingsActivity extends Activity {
    private TextView titleTv;
    private TextView difficultyTv;
    public static final int EASY = 15;
    public static final int NORMAL = 25;
    public static final int HARD = 35;
    private SeekBar seekBar;
    private TextView versionTv;
    private TableLayout cellTable;

    private int minCellSize = 50;
    private int maxCellSize;
    private ProgressBar loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        int screenWidth = DisplayUtils.getScreenWidth(this);
        maxCellSize = screenWidth/8;
        minCellSize = DisplayUtils.dip2px(this, 20);
        initView();
    }

    public void onDifficultyClick(View view) {
        boolean[] checkedArr = new boolean[] {false, false, false};
        switch (PreferenceUtils.getDifficulty()) {
            case EASY:
                checkedArr[0] = true;
                break;
            case NORMAL:
                checkedArr[1] = true;
                break;
            case HARD:
                checkedArr[2] = true;
                break;
        }
        new AlertDialog.Builder(this).setTitle(R.string.settings_difficulty).setMultiChoiceItems(
                R.array.arr_difficulty, checkedArr, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                updateDifficulty(EASY);
                                break;
                            case 1:
                                updateDifficulty(NORMAL);
                                break;
                            case 2:
                                updateDifficulty(HARD);
                                break;
                        }

                    }
                }).create().show();
    }

    public void onNavigationClick(View view) {
        finish();
    }

    private void initView() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.settings_activity_layout,null);
        setContentView(addContentView(contentView));
        titleTv.setText(R.string.settings_label);
        difficultyTv = (TextView) findViewById(R.id.difficulty_detail);
        versionTv = (TextView) findViewById(R.id.version_info);
        updateDifficulty(PreferenceUtils.getDifficulty());

        versionTv.setText(getString(R.string.settings_right) + "\n" + "Version: " + BuildConfig.VERSION_NAME);
        cellTable = (TableLayout)findViewById(R.id.mine_table);
        loadingProgress = findViewById(R.id.reward_loading_progress);
        loadingProgress.setVisibility(View.GONE);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (maxCellSize <= minCellSize) {
                    MLogs.logBug("Wrong maxCellSize : " + maxCellSize);
                    return;
                }
                int size = minCellSize + (maxCellSize - minCellSize) * progress/100;
                PreferenceUtils.setCellSize(size);
                updateCell(size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int size = PreferenceUtils.getCellSize();
        int progress = (size - minCellSize)*100/(maxCellSize - minCellSize);
        seekBar.setProgress(progress);
    }

    private void updateCell(int size) {
        for (int i = 0; i < 3; i ++ ) {
            TableRow tr = (TableRow) cellTable.getChildAt(i);
            for (int j= 0 ;j < 3; j++) {
                Button b = (Button) tr.getChildAt(j);
                b.setLayoutParams(new TableRow.LayoutParams(size, size));
            }
        }

    }
    private void updateDifficulty(int level) {
        switch (level) {
            case EASY:
                PreferenceUtils.setDifficulty(level);
                difficultyTv.setText(R.string.difficulty_easy);
                break;
            case NORMAL:
                PreferenceUtils.setDifficulty(level);
                difficultyTv.setText(R.string.difficulty_normal);
                break;
            case HARD:
                difficultyTv.setText(R.string.difficulty_hard);
                PreferenceUtils.setDifficulty(level);
                break;
        }
    }

    private View addContentView(View contentView){
        View customLayout = LayoutInflater.from(this).inflate(R.layout.common_layout,null);
        FrameLayout contentLayout = (FrameLayout) customLayout.findViewById(R.id.content_home);
        titleTv = (TextView) customLayout.findViewById(R.id.title);
        contentLayout.addView(contentView);
        return customLayout;
    }

    public void onRateUsClick(View view) {
        new RateDialog(this, RateDialog.FROM_SETTINGS).show();
    }

    public void onShareClick(View view) {
        shareWithFriends(this);
    }

    public void shareWithFriends(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String appName = context.getResources().getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
        String shareContent = "Come to challenge me at: ";
        shareContent = shareContent + "https://play.google.com/store/apps/details?id="
                + context.getPackageName() +  "&referrer=utm_source%3Duser_share";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        context.startActivity(Intent.createChooser(shareIntent, getString(R.string.share_tips)));
    }



    public void onCollectScanner(View view) {
        FuseAdLoader adLoader = FuseAdLoader.get(GameActivity.SLOT_SCANNER_REWARD_VIDEO, this);
        if (!adLoader.hasValidCache()) {
            loadingProgress.setVisibility(View.VISIBLE);
        }
        adLoader.loadAd(this, 2, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {
                ad.show();
                loadingProgress.setVisibility(View.GONE);
            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FuseAdLoader.get(GameActivity.SLOT_SCANNER_REWARD_VIDEO, SettingsActivity.this).preloadAd(SettingsActivity.this);
                    }
                });
            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(SettingsActivity.this, R.string.toast_no_reward_content, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewarded(IAdAdapter ad) {
                EventReporter.reportReward("settings_scanner_"+ad.getAdType());
                Toast.makeText(SettingsActivity.this, R.string.toast_get_scanner, Toast.LENGTH_SHORT).show();
                PreferenceUtils.incScannerNumber(SettingsActivity.this);
            }
        });
    }
}
