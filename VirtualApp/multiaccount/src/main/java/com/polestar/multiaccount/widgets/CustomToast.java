package com.polestar.multiaccount.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.AnimatorHelper;
import com.polestar.multiaccount.utils.DisplayUtils;

/**
 * Created by yxx on 2016/7/25.
 */
public class CustomToast extends Toast {
    private View view;              //自定义的view
    private ImageView toastImg;     //图片
    private TextView toastText; //图片下方的文字
    private Context mContext;
    private RelativeLayout content_layout;
    public CustomToast(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        if (null != inflater) {
            view = inflater.inflate(R.layout.toast_layout, null);
            initView();
        }
    }

    private void initView() {
        if (null != view) {
            toastImg = (ImageView) view.findViewById(R.id.app_toast_img);
            toastText = (TextView) view.findViewById(R.id.app_toast_text);
            content_layout = (RelativeLayout) view.findViewById(R.id.content_layout);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content_layout.getLayoutParams();
            params.height =  DisplayUtils.getScreenWidth(mContext) / 7 * 4;
            params.width =  DisplayUtils.getScreenWidth(mContext) / 7 * 4;
            content_layout.setLayoutParams(params);
            setView(view);
        }
    }

    /**
     * 只显示图片
     *
     * @param imgRes
     */
    public void showSingleImage(final int imgRes) {
        toastText.setVisibility(View.GONE);
        toastImg.setVisibility(View.VISIBLE);
        if (null != toastImg) {
            toastImg.setImageResource(imgRes);
        }
        show();
    }

    @Override
    public void show() {
        super.show();
        AnimatorHelper.elasticScale(view);
    }

    /**
     * 竖排显示：上面图片，下面文字
     *
     * @param imgRes
     */
    public void showImageWithMsg(final int imgRes, final String msg) {
        toastText.setVisibility(View.VISIBLE);
        toastImg.setVisibility(View.VISIBLE);
        setText(toastText, msg);
        if (null != toastImg) {
            toastImg.setImageResource(imgRes);
        }
        show();
    }

    private void setText(final TextView t, final String s) {
        if (null != t && null != s) {
            t.setVisibility(View.VISIBLE);
            t.setText(s);
        }
    }

}
