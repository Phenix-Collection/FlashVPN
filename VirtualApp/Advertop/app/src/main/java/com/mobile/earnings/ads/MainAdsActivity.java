package com.mobile.earnings.ads;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.ads.fragments.PartnerAdFragment;
import com.mobile.earnings.api.BaseActivity;
import com.fyber.Fyber;

import butterknife.ButterKnife;

import static com.mobile.earnings.utils.Constantaz.FYBER_APP_ID;
import static com.mobile.earnings.utils.Constantaz.FYBER_SECURITY_TOKEN;


public class MainAdsActivity extends BaseActivity{

	private static final String EXTRA_IS_VIDEO = "is.video.extra";

	public static Intent getLaunchIntent(Context context, boolean isVideoSelected){
		Intent intent = new Intent(context, MainAdsActivity.class);
		intent.putExtra(EXTRA_IS_VIDEO, isVideoSelected);
		return intent;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ads);
		//Fyber initialization
		Fyber.with(FYBER_APP_ID, this).withUserId(App.getDeviceID()).withSecurityToken(FYBER_SECURITY_TOKEN).start();
		ButterKnife.bind(this);
		boolean isVideoSelected = getIntent().getBooleanExtra(EXTRA_IS_VIDEO, false);
		setSupportActionBar(setToolBar(isVideoSelected ? getString(R.string.toolbar_videoAdTitle) : getString(R.string.toolbar_offerWallTitle)));
		if(getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportFragmentManager().beginTransaction().add(R.id.adsAct_adsContainer, PartnerAdFragment.newInstance(isVideoSelected)).commit();
	}

}
