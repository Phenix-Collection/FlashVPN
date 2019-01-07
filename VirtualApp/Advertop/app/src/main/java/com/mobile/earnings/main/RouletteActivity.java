package com.mobile.earnings.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.api.responses.RouletteSettingsResponse;
import com.mobile.earnings.main.presenterImpls.RoulettePresenter;
import com.mobile.earnings.main.views.RouletteView;
import com.mobile.earnings.utils.ReportEvents;
import com.yandex.metrica.YandexMetrica;

import org.apache.commons.lang3.StringEscapeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class RouletteActivity extends BaseActivity implements RouletteView{

	@BindView(R.id.rouletteAct_makeBetBut)
	Button      playBut;
	@BindView(R.id.loadingWidget)
	FrameLayout loadingWidget;
	@BindView(R.id.rouletteAct_firstBet)
	Button    firstBetTV;
	@BindView(R.id.rouletteAct_secondBet)
    Button    secondBetTV;
	@BindView(R.id.rouletteAct_thirdBet)
    Button    thirdBetTV;

	private RoulettePresenter presenter;
	private float   bet      = 0;
	private boolean result   = false;
	private float   amount   = 0f;
	private float   firstBet = 0f, secondBet = 0f, thirdBet = 0f;
	private float userBalance = 0f;

	private static final String EXTRA_BALANCE = "user.balance";

	public static Intent getRouletteIntent(Context context, float userBalance){
		Intent intent = new Intent(context, RouletteActivity.class);
		intent.putExtra(EXTRA_BALANCE, userBalance);
		return intent;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roulette);
		ButterKnife.bind(this);
		presenter = new RoulettePresenter(this);
		setSupportActionBar(setToolBar(getString(R.string.rouletteAct_toolbarTitle)));
		displayHomeAsUpEnabled(true);
		userBalance = getIntent().getFloatExtra(EXTRA_BALANCE, 0f);
		presenter.getBets();
	}

	@OnClick(R.id.rouletteAct_makeBetBut)
	void onPlayButClick(){
		YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_ROULETTE);
		playBut.setEnabled(false);
		playBut.setClickable(false);
		presenter.startRolling(bet);
	}


