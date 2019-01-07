package com.mobile.earnings.main.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseFragment;
import com.mobile.earnings.api.modules.SharingModule;
import com.mobile.earnings.main.presenterImpls.ReferralPresenterImpl;
import com.mobile.earnings.main.views.ReferralView;
import com.mobile.earnings.utils.Constantaz;
import com.mobile.earnings.utils.ReportEvents;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.yandex.metrica.YandexMetrica;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class ReferralFragment extends BaseFragment implements ReferralView{

	@BindView(R.id.referralFrag_ActiveReferralsLayout)
	LinearLayout activeReferralsLayout;
	@BindView(R.id.referralFrag_promoCodeTV)
	TextView     promoCodeTV;
	@BindView(R.id.referralFrag_referralCount)
	TextView     referralCountTV;
	@BindView(R.id.referralFrag_activeReferralCountTV)
	TextView     activeReferralCountTV;
	@BindView(R.id.referralFrag_referralBalance)
	TextView     referralBalance;
	@BindView(R.id.referralFrag_refInfoTV)
	TextView referralInfoTV;

	public static final String EXTRA_ACTIVE_REFERRALS = "active_referrals";
	public static final String EXTRA_PROMO_CODE       = "promo_code";

	private ReferralPresenterImpl presenter;
	private String                promoCode;
	private CallbackManager callbackManager;
	private ShareDialog     shareDialog;
	private boolean         isActive;
	private Activity activity;

	public static ReferralFragment getInstance(boolean showActive, String promoCode){
		ReferralFragment fragment = new ReferralFragment();
		Bundle args = new Bundle();
		args.putBoolean(EXTRA_ACTIVE_REFERRALS, showActive);
		args.putString(EXTRA_PROMO_CODE, promoCode);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		presenter = new ReferralPresenterImpl(this);
		activity = getActivity();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		View root = inflater.inflate(R.layout.fragment_first_referal, container, false);
		ButterKnife.bind(this, root);
		isActive = getArguments().getBoolean(EXTRA_ACTIVE_REFERRALS);
		revealActiveReferralLayout(isActive);
		promoCode = getArguments().getString(EXTRA_PROMO_CODE);
		promoCodeTV.setText(promoCode);
		presenter.getDataFromServer(isActive);
		if(isActive) {
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REF_2);
			referralInfoTV.setText(activity.getString(R.string.secondsReferralFrag_title));
		}else{
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REF_1);
			referralInfoTV.setText(activity.getString(R.string.firstReferralFrag_title));
		}
		FacebookSdk.sdkInitialize(App.getContext());
		callbackManager = CallbackManager.Factory.create();
		shareDialog = new ShareDialog(this);
		return root;
	}

	private void revealActiveReferralLayout(boolean reveal){
		if(reveal)
			activeReferralsLayout.setVisibility(View.VISIBLE);
		else
			activeReferralsLayout.setVisibility(View.GONE);
	}

	@OnClick(R.id.referralFrag_copyCodeBut)
	public void copyPromoCode(){
		if(isActive)
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_PROMO2);
		else
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_PROMO1);
		presenter.copyCodeToBuffer(getActivity(), promoCode);
	}

	@OnClick(R.id.referralFrag_FacebookShareBut)
	void facebookShare(){
		if(isActive)
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REPOST_FB2);
		else
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REPOST_FB1);
		setUpFBSharing();
		ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentTitle(getResources().getString(R.string.app_name)).setContentDescription(String.format(getResources().getString(R.string.share_message), promoCode)).setContentUrl(Uri.parse(getResources().getString(R.string.share_link))).build();
		shareDialog.show(linkContent);
	}

	@OnClick(R.id.referralFrag_GoogleShareBut)
	void googleShare(){
		if(isActive)
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REPOST_GOOGLE2);
		else
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REPOST_GOOGLE1);
		startActivityForResult(SharingModule.shareThroughGoogle(getActivity(), String.format(getResources().getString(R.string.share_message), promoCode), Uri.parse(getResources().getString(R.string.share_link))), 456);
	}

	@OnClick(R.id.referralFrag_VkShareBut)
	void vkShare(){
		if(isActive)
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REPOST_VK2);
		else
			YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_REPOST_VK1);
		SharingModule.shareThroughVkifLoggedIn(getActivity(), String.format(getResources().getString(R.string.share_message), promoCode) + getResources().getString(R.string.share_link), Constantaz.VK_SCOPE);
	}

	@Override
	public void setData(int activeRefCount, int referralCount, String referralsBalance, String currency){
		activeReferralCountTV.setText(activity.getString(R.string.secondReferralFrag_activeTitle, activeRefCount));
		referralCountTV.setText(activity.getString(R.string.firstReferralFrag_referralCountTitle, referralCount));
		referralBalance.setText(activity.getString(R.string.referralFrag_referralBalance, referralsBalance).concat(currency));
		promoCodeTV.setText(promoCode);
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	public void setUpFBSharing(){
		shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>(){
			@Override
			public void onSuccess(Sharer.Result result){
				Toast.makeText(App.getContext(), App.getContext().getString(R.string.referralFrag_shareToast), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel(){

			}

			@Override
			public void onError(FacebookException error){
				Log.e("FACEBOOK", "Error: " + error.getMessage());
				Toast.makeText(App.getContext(), App.getContext().getString(R.string.referralFrag_shareExceptionToast), Toast.LENGTH_SHORT).show();
			}
		});
	}
}
