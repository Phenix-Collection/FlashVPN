package com.polestar.minesweeperclassic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.minesweeperclassic.R;

/**
 * Created by doriscoco on 2017/4/4.
 */

public class ReportActivity extends Activity{
    private TextView title;
    private ImageView labelImage;
    private TextView reportDetail;
    private static final String EXTRA_STEPS = "steps";
    private static final String EXTRA_TIME = "time";
    private static final String EXTRA_DIFFICULTY = "difficulty";
    private static final String EXTRA_MINES = "mines";
    private Intent intent;

    public static void start(Activity activity, int request, int steps, int mines, int difficulty, long time) {
        Intent intent = new Intent();
        intent.setClass(activity, ReportActivity.class);
        intent.putExtra(EXTRA_STEPS,steps);
        intent.putExtra(EXTRA_MINES,mines);
        intent.putExtra(EXTRA_TIME,time);
        intent.putExtra(EXTRA_DIFFICULTY,difficulty);
        activity.startActivityForResult(intent, request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        intent = getIntent();
        initData();
        initView();
    }

    public void onConfirm(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onAgain(View view) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void initView(){
        setContentView(R.layout.report_activity_layout);
        title = (TextView) findViewById(R.id.label);
        labelImage = (ImageView) findViewById(R.id.label_img);
        labelImage.setImageResource(R.drawable.icon_congratulation);
        title.setText(R.string.congratulation);
        reportDetail = (TextView)findViewById(R.id.report_detail);
        int time = (int)((getIntent().getLongExtra(EXTRA_TIME, 50000))/1000);
        String detail = getString(R.string.report_detail_mine_clear) +
                getIntent().getIntExtra(EXTRA_MINES, 0) + "\n " +
                getString(R.string.report_detail_difficulty) + "Easy" + "\n" +
                getString(R.string.report_detail_time_cost) + time + "\n" +
                getString(R.string.report_detail_steps) + getIntent().getIntExtra(EXTRA_STEPS, 200);
        reportDetail.setText(detail);

    }

    private void initData() {

    }
}
