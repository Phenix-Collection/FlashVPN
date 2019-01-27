package com.polestar.task.network.datamodels;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.datamodels.AdTask;
import com.polestar.task.database.datamodels.CheckInTask;
import com.polestar.task.database.datamodels.ReferTask;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.database.datamodels.ShareTask;
import com.polestar.task.network.MiscUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Task extends TimeModel {
    /**
     * $table->integer('task_type');
     $table->string('description');
     $table->integer('status')->default(0);
     $table->integer('rank')->default(0); //用户级别超过一定值可以玩
     $table->decimal('payout', 5, 2)->default(0);
     $table->integer('limit_total')->default(1);
     $table->integer('limit_per_day')->default(1);
     $table->string('detail',10000)->nullable();
     $table->integer('days')->default(5);

     $table->integer('country_id')->nullable();
     $table->integer('lang_id')->nullable();

     $table->bigInteger('created_by')->unsigned();
     */

    public static final int TASK_TYPE_AD_TASK = 1;
    public static final int TASK_TYPE_CHECKIN_TASK = 2;
    public static final int TASK_TYPE_REWARDVIDEO_TASK = 3;
    public static final int TASK_TYPE_REFER_TASK = 4; //主动提交referralCode的提交人的奖励
    public static final int TASK_TYPE_REFERREE_TASK = 5; //被动提交referralCode的推荐人的奖励
    public static final int TASK_TYPE_SHARE_TASK = 6;

    @SerializedName("id")
    public long mId;
    //任务自己的描述
    @SerializedName("task_type")
    public int mTaskType;
    @SerializedName("description")
    public String mDescription;
    @SerializedName("status")
    public int mStatus;
    @SerializedName("rank")
    public int mRank;
    @SerializedName("payout")
    public float mPayout;
    @SerializedName("limit_total")
    public int mLimitTotal;
    @SerializedName("limit_per_day")
    public int mLimitPerDay;
    @SerializedName("end_time")
    public String mEndTime;
    @SerializedName("detail")
    public String mDetail;

    public Task(Task task) {
        mId = task.mId;
        mTaskType = task.mTaskType;
        mDescription = task.mDescription;
        mStatus = task.mStatus;
        mRank = task.mRank;
        mPayout = task.mPayout;
        mLimitPerDay = task.mLimitPerDay;
        mLimitTotal = task.mLimitTotal;
        mEndTime = task.mEndTime;
        mDetail = task.mDetail;

        parseDetailInfo();
    }

    public long endTime;

    private AdTask mAdTask;
    private CheckInTask mCheckInTask;
    private RewardVideoTask mRewardVideoTask;
    private ShareTask mShareTask;
    private ReferTask mReferTask;

    public boolean isAdTask() {
        return mTaskType == TASK_TYPE_AD_TASK;
    }

    public AdTask getAdTask() {
        if (isAdTask()) {
            if (mAdTask == null) {
                mAdTask = new AdTask(this);
            }
            if (!mAdTask.parseDetailInfo()) {
                Log.e(DatabaseApi.TAG, "Failed to parse " + mDetail + " to ADTask");
                return null;
            }
            return mAdTask;
        } else {
            return null;
        }
    }

    public boolean isCheckInTask() {
        return mTaskType == TASK_TYPE_CHECKIN_TASK;
    }

    public CheckInTask getCheckInTask() {
        if (isCheckInTask()) {
            if (mCheckInTask == null) {
                mCheckInTask = new CheckInTask(this);
            }
            if (!mCheckInTask.parseDetailInfo()) {
                Log.e(DatabaseApi.TAG, "Failed to parse " + mDetail + " to CheckInTask");
                return null;
            }
            return mCheckInTask;
        } else {
            return null;
        }
    }

    public boolean isRewardVideoTask() {
        return mTaskType == TASK_TYPE_REWARDVIDEO_TASK;
    }

    public RewardVideoTask getRewardVideoTask() {
        if (isRewardVideoTask()) {
            if (mRewardVideoTask == null) {
                mRewardVideoTask = new RewardVideoTask(this);
            }
            if (!mRewardVideoTask.parseDetailInfo()) {
                Log.e(DatabaseApi.TAG, "Failed to parse " + mDetail + " to RewardVideoTask");
                return null;
            }
            return mRewardVideoTask;
        } else {
            return null;
        }
    }

    public boolean isShareTask() {
        return mTaskType == TASK_TYPE_SHARE_TASK;
    }

    public ShareTask getShareTask() {
        if (isShareTask()) {
            if (mShareTask == null) {
                mShareTask = new ShareTask(this);
            }
            if (!mShareTask.parseDetailInfo()) {
                Log.e(DatabaseApi.TAG, "Failed to parse " + mDetail + " to ShareTask");
                return null;
            }
            return mShareTask;
        } else {
            return null;
        }
    }

    public boolean isReferTask() {
        return mTaskType == TASK_TYPE_REFER_TASK;
    }

    public ReferTask getReferTask() {
        if (isReferTask()) {
            if (mReferTask == null) {
                mReferTask = new ReferTask(this);
            }
            if (!mReferTask.parseDetailInfo()) {
                Log.e(DatabaseApi.TAG, "Failed to parse " + mDetail + " to ReferTask");
                return null;
            }
            return mReferTask;
        } else {
            return null;
        }
    }

    /* 目前不需要
    //用户获取的具体的任务的情况
    @SerializedName("complete_status")
    public String mCompleteStatus;
    @SerializedName("version_code")
    public int mVersionCode;
    @SerializedName("end_time")
    public String mEndTime;*/


    // return false if detail info not valid
    public boolean parseDetailInfo() {
        endTime = MiscUtils.getTimeInMilliSecondsFromUTC(mEndTime);
        if (mDetail == null || mDetail.isEmpty()) {
            return false;
        }

        try {
            JSONObject jsonObject = new JSONObject(mDetail);
            return parseTaskDetail(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean parseTaskDetail(JSONObject jsonObject) {
        return false;
    }

    /**
     * Whether it's a valid task
     * @return
     */
    protected boolean isValid(){
        return mLimitPerDay > 0 && mLimitTotal > 0 && mLimitPerDay < mLimitTotal
                && mPayout >= 0;
    }

    /**
     * Whether the task is effective and can be rewarded
     * @return
     */
    public boolean isEffective() {
        return isValid() && System.currentTimeMillis() < endTime;
    }
}
