package com.polestar.minesweeperclassic.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.polestar.minesweeperclassic.MApp;
import com.polestar.minesweeperclassic.R;
import com.polestar.minesweeperclassic.activity.FeedbackActivity;
import com.polestar.minesweeperclassic.activity.SettingsActivity;
import com.polestar.minesweeperclassic.utils.CommonUtils;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;
import com.polestar.minesweeperclassic.utils.PreferenceUtils;
import com.polestar.minesweeperclassic.utils.RemoteConfig;

/**
 * Created by doriscoco on 2017/4/11.
 */

public class RateDialog {
    private Context mContext;
    private String mFrom;

    public static final String FROM_GAME_SUCCESS = "game_finish";
    public static final String FROM_GAME_FAIL = "game_fail";
    public static final String FROM_SETTINGS = "settings";

    private static final String CONFIG_RATING_GAME_GATE = "rating_game_gate";
    private static final String CONFIG_RATING_INTERVAL = "rating_interval";
    public RateDialog(Context context, String from) {
        mContext = context;
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
                interval = RemoteConfig.getLong(CONFIG_RATING_INTERVAL);
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
                interval = RemoteConfig.getLong(CONFIG_RATING_INTERVAL);
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

    public void show(){
        EventReporter.reportRate(mContext,"start", mFrom);
        PreferenceUtils.updateRateDialogTime(mContext);
        String title = mContext.getString(R.string.rate_us);
        UpDownDialog.show(mContext, title,
                mContext.getString(R.string.dialog_rating_us_content), mContext.getString(R.string.not_really),
                mContext.getString(R.string.yes), R.drawable.dialog_tag_congratulations,
                R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                PreferenceUtils.setLoveApp(false);
                                EventReporter.loveApp(mContext, false, mFrom );
                                UpDownDialog.show(mContext, mContext.getString(R.string.feedback),
                                        mContext.getString(R.string.dialog_feedback_content),
                                        mContext.getString(R.string.no_thanks),
                                        mContext.getString(R.string.ok), R.drawable.dialog_tag_comment,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        EventReporter.reportRate(mContext, "go_faq", mFrom);
                                                        Intent feedback = new Intent(mContext, FeedbackActivity.class);
                                                        mContext.startActivity(feedback);
                                                        break;
                                                }
                                            }
                                        });
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                PreferenceUtils.setLoveApp(true);
                                EventReporter.loveApp(mContext, true, mFrom );
                                UpDownDialog.show(mContext, mContext.getString(R.string.dialog_love_title),
                                        mContext.getString(R.string.dialog_love_content),
                                        mContext.getString(R.string.remind_me_later),
                                        mContext.getString(R.string.star_rating), R.drawable.dialog_tag_love,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        EventReporter.reportRate(mContext, "go_rating",mFrom);
                                                        PreferenceUtils.setRated(true);
                                                        CommonUtils.jumpToMarket(mContext, mContext.getPackageName());
                                                        break;
                                                }
                                            }
                                        });
                                break;
                        }
                    }
                });

    }
}
