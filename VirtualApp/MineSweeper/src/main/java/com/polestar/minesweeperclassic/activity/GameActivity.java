package com.polestar.minesweeperclassic.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.minesweeperclassic.MApp;
import com.polestar.minesweeperclassic.R;
import com.polestar.minesweeperclassic.Service.DaemonService;
import com.polestar.minesweeperclassic.utils.DisplayUtils;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;
import com.polestar.minesweeperclassic.utils.PreferenceUtils;
import com.polestar.minesweeperclassic.utils.RemoteConfig;
import com.polestar.minesweeperclassic.widget.CustomDialog;
import com.polestar.minesweeperclassic.widget.MineCell;
import com.polestar.minesweeperclassic.widget.RateDialog;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


/**
 * Created by doriscoco on 2017/4/3.
 */

public class GameActivity extends Activity{
    private boolean isFlagMode = false;
    private Button flagMineButton ;
    private TableLayout mineTableLayout;
    private int lineOfMine;
    private int cellSize;
    private int difficulty;
    private int rowOfMine;
    private MineCell[][] mineGrids;
    private int numOfMine;
    private int leftFlags;

    private ImageView leftFlagNum3;
    private ImageView leftFlagNum2;
    private ImageView leftFlagNum1;
    private int clicked;

    private long startTime;
    private ImageView timeNum4;
    private ImageView timeNum3;
    private ImageView timeNum2;
    private ImageView timeNum1;
    private Handler mHandler;
    private static final int TIME_COUNT_MSG = 0;
    private static final int REQUEST_SHOW_REPORT = 0;

    private int steps;

    private Button resetButton;

    private final static String SLOT_GAME_INTERSTITIAL = "slot_game";
    private final static String AD_INTERVAL = "ad_interval";
    private static int mResetTimes ;
    private int adInterval ;
    private FuseAdLoader inGameAdLoader;
    private int rewardLives = 0;
    private static int failTimes = 0;

    private boolean isGameFinish;
    private long lastPauseTime;

    public final static String SLOT_ENTER_INTERSTITIAL = "slot_app_start";
    private final static String CONF_APP_START_INTERVAL = "app_start_ad_interval_sec";
    private final static String SLOT_GAME_LIVES_REWARD_VIDEO = "slot_game_lives_ad";
    public final static String SLOT_SCANNER_REWARD_VIDEO = "slot_scanner_ad";
    private final static String CONF_REWARD_LIVES = "conf_reward_lives";

    private boolean isScanner = false;
    private TextView textScannerNum;
    private RelativeLayout layoutScanner;
    private ProgressBar rewardProgressBar;
    

    public static boolean needAppStartAd() {
        boolean ret = PreferenceUtils.hasShownRateDialog() &&
                (System.currentTimeMillis() - PreferenceUtils.getLastAppStartAdTime(MApp.getApp())) >= 1000*RemoteConfig.getLong(CONF_APP_START_INTERVAL);
        MLogs.d("needAppStartAd: " + ret);
        return ret;
    }

    private boolean useRewardAd() {
        return  rewardLives > 0;
    }

