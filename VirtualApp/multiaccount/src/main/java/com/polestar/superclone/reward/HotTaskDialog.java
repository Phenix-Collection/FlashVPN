package com.polestar.superclone.reward;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.superclone.R;
import com.polestar.superclone.utils.AnimatorHelper;
import com.polestar.superclone.utils.DisplayUtils;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.IconFontTextView;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.AdTask;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;
import java.util.List;

import static com.polestar.superclone.widgets.IconFontTextView.BG_SHAPE_OVAL;


/**
 * Created by guojia on 2019/1/31.
 */

public class HotTaskDialog extends Dialog {
    private String title;
    private View dialogView;
    private Activity mActivity;
    private GridView mTaskGrid;
    private BaseAdapter mGridAdapter;
    private List<Task> mTaskList;
    private OnClickListener listener;

    private static final String SLOT_HOT_TASK = "slot_hot_dialog";
    private List<IAdAdapter> mAds;

    private HotTaskDialog(@NonNull Activity activity, int styleRes) {
        super(activity, styleRes);
        mActivity = activity;

        dialogView = LayoutInflater.from(activity).inflate(R.layout.hot_task_dialog_layout, null);

        mTaskList = AppUser.getInstance().getRecommendTasks();
        mTaskGrid = dialogView.findViewById(R.id.task_slot_grid);
        mGridAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                int cnt = mTaskList.size() + 1;
                if (mAds != null) {
                    cnt += mAds.size();
                }
                return cnt;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
//                if (convertView == null) {
                    View inflateView = LayoutInflater.from(mActivity).inflate(R.layout.task_slot_item, null);
                    IconFontTextView icon = inflateView.findViewById(R.id.icon);
                    TextView payout = inflateView.findViewById(R.id.payout);
                    TextView title = inflateView.findViewById(R.id.title);
                    MLogs.d("hot item pos " + position);
                    if (position == 0) {
                        icon.setText(R.string.iconfont_crown);
                        icon.setBackgroundShapeDrawable(BG_SHAPE_OVAL, mActivity.getResources().getColor(R.color.vip_btn_color));
                        payout.setText("VIP");
                    } else {
                        if (position - 1 < mTaskList.size()) {
                            Task task = (Task) mTaskList.get(position - 1);
                            title.setText(task.mTitle);
                            payout.setText("+" + (int) task.mPayout);
                            inflateView.setTag(task);
                            switch (task.mTaskType) {
                                case Task.TASK_TYPE_SHARE_TASK:
                                    icon.setText((R.string.iconfont_invite));
                                    icon.setBackgroundShapeDrawable(BG_SHAPE_OVAL, mActivity.getResources().getColor(R.color.share_task_btn));
                                    break;
                                case Task.TASK_TYPE_REWARDVIDEO_TASK:
                                    icon.setText((R.string.iconfont_video));
                                    icon.setBackgroundShapeDrawable(BG_SHAPE_OVAL, Color.parseColor("#4B57C0"));
                                    break;
                            }
                        } else {
                            int adIdx = position - 1 - mTaskList.size();
                            if (adIdx >= 0 && adIdx < mAds.size()) {
                                IAdAdapter adAdapter = mAds.get(adIdx);
                                AdViewBinder viewBinder = new AdViewBinder.Builder(R.layout.adtask_slot_item)
                                        .iconImageId(R.id.icon).build();
                                View adView = adAdapter.getAdView(mActivity,viewBinder);
                                payout = adView.findViewById(R.id.payout);
                                payout.setText("+" + String.format("%.0f", ((AdTask)(adAdapter.getAdObject())).mPayout));
                                inflateView = adView;
                            }
                        }
                    }
//                }
                return inflateView;
            }
        };
        mTaskGrid.setAdapter(mGridAdapter);
        mTaskGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    VIPActivity.start(activity, VIPActivity.FROM_TASK_DIALOG);
                    return;
                }
                Task task = (Task) view.getTag();
                if (task != null) {
                    new TaskExecutor(mActivity).execute(task, new ITaskStatusListener() {
                        @Override
                        public void onTaskSuccess(long taskId, float payment, float balance) {
                            Toast.makeText(mActivity,
                                    RewardErrorCode.getToastMessage(RewardErrorCode.TASK_OK, payment), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onTaskFail(long taskId, ADErrorCode code) {

                        }

                        @Override
                        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {

                        }

                        @Override
                        public void onGeneralError(ADErrorCode code) {

                        }
                    });
                }
            }
        });

        FuseAdLoader.get(SLOT_HOT_TASK, mActivity).loadAdList(mActivity, 4, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {

            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {

            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {
                mAds = ads;
                MLogs.d("hot ad loaded: " + ads.size());
                mGridAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {

            }

            @Override
            public void onRewarded(IAdAdapter ad) {

            }
        });
        setContentView(dialogView);
    }

    @Override
    public void show() {
        ((TextView)dialogView.findViewById(R.id.dialog_title)).setText(title);
        int dialogwidth = DisplayUtils.getScreenWidth(mActivity) * 9 / 10;
        // 设置Dialog的大小
        getWindow().setLayout(dialogwidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);
        setCanceledOnTouchOutside(false);
        try {
            super.show();
        }catch (Exception e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        AnimatorHelper.verticalShowFromBottom(dialogView);
//        return dialog;
//        super.show();
    }

    public static class Builder {
        private HotTaskDialog dialog;
        public Builder(Activity context){
             dialog = new HotTaskDialog(context, R.style.CustomDialog);
        }

        public Builder setTitle(String title) {
            dialog.title = title;
            return this;
        }

        public Builder  setOnClickListener(OnClickListener listener) {
            dialog.listener = listener;
            return this;
        }

        public HotTaskDialog build() {
            return dialog;
        }
    }
}
