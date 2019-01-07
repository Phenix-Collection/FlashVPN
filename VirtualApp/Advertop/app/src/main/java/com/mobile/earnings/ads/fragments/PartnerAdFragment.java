package com.mobile.earnings.ads.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.earnings.R;
import com.mobile.earnings.ads.adapter.AdModel;
import com.mobile.earnings.ads.adapter.AdsAdapter;
import com.mobile.earnings.ads.annotations.ImplementedOfferWallPlatforms;
import com.mobile.earnings.ads.annotations.ImplementedVideoAdPlatforms;
import com.mobile.earnings.main.TaskItemDecorator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mobile.earnings.ads.annotations.ImplementedOfferWallPlatforms.OFFER_WALL_PLATFORMS_COUNT;
import static com.mobile.earnings.ads.annotations.ImplementedVideoAdPlatforms.VIDEO_AD_PLATFORMS_COUNT;
import static com.mobile.earnings.utils.Constantaz.CARD_VIEW_PADDING;



public class PartnerAdFragment extends BaseAdsFragment implements AdsAdapter.OnAdSelectedInterface{

	@BindView(R.id.adsFrag_recyclerView)
	RecyclerView mRecyclerView;
//	@BindView(R.id.adsFrag_headerTv)
//	TextView     listHeaderTv;
	@BindView(R.id.adsFrag_footerTv)
	TextView     listFooterTv;
	@BindView(R.id.adsFrag_headerLayout)
	CardView     headerLayout;
	@BindView(R.id.adsFrag_headerLayoutIcon)
	ImageView    dropDownIcon;
	@BindView(R.id.adsFrag_headerTextLayout)
	CardView     headerTextLayout;

	private boolean mIsAnimationDown = true;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.fragment_ads, container, false);
		ButterKnife.bind(this, rootView);
		initRecyclerView();
		return rootView;
	}

	private void initRecyclerView(){
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.addItemDecoration(new TaskItemDecorator(CARD_VIEW_PADDING));
		mRecyclerView.setHasFixedSize(true);
		AdsAdapter adapter = new AdsAdapter(provideItems());
		adapter.setOnAdSelectedInterface(this);
		mRecyclerView.setAdapter(adapter);
		if(!getArguments().getBoolean(ARGS_IS_VIDEO)) {
			listFooterTv.setText(R.string.offerWallFooterText);
//			listHeaderTv.setVisibility(View.GONE);
		} else{
			headerLayout.setVisibility(View.GONE);
		}
	}

	private ArrayList<AdModel> provideItems(){
		//video resources
		TypedArray icons = getResources().obtainTypedArray(R.array.videoAdIcons);
		String[] titles = getResources().getStringArray(R.array.videoAdPlatforms);
		String[] descriptions = getResources().getStringArray(R.array.videoAdDescriptions);
		//offerWall resources
		TypedArray offerWallIcons = getResources().obtainTypedArray(R.array.offerWallIcons);
		String[] offerWallTitles = getResources().getStringArray(R.array.offerWallPlatforms);
		String[] offerWallDescs = getResources().getStringArray(R.array.offerWallDescriptions);
		ArrayList<AdModel> tempList = new ArrayList<>();
		if(getArguments().getBoolean(ARGS_IS_VIDEO)) {
			for(int i = 0; i < VIDEO_AD_PLATFORMS_COUNT; i++){
				tempList.add(new AdModel(i, icons.getResourceId(i, -1), getString(R.string.videoAdTitle, titles[i]), descriptions[i]));
			}
		} else{
			for(int i = 0; i < OFFER_WALL_PLATFORMS_COUNT; i++){
				tempList.add(new AdModel(i, offerWallIcons.getResourceId(i, -1), getString(R.string.offerWallAdTitle, offerWallTitles[i]), offerWallDescs[i]));
			}
		}
		icons.recycle();
		offerWallIcons.recycle();
		return tempList;
	}

	@OnClick({R.id.adsFrag_headerLayout, R.id.adsFrag_headerTextLayout})
	void openConditions(){
		changeHeaderLayout();
	}

	@Override
	public void onAdSelected(int id){
		if(getArguments().getBoolean(ARGS_IS_VIDEO)) {
			switch(id){
				case ImplementedVideoAdPlatforms.AD_COLONY:
					showAdColonyVideo();
					break;
				case ImplementedVideoAdPlatforms.APP_NEXT:
					showAppNextFullscreenVideo();
					break;
				case ImplementedVideoAdPlatforms.UNITY_AD_VIDEO:
					showUnityAdVideo();
					break;
				case ImplementedVideoAdPlatforms.FYBER_REWARDED_VIDEO:
					showFyberRewardedVideo();
					break;
				case ImplementedVideoAdPlatforms.FACEBOOK:
					showFacebookVideo();
					break;
				default:
					break;
			}
		} else{
			switch(id){
				case ImplementedOfferWallPlatforms.FYBER_OFFER_WALL:
					showFyberOfferWall();
					break;
				case ImplementedOfferWallPlatforms.ADS_CENT_OFFER_WALL:
					showAdsCentOfferWall();
					break;
				case ImplementedOfferWallPlatforms.AD_TRIAL:
					showAdTrial();
					break;
				case ImplementedOfferWallPlatforms.OFFER_TORO:
					showOfferTorro();
					break;
				default:
					break;
			}
		}
	}

	private void changeHeaderLayout(){
		if(mIsAnimationDown) {
			headerLayout.setVisibility(View.GONE);
			headerTextLayout.setVisibility(View.VISIBLE);
			mIsAnimationDown = false;
		} else{
			headerLayout.setVisibility(View.VISIBLE);
			headerTextLayout.setVisibility(View.GONE);
			mIsAnimationDown = true;
		}
	}

}
