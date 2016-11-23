package com.polestar.multiaccount.component.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.net.HttpUtil;
import com.polestar.multiaccount.utils.ToastUtils;

public class FeedbackActivity extends BaseActivity {

    private Context mContext;
    private EditText mEtFeedback;
    private EditText mEtEmail;
    private Button mBtSubmit;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mContext = this;
        initView();
    }

    private void initView() {
        setTitle(Constants.TITLE_FEEDBACK);

        mProgressBar = (ProgressBar) findViewById(R.id.fb_progressbar);
        mEtFeedback = (EditText) findViewById(R.id.et_feedback);
        mEtEmail = (EditText) findViewById(R.id.et_email);
        mBtSubmit = (Button) findViewById(R.id.bt_submit);
        mBtSubmit.setEnabled(false);

        mEtFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    mBtSubmit.setEnabled(true);
                } else {
                    mBtSubmit.setEnabled(false);
                }
            }
        });

        mBtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEtFeedback.getText().toString();
                if (content == null) {
                    ToastUtils.ToastDefult(mContext, "Description can not be empty!");
                    return;
                }
                String email = mEtEmail.getText().toString();
                showProgressBar();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result = HttpUtil.submitFeedback(mContext, content, email);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressBar();
                                if (result == 0) {
                                    ToastUtils.ToastDefult(mContext, getString(R.string.submit_success));
                                    finish();
                                } else {
                                    ToastUtils.ToastDefult(mContext, getString(R.string.submit_failed));
                                }
                            }
                        });

                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgressBar() {
        mEtFeedback.setEnabled(false);
        mEtEmail.setEnabled(false);
        mBtSubmit.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mEtFeedback.setEnabled(true);
        mEtEmail.setEnabled(true);
        mBtSubmit.setEnabled(true);
    }

}
