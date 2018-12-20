package com.polestar.booster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;


public class BoosterActivity extends Activity {

    public static final String SHORT_CUT = "short_cut";
    public static final String SLOT_ID = "slot_id";

    private boolean mShortcut;
    private String mSlotId;
    private static CleanActivityListener mListener;
    private String from;

    public static void startCleanActivity(Context context, boolean shortcut, CleanActivityListener listener, String from) {
        try {
            Intent intent = new Intent(context, BoosterActivity.class);
            intent.putExtra(SHORT_CUT, shortcut);
            intent.putExtra(BoosterSdk.EXTRA_SHORTCUT_CLICK_FROM, from);
            intent.putExtra(SLOT_ID, BoosterSdk.boosterConfig.boostAdSlot);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
            mListener = listener;
        }
        catch (Exception e)
        {
        }
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booster_view);
        if (!initData()) {
            finish();
        }
        initView();
        BoosterSdk.useRealUserPresent(true);
        if (mListener != null) {
            mListener.onActivityCreate(this);
        }
        BoosterLog.boostEnter(from);
    }

    private boolean initData() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        this.mShortcut = intent.getBooleanExtra("short_cut", false);
        this.mSlotId = intent.getStringExtra("slot_id");

        from = intent.getStringExtra(BoosterSdk.EXTRA_SHORTCUT_CLICK_FROM);

        if ((this.mSlotId == null) ) {
            return false;
        }
        return true;
    }

    private void initView() {
        LinearLayout contentView = (LinearLayout)findViewById(R.id.cleanersdk_activity_clean_content);
        BoostView boostView = new BoostView(this, mShortcut, mSlotId, sCleanerViewListener);
        boostView.loadAd();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        contentView.addView(boostView, lp);
    }

    private BoostView.CleanerViewListener sCleanerViewListener = new BoostView.CleanerViewListener() {

        public void closeViewCallback() {
            finish();
        }
    };

    public void onBackPressed() {

    }

    public static abstract interface CleanActivityListener {

        public abstract void onActivityCreate(Activity paramActivity);

    }

}
