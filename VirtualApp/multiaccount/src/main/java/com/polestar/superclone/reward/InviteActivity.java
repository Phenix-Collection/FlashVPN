package com.polestar.superclone.reward;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.superclone.R;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.widgets.IconFontTextView;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.ReferTask;
import com.polestar.task.database.datamodels.ShareTask;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;

/**
 * Created by guojia on 2019/1/23.
 */

public class InviteActivity extends Activity implements ITaskStatusListener {
    private EditText codeInput;
    private View submitLayout;
    private View inviteLayout;
    private TextView submitTitle;
    private TextView submitDesc;
    private TextView inviteTitle;
    private TextView inviteDesc;
    private IconFontTextView submitButton;
    private TextView inviteCoins;
    private TextView inviteCode;

    private ShareTask inviteTask;
    private ReferTask submitCodeTask;

    private AppUser appUser;
    private ShareActions shareActions;

    public static void start(Activity activity){
        Intent intent = new Intent();
        intent.setClass(activity, InviteActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_layout);
        EventReporter.rewardEvent("enter_invite");
        initView();
        initData();
    }

    private void initView() {
        codeInput = findViewById(R.id.text_input_invite_code);
        submitLayout = findViewById(R.id.submit_code_layout);
        submitTitle = findViewById(R.id.submit_title);
        submitDesc = findViewById(R.id.submit_description);
        inviteTitle = findViewById(R.id.invite_title);
        inviteDesc = findViewById(R.id.invite_description);
        inviteCoins = findViewById(R.id.invite_reward);
        inviteCode = findViewById(R.id.invite_code);
        submitButton = findViewById(R.id.submit_button);
        IconFontTextView icon = findViewById(R.id.task_icon);
        icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, getResources().getColor(R.color.share_task_btn));
    }

    private void initData() {
        appUser = AppUser.getInstance();
        inviteTask = appUser.getInviteTask();
        submitCodeTask = appUser.getReferTask();
        shareActions = new ShareActions(this, inviteTask);
        int submitStatus = TaskExecutor.checkTask(submitCodeTask);
        if (submitStatus != RewardErrorCode.TASK_OK) {
            if (submitStatus == RewardErrorCode.TASK_CODE_ALREADY_SUBMITTED) {
                submitButton.setTextColor(getResources().getColor(R.color.reward_done));
                submitButton.setText(R.string.iconfont_done);
                codeInput.setText(appUser.getReferrerCode());
                codeInput.setEnabled(false);
                submitTitle.setText(submitCodeTask.mTitle);
                submitDesc.setText(submitCodeTask.mDescription);
            } else {
                submitLayout.setVisibility(View.GONE);
            }
        } else {
            submitTitle.setText(submitCodeTask.mTitle);
            submitDesc.setText(submitCodeTask.mDescription);
        }
        if (submitStatus != RewardErrorCode.TASK_CODE_ALREADY_SUBMITTED) {
            String hint = TaskPreference.getReferredHint();
            if (!TextUtils.isEmpty(hint) && hint.length() < 16){
                codeInput.setText(hint);
            }
        }
        inviteTitle.setText(inviteTask.mTitle);
        inviteDesc.setText(inviteTask.mDescription);
        inviteCoins.setText("+"+(int)inviteTask.mPayout);
        inviteCode.setText(appUser.getInviteCode());

    }

    public void onSubmitClick(View view) {
        new TaskExecutor(this).submitInviteCode(submitCodeTask, codeInput.getText().toString(), this);
    }

    public void onFacebookClick(View view) {
        copyCode();
        shareActions.shareFacebook();
    }
    public void onTwitterClick(View view) {
        copyCode();
        shareActions.shareTwitter();
    }
    public void onWhatsAppClick(View view) {
        copyCode();
        shareActions.shareWhatsApp();
    }
    public void onMoreClick(View view) {
        copyCode();
        shareActions.shareWithFriends("");
    }

    public void onMailClick(View view) {
        copyCode();
        shareActions.shareMail();
    }

    private void copyCode() {
        if(shareActions.copy(false)) {
            Toast.makeText(this, R.string.invite_copied, Toast.LENGTH_SHORT).show();
        }
    }
    public void onCopyClick(View view) {
        copyCode();
    }

    public void onCloseClick(View View) {
        finish();
    }

    @Override
    public void onTaskSuccess(long taskId, float payment, float balance) {
//        initData();
        toast(RewardErrorCode.TASK_SUBMIT_CODE_OK);
        appUser.setReferrerCode(codeInput.getText().toString());
        submitButton.setTextColor(getResources().getColor(R.color.reward_done));
        submitButton.setText(R.string.iconfont_done);
        codeInput.setEnabled(false);
    }

    @Override
    public void onTaskFail(long taskId, ADErrorCode code) {
//        initData();
        toast(code.getErrCode());
    }

    @Override
    public void onGetAllAvailableTasks(ArrayList<Task> tasks) {

    }

    @Override
    public void onGeneralError(ADErrorCode code) {
//        initData();
        toast(code.getErrCode());
    }

    private void toast(int code, Object... args){
        Toast.makeText(this, RewardErrorCode.getToastMessage(code, args), Toast.LENGTH_SHORT).show();
    }
}
