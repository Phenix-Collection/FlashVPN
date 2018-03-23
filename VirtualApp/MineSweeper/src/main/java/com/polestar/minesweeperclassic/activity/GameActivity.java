package com.polestar.minesweeperclassic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.polestar.minesweeperclassic.R;
import com.polestar.minesweeperclassic.utils.DisplayUtils;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;
import com.polestar.minesweeperclassic.utils.PreferenceUtils;
import com.polestar.minesweeperclassic.widget.MineCell;
import com.polestar.minesweeperclassic.widget.RateDialog;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
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
        initData();
        initView();
        EventReporter.homeShow(this);
        EventReporter.newGame(this, PreferenceUtils.getDifficulty(), numOfMine);
    }

    private void initData() {
        int screenWidth = DisplayUtils.getScreenWidth(this);
        int screenHeight = DisplayUtils.getScreenHeight(this);
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

    public void onReset(View view) {
        doReset();
    }

    private void doReset() {
        initData();
        initView();
        EventReporter.newGame(this, PreferenceUtils.getDifficulty(), numOfMine);
    }

    private void initView() {
        setContentView(R.layout.game_main_layout);
        flagMineButton = (Button) findViewById(R.id.ButtonMineFlag);
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
    protected void onResume() {
        super.onResume();
        int newSize = PreferenceUtils.getCellSize();
        if (newSize != cellSize || difficulty != PreferenceUtils.getDifficulty()) {
            cellSize = newSize;
            difficulty = PreferenceUtils.getDifficulty();
            doReset();
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
        mHandler.removeMessages(TIME_COUNT_MSG);
        for (int i = 0; i < rowOfMine; i ++ ) {
            for (int j = 0; j < lineOfMine; j ++ ) {
                mineGrids[i][j].onShow();
            }
        }
        ReportActivity.start(this, REQUEST_SHOW_REPORT, steps, numOfMine, 0, System.currentTimeMillis() - startTime);
        showRateDialogNeeded(RateDialog.FROM_GAME_SUCCESS);
    }

    private void onMineCellClick(MineCell cell, boolean isFlagMode, boolean countStep) {
        if (cell.state == MineCell.STATE_CLICKED) {
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
