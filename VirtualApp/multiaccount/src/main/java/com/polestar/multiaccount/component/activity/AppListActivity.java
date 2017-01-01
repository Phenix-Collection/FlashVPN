package com.polestar.multiaccount.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.AppGridAdapter;
import com.polestar.multiaccount.component.adapter.AppListAdapter;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.pbinterface.DataObserver;
import com.polestar.multiaccount.utils.AppListUtils;
import com.polestar.multiaccount.widgets.FixedGridView;
import com.polestar.multiaccount.widgets.FixedListView;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

/**
 * Created by yxx on 2016/7/15.
 */
public class AppListActivity extends BaseActivity implements DataObserver {
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);


    private TextView mTextPopular;
    private TextView mTextMore;
    private FixedListView mListView;
    private FixedGridView mGradView;
    private AppListAdapter mAppListAdapter;
    private AppGridAdapter mAppGridAdapter;
    private List<AppModel> mPopularModels;
    private List<AppModel> mInstalledModels;
    private Context mContext;
    private FrameLayout adContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        mContext = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppListUtils.getInstance(this).registerObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppListUtils.getInstance(this).unregisterObserver(this);
    }

    private void initView() {
        setTitle(getResources().getString(R.string.clone_apps_title));

        mTextPopular = (TextView) findViewById(R.id.text_popular);
        mTextMore = (TextView) findViewById(R.id.text_more);
        mListView = (FixedListView) findViewById(R.id.app_list_popular);
        mGradView = (FixedGridView) findViewById(R.id.app_list_more);
        adContent = (FrameLayout) findViewById(R.id.ad_content);

        mTextMore.setVisibility(View.INVISIBLE);

        mAppListAdapter = new AppListAdapter(mContext);
        mAppGridAdapter = new AppGridAdapter(mContext);
        mListView.setAdapter(mAppListAdapter);
        mGradView.setAdapter(mAppGridAdapter);

        mPopularModels = AppListUtils.getInstance(this).getPopularModels();
        mInstalledModels = AppListUtils.getInstance(this).getInstalledModels();

        if (mPopularModels == null || mPopularModels.size() == 0) {
            mTextPopular.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        } else {
            mAppListAdapter.setModels(mPopularModels);
        }

        if (mInstalledModels == null || mInstalledModels.size() == 0) {
            mTextMore.setVisibility(View.GONE);
            mGradView.setVisibility(View.GONE);
        } else {
            mTextMore.setVisibility(View.VISIBLE);
            showMoreApps();
        }

        mListView.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mInstalledModels.size() != 0) {
                    mTextMore.setVisibility(View.VISIBLE);
                    showMoreApps();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra(AppConstants.EXTRA_APP_MODEL, mPopularModels.get(position));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        mGradView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra(AppConstants.EXTRA_APP_MODEL, mInstalledModels.get(position));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    private void showMoreApps() {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mTextMore, "alpha", 0.0f, 1.0f);
        alpha.setDuration(300);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mInstalledModels.size() != 0) {
                    mAppGridAdapter.setModels(mInstalledModels);
                }
            }
        });
        alpha.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        watch AppListUtils state
     */
    @Override
    public void onChanged() {
        mAppListAdapter.notifyDataSetChanged();
        mAppGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInvalidated() {

    }
}