//	@OnClick(R.id.rouletteAct_moreBut)
//	void onMoreButClick(){
//		try{
//			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/advertapp_me"));
//			startActivity(browserIntent);
//		} catch(ActivityNotFoundException e){
//			informUser(R.string.registerAct_browserActNotFoundException);
//		}
//	}

	@SuppressWarnings("deprecation")
	@OnClick({R.id.rouletteAct_firstBet, R.id.rouletteAct_secondBet, R.id.rouletteAct_thirdBet})
	void onBetSelected(View v){
		switch(v.getId()){
			case R.id.rouletteAct_firstBet:
				firstBetTV.setBackgroundResource(R.drawable.rounded_alpha);
				firstBetTV.setTextColor(getResources().getColor(R.color.colorPrimary));
				secondBetTV.setBackgroundResource(R.drawable.shape_bid_unselected);
				secondBetTV.setTextColor(getResources().getColor(R.color.primaryText));
				thirdBetTV.setBackgroundResource(R.drawable.shape_bid_unselected);
				thirdBetTV.setTextColor(getResources().getColor(R.color.primaryText));
				bet = firstBet;
				break;
			case R.id.rouletteAct_secondBet:
				firstBetTV.setBackgroundResource(R.drawable.shape_bid_unselected);
				firstBetTV.setTextColor(getResources().getColor(R.color.primaryText));
				secondBetTV.setBackgroundResource(R.drawable.rounded_alpha);
				secondBetTV.setTextColor(getResources().getColor(R.color.colorPrimary));
				thirdBetTV.setBackgroundResource(R.drawable.shape_bid_unselected);
				thirdBetTV.setTextColor(getResources().getColor(R.color.primaryText));
				bet = secondBet;
				break;
			case R.id.rouletteAct_thirdBet:
				firstBetTV.setBackgroundResource(R.drawable.shape_bid_unselected);
				firstBetTV.setTextColor(getResources().getColor(R.color.primaryText));
				secondBetTV.setBackgroundResource(R.drawable.shape_bid_unselected);
				secondBetTV.setTextColor(getResources().getColor(R.color.primaryText));
				thirdBetTV.setBackgroundResource(R.drawable.rounded_alpha);
				thirdBetTV.setTextColor(getResources().getColor(R.color.colorPrimary));
				bet = thirdBet;
				break;
			default:
				break;
		}
		playBut.setEnabled(userBalance >= bet);
	}

	@Override
	public void onBackPressed(){
		handler.removeCallbacks(mUpdateTimeTask);
		super.onBackPressed();
	}

	@Override
	public void updateSettings(RouletteSettingsResponse.Settings settings){
		String currency = StringEscapeUtils.unescapeJava("\\" + settings.currency);
		firstBet = settings.firstBetAmount;
		secondBet = settings.secondBetAmount;
		thirdBet = settings.thirdBetAmount;
		firstBetTV.setText(String.valueOf(firstBet).concat(currency));
		secondBetTV.setText(String.valueOf(secondBet).concat(currency));
		thirdBetTV.setText(String.valueOf(thirdBet).concat(currency));
		bet = firstBet;
		playBut.setEnabled(userBalance >= bet);
		playBut.setClickable(userBalance >= bet);
	}

	@Override
	public void setResultData(boolean result, float amount, float currentBalance){
		this.result = result;
		this.amount = amount;
		this.userBalance = currentBalance;
	}

	@Override
	public void startTimer(){
//		GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(rouletteAnimIv);
//		try{
//			Glide.with(this).load(R.drawable.gif_roulette).into(target);
//		} catch(IllegalArgumentException e){
//			Crashlytics.logException(e);
//			informUser(R.string.cannotLoadAnimationError);
//		}
		startTime = System.currentTimeMillis();
		handler.removeCallbacks(mUpdateTimeTask);
		handler.post(mUpdateTimeTask);
		disableAllViews();
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	@Override
	public void showLoading(){
		loadingWidget.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideLoading(){
		loadingWidget.setVisibility(View.GONE);
	}

	private void onTimerFinish(){
//		rouletteAnimIv.setImageResource(R.drawable.ic_roulette_large);
		startResultDialog(result, amount);
		enableAllViews();
	}

	private void disableAllViews(){
		firstBetTV.setEnabled(false);
		secondBetTV.setEnabled(false);
		thirdBetTV.setEnabled(false);
		playBut.setEnabled(false);
//		moreInfoBut.setEnabled(false);
	}

	private void enableAllViews(){
		firstBetTV.setEnabled(true);
		secondBetTV.setEnabled(true);
		thirdBetTV.setEnabled(true);
		firstBetTV.performClick();
		playBut.setEnabled(userBalance >= bet);
		playBut.setClickable(userBalance >= bet);
//		moreInfoBut.setEnabled(true);
	}

	private void startResultDialog(boolean win, float amount){
		String message = win
				? App.getContext().getString(R.string.rouletteAct_winMessage, String.valueOf(amount))
				: App.getContext().getString(R.string.rouletteAct_loseMessage, String.valueOf(bet));
		Toast.makeText(App.getContext(), message, Toast.LENGTH_LONG).show();
	}

	private Handler handler = new Handler();
	private long startTime;
	private Runnable mUpdateTimeTask = new Runnable(){
		@Override
		public void run(){
			long start = startTime;
			long millis = System.currentTimeMillis() - start;
			int seconds = (int) (millis / 1000);
			seconds %= 60;
			seconds = 3 - seconds;
			if(seconds == 0) {
				onTimerFinish();
				return;
			}
			handler.postDelayed(this, 1000);
		}
	};

}
