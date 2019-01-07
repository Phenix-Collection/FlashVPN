package com.mobile.earnings.ads.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adscendmedia.sdk.ui.OffersActivity;
import com.adtrial.sdk.AdTrial;
import com.adtrial.sdk.AdTrialListener;
import com.appnext.ads.fullscreen.FullScreenVideo;
import com.appnext.base.Appnext;
import com.appnext.core.callbacks.OnAdClicked;
import com.appnext.core.callbacks.OnAdClosed;
import com.appnext.core.callbacks.OnAdError;
import com.appnext.core.callbacks.OnAdLoaded;
import com.appnext.core.callbacks.OnAdOpened;
import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.ads.MainAdsActivity;
import com.mobile.earnings.ads.fragments.presentations.AdsPresenter;
import com.mobile.earnings.ads.fragments.presentations.AdsView;
import com.mobile.earnings.api.BaseFragment;
import com.mobile.earnings.utils.GlobalRandomize;
import com.crashlytics.android.Crashlytics;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.fyber.ads.AdFormat;
import com.fyber.ads.videos.RewardedVideoActivity;
import com.fyber.currency.VirtualCurrencyErrorResponse;
import com.fyber.currency.VirtualCurrencyResponse;
import com.fyber.requesters.OfferWallRequester;
import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.RewardedVideoRequester;
import com.fyber.requesters.VirtualCurrencyCallback;
import com.offertoro.sdk.OTOfferWallSettings;
import com.offertoro.sdk.interfaces.OfferWallListener;
import com.offertoro.sdk.sdk.OffersInit;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.yandex.metrica.YandexMetrica;

import static com.mobile.earnings.utils.Constantaz.ADS_CENT_PUBLISHER_ID;
import static com.mobile.earnings.utils.Constantaz.ADS_CENT_WALL_ID;
import static com.mobile.earnings.utils.Constantaz.AD_COLONY_APP_ID;
import static com.mobile.earnings.utils.Constantaz.AD_COLONY_ZONE_ID;
import static com.mobile.earnings.utils.Constantaz.AD_TRIAL_APP_ID;
import static com.mobile.earnings.utils.Constantaz.APP_NEXT_PLACEMENT_ID;
import static com.mobile.earnings.utils.Constantaz.FACEBOOK_PLACEMENT_ID;
import static com.mobile.earnings.utils.Constantaz.OFFER_TORO_APP_ID;
import static com.mobile.earnings.utils.Constantaz.OFFER_TORO_SECRET_KEY;
import static com.mobile.earnings.utils.Constantaz.UNITY_GAME_ID;
import static com.mobile.earnings.utils.GlobalRandomize.randomize;
import static com.mobile.earnings.utils.ReportEvents.REPORT_EVENT_AD_COLONY;
import static com.mobile.earnings.utils.ReportEvents.REPORT_EVENT_APP_NEXT_1;
import static com.mobile.earnings.utils.ReportEvents.REPORT_EVENT_OW_ADS_CENT;
import static com.mobile.earnings.utils.ReportEvents.REPORT_EVENT_OW_FYBER;
import static com.mobile.earnings.utils.ReportEvents.REPORT_EVENT_UNITY_AD;