    private boolean needGetLife() {
        return useRewardAd() && failTimes >= rewardLives && PreferenceUtils.hasShownRateDialog() &&
                FuseAdLoader.get(SLOT_GAME_LIVES_REWARD_VIDEO, this).hasValidCache();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        rewardLives = (int) RemoteConfig.getLong(CONF_REWARD_LIVES);
        adInterval = (int) RemoteConfig.getLong(AD_INTERVAL);
        if (inGameAdLoader == null) {
            inGameAdLoader = FuseAdLoader.get(SLOT_GAME_INTERSTITIAL, this);
        }
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TIME_COUNT_MSG :
                        updateTime();
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(TIME_COUNT_MSG), 250);
                        break;
                }
            }
        };

        if (PreferenceUtils.getScannerNumber(this ) <= 1) {
            FuseAdLoader.get(GameActivity.SLOT_SCANNER_REWARD_VIDEO, this).preloadAd(this);
        }

        FuseAdLoader enterAdLoader = FuseAdLoader.get(SLOT_ENTER_INTERSTITIAL, this);
        if (needAppStartAd() && enterAdLoader.hasValidCache()) {
            enterAdLoader.loadAd(this, 1, 0, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    mResetTimes = 0;
                    ad.show();
                    PreferenceUtils.updateLastAppStartAdTime(GameActivity.this);
                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initData();
                            initView();
                        }
                    });
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {

                }

                @Override
                public void onRewarded(IAdAdapter ad) {

                }
            });
        } else {
            initData();
            initView();
            MLogs.d("no ad cache for app start");
        }
        EventReporter.homeShow(this);
        EventReporter.newGame(this, PreferenceUtils.getDifficulty(), numOfMine);
        EventReporter.reportWake(this, "new_game");
        DaemonService.startup(this);
    }


    public void onScannerClick(View view) {
        if (!isScanner) {
            if (PreferenceUtils.getScannerNumber(this) > 0) {
                isScanner = true;
            } else {
                //showDialog
                showScannerDialog();
            }
        } else {
            isScanner = false;
        }
        updateScannerLayout();
    }

    private void updateScannerLayout() {
        textScannerNum.setText(""+PreferenceUtils.getScannerNumber(this));
        if (!isScanner) {
            layoutScanner.setBackgroundColor(getResources().getColor(R.color.color_not_scanner_mode));
        } else {
            layoutScanner.setBackgroundColor(getResources().getColor(R.color.color_scanner_mode));
        }
    }

    private void initData() {
        int screenWidth = DisplayUtils.getScreenWidth(this);
        int screenHeight = DisplayUtils.getScreenHeight(this);
        isScanner = false;
        cellSize = PreferenceUtils.getCellSize();
        lineOfMine= screenWidth/cellSize;
        int headHeight = DisplayUtils.dip2px(this, 50);
        rowOfMine = (screenHeight - headHeight)/cellSize;

        mineGrids = (MineCell[][]) Array.newInstance(MineCell.class, new int[] { rowOfMine, lineOfMine });
        difficulty = PreferenceUtils.getDifficulty();
        numOfMine = (int)((float)rowOfMine*(float)lineOfMine* difficulty/100);
        leftFlags = numOfMine;
        clicked = 0;
        steps = 0;
        isGameFinish = false;
        lastPauseTime = 0;
        startTime = System.currentTimeMillis();
        mHandler.removeMessages(TIME_COUNT_MSG);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(TIME_COUNT_MSG), 250);
        MLogs.d("Line*Row: " + lineOfMine + "*" + rowOfMine + " Mines:"+ numOfMine);
    }

    private void updateLeftFlags() {
        int num3 = leftFlags/100;
        int num2 = (leftFlags - num3*100)/10;
        int num1 = (leftFlags - num3*100 - num2*10);
        updateNumberImage(leftFlagNum3, num3);
        updateNumberImage(leftFlagNum2, num2);
        updateNumberImage(leftFlagNum1, num1);

    }

    private void updateNumberImage(ImageView iv, int num) {
        switch (num) {
            case 0:
                iv.setImageResource(R.drawable.c0);
                break;
            case 1:
                iv.setImageResource(R.drawable.c1);
                break;
            case 2:
                iv.setImageResource(R.drawable.c2);
                break;
            case 3:
                iv.setImageResource(R.drawable.c3);
                break;
            case 4:
                iv.setImageResource(R.drawable.c4);
                break;
            case 5:
                iv.setImageResource(R.drawable.c5);
                break;
            case 6:
                iv.setImageResource(R.drawable.c6);
                break;
            case 7:
                iv.setImageResource(R.drawable.c7);
                break;
            case 8:
                iv.setImageResource(R.drawable.c8);
                break;
            case 9:
                iv.setImageResource(R.drawable.c9);
                break;
        }
    }

    public void onResetClick(View view) {
        if (needGetLife()) {
            showGetLifeDialog();
        } else {
            MLogs.d("interval " + adInterval + " reset: " + mResetTimes);
            if (adInterval != 0 && (++mResetTimes % adInterval) == 0
                    && PreferenceUtils.hasShownRateDialog()) {
                MLogs.d("need show ad");
                if (inGameAdLoader.hasValidCache()) {
                    MLogs.d("hasValidCache");
                    inGameAdLoader.loadAd(this, 2,500, new IAdLoadListener() {
                        @Override
                        public void onRewarded(IAdAdapter ad) {

                        }

                        @Override
                        public void onAdClicked(IAdAdapter ad) {
                        }

                        @Override
                        public void onAdClosed(IAdAdapter ad) {
                            if (adInterval != 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //  inGameAdLoader.preloadAd(GameActivity.this);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onAdLoaded(IAdAdapter ad) {
                            ad.show();
                        }

                        @Override
                        public void onAdListLoaded(List<IAdAdapter> ads) {

                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
                } else {
                    mResetTimes--;
                    inGameAdLoader.preloadAd(GameActivity.this);
                    MLogs.d("no cache");
                }
            } else {
                MLogs.d("no need to show ");
                if (adInterval != 0) {
                    inGameAdLoader.preloadAd(GameActivity.this);
                }
            }
            doReset();
        }
    }

    private void showScannerDialog() {
        FuseAdLoader.get(SLOT_SCANNER_REWARD_VIDEO, GameActivity.this).loadAd(GameActivity.this, 2, 0, null);
        CustomDialog.show(this, getString(R.string.scanner_title),
                getString(R.string.scanner_description), null, RemoteConfig.getString("reward_dialog_button"),
                R.drawable.reward_video, R.layout.reward_video_dialog,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FuseAdLoader adLoader = FuseAdLoader.get(SLOT_SCANNER_REWARD_VIDEO, GameActivity.this);
                        if (!adLoader.hasValidCache()) {
                            rewardProgressBar.setVisibility(View.VISIBLE);
                        }
                        FuseAdLoader.get(SLOT_SCANNER_REWARD_VIDEO, GameActivity.this).loadAd(GameActivity.this, 2, 0, new IAdLoadListener() {
                            @Override
                            public void onAdLoaded(IAdAdapter ad) {
                                ad.show();
                                rewardProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAdClicked(IAdAdapter ad) {

                            }

                            @Override
                            public void onAdClosed(IAdAdapter ad) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rewardProgressBar.setVisibility(View.GONE);
                                        FuseAdLoader.get(SLOT_SCANNER_REWARD_VIDEO, GameActivity.this).preloadAd(GameActivity.this);
                                    }
                                });
                            }

                            @Override
                            public void onAdListLoaded(List<IAdAdapter> ads) {

                            }

                            @Override
                            public void onError(String error) {
                                rewardProgressBar.setVisibility(View.GONE);
                                Toast.makeText(GameActivity.this, R.string.toast_no_reward_content, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onRewarded(IAdAdapter ad) {
                                MLogs.d("onRewarded");
                                PreferenceUtils.incScannerNumber(GameActivity.this);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        isScanner = true;
                                        Toast.makeText(GameActivity.this, R.string.toast_get_scanner, Toast.LENGTH_SHORT).show();
                                        updateScannerLayout();
                                    }
                                });
                                EventReporter.setUserProperty(EventReporter.PROP_REWARD_USER, "true");
                                EventReporter.generalEvent("reward_" + ad.getAdType());
                                EventReporter.reportReward("game_scanner_"+ad.getAdType());
                            }
                        });
                    }
                });
    }

    private void showGetLifeDialog() {
        FuseAdLoader.get(SLOT_GAME_LIVES_REWARD_VIDEO, GameActivity.this).loadAd(GameActivity.this, 2, 0, null);
        CustomDialog.show(this, RemoteConfig.getString("reward_dialog_title"),
                RemoteConfig.getString("reward_dialog_content"), null, RemoteConfig.getString("reward_dialog_button"),
                R.drawable.reward_video, R.layout.reward_video_dialog,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FuseAdLoader.get(SLOT_GAME_LIVES_REWARD_VIDEO, GameActivity.this).loadAd(GameActivity.this, 2, 0, new IAdLoadListener() {
                            @Override
                            public void onAdLoaded(IAdAdapter ad) {
                                ad.show();
                            }

                            @Override
                            public void onAdClicked(IAdAdapter ad) {

                            }

                            @Override
                            public void onAdClosed(IAdAdapter ad) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        FuseAdLoader.get(SLOT_GAME_LIVES_REWARD_VIDEO, GameActivity.this).preloadAd(GameActivity.this);
                                    }
                                });
                            }

                            @Override
                            public void onAdListLoaded(List<IAdAdapter> ads) {

                            }

                            @Override
                            public void onError(String error) {

                            }

                            @Override
                            public void onRewarded(IAdAdapter ad) {
                                failTimes = 0;
                                MLogs.d("onRewarded");
                                EventReporter.setUserProperty(EventReporter.PROP_REWARD_USER, "true");
                                EventReporter.generalEvent("reward_" + ad.getAdType());
                                EventReporter.reportReward("game_live_"+ad.getAdType());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        doReset();
                                    }
                                });
                            }
                        });
                    }
                });
    }

    private void doReset() {
        MLogs.d("doReset");
        initData();
        initView();
        EventReporter.newGame(this, PreferenceUtils.getDifficulty(), numOfMine);
    }

    private void initView() {
        setContentView(R.layout.game_main_layout);
        flagMineButton = (Button) findViewById(R.id.ButtonMineFlag);
        layoutScanner = findViewById(R.id.scanner_layout);
        textScannerNum = findViewById(R.id.text_scanner_num);
        rewardProgressBar = findViewById(R.id.reward_loading_progress);
        rewardProgressBar.setVisibility(View.GONE);
        updateScannerLayout();
        if (isFlagMode) {
            flagMineButton.setBackgroundResource(R.drawable.flagmineswitcher_flag);
        } else {
            flagMineButton.setBackgroundResource(R.drawable.flagmineswitcher_mine);
        }
//        headLayout = (FrameLayout) findViewById(R.id.head_layout);
//        headLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, headLayoutHeight));
        mineTableLayout = (TableLayout) findViewById(R.id.table_layout);
        leftFlagNum3 = (ImageView)findViewById(R.id.left_flag_num3);
        leftFlagNum2 = (ImageView)findViewById(R.id.left_flag_num2);
        leftFlagNum1 = (ImageView)findViewById(R.id.left_flag_num1);

        timeNum4 = (ImageView)findViewById(R.id.time_num4);
        timeNum3 = (ImageView)findViewById(R.id.time_num3);
        timeNum2 = (ImageView)findViewById(R.id.time_num2);
        timeNum1 = (ImageView)findViewById(R.id.time_num1);

        resetButton = (Button) findViewById(R.id.reset_button);
        resetButton.setBackgroundResource(R.drawable.smile);

        initMineTable();
        updateLeftFlags();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isGameFinish) {
            lastPauseTime = System.currentTimeMillis();
            mHandler.removeMessages(TIME_COUNT_MSG);
            MLogs.d("paused ");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isGameFinish && lastPauseTime != 0) {
            startTime += (System.currentTimeMillis() - lastPauseTime);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(TIME_COUNT_MSG), 100);
            MLogs.d("resume offset : " + (System.currentTimeMillis() - lastPauseTime));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int newSize = PreferenceUtils.getCellSize();
        if (newSize != cellSize || difficulty != PreferenceUtils.getDifficulty()) {
            MLogs.d("setting changed");
            cellSize = newSize;
            difficulty = PreferenceUtils.getDifficulty();
            initData();
            initView();
        }
        updateScannerLayout();
        if (useRewardAd()) {
            FuseAdLoader.get(SLOT_GAME_LIVES_REWARD_VIDEO, this).preloadAd(this);
        }
        if (adInterval != 0) {
            inGameAdLoader.preloadAd(this);
        }
    }

    private void updateTime(){
        long used = System.currentTimeMillis() - startTime;
        int seconds = (int)used/1000%60;
        int minutes = (int)used/1000/60;
        if (minutes > 99) minutes = 99;
        updateNumberImage(timeNum4, minutes/10);
        updateNumberImage(timeNum3, minutes%10);
        updateNumberImage(timeNum2, seconds/10);
        updateNumberImage(timeNum1, seconds%10);
    }
    private void initMineTable() {

        ArrayList<Integer> shuffle = new ArrayList<>(rowOfMine*lineOfMine);
        for (int i = 0; i < rowOfMine*lineOfMine; i ++ ) {
            shuffle.add(i,i);
        }
        Collections.shuffle(shuffle);

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        tableParams.weight = 1.0F;

        for(int i = 0 ; i < rowOfMine; i ++ ) {
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            rowParams.weight = 1.0F;
            TableRow localTableRow = new TableRow(this);
            localTableRow.setLayoutParams(tableParams);
            localTableRow.setOrientation(LinearLayout.HORIZONTAL);
            mineTableLayout.addView(localTableRow);
            for (int j = 0; j < lineOfMine; j++) {
                mineGrids[i][j] = new MineCell(this);
                mineGrids[i][j].onInit();
                mineGrids[i][j].setLayoutParams(rowParams);
                mineGrids[i][j].row = i;
                mineGrids[i][j].col = j;
                mineGrids[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMineCellClick((MineCell) v);
                    }
                });
                mineGrids[i][j].setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        onMineCellLongClick((MineCell) v);
                        return true;
                    }
                });
                localTableRow.addView(mineGrids[i][j]);
            }
        }

        for(int i = 0; i < numOfMine; i++) {
            int num = shuffle.get(i);
            int row = num/lineOfMine;
            int col = num%lineOfMine;
            mineGrids[row][col].isMine = true;
            ArrayList<Integer> aroundList = getAroundCells(mineGrids[row][col]);
            for(int id:aroundList) {
                mineGrids[id/lineOfMine][id%lineOfMine].around ++;
            }
        }
    }

    private ArrayList<Integer> getAroundCells(MineCell cell) {
        ArrayList<Integer> arr = new ArrayList<>(8);
        for (int row = cell.row -1; row <= cell.row + 1; row++) {
            for (int col = cell.col -1; col<= cell.col + 1; col++) {
                if (row >=0 && col >= 0 && row < rowOfMine && col < lineOfMine ) {
                    if (row == cell.row && col == cell.col) continue;
                    arr.add(row*lineOfMine + col);
                }
            }
        }
        return arr;
    }

    private boolean isSuccess() {
        MLogs.d("isSuccess: clicked: " +  clicked + " total: " + ((rowOfMine*lineOfMine) - numOfMine));
        return clicked >= ((rowOfMine*lineOfMine) - numOfMine);
    }

    private void showRateDialogNeeded(String from){
        if(RateDialog.needShow(from)) {
            new RateDialog(this, from).show();
        }
    }
    private void fail() {
        MLogs.d("failed");
        PreferenceUtils.incGameCount();
        failTimes ++;
        isGameFinish = true;
        mHandler.removeMessages(TIME_COUNT_MSG);
        for (int i = 0; i < rowOfMine; i ++ ) {
            for (int j = 0; j < lineOfMine; j ++ ) {
                mineGrids[i][j].onShow();
            }
        }
        resetButton.setBackgroundResource(R.drawable.sorrow);
        showRateDialogNeeded(RateDialog.FROM_GAME_FAIL);
    }

    private void success() {
        MLogs.d("success");
        PreferenceUtils.incGameCount();
        isGameFinish = true;
        mHandler.removeMessages(TIME_COUNT_MSG);
        for (int i = 0; i < rowOfMine; i ++ ) {
            for (int j = 0; j < lineOfMine; j ++ ) {
                mineGrids[i][j].onShow();
            }
        }
        ReportActivity.start(this, REQUEST_SHOW_REPORT, steps, numOfMine, 0, System.currentTimeMillis() - startTime);
        showRateDialogNeeded(RateDialog.FROM_GAME_SUCCESS);
    }

    private void doMineClick(int row, int line, boolean isFlagMode, boolean countStep) {
        MineCell cell = mineGrids[row][line];
        if (cell != null) {
            onMineCellClick(cell, isFlagMode, countStep);
        }
    }
    private void onMineCellClick(final MineCell cell, boolean isFlagMode, boolean countStep) {
        if (cell.state == MineCell.STATE_CLICKED) {
            return;
        }
        if (cell.state == MineCell.STATE_INIT
                && isScanner) {
            cell.updateState(MineCell.STATE_SHOW);
            cell.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isGameFinish) {
                        cell.updateState(MineCell.STATE_INIT);
                    }
                }
            }, 1000);
            PreferenceUtils.decScannerNumber(this);
            isScanner = false;
            updateScannerLayout();
            return;
        }
        if (countStep) steps ++;
        if (isFlagMode) {
            cell.onFlag();
            if (cell.state == MineCell.STATE_FLAG ) {
                leftFlags --;
            }else {
                leftFlags ++;
            }
            updateLeftFlags();
        } else {
            if (cell.state == MineCell.STATE_INIT) {
                if (steps == 1 && cell.isMine) {
                    //avoid first click cell;
                    MLogs.d("bac luck re-init");
                    long prePauseTime = lastPauseTime;
                    long lastStartTime = startTime;
                    doReset();
                    lastPauseTime = prePauseTime;
                    startTime = lastStartTime;
                    doMineClick(cell.row, cell.col, isFlagMode, countStep);
                    return;
                }
                cell.onClick();
                if (cell.isMine) {
                    fail();
                } else {
                    clicked ++ ;
                    if (isSuccess()) {
                        success();
                    } else if (cell.around == 0) {
                        sweepAround(cell);
                    }
                }
            }
        }
    }

    private void sweepAround(MineCell cell) {
        ArrayList<Integer> around = getAroundCells(cell);
        for (int id :around) {
            onMineCellClick(mineGrids[id/lineOfMine][id%lineOfMine], false, false);
        }
    }

    private void onMineCellClick(MineCell cell) {
        onMineCellClick(cell, isFlagMode, true);
    }

    private void onMineCellLongClick(MineCell cell) {
        if (cell.state != MineCell.STATE_CLICKED) {
            onMineCellClick(cell, !isFlagMode, true);
        }
    }

    public void onMineFlagSwitch(View v) {
        isFlagMode = !isFlagMode;
        if (isFlagMode) {
            flagMineButton.setBackgroundResource(R.drawable.flagmineswitcher_flag);
        } else {
            flagMineButton.setBackgroundResource(R.drawable.flagmineswitcher_mine);
        }
    }

    public void onSettingClick(View v) {
        Intent setting = new Intent();
        setting.setClass(this, SettingsActivity.class);
        startActivity(setting);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_SHOW_REPORT:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        break;
                    case RESULT_OK:
                        doReset();
                        break;
                }
                break;
            default:
                break;
        }
    }
}
