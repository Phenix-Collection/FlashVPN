package winterfell.flash.vpn.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import winterfell.flash.vpn.R;
import winterfell.flash.vpn.ui.FeedbackActivity;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;


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
                    PreferenceUtils.setLoveApp(true);
                    PreferenceUtils.setRated(true);
                    EventReporter.reportRate(activity, from + "_" + rating, from);
                } else {
                    FeedbackActivity.start(activity, rating);
                    PreferenceUtils.setLoveApp(false);
                    EventReporter.reportRate(activity, from + "_" + rating, from);
                }
                dialog.dismiss();
            }
        });
        dialog.setContentView(dialogView);
        int dialogwidth = CommonUtils.getScreenWidth(activity) * 5 / 6;
        // 设置Dialog的大小
        dialog.getWindow().setLayout(dialogwidth, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        try {
            dialog.show();
        }catch (Exception e) {
            MLogs.logBug(e);
        }
        //AnimatorHelper.elasticScale(dialogView);
        return dialog;
    }
}