public class BaseAdsFragment extends BaseFragment implements RequestCallback,
        IUnityAdsListener, AdsView, VirtualCurrencyCallback, AdTrialListener,
        OfferWallListener, RewardedVideoAdListener{

	protected static final String ARGS_IS_VIDEO = "is.video";

	private AdsPresenter presenter;

	private Intent               mFyberIntent;
	private Intent               mAdsCentOfferWallIntent;
	private AdColonyInterstitial mAdColonyInterstitialAd;
	private FullScreenVideo      mAppNextFullScreenVideo;
	private RewardedVideoAd      facebookVideoAd;
	private MainAdsActivity      activity;

	public static PartnerAdFragment newInstance(boolean isVideo){
		Bundle args = new Bundle();
		PartnerAdFragment fragment = new PartnerAdFragment();
		args.putBoolean(ARGS_IS_VIDEO, isVideo);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context){
		super.onAttach(context);
		activity = (MainAdsActivity) context;
		if (getArguments().getBoolean(ARGS_IS_VIDEO)) {
			//AdColony
			AdColonyAppOptions appOptions = new AdColonyAppOptions().setUserID(App.getDeviceID());
			AdColony.configure(activity, appOptions, AD_COLONY_APP_ID, AD_COLONY_ZONE_ID);
			AdColonyInterstitialListener adColonyInterstitialListener = new AdColonyInterstitialListener(){
				@Override
				public void onRequestFilled(AdColonyInterstitial adColonyInterstitial){
					Log.i("AD_COLONY", "onRequestFilled: ");
					Toast.makeText(App.getContext(), App.getContext().getString(R.string.platformVideoIsReady, "AdColony"), Toast.LENGTH_SHORT).show();
					mAdColonyInterstitialAd = adColonyInterstitial;
				}
			};
			AdColony.requestInterstitial(AD_COLONY_ZONE_ID, adColonyInterstitialListener);
			//AppNext
			Appnext.init(activity);
			mAppNextFullScreenVideo = new FullScreenVideo(activity, APP_NEXT_PLACEMENT_ID);
			mAppNextFullScreenVideo.loadAd();
			mAppNextFullScreenVideo.setOnAdLoadedCallback(new OnAdLoaded(){
				@Override
				public void adLoaded(){
					Toast.makeText(App.getContext(), App.getContext().getString(R.string.platformVideoIsReady, "AppNext"), Toast.LENGTH_SHORT).show();
					Log.i("APP_NEXT", "adLoaded: ");
				}
			});
			mAppNextFullScreenVideo.setOnAdClickedCallback(new OnAdClicked(){
				@Override
				public void adClicked(){
					Log.i("APP_NEXT", "adClicked: ");
				}
			});
			mAppNextFullScreenVideo.setOnAdErrorCallback(new OnAdError(){
				@Override
				public void adError(String s){
					Log.e("APP_NEXT", "adError: " + s);
				}
			});
			mAppNextFullScreenVideo.setOnAdClosedCallback(new OnAdClosed(){
				@Override
				public void onAdClosed(){
					presenter.payForVideoAd(randomize());
					Log.i("APP_NEXT", "onAdClosed: ");
				}
			});
			mAppNextFullScreenVideo.setOnAdOpenedCallback(new OnAdOpened(){
				@Override
				public void adOpened(){
					Log.i("APP_NEXT", "adOpened: ");
				}
			});
			//UnityAd
			UnityAds.initialize(activity, UNITY_GAME_ID, this);
			if (UnityAds.isInitialized()) {
				Toast.makeText(App.getContext(), App.getContext().getString(R.string.platformVideoIsReady, "UnityAds"), Toast.LENGTH_SHORT).show();
			}
			//Fyber
			RewardedVideoRequester.create(this).request(activity);
			//Facebook
			facebookVideoAd = new RewardedVideoAd(activity, FACEBOOK_PLACEMENT_ID);
			facebookVideoAd.setAdListener(this);
//			AdSettings.addTestDevice("2ba77f5ee3dc9d832d509a6edc84b74d");
			facebookVideoAd.loadAd();
		} else {
			//OfferToro
			OTOfferWallSettings.getInstance().configInit(OFFER_TORO_APP_ID, OFFER_TORO_SECRET_KEY, App.getDeviceID());
			OffersInit.getInstance().setOfferWallListener(this);
			OffersInit.getInstance().create(activity);
			//Fyber
			OfferWallRequester.create(this).closeOnRedirect(true).request(activity);
			//AdsCent
			mAdsCentOfferWallIntent = OffersActivity.getIntentForOfferWall(activity, ADS_CENT_PUBLISHER_ID, ADS_CENT_WALL_ID, App.getDeviceID());
			//AdTrial
			AdTrial.getInstance().init(activity, this, AD_TRIAL_APP_ID);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		presenter = new AdsPresenter(this);
	}

	@Override
	public void onDestroy(){
		if (facebookVideoAd != null) {
			facebookVideoAd.destroy();
			facebookVideoAd = null;
		}
		super.onDestroy();
	}

	@Override
	public void onAdAvailable(Intent intent){
		Log.i("FYBER", "onAdAvailable: ");
		Toast.makeText(App.getContext(), App.getContext().getString(R.string.platformVideoIsReady, "Fyber"), Toast.LENGTH_SHORT).show();
		mFyberIntent = intent;
	}

	@Override
	public void onAdNotAvailable(AdFormat adFormat){
		Log.i("FYBER", "onAdNotAvailable: " + adFormat.name());
	}

	@Override
	public void onRequestError(RequestError requestError){
		Log.e("FYBER", "onRequestError: " + requestError.getDescription());
	}

	@Override
	public void onError(VirtualCurrencyErrorResponse virtualCurrencyErrorResponse){
		Log.e("FYBER", "onErrorCurrency: " + virtualCurrencyErrorResponse.getErrorMessage() + " ErrorCode: " + virtualCurrencyErrorResponse.getErrorCode());
	}

	@Override
	public void onSuccess(VirtualCurrencyResponse virtualCurrencyResponse){
		Log.i("FYBER", "onSuccess: " + virtualCurrencyResponse.getDeltaOfCoins());
//		presenter.payForOfferWall((float) virtualCurrencyResponse.getDeltaOfCoins(), "fyber");
	}

	@Override
	public void onUnityAdsReady(String s){
		Log.i("UNITY_AD", "onUnityAdsReady: " + s);
		Toast.makeText(App.getContext(), App.getContext().getString(R.string.platformVideoIsReady, "UnityAds"), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onUnityAdsStart(String s){
		Log.i("UNITY_AD", "onUnityAdsStart: " + s);
	}

	@Override
	public void onUnityAdsFinish(String s, UnityAds.FinishState finishState){
		Log.i("UNITY_AD", "onUnityAdsFinish: " + s + " Finish state: " + finishState.toString());
		if (finishState.toString().contentEquals("COMPLETED")) presenter.payForVideoAd(randomize());
		else informUser(R.string.videoTaskException);
	}

	@Override
	public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s){
		Log.e("UNITY_AD", "Error: " + unityAdsError + " string: " + s);
	}

	@Override
	public void onRewardedVideoCompleted(){
		Log.i("FACEBOOK_ADS", "onRewardedVideoCompleted: ");
		presenter.payForVideoAd(randomize());
	}

	@Override
	public void onLoggingImpression(Ad ad){
		Log.i("FACEBOOK_ADS", "onLoggingImpression: ");
	}

	@Override
	public void onRewardedVideoClosed(){
		Log.i("FACEBOOK_ADS", "onRewardedVideoClosed: ");
	}

	@Override
	public void onError(Ad ad, AdError adError){
		Log.e("FACEBOOK_ADS", "onError: " + adError.getErrorMessage());
	}

	@Override
	public void onAdLoaded(Ad ad){
		Log.i("FACEBOOK_ADS", "onAdLoaded: ");
		Toast.makeText(App.getContext(), App.getContext().getString(R.string.platformVideoIsReady, "Facebook"), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onAdClicked(Ad ad){
		Log.i("FACEBOOK_ADS", "onAdClicked: ");
	}

	@Override
	public void onOTOfferWallInitSuccess(){
		Log.i("OFFER_TORO", "onOTOfferWallInitSuccess: ");
	}

	@Override
	public void onOTOfferWallInitFail(String s){
		Log.e("OFFER_TORO", "onError: " + s);
	}

	@Override
	public void onOTOfferWallOpened(){
		Log.i("OFFER_TORO", "onOTOfferWallOpened: ");
	}

	@Override
	public void onOTOfferWallCredited(double v, double v1){
		Log.i("OFFER_TORO", "onOTOfferWallCredited: " + v + " V2: " + v1);
	}

	@Override
	public void onOTOfferWallClosed(){
		Log.i("OFFER_TORO", "onOTOfferWallClosed: ");
		OffersInit.getInstance().getOTOfferWallCredits();
	}

	@Override
	public void informUser(int message){
		Toast.makeText(App.getContext(), App.getContext().getString(message), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
//		if(requestCode == REQUEST_CODE_FYBER) {
//			Log.i("FYBER", "onActivityResult: Offer wall Closed");
//			VirtualCurrencyRequester.create(this).request(activity);
//		}
		if (requestCode == RC_FYBER_VIDEO && data != null) {
			String engagementResult = data.getStringExtra(RewardedVideoActivity.ENGAGEMENT_STATUS);
			switch(engagementResult) {
				case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_FINISHED_VALUE:
					// The user watched the entire video and will be rewarded
					Log.i("FYBER", "The video ad was dismissed because the user completed it");
					presenter.payForVideoAd(GlobalRandomize.randomize());
					break;
				case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_ABORTED_VALUE:
					// The user stopped the video early and will not be rewarded
					Log.i("FYBER", "The video ad was dismissed because the user explicitly closed it");
					break;
				case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_ERROR:
					// An error occurred while showing the video and the user will not be rewarded
					Log.i("FYBER", "The video ad was dismissed error during playing");
					break;
			}
		} else {
			informUser(R.string.someErrorText);
		}
	}

	@Override
	public void onAdClosed(int i){
		switch(i) {
			case RESULT_CODE_NO_AD:
				Log.e("AD_TRIAL", "NoAdAvailable");
				informUser(R.string.ad_content_is_not_available);
				break;
			case RESULT_CODE_AD_NO_REWARD:
				Log.e("AD_TRIAL", "NoAdReward");
				break;
			case RESULT_CODE_AD_REWARDED:
				Log.e("AD_TRIAL", "AdRewarded");
				presenter.payForVideoAd(randomize());
				break;
			default:
				Log.e("AD_TRIAL", "Something goes wrong");
				break;
		}
	}

	private static final int REQUEST_CODE_FYBER = 789;
	private static final int RC_FYBER_VIDEO     = 790;

	public void showFyberOfferWall(){
		if (mFyberIntent != null) {
			this.startActivityForResult(mFyberIntent, REQUEST_CODE_FYBER);
			YandexMetrica.reportEvent(REPORT_EVENT_OW_FYBER);
		} else {
			informUser(R.string.offerWallNotReadyException);
		}
	}

	public void showFyberRewardedVideo(){
		if (mFyberIntent != null) {
			try {
				this.startActivityForResult(mFyberIntent, RC_FYBER_VIDEO);
			} catch(NullPointerException e) {
				Crashlytics.logException(e);
				informUser(R.string.videoNotReadyError);
			}
		} else {
			informUser(R.string.videoNotReadyError);
		}
	}

	public void showAdsCentOfferWall(){
		if (mAdsCentOfferWallIntent != null) {
			startActivity(mAdsCentOfferWallIntent);
			YandexMetrica.reportEvent(REPORT_EVENT_OW_ADS_CENT);
		} else {
			informUser(R.string.offerWallNotReadyException);
		}
	}

	public void showOfferTorro(){
		OffersInit.getInstance().showOfferWall(activity);
	}

	public void showAdColonyVideo(){
		if (mAdColonyInterstitialAd != null && !mAdColonyInterstitialAd.isExpired()) {
			mAdColonyInterstitialAd.show();
			presenter.payForVideoAd(randomize());
			YandexMetrica.reportEvent(REPORT_EVENT_AD_COLONY);
		} else {
			informUser(R.string.videoNotReadyError);
		}
	}

	public void showAppNextFullscreenVideo(){
		if (mAppNextFullScreenVideo != null && mAppNextFullScreenVideo.isAdLoaded()) {
			mAppNextFullScreenVideo.showAd();
			YandexMetrica.reportEvent(REPORT_EVENT_APP_NEXT_1);
		} else {
			informUser(R.string.videoNotReadyError);
		}
	}

	public void showUnityAdVideo(){
		if (UnityAds.isReady()) {
			UnityAds.show(activity);
			YandexMetrica.reportEvent(REPORT_EVENT_UNITY_AD);
		} else {
			informUser(R.string.videoNotReadyError);
		}
	}

	public void showFacebookVideo(){
		if (facebookVideoAd.isAdLoaded()) {
			facebookVideoAd.show();
		} else {
			informUser(R.string.videoNotReadyError);
		}
	}

	public void showAdTrial(){
		if (AdTrial.getInstance().checkAd()) {
			boolean isAdAvailable = AdTrial.getInstance().requestAd("");
			if (!isAdAvailable) {
				informUser(R.string.ad_content_is_not_available);
			}
		} else {
			informUser(R.string.ad_content_is_not_available);
		}
	}

}
