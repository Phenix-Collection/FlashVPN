package com.polestar.superclone.reward;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.polestar.task.database.datamodels.ShareTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2019/1/24.
 */

public class InviteDialog extends BaseFragmentDialog {
    private static final String TAG = "InviteDialog";
    private boolean animated;
    private ImageButton closeButton;
    private RelativeLayout creditsOfferLayout;
    private static ShareTask currentOffer = null;
    private static int currentPosition = 0;
    private LinearLayout detailsLayout;
    private LinearLayout detailsLayoutContent;
    private RelativeLayout dialogLayout;
    private ImageButton facebookButton;
    private RelativeLayout footerLayout;
    private LinearLayout headerLayout;
    private View iconBg;
    private EditText inviteCodeEt;
    private LinearLayout inviteLayout;
    private ImageButton mailButton;
    private RelativeLayout offerItem;
    private RelativeLayout textOfferLayout;
    private ImageButton twitterButton;
    private ImageButton whatsappButton;

    public InviteDialog() {
        super();
        animated = false;
    }

    private void animateOfferPos(float arg10) {
        AlphaAnimation v5 = new AlphaAnimation(1f, 0f);
        v5.setDuration(600);
        v5.setFillAfter(true);
        textOfferLayout.startAnimation(((Animation)v5));
        creditsOfferLayout.startAnimation(((Animation)v5));
        detailsLayoutContent.startAnimation(((Animation)v5));
        ObjectAnimator v6 = ObjectAnimator.ofFloat(offerItem, "translationY", new float[]{arg10 - (((float)statusBarHeight()))});
        v6.setDuration(300);
        v6.start();
        v6.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator arg1) {
            }

            public void onAnimationEnd(Animator arg9) {
                headerLayout.setVisibility(View.VISIBLE);
                ObjectAnimator v5 = ObjectAnimator.ofFloat(headerLayout, "translationY", new float[]{0f});
                v5.setDuration(300);
                v5.start();
                v5.addListener(new Animator.AnimatorListener() {
                    public void onAnimationCancel(Animator arg1) {
                    }

                    public void onAnimationEnd(Animator arg4) {
                        AlphaAnimation v2 = new AlphaAnimation(0f, 1f);
                        v2.setDuration(600);
                        v2.setFillAfter(true);
                        textOfferLayout.startAnimation(((Animation)v2));
                        creditsOfferLayout.startAnimation(((Animation)v2));
                        detailsLayoutContent.startAnimation(((Animation)v2));
                    }

                    public void onAnimationRepeat(Animator arg1) {
                    }

                    public void onAnimationStart(Animator arg1) {
                    }
                });
                detailsLayout.measure(-1, -2);
                int v6 = detailsLayout.getHeight();
                detailsLayout.getLayoutParams().height = 0;
                detailsLayout.setVisibility(View.VISIBLE);
                ResizeHeightAnimation v7 = new ResizeHeightAnimation(detailsLayout, v6, 0);
                v7.setDuration(300);
                v7.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation arg2) {
                        detailsLayoutContent.invalidate();
                    }

                    public void onAnimationRepeat(Animation arg1) {
                    }

