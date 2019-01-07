package com.mobile.earnings.main.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseFragment;
import com.mobile.earnings.utils.Constantaz;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class ReferralScreen extends BaseFragment{

	public static final int FIRST_PAGE = 0;
	private static final String EXTRA_PAGE = "page_number";

	@BindView(R.id.referalScreen_firstPage)
	TextView firstPage;
	@BindView(R.id.referalScreen_secondPage)
	TextView secondPage;

	public static ReferralScreen getInstance(int page){
		ReferralScreen fragment = new ReferralScreen();
		Bundle args = new Bundle();
		args.putInt(EXTRA_PAGE, page);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		View root = inflater.inflate(R.layout.fragment_referrals, container, false);
		ButterKnife.bind(this, root);
		int selectedPage = getArguments().getInt(EXTRA_PAGE);
		if(selectedPage == FIRST_PAGE)
			openFirstTab();
		else
			openSecondTab();
		return root;
	}

	@OnClick(R.id.referalScreen_firstPage)
	void onFirstPageClick(){
		openFirstTab();
	}

	@OnClick(R.id.referalScreen_secondPage)
	void onSecondPageClick(){
		openSecondTab();
	}

	private void openFirstTab(){
		firstPage.setActivated(true);
		firstPage.setTextColor(getResources().getColor(android.R.color.black));
		secondPage.setActivated(false);
		secondPage.setTextColor(getResources().getColor(R.color.primaryText));
		String promoCode = App.getPrefs().getString(Constantaz.PREFS_FIRST_PROMO, "");
		replaceReferralFragment(ReferralFragment.getInstance(false, promoCode));
	}

	private void openSecondTab(){
		firstPage.setActivated(false);
		firstPage.setTextColor(getResources().getColor(R.color.primaryText));
		secondPage.setActivated(true);
		secondPage.setTextColor(getResources().getColor(android.R.color.black));
		String promoCode = App.getPrefs().getString(Constantaz.PREFS_SECOND_PROMO, "");
		replaceReferralFragment(ReferralFragment.getInstance(true, promoCode));
	}
}
