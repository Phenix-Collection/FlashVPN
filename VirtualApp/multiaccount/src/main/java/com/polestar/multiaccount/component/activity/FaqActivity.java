package com.polestar.multiaccount.component.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;

/**
 * Created by guojia on 2016/12/10.
 */

public class FaqActivity extends BaseActivity {
    private Context mContext;

    private void initView(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_layout);
        setTitle(getResources().getString(R.string.faq));
        mContext = this;
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