                    public void onAnimationStart(Animation arg1) {
                    }
                });
                detailsLayout.setAnimation(((Animation)v7));
            }

            public void onAnimationRepeat(Animator arg1) {
            }

            public void onAnimationStart(Animator arg1) {
            }
        });
        ObjectAnimator v7 = ObjectAnimator.ofFloat(footerLayout, "translationY", new float[]{0f});
        v7.setDuration(600);
        v7.start();
        v7.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator arg1) {
            }

            public void onAnimationEnd(Animator arg3) {
                dialogLayout.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animator arg1) {
            }

            public void onAnimationStart(Animator arg1) {
            }
        });
        ObjectAnimator v8 = ObjectAnimator.ofFloat(iconBg, "translationY", new float[]{0f});
        v8.setDuration(600);
        v8.start();
    }

    private void closeDialog(boolean arg12) {
        dialogLayout.setVisibility(View.INVISIBLE);
        ObjectAnimator v5 = ObjectAnimator.ofFloat(footerLayout, "translationY", new float[]{((float)getScreenHeight())});
        v5.setDuration(600);
        v5.start();
        v5.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator arg1) {
            }

            public void onAnimationEnd(Animator arg7) {
                ObjectAnimator v5 = ObjectAnimator.ofFloat(offerItem, "translationY", new float[]{((float)(InviteDialog.currentPosition + statusBarHeight()))});
                v5.setDuration(600);
                v5.start();
                v5.addListener(new Animator.AnimatorListener() {
                    public void onAnimationCancel(Animator arg1) {
                    }

                    public void onAnimationEnd(Animator arg3) {
//                        if(getParentFragment() != null) {
//                            if(this$1.val$openOffer) {
//                                getParentFragment().openOffer(currentOffer);
//                            }
//                            else {
//                                getParentFragment().dialogClosed();
//                            }
//                        }

                        dismissAllowingStateLoss();
                    }

                    public void onAnimationRepeat(Animator arg1) {
                    }

                    public void onAnimationStart(Animator arg1) {
                    }
                });
            }

            public void onAnimationRepeat(Animator arg1) {
            }

            public void onAnimationStart(Animator arg1) {
            }
        });
        ObjectAnimator v6 = ObjectAnimator.ofFloat(iconBg, "translationY", new float[]{-2000f});
        v6.setDuration(600);
        v6.start();
        ResizeHeightAnimation v7 = new ResizeHeightAnimation(headerLayout, 0, headerLayout.getHeight());
        v7.setDuration(600);
        headerLayout.setAnimation(((Animation)v7));
        ObjectAnimator v8 = ObjectAnimator.ofFloat(headerLayout, "translationY", new float[]{offerItem.getY() + (((float)statusBarHeight()))});
        v8.setDuration(600);
        v8.start();
        v8.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator arg1) {
            }

            public void onAnimationEnd(Animator arg3) {
                headerLayout.setVisibility(View.INVISIBLE);
            }

            public void onAnimationRepeat(Animator arg1) {
            }

            public void onAnimationStart(Animator arg1) {
            }
        });
        int v9 = 0;
        if(detailsLayout != null) {
            v9 = detailsLayout.getHeight();
        }

        ResizeHeightAnimation v10 = new ResizeHeightAnimation(detailsLayout, 0, v9);
        v10.setDuration(300);
        v10.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation arg3) {
                detailsLayout.setVisibility(View.INVISIBLE);
            }

            public void onAnimationRepeat(Animation arg1) {
            }

            public void onAnimationStart(Animation arg1) {
            }
        });
        if(detailsLayout != null) {
            detailsLayout.setAnimation(((Animation)v10));
        }
    }

    public void closeDialog() {
        closeDialog(false);
    }

    private void fillOffer(View arg10) {
//        int v8;
//        if(InviteDialog.currentOffer != null) {
//            eu.ˋ(getActivity()).ˏ(2130837643).ˎ(new RoundedTransformation(25)).ˊ(arg10.findViewById(2131624163));
//            arg10.findViewById(2131624391).setText(InviteDialog.currentOffer.getTitle());
//            View v5 = arg10.findViewById(2131624392);
//            ((TextView)v5).setText(InviteDialog.currentOffer.getRequiredActions());
//            ABOFontText.setRobotoLight(((TextView)v5), getActivity());
//            arg10.findViewById(2131624401).setVisibility(4);
//            View v7 = arg10.findViewById(2131624166);
//            if(InviteDialog.currentOffer.getBoostedCredits() > 0 && !InviteDialog.currentOffer.getPromoted().booleanValue()) {
//                v8 = InviteDialog.currentOffer.getBoostedCredits();
//            }
//            else if(InviteDialog.currentOffer.getUserCredits() > 0) {
//                v8 = InviteDialog.currentOffer.getUserCredits();
//            }
//            else {
//                v8 = InviteDialog.currentOffer.getCredits();
//            }
//
//            ((TextView)v7).setText(String.valueOf(v8));
//            ABOFontText.setRobotoMedium(((TextView)v7), getActivity());
//        }
//        else {
//            closeDialog(false);
//        }
    }

    public static InviteDialog newInstance(ShareTask task, int arg2) {
        InviteDialog v0 = new InviteDialog();
        currentOffer = task;
        currentPosition = arg2;
        return v0;
    }

    public View onCreateView(LayoutInflater arg13, ViewGroup arg14, Bundle arg15) {
        defaultDialogSettings();
        return  null;
//        View v3 = arg13.inflate(2130903163, arg14);
//        dialogLayout = v3.findViewById(2131624149);
//        dialogLayout.setVisibility(View.INVISIBLE);
//        headerLayout = v3.findViewById(2131624150);
//        headerLayout.setVisibility(View.INVISIBLE);
////        ABOFontText.setRobotoLight(v3.findViewById(2131624151), getActivity());
//        footerLayout = v3.findViewById(2131624126);
//        footerLayout.setY(((float)getScreenHeight()));
//        iconBg = v3.findViewById(2131624146);
//        iconBg.setY(-2000f);
//        ArrayList v5 = new ArrayList();
//        ((List)v5).add(new RoundedTransformation(25));
//        ((List)v5).add(new BlurTransformation(getActivity()));
//        inviteLayout = v3.findViewById(2131624152);
//        inviteLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                if(animated) {
//                    inviteLayout.getViewTreeObserver().removeGlobalOnLayoutListener(((ViewTreeObserver.OnGlobalLayoutListener)this));
//                }
//                else {
//                    animated = true;
//                    int[] v3 = new int[2];
//                    inviteLayout.getLocationOnScreen(v3);
//                    headerLayout.setY(((float)(v3[1] - statusBarHeight())));
//                    animateOfferPos(inviteLayout.getY() - (((float)statusBarHeight())) + (((float)inviteLayout.getHeight())));
//                }
//            }
//        });
//        offerItem = v3.findViewById(2131624154);
//        offerItem.setY(((float)(InviteDialog.currentPosition + statusBarHeight())));
//        fillOffer(offerItem);
//        textOfferLayout = offerItem.findViewById(2131624390);
//        creditsOfferLayout = offerItem.findViewById(2131624164);
//        closeButton = v3.findViewById(2131624148);
//        closeButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg3) {
//                closeDialog(false);
//            }
//        });
//        detailsLayout = v3.findViewById(2131624155);
//        detailsLayout.setVisibility(View.INVISIBLE);
//        detailsLayoutContent = v3.findViewById(2131624183);
//        View v6 = detailsLayoutContent.findViewById(2131624172);
//        ((LinearLayout)v6).findViewById(2131624518).setImageResource(2130837787);
//        ((LinearLayout)v6).findViewById(2131624519).setText(2131165505);
//        View v9 = detailsLayoutContent.findViewById(2131624184);
//        ((LinearLayout)v9).findViewById(2131624518).setImageResource(2130837811);
//        ((LinearLayout)v9).findViewById(2131624519).setText(2131165507);
//        inviteCodeEt = v3.findViewById(2131624185);
//        if(AppUser.getInstance() != null) {
//            inviteCodeEt.setText(AppUser.getInstance().getInviteCode());
//        }
//
////        ABOFontText.setRobotoMedium(inviteCodeEt, getActivity());
//        inviteCodeEt.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg7) {
//                String v2 = ShareActions.getMyInviteUrl();
//                ClipboardManager v3 = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData v4 = ClipData.newPlainText("AppBounty invite", ((CharSequence)v2));
//                if(v4 != null && v3 != null) {
//                    ((ClipboardManager)v3).setPrimaryClip(v4);
//                    AlertDialog.Builder v5 = new AlertDialog.Builder(getActivity(), 16974130);
//                    v5.setTitle("Success");
//                    v5.setMessage("Your invite code has been copied to your clipboard!");
//                    v5.setNegativeButton("OK", null);
//                    v5.show();
//                }
//            }
//        });
//        facebookButton = v3.findViewById(2131624170);
//        facebookButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg3) {
//                new ShareActions(getActivity());
//                ShareActions.shareFacebook();
//            }
//        });
//        twitterButton = v3.findViewById(2131624186);
//        twitterButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg3) {
//                new ShareActions(getActivity());
//                ShareActions.shareTwitter();
//            }
//        });
//        whatsappButton = v3.findViewById(2131624187);
//        whatsappButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg3) {
//                new ShareActions(getActivity());
//                ShareActions.shareWhatsApp();
//            }
//        });
//        mailButton = v3.findViewById(2131624188);
//        mailButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg3) {
//                new ShareActions(getActivity());
//                ShareActions.shareMail();
//            }
//        });
//        return v3;
    }
}
