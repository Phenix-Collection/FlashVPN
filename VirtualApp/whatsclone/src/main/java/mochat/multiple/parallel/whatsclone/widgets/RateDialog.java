package mochat.multiple.parallel.whatsclone.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.activity.FeedbackActivity;
import mochat.multiple.parallel.whatsclone.utils.AnimatorHelper;
import mochat.multiple.parallel.whatsclone.utils.CommonUtils;
import mochat.multiple.parallel.whatsclone.utils.DisplayUtils;
import mochat.multiple.parallel.whatsclone.utils.EventReporter;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;


/**
 * Created by guojia on 2018/6/2.
 */

public class RateDialog {
    TextView button;
    ImageView star1;
    ImageView star2;
    ImageView star3;
    ImageView star4;
    ImageView star5;
    int rating = 0;
    String from;
    Context activity;
    private Dialog dialog;

    public RateDialog(Context activity, String from) {
        this.activity = activity;
        this.from = from;
    }

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
                    PreferencesUtils.setLoveApp(true);
                    PreferencesUtils.setRated(true);
                    EventReporter.reportRate(activity, from + "_" + rating, from);
                } else {
                    Intent feedback = new Intent(activity, FeedbackActivity.class);
                    activity.startActivity(feedback);
                    PreferencesUtils.setLoveApp(false);
                    EventReporter.reportRate(activity, from + "_" + rating, from);
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
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        AnimatorHelper.elasticScale(dialogView);
        return dialog;
    }
}
