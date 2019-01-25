package com.polestar.superclone.reward;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;

/**
 * Created by guojia on 2019/1/24.
 */

public class BaseFragmentDialog extends DialogFragment{
    public final int LONG_ANIM;
    public final int MEDIUM_ANIM;
    public final int SHORT_ANIM;

    public BaseFragmentDialog() {
        super();
        this.SHORT_ANIM = 100;
        this.MEDIUM_ANIM = 300;
        this.LONG_ANIM = 600;
    }

    public void closeDialog() {
    }

    public void defaultDialogSettings() {
        if(this.getDialog() != null && this.getDialog().getWindow() != null) {
            this.getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        this.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface arg2, int arg3, KeyEvent arg4) {
                if(arg3 == KeyEvent.KEYCODE_BACK) {
                    BaseFragmentDialog.this.closeDialog();
                    return true;
                }

                return false;
            }
        });
    }

    public int getScreenHeight() {
        if(this.getActivity() != null) {
            Display v1 = this.getActivity().getWindowManager().getDefaultDisplay();
            Point v2 = new Point();
            v1.getSize(v2);
            return v2.y;
        }

        return 0;
    }

    public void onCreate(Bundle arg4) {
        super.onCreate(arg4);
        //this.setStyle(STYLE_NO_FRAME, 16973934);
        this.setStyle(STYLE_NO_FRAME, 16973934);
    }

    public Dialog onCreateDialog(Bundle arg4) {
        Dialog v2 = super.onCreateDialog(arg4);
//        if(v2.getWindow() != null && v2.getWindow().getAttributes() != null) {
//            v2.getWindow().getAttributes().windowAnimations = 2131296433;
//        }

        return v2;
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    public int statusBarHeight() {
        int result = 0;
        int resourceId = getActivity().getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = getActivity().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
//    public int statusBarHeight() {
//        Rect v1 = new Rect();
//        if(this.getActivity() != null) {
//            Window v2 = this.getActivity().getWindow();
//            v2.getDecorView().getWindowVisibleDisplayFrame(v1);
//            return v2.findViewById(16908290).getTop() - v1.top;
//        }
//
//        return 0;
//    }
}