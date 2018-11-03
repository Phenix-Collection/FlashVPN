package com.polestar.minesweeperclassic.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.minesweeperclassic.MApp;
import com.polestar.minesweeperclassic.R;
import com.polestar.minesweeperclassic.activity.FeedbackActivity;
import com.polestar.minesweeperclassic.activity.SettingsActivity;
import com.polestar.minesweeperclassic.utils.CommonUtils;
import com.polestar.minesweeperclassic.utils.DisplayUtils;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;
import com.polestar.minesweeperclassic.utils.PreferenceUtils;
import com.polestar.minesweeperclassic.utils.RemoteConfig;

/**
 * Created by doriscoco on 2017/4/11.
 */

public class RateDialog {
    private Activity activity;
    private String mFrom;

    private Dialog dialog;
    TextView button;
    ImageView star1;
    ImageView star2;
    ImageView star3;
    ImageView star4;
    ImageView star5;
    int rating = 0;
    public static final String FROM_GAME_SUCCESS = "game_finish";
    public static final String FROM_GAME_FAIL = "game_fail";
    public static final String FROM_SETTINGS = "settings";

    private static final String CONFIG_RATING_GAME_GATE = "rating_game_gate";
    private static final String CONFIG_RATING_INTERVAL = "rating_interval";
    public RateDialog(Activity context, String from) {
        activity = context;
        mFrom = from;
    }
    public static boolean needShow(String from){
        if(FROM_SETTINGS.equals(from)) {
            return true;
        }
        if (FROM_GAME_SUCCESS.equals(from) && !PreferenceUtils.isRated()){
            long current = System.currentTimeMillis();
            int count = PreferenceUtils.getGameCount();
            long interval;
            if(PreferenceUtils.getLoveApp() == 0) {
                interval = 0;
            } else {
                interval = PreferenceUtils.getLoveApp() == 1? RemoteConfig.getLong(CONFIG_RATING_INTERVAL) :
                        3*RemoteConfig.getLong(CONFIG_RATING_INTERVAL);
            }
            if (count >= RemoteConfig.getLong(CONFIG_RATING_GAME_GATE)
                    && (current - PreferenceUtils.getRateDialogTime(MApp.getApp())) > interval*3600*1000) {
                return true;
            }
        }
        if (FROM_GAME_FAIL.equals(from) && !PreferenceUtils.isRated()){
            long current = System.currentTimeMillis();
            int count = PreferenceUtils.getGameCount();
            long interval;
            if(PreferenceUtils.getLoveApp() == 0) {
                interval = 0;
            } else {
                interval = PreferenceUtils.getLoveApp() == 1? RemoteConfig.getLong(CONFIG_RATING_INTERVAL) :
                        3*RemoteConfig.getLong(CONFIG_RATING_INTERVAL);
            }
            MLogs.d("Count: " + count + " interval: " + interval
                    + " last: " + PreferenceUtils.getRateDialogTime(MApp.getApp()));
            if (count >= RemoteConfig.getLong(CONFIG_RATING_GAME_GATE)
                    && (current - PreferenceUtils.getRateDialogTime(MApp.getApp())) > interval*3600*1000) {
                return true;
            }
        }

        return false;
    }

//    public void show(){
//        EventReporter.reportRate(activity,"start", mFrom);
//        PreferenceUtils.updateRateDialogTime(activity);
//        String title = activity.getString(R.string.rate_us);
//        UpDownDialog.show(activity, title,
//                activity.getString(R.string.dialog_rating_us_content), activity.getString(R.string.not_really),
//                activity.getString(R.string.yes), R.drawable.dialog_tag_congratulations,
//                R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        switch (which) {
//                            case UpDownDialog.NEGATIVE_BUTTON:
//                                PreferenceUtils.setLoveApp(false);
//                                EventReporter.loveApp(activity, false, mFrom );
//                                UpDownDialog.show(activity, activity.getString(R.string.feedback),
//                                        activity.getString(R.string.dialog_feedback_content),
//                                        activity.getString(R.string.no_thanks),
//                                        activity.getString(R.string.ok), R.drawable.dialog_tag_comment,
//                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                switch (which) {
//                                                    case UpDownDialog.POSITIVE_BUTTON:
//                                                        EventReporter.reportRate(activity, "go_faq", mFrom);
//                                                        Intent feedback = new Intent(activity, FeedbackActivity.class);
//                                                        activity.startActivity(feedback);
//                                                        break;
//                                                }
//                                            }
//                                        });
//                                break;
//                            case UpDownDialog.POSITIVE_BUTTON:
//                                PreferenceUtils.setLoveApp(true);
//                                EventReporter.loveApp(activity, true, mFrom );
//                                UpDownDialog.show(activity, activity.getString(R.string.dialog_love_title),
//                                        activity.getString(R.string.dialog_love_content),
//                                        activity.getString(R.string.remind_me_later),
//                                        activity.getString(R.string.star_rating), R.drawable.dialog_tag_love,
//                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                switch (which) {
//                                                    case UpDownDialog.POSITIVE_BUTTON:
//                                                        EventReporter.reportRate(activity, "go_rating",mFrom);
//                                                        PreferenceUtils.setRated(true);
//                                                        CommonUtils.jumpToMarket(activity, activity.getPackageName());
//                                                        break;
//                                                }
//                                            }
//                                        });
//                                break;
//                        }
//                    }
//                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                PreferenceUtils.setLoveApp(false);
//            }
//        });
//
//    }
    public Dialog show( ) {
        dialog = new Dialog(activity, R.style.CustomDialog);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.rate_dialog, null);
        button = (TextView) dialogView.findViewById(R.id.button_submit);
        star1 = (ImageView) dialogView.findViewById(R.id.star1);
        star2 = (ImageView) dialogView.findViewById(R.id.star2);
        star3 = (ImageView) dialogView.findViewById(R.id.star3);
        star4 = (ImageView) dialogView.findViewById(R.id.star4);
        star5 = (ImageView) dialogView.findViewById(R.id.star5);
        button.setVisibility(View.INVISIBLE);

        star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star1.setImageResource(R.drawable.five_star_y);
                star2.setImageResource(R.drawable.five_star_g);
                star3.setImageResource(R.drawable.five_star_g);
                star4.setImageResource(R.drawable.five_star_g);
                star5.setImageResource(R.drawable.five_star_g);
                rating = 1;
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.feedback);
            }
        });

        star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star1.setImageResource(R.drawable.five_star_y);
                star2.setImageResource(R.drawable.five_star_y);
                star3.setImageResource(R.drawable.five_star_g);
                star4.setImageResource(R.drawable.five_star_g);
                star5.setImageResource(R.drawable.five_star_g);
                rating = 2;
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.feedback);
            }
        });

        star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star1.setImageResource(R.drawable.five_star_y);
                star2.setImageResource(R.drawable.five_star_y);
                star3.setImageResource(R.drawable.five_star_y);
                star4.setImageResource(R.drawable.five_star_g);
                star5.setImageResource(R.drawable.five_star_g);
                rating = 3;
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.feedback);
            }
        });

        star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star1.setImageResource(R.drawable.five_star_y);
                star2.setImageResource(R.drawable.five_star_y);
                star3.setImageResource(R.drawable.five_star_y);
                star4.setImageResource(R.drawable.five_star_y);
                star5.setImageResource(R.drawable.five_star_g);
                rating = 4;
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.feedback);
            }
        });

        star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star1.setImageResource(R.drawable.five_star_y);
                star2.setImageResource(R.drawable.five_star_y);
                star3.setImageResource(R.drawable.five_star_y);
                star4.setImageResource(R.drawable.five_star_y);
                star5.setImageResource(R.drawable.five_star_y);
                rating = 5;
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.star_rating);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rating == 5) {
                    CommonUtils.jumpToMarket(activity, activity.getPackageName());
                    PreferenceUtils.setLoveApp(true);
                    PreferenceUtils.setRated(true);
                    EventReporter.reportRate(activity, mFrom + "_" + rating, mFrom);
                } else {
                    FeedbackActivity.start(activity, rating);
                    PreferenceUtils.setLoveApp(false);
                    EventReporter.reportRate(activity, mFrom + "_" + rating, mFrom);
                }
                dialog.dismiss();
            }
        });
        dialog.setContentView(dialogView);
        int dialogwidth = DisplayUtils.getScreenWidth(activity) * 5 / 6;
        // 设置Dialog的大小
        dialog.getWindow().setLayout(dialogwidth, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        try {
            dialog.show();
        }catch (Exception e) {
           e.printStackTrace();
        }
        //AnimatorHelper.elasticScale(dialogView);
        return dialog;
    }
}
