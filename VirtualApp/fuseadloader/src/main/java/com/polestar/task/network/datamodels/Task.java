package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class Task {
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
    @SerializedName("detail")
    public String mDetail;

    /* 目前不需要
    //用户获取的具体的任务的情况
    @SerializedName("complete_status")
    public String mCompleteStatus;
    @SerializedName("version_code")
    public int mVersionCode;
    @SerializedName("end_time")
    public String mEndTime;*/
}
