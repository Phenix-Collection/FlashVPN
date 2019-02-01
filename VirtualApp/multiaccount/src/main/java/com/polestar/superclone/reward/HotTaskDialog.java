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
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.superclone.R;
import com.polestar.superclone.utils.AnimatorHelper;
import com.polestar.superclone.utils.DisplayUtils;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.IconFontTextView;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by guojia on 2019/1/31.
 */

public class HotTaskDialog extends Dialog {
    private String title;
    private OnClickListener listener;
    private View dialogView;
    private IconFontTextView vipView;
    private Activity mActivity;
    private GridView mTaskGrid;
    private List<Task> mTaskList;

    private HotTaskDialog(@NonNull Activity activity, int styleRes) {
        super(activity, styleRes);
        mActivity = activity;

        dialogView = LayoutInflater.from(activity).inflate(R.layout.hot_task_dialog_layout, null);
        vipView = dialogView.findViewById(R.id.vip);
        vipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VIPActivity.start(activity);
            }
        });
        mTaskList = AppUser.getInstance().getRecommendTasks();
        mTaskGrid = dialogView.findViewById(R.id.task_slot_grid);
        mTaskGrid.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mTaskList.size();
            }

            @Override
            public Object getItem(int position) {
                return mTaskList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return mTaskList.get(position).mId;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mActivity).inflate(R.layout.task_slot_item, null);
                    Task task = (Task) getItem(position);
                    IconFontTextView icon = convertView.findViewById(R.id.icon);
                    TextView title = convertView.findViewById(R.id.title);
                    title.setText(task.mTitle);
                    TextView payout = convertView.findViewById(R.id.payout);
                    payout.setText("+" + (int)task.mPayout);
                    convertView.setTag(task);
                    switch (task.mTaskType) {
                        case Task.TASK_TYPE_SHARE_TASK:
                            icon.setText((R.string.iconfont_invite));
                            icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, Color.parseColor("#fd215c"));
                            break;
                        case Task.TASK_TYPE_REWARDVIDEO_TASK:
                            icon.setText((R.string.iconfont_video));
                            icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, Color.parseColor("#4B57C0"));
                            break;
                    }
                }
                return convertView;
            }
        });
        mTaskGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
        setContentView(dialogView);
    }

    @Override
    public void show() {
        ((TextView)dialogView.findViewById(R.id.dialog_title)).setText(title);
        vipView.setBackgroundColor(Color.parseColor("#fd7921"));

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
