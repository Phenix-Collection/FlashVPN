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
import com.polestar.minesweeperclassic.utils.CommonUtils;
import com.polestar.minesweeperclassic.utils.EventReporter;

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

    public static final int EASY = 15;
    public static final int NORMAL = 25;
    public static final int HARD = 35;

    private int time;
    private int level;
    private int steps;

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

    public void shareWithFriends(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String appName = context.getResources().getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
        String shareContent = "Clear mines in "
                + getDifficultyString(level).toLowerCase() + " level "
                + "in " + time + " second. Come to challenge me at: ";
        shareContent = shareContent + "https://play.google.com/store/apps/details?id="
                + context.getPackageName() +  "&referrer=utm_source%3Duser_share";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        context.startActivity(Intent.createChooser(shareIntent, getString(R.string.share_tips)));
    }

    private String getDifficultyString(int level) {
        switch (level) {
            case EASY:
                return getString(R.string.difficulty_easy);
            case NORMAL:
                return getString(R.string.difficulty_normal);
            case HARD:
                return getString(R.string.difficulty_hard);
            default:
                return getString(R.string.difficulty_easy);
        }
    }


    public void onConfirm(View view) {
        setResult(RESULT_CANCELED);
        shareWithFriends(this);
        EventReporter.generalEvent("success_confirm");
       // finish();
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
        time = (int)((getIntent().getLongExtra(EXTRA_TIME, 50000))/1000);
        level = getIntent().getIntExtra(EXTRA_DIFFICULTY, EASY);
        steps = getIntent().getIntExtra(EXTRA_STEPS, 200);
        String detail = getString(R.string.report_detail_mine_clear) +
                getIntent().getIntExtra(EXTRA_MINES, 0) + "\n " +
                getString(R.string.report_detail_difficulty) + getDifficultyString(level) + "\n" +
                getString(R.string.report_detail_time_cost) + time + "s\n" +
                getString(R.string.report_detail_steps) + steps;
        reportDetail.setText(detail);

    }

    private void initData() {

    }
}
