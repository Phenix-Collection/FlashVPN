package in.dualspace.cloner.widget;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.polestar.clone.CustomizeAppData;

import in.dualspace.cloner.R;
import in.dualspace.cloner.utils.DisplayUtils;
import in.dualspace.cloner.utils.MLogs;

/**
 * Created by guojia on 2019/2/19.
 */

public class BottomProgressPopup {

    private PopupWindow popupWindow;
    private View popupView;
    private Activity activity;
    private long minDuration = 0;
    private long autoDismissDuration = -1;
    private boolean hasCallDismiss;
    private long popupTime = 0;
    private Handler mainHandler;
    private boolean isShowing;

    private static final int MSG_MIN_DURATION_PASS = 1;
    private static final int MSG_AUTO_DISSMISS_DURATION_PASS = 2;

    public BottomProgressPopup(Activity context) {
        activity = context;
        popupView = View.inflate(context, R.layout.bar_progress_layout, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
//        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.setAnimationStyle(R.style.anim_bottombar);
        mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_AUTO_DISSMISS_DURATION_PASS:
                        doDismiss();
                        break;
                    case MSG_MIN_DURATION_PASS:
                        if (hasCallDismiss) {
                            doDismiss();
                        } else {
                            MLogs.d("Haven't call dismiss!");
                        }
                        break;
                }
            }
        };
        reset();
    }

    public void setTitle(String s){
        TextView textView = popupView.findViewById(R.id.loading_title);
        textView.setText(s);
    }

    public void setTips(String s) {
        TextView textView = popupView.findViewById(R.id.loading_tips);
        if (!TextUtils.isEmpty(s)) {
            textView.setText(s);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    public void setIconRes(int resId) {
        ImageView imageView = popupView.findViewById(R.id.img_app_icon);
        imageView.setImageResource(resId);
    }

    public void setMinDuration(long ms) {
        minDuration = ms;
    }

    public void setAutoDismissDuration (long ms) {
        autoDismissDuration = ms;
    }

    public void setIconBitmap(Bitmap bmp) {
        ImageView imageView = popupView.findViewById(R.id.img_app_icon);
        imageView.setImageBitmap(bmp);
    }

    public void cancel() {
        MLogs.d("cancel");
        doDismiss();
    }

    public void popup() {
        MLogs.d("popup");
        if (popupWindow != null) {
            View root = activity.findViewById(android.R.id.content);
            if (root != null) {
                popupTime = System.currentTimeMillis();
                if (minDuration > 0 ) {
                    mainHandler.removeMessages(MSG_MIN_DURATION_PASS);
                    mainHandler.sendEmptyMessageDelayed(MSG_MIN_DURATION_PASS, minDuration);
                }
                if (autoDismissDuration > 0) {
                    mainHandler.removeMessages(MSG_AUTO_DISSMISS_DURATION_PASS);
                    mainHandler.sendEmptyMessageDelayed(MSG_AUTO_DISSMISS_DURATION_PASS, autoDismissDuration);
                }
                popupWindow.showAtLocation(root, Gravity.BOTTOM, DisplayUtils.dip2px(activity, 10), DisplayUtils.dip2px(activity, 10));
                ImageView icon = popupView.findViewById(R.id.img_app_icon);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0.7f, 1.2f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0.7f, 1.2f, 1.0f);
                AnimatorSet animSet = new AnimatorSet();
                animSet.play(scaleX).with(scaleY);
                animSet.setInterpolator(new BounceInterpolator());
                animSet.setDuration(800).start();

                ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(1000);
                sa.setRepeatMode(Animation.RESTART);
                sa.setRepeatCount(Animation.INFINITE);
                AlphaAnimation aa = new AlphaAnimation(0, 1);
                aa.setDuration(1000);
                aa.setRepeatMode(Animation.RESTART);
                aa.setRepeatCount(Animation.INFINITE);
                AnimationSet as = new AnimationSet(true);
                as.addAnimation(sa);
                as.addAnimation(aa);
                ImageView circle = popupView.findViewById(R.id.img_success_bg2);
                circle.startAnimation(as);
            }
//            if(!mIsShowing){
//                params.alpha= 0.3f;
//                getWindow().setAttributes(params);
//
//                mIsShowing = true;
//            }
            isShowing = true;
        }

    }

    private void reset() {
        hasCallDismiss = false;
        isShowing = false;
        mainHandler.removeMessages(MSG_AUTO_DISSMISS_DURATION_PASS);
        mainHandler.removeMessages(MSG_MIN_DURATION_PASS);
        popupTime = 0;
        if (popupWindow != null) {
            popupWindow.setOnDismissListener(null);
        }
    }

    public void dismiss() {
        MLogs.d("dismiss");
        hasCallDismiss = true;
        if (minDuration <= 0
                || (System.currentTimeMillis() > (minDuration + popupTime))) {
            doDismiss();
        } else {
            MLogs.d("delay dimiss");
        }
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        if (popupWindow != null) {
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    listener.onDismiss();
                }
            });
        }
    }

    private void doDismiss() {
        MLogs.d("do dismiss");
        if (popupWindow != null) {
            ImageView circle = popupView.findViewById(R.id.img_success_bg2);
            circle.clearAnimation();
            popupWindow.dismiss();
        }
        reset();
    }

    public boolean isShowing() {
        return isShowing;
    }
}
