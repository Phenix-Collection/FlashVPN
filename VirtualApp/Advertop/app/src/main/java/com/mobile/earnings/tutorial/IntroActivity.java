package com.mobile.earnings.tutorial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.main.MainActivity;
import com.mobile.earnings.tutorial.adapter.IntroAdapter;
import com.mobile.earnings.utils.Constantaz;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends BaseActivity implements ViewPager.OnPageChangeListener{

	@BindView(R.id.introViewPager)
	ViewPager pager;
	@BindView(R.id.introPagerFirstDot)
	ImageView firstDot;
	@BindView(R.id.introPagerSecDot)
	ImageView secondDot;
	@BindView(R.id.introPagerThirdDot)
	ImageView thirdDot;
	@BindView(R.id.introPagerFourthDot)
	ImageView fourthDot;
	@BindView(R.id.introPagerFifthDot)
	ImageView fifthDot;
	@BindView(R.id.introBut)
	Button    butStart;
	@BindView(R.id.bg_intro)
	FrameLayout screenBg;
	private int position = 0;

	public static Intent getIntroIntent(@NonNull Context context){
		return new Intent(context, IntroActivity.class);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		ButterKnife.bind(this);
		IntroAdapter adapter = new IntroAdapter(getSupportFragmentManager());
		pager.setOffscreenPageLimit(1);
		pager.addOnPageChangeListener(this);
		pager.setAdapter(adapter);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){

	}

	@Override
	public void onPageSelected(int position){
		this.position = position;
		switch(position){
			case 0:
				firstDot.setImageResource(R.drawable.selected_page_dot);
				secondDot.setImageResource(R.drawable.unselected_page_dot);
				thirdDot.setImageResource(R.drawable.unselected_page_dot);
				fourthDot.setImageResource(R.drawable.unselected_page_dot);
				fifthDot.setImageResource(R.drawable.unselected_page_dot);
//				butStart.setBackgroundResource(R.drawable.selector_intro_but);
//				butStart.setTextColor(App.getContext().getResources().getColor(android.R.color.white));
				butStart.setText(R.string.intro_next_but);
				screenBg.setBackgroundColor(App.getContext().getResources().getColor(R.color.colorFirstSlide));
				break;
			case 1:
				firstDot.setImageResource(R.drawable.unselected_page_dot);
				secondDot.setImageResource(R.drawable.selected_page_dot);
				thirdDot.setImageResource(R.drawable.unselected_page_dot);
				fourthDot.setImageResource(R.drawable.unselected_page_dot);
				fifthDot.setImageResource(R.drawable.unselected_page_dot);
//				butStart.setBackgroundResource(R.drawable.selector_intro_but);
//				butStart.setTextColor(App.getContext().getResources().getColor(android.R.color.white));
				butStart.setText(R.string.intro_next_but);
				screenBg.setBackgroundColor(App.getContext().getResources().getColor(R.color.colorSecondSlide));
				break;
			case 2:
				firstDot.setImageResource(R.drawable.unselected_page_dot);
				secondDot.setImageResource(R.drawable.unselected_page_dot);
				thirdDot.setImageResource(R.drawable.selected_page_dot);
				fourthDot.setImageResource(R.drawable.unselected_page_dot);
				fifthDot.setImageResource(R.drawable.unselected_page_dot);
//				butStart.setBackgroundResource(R.drawable.selector_intro_but);
//				butStart.setTextColor(App.getContext().getResources().getColor(android.R.color.white));
				butStart.setText(R.string.intro_next_but);
				screenBg.setBackgroundColor(App.getContext().getResources().getColor(R.color.colorThirdSlide));
				break;
			case 3:
				firstDot.setImageResource(R.drawable.unselected_page_dot);
				secondDot.setImageResource(R.drawable.unselected_page_dot);
				thirdDot.setImageResource(R.drawable.unselected_page_dot);
				fourthDot.setImageResource(R.drawable.selected_page_dot);
				fifthDot.setImageResource(R.drawable.unselected_page_dot);
//				butStart.setBackgroundResource(R.drawable.selector_intro_but);
//				butStart.setTextColor(App.getContext().getResources().getColor(android.R.color.white));
				butStart.setText(R.string.intro_next_but);
				screenBg.setBackgroundColor(App.getContext().getResources().getColor(R.color.colorFourthSlide));
				break;
			case 4:
				firstDot.setImageResource(R.drawable.unselected_page_dot);
				secondDot.setImageResource(R.drawable.unselected_page_dot);
				thirdDot.setImageResource(R.drawable.unselected_page_dot);
				fourthDot.setImageResource(R.drawable.unselected_page_dot);
				fifthDot.setImageResource(R.drawable.selected_page_dot);
//				butStart.setBackgroundResource(R.drawable.selector_gray_clickable_but);
//				butStart.setTextColor(App.getContext().getResources().getColor(R.color.primaryText));
				butStart.setText(R.string.register_but_text);
				screenBg.setBackgroundColor(App.getContext().getResources().getColor(R.color.colorFifthSlide));
				break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int state){

	}

	@OnClick(R.id.introBut)
	void onButClick(){
		if(position != 4) {
			pager.setCurrentItem(position + 1);
		} else{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
			App.getPrefs().edit().putBoolean(Constantaz.PREFS_TUTORIAL, true).apply();
		}
	}

}
