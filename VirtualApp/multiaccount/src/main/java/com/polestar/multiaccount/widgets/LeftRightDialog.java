package com.polestar.multiaccount.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.AnimatorHelper;
import com.polestar.multiaccount.utils.DisplayUtils;

public class LeftRightDialog {

    public final static int LEFT_BUTTON = 1;
    public final static int RIGHT_BUTTON = 2;


    public static Dialog show(Context context, String title, String content, String leftBtnName, String rightBtnName, final DialogInterface.OnClickListener onClickListener) {
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
        DialogViewHolder holder = new DialogViewHolder();
        holder.title = (TextView) dialogView.findViewById(R.id.dialog_title);
        holder.content = (TextView) dialogView.findViewById(R.id.dialog_content);
        holder.leftBtn = (TextView) dialogView.findViewById(R.id.dialog_left_btn);
        holder.rightBtn = (TextView) dialogView.findViewById(R.id.dialog_right_btn);
        holder.btnDivider = dialogView.findViewById(R.id.btn_divider);

        if (rightBtnName == null || TextUtils.isEmpty(rightBtnName)) {
            holder.btnDivider.setVisibility(View.GONE);
            holder.rightBtn.setVisibility(View.GONE);
        } else {
            holder.rightBtn.setText(rightBtnName);
        }

        if (title == null || TextUtils.isEmpty(title)) {
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setText(title);
        }

        holder.content.setText(content);
        holder.leftBtn.setText(leftBtnName);

        holder.leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(dialog, LEFT_BUTTON);
                }
            }
        });
        holder.rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(dialog, RIGHT_BUTTON);
                }
            }
        });

        dialog.setContentView(dialogView);
        int dialogwidth = DisplayUtils.getScreenWidth(context) * 5 / 6;
        // 设置Dialog的大小
        dialog.getWindow().setLayout(dialogwidth, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        AnimatorHelper.elasticScale(dialogView);
        return dialog;
    }

    static class DialogViewHolder {
        TextView title;
        TextView content;
        TextView leftBtn;
        TextView rightBtn;
        View btnDivider;
    }
}