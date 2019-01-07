package com.mobile.earnings.tutorial.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mobile.earnings.tutorial.slides.IntroFragment;

public class IntroAdapter extends FragmentStatePagerAdapter{

	private static final int INTRO_PAGES_COUNT = 5;

	public IntroAdapter(FragmentManager fm){
		super(fm);
	}

	@Override
	public Fragment getItem(int position){
		return IntroFragment.getInstance(position);
	}

	@Override
	public int getCount(){
		return INTRO_PAGES_COUNT;
	}
}
