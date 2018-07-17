package com.example.bluelight.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.bluelight.R;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {
    private PagerAdapter adapter;
    private List<View> helpList;
    private ImageView[] imageViews;
    private ViewPager viewPager;

    public HelpActivity() {
        super();
        this.helpList = new ArrayList<>();
        this.imageViews = new ImageView[3];
        this.adapter = new MyPageAdapter();
    }

    public static void startHelp(Context arg2) {
        arg2.startActivity(new Intent(arg2, HelpActivity.class));
    }

    List<View> getViewList() {
        return this.helpList;
    }

    public void initViews() {
        this.viewPager = this.findViewById(R.id.viewpager);
        this.imageViews[0] = this.findViewById(R.id.pager_index_0);
        this.imageViews[1] = this.findViewById(R.id.pager_index_1);
        this.imageViews[2] = this.findViewById(R.id.pager_index_2);
        this.setHelpLayouts();
        this.setupAdapter();
    }

    protected void onCreate(@Nullable Bundle arg2) {
        super.onCreate(arg2);
        this.setContentView(R.layout.activity_help);
        this.initViews();
    }

    public void setChecked(int arg4) {
        int v0;
        for (v0 = 0; v0 < this.imageViews.length; ++v0) {
            this.imageViews[v0].setImageResource(R.drawable.ic_pager_index);
        }

        this.imageViews[arg4].setImageResource(R.drawable.ic_pager_index_checked);
    }

    private void setHelpLayouts() {
        LayoutInflater v0 = LayoutInflater.from(((Context) this));
        View v2 = v0.inflate(R.layout.layout_help_0, null);
        View v3 = v0.inflate(R.layout.layout_help_1, null);
        View v1 = v0.inflate(R.layout.layout_help_2, null);
        this.helpList.add(v2);
        this.helpList.add(v3);
        this.helpList.add(v1);
        this.helpList.add(new View(((Context) this)));
    }

    private void setupAdapter() {
        this.viewPager.setAdapter(this.adapter);
        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg1) {
            }

            public void onPageScrolled(int arg1, float arg2, int arg3) {
            }

            public void onPageSelected(int arg4) {
                if (arg4 < HelpActivity.this.getViewList().size() - 1) {
                    HelpActivity.this.setChecked(arg4);
                } else if (arg4 == HelpActivity.this.getViewList().size() - 1) {
                    HelpActivity.this.finish();
                    HelpActivity.this.startActivity(new Intent(HelpActivity.this, MainActivity.class));
                }
            }
        });
    }

    class MyPageAdapter extends PagerAdapter {
        public void destroyItem(@NonNull ViewGroup arg2, int arg3, @NonNull Object arg4) {
            arg2.removeView(HelpActivity.this.getViewList().get(arg3));
        }

        public int getCount() {
            return HelpActivity.this.getViewList().size();
        }

        public Object instantiateItem(@NonNull ViewGroup arg2, int arg3) {
            arg2.addView(HelpActivity.this.getViewList().get(arg3));
            return HelpActivity.this.getViewList().get(arg3);
        }

        public boolean isViewFromObject(@NonNull View arg2, @NonNull Object arg3) {
            boolean v0 = arg2 == arg3 ? true : false;
            return v0;
        }
    }
}

