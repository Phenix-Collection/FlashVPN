package com.mobile.earnings.single.task;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.data_models.ReviewAppModel;
import com.mobile.earnings.api.modules.SharingModule;
import com.mobile.earnings.autorization.VkLoginCallback;
import com.mobile.earnings.main.MainActivity;
import com.mobile.earnings.single.task.presenter.impl.DetailedPresenterImpl;
import com.mobile.earnings.single.task.view.DetailedTaskView;
import com.mobile.earnings.timer.TimerService;
import com.mobile.earnings.utils.Constantaz;
import com.mobile.earnings.utils.ImageUtils;
import com.mobile.earnings.utils.PackageUtil;
import com.mobile.earnings.utils.ReportEvents;
import com.mobile.earnings.utils.TextUtils;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.vk.sdk.VKSdk;
import com.yandex.metrica.YandexMetrica;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mobile.earnings.main.adapters.BaseTasksAdapter.TASK_DELAY_THREE_DAYS;
import static com.mobile.earnings.main.adapters.BaseTasksAdapter.TASK_DELAY_TWO_DAYS;
import static com.mobile.earnings.utils.Constantaz.EXTRA_TASK_MODEL;
import static com.mobile.earnings.utils.Constantaz.PREFS_SECOND_PROMO;
import static com.mobile.earnings.utils.Constantaz.PREF_COMMENT_REWARD;
import static com.mobile.earnings.utils.Constantaz.PREF_REVIEW_REWARD;
import static com.mobile.earnings.utils.Constantaz.PREF_TIMER_DONE;
import static com.mobile.earnings.utils.Constantaz.UA_CURRENCY_CODE;

public class DetailedTaskActivity extends BaseActivity implements DetailedTaskView{

	public static final String EXTRA_FROM_PUSH = "task.from.push";

	@BindView(R.id.activitySingleTaskIcon)
	ImageView    iconIv;
	@BindView(R.id.activitySingleTaskTitle)
	TextView     taskTitle;
	@BindView(R.id.singleAct_firstCondition)
	TextView     firstConditionTv;
	@BindView(R.id.singleAct_secondCondition)
	TextView     secondConditionTv;
	@BindView(R.id.singleAct_thirdCondition)
	TextView     thirdConditionTv;
	@BindView(R.id.singleAct_fourthCondition)
	TextView     fourthConditionTv;
	@BindView(R.id.singleAct_fifthCondition)
	TextView     fifthConditionTv;
	@BindView(R.id.singleTask_daysLeftTitle)
	TextView     daysLeftTitleTV;
	@BindView(R.id.referralFrag_promoCodeTV)
	TextView     promoCodeTV;
	@BindView(R.id.singleTask_translatableLayout)
	LinearLayout translatableLayout;
	@BindView(R.id.activitySingleTaskGetTaskBut)
	LinearLayout getOpenAppBut;
	@BindView(R.id.singleTask_dailyAttentionTV)
	TextView     dailyAttentionTv;
	@BindView(R.id.singleTask_attentionBannerTv)
	TextView     dailyAttentionBannerTv;
	@BindView(R.id.singleAct_openButTitle)
	TextView     openButTitleTv;
	@BindView(R.id.singleAct_openButDesc)
	TextView     openButDescTv;
	//Additional task
	@BindView(R.id.detailedTaskAct_reviewAppContainer)
	LinearLayout reviewTaskContainer;
	@BindView(R.id.detailedTaskAct_additionalTaskText)
	TextView     additionalTaskPriceTv;
	@BindView(R.id.detailedTaskAct_additionalTaskBut)
	Button       additionalTaskBut;

	private boolean ifOpenButClicked = false;

	private @Nullable
	AppModel model;
	private DetailedPresenterImpl presenter;
	private boolean               isActiveApp;
	private ShareDialog           shareDialog;
	private CallbackManager       callbackManager;
	private String                promoCode;
	private Intent timerIntent = null;

	public static Intent getDetailedTaskIntent(Context context, AppModel model){
		Intent intent = new Intent(context, DetailedTaskActivity.class);
		intent.putExtra(EXTRA_TASK_MODEL, model);
		return intent;
	}

	public static Intent getDetailedTaskIntent(Context context, AppModel model, boolean isFromPush){
		Intent intent = new Intent(context, DetailedTaskActivity.class);
		intent.putExtra(EXTRA_TASK_MODEL, model);
		intent.putExtra(EXTRA_FROM_PUSH, isFromPush);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_task);
		ButterKnife.bind(this);
		presenter = new DetailedPresenterImpl(this);
		FacebookSdk.sdkInitialize(App.getContext());
		callbackManager = CallbackManager.Factory.create();
		shareDialog = new ShareDialog(this);
		model = getIntent().getParcelableExtra(EXTRA_TASK_MODEL);
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if(manager != null) {
			manager.cancel(TimerService.NOTIFICATION_ID);
		}
		getOpenAppBut.setActivated(true);
		if(model != null) {
			presenter.markTaskWatched(model.id);
			setToolbarTitle(model);
			updateUi(model);
			handleKeyWords(model);
		}
		//getting first referral code
		promoCode = App.getPrefs().getString(PREFS_SECOND_PROMO, "");
		promoCodeTV.setText(promoCode);
		displayHomeAsUpEnabled(true);
	}

	private void startTimer(boolean pause){
		timerIntent = new Intent(this, TimerService.class);
		timerIntent.putExtra(TimerService.PAUSE, pause);
		timerIntent.putExtra(EXTRA_FROM_PUSH, model);
		startService(timerIntent);
	}

	private void stopTimer(){
		stopService(timerIntent);
	}

	private void showTimerErrorDialog(){
		new AlertDialog.Builder(this)
				.setTitle(R.string.app_name)
				.setMessage(R.string.timer_error)
				.setCancelable(false)
				.setPositiveButton(R.string.timer_ok_but, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						Intent launchIntent = getPackageManager().getLaunchIntentForPackage(model.packageName);
						if(launchIntent != null) {
							ifOpenButClicked = true;
							startActivity(launchIntent);
							startTimer(false);
						}
					}
				})
				.create().show();
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(timerIntent != null) {
			startTimer(true);
			showTimerErrorDialog();
		}
		if(App.getPrefs().getBoolean(PREF_TIMER_DONE, false)) {
			if(model != null) {
				presenter.updateTask(model.id);
				App.getPrefs().edit().putBoolean(PREF_TIMER_DONE, false).apply();
			}
		} else{
			if(model != null) {
				if(PackageUtil.isPackageDownloaded(model.packageName, this)) {
					updateNotEnabled(true);
				}
				if(PackageUtil.isPackageDownloaded(model.packageName, this) && model.isAvailable != null) {
					updateNotEnabled(model.isAvailable);
				}
			}
		}
	}

	@Override
	public void updateUi(@NonNull AppModel model){
		if(!isFinishing()) {
			Glide.with(this).load(TextUtils.getRightUri(model.image)).into(iconIv);
		}
		if(model.daysLeft != 0) {
			//noinspection deprecation
			daysLeftTitleTV.setText(Html.fromHtml(App.getRes().getString(R.string.singleTask_taskEndingText, model.daysLeft)));
		} else{
			daysLeftTitleTV.setVisibility(View.GONE);
		}
		String currencyCode;
		if(model.currency.contentEquals(UA_CURRENCY_CODE)) {
			currencyCode = " грн";
		} else{
			currencyCode = " руб.";
		}
		taskTitle.setText(getString(R.string.singleAct_taskTitle, model.priceForDay + currencyCode, model.days));
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	@Override
	public void updateNotEnabled(boolean isEnabled){
		isActiveApp = isEnabled;
		openButDescTv.setVisibility(View.VISIBLE);
		getOpenAppBut.setActivated(isEnabled);
		getOpenAppBut.setClickable(isEnabled);
		openButTitleTv.setText(isEnabled ? getString(R.string.singleTask_launchAppBut) : getString(R.string.singleTask_doneText));
		if(model != null) {
			if(model.currency.contentEquals(UA_CURRENCY_CODE)) {
				openButDescTv.setText(isEnabled ? getString(R.string.singleTask_launchAppButDesc, model.priceForDay + " грн") : getString(R.string.singleTask_doneTextDesc, model.priceForDay + " грн"));
			} else{
				openButDescTv.setText(isEnabled ? getString(R.string.singleTask_launchAppButDesc, model.priceForDay + " руб") : getString(R.string.singleTask_doneTextDesc, model.priceForDay + " руб"));
			}
		}
		dailyAttentionTv.setText(isEnabled ? R.string.empty : R.string.singleTask_attention);
		if(isEnabled) {
			dailyAttentionBannerTv.setVisibility(View.GONE);
		} else{
			dailyAttentionBannerTv.setVisibility(View.VISIBLE);
		}
		//getting additional task info
		presenter.getReviewAppModel(model.id);
	}

	@Override
	public void showLoading(){
		showProgressDialog();
	}

	@Override
	public void hideLoading(){
		hideProgressDialog();
	}

	@Override
	public void onBackPressed(){
		if(getIntent().getBooleanExtra(EXTRA_FROM_PUSH, false)) {
			TaskStackBuilder b = TaskStackBuilder.create(this);
			b.addNextIntent(new Intent(this, MainActivity.class));
			b.startActivities();
		} else{
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private static final String REVIEW_ONLY = "review";

	@Override
	public void setUpReviewAppTaskContainer(@NonNull final ReviewAppModel.ReviewModel reviewAppModel){
		if(reviewAppModel.isReviewTaskExists) {
			reviewTaskContainer.setVisibility(View.VISIBLE);
			String currencyCode = StringEscapeUtils.unescapeJava("\\" + model.currency);
			if(reviewAppModel.isReviewAvailable) {
				float reward = 0f;
				final boolean isReviewOnly = reviewAppModel.type.contentEquals(REVIEW_ONLY);
				if(isReviewOnly) {
					if(App.getPrefs().contains(PREF_REVIEW_REWARD)) {
						reward = App.getPrefs().getFloat(PREF_REVIEW_REWARD, 0f);
					}
				} else{
					if(App.getPrefs().contains(PREF_COMMENT_REWARD)) {
						reward = App.getPrefs().getFloat(PREF_COMMENT_REWARD, 0f);
					}
				}
				additionalTaskPriceTv.setText(getString(R.string.detailedTaskAct_additionalTaskPrice, reward, currencyCode));
				additionalTaskBut.setVisibility(View.VISIBLE);
				additionalTaskBut.setEnabled(true);
				additionalTaskBut.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v){
						createReviewAppTaskDialog(isReviewOnly, reviewAppModel.keywords, reviewAppModel.stars);
					}
				});
			} else{
				if(reviewAppModel.state != null) {
					additionalTaskBut.setVisibility(View.VISIBLE);
					additionalTaskBut.setEnabled(false);
					switch(reviewAppModel.state){
						case ReviewState.MODERATING:
							additionalTaskPriceTv.setText(getString(R.string.detailedTaskAct_additionalTaskOnModerationText));
							additionalTaskBut.setText(getString(R.string.detailedTaskAct_additionalTaskUnAvailableTaskText));
							break;
						case ReviewState.PAID:
							additionalTaskPriceTv.setText(getString(R.string.single_task_status_finished));
							break;
						case ReviewState.FAILED:
//						additionalTaskPriceTv.setText(getString(R.string.detailedTaskAct_additionalTaskFailedModerationText));
							additionalTaskPriceTv.setText(getString(R.string.detailedTaskAct_taskFailedException));
							break;
						default:
							break;
					}
				} else{
					additionalTaskPriceTv.setText(getString(R.string.detailedTaskAct_additionalTaskNotAvailableText));
				}
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		VkLoginCallback vkLoginCallback = new VkLoginCallback();
		if(requestCode == RC_PICK_IMAGE && data != null) {
			String realPath;
			// SDK >= 11 && SDK < 19
			if(Build.VERSION.SDK_INT < 19) {
				realPath = ImageUtils.getRealPathFromURI_API11to18(this, data.getData());
			}
			// SDK > 19 (Android 4.4)
			else{
				realPath = ImageUtils.getRealPathFromURI_API19(this, data.getData());
			}
			if(realPath != null) {
				presenter.sendScreenshotOnModeration(realPath, model.id);
			} else{
				informUser(R.string.someErrorText);
			}
		}
		if(!VKSdk.onActivityResult(requestCode, resultCode, data, vkLoginCallback)) {
			super.onActivityResult(requestCode, resultCode, data);
		} else{
			SharingModule.vkShare(String.format(getResources().getString(R.string.share_message), promoCode) + getResources().getString(R.string.share_link));
		}
	}

	@OnClick(R.id.activitySingleTaskGetTaskBut)
	public void getTask(){
		if(model != null && model.name != null) {
			YandexMetrica.reportEvent(String.format(ReportEvents.REPORT_EVENT_EXECUTE, model.name.concat(":").concat(String.valueOf(model.id))));
		}
		if(isActiveApp) {
			startApp();
		} else{
			downloadApp();
		}
	}

	private boolean translateBackward = false;

	@OnClick(R.id.singleTask_referralScreen)
	void goToReferralScreen(){
		translatableLayout.getLayoutParams();
		startAnim(translateBackward, translatableLayout);
	}

	@OnClick(R.id.referralFrag_FacebookShareBut)
	void facebookShare(){
		setUpFBSharing();
		ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentTitle(getResources().getString(R.string.app_name)).setContentDescription(String.format(getResources().getString(R.string.share_message), promoCode)).setContentUrl(Uri.parse(getResources().getString(R.string.share_link))).build();
		shareDialog.show(linkContent);
	}

	@OnClick(R.id.referralFrag_GoogleShareBut)
	void googleShare(){
		startActivityForResult(SharingModule.shareThroughGoogle(this, String.format(getResources().getString(R.string.share_message), promoCode), Uri.parse(getResources().getString(R.string.share_link))), 456);
	}

	@OnClick(R.id.referralFrag_VkShareBut)
	void vkShare(){
		SharingModule.shareThroughVkifLoggedIn(this, String.format(getResources().getString(R.string.share_message), promoCode) + getResources().getString(R.string.share_link), Constantaz.VK_SCOPE);
	}

	@OnClick(R.id.referralFrag_copyCodeBut)
	void onCopyReferralCode(){
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		presenter.copyCodeToBuffer(clipboard, promoCode);
	}

	private void setUpFBSharing(){
		shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>(){
			@Override
			public void onSuccess(Sharer.Result result){
				Toast.makeText(DetailedTaskActivity.this, R.string.referralFrag_shareToast, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel(){

			}

			@Override
			public void onError(FacebookException error){
				Log.e("FACEBOOK", "Error: " + error.getMessage());
				Toast.makeText(DetailedTaskActivity.this, R.string.referralFrag_shareExceptionToast, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void setToolbarTitle(@NonNull AppModel model){
		String toolbarTitle;
		if(model.currency != null && model.currency.contentEquals(UA_CURRENCY_CODE)) {
			toolbarTitle = getString(R.string.singleAct_toolbarTitle, model.amount).concat(" грн");
		} else{
			toolbarTitle = getString(R.string.singleAct_toolbarTitle, model.amount).concat(" руб");
		}
		setSupportActionBar(setToolBar(toolbarTitle));
	}

	private void handleKeyWords(@NonNull AppModel model){
		int timeDelayInDays = model.timeDelayInSeconds / 24 / 60 / 60;
		if(model.keywords != null && !model.keywords.isEmpty()) {
			firstConditionTv.setText(getString(R.string.singleAct_firstCondition, TextUtils.provideKeywords(model.keywords)));
			secondConditionTv.setText(getString(R.string.singleAct_secondCondition, 2, model.days));
			switch(timeDelayInDays){
				case TASK_DELAY_TWO_DAYS:
					thirdConditionTv.setText(getString(R.string.singleAct_thirdCondition_48, 3, model.days));
					break;
				case TASK_DELAY_THREE_DAYS:
					thirdConditionTv.setText(getString(R.string.singleAct_thirdCondition_72, 3, model.days));
					break;
				default:
					thirdConditionTv.setText(getString(R.string.singleAct_thirdCondition, 3, model.days));
					break;
			}
			fourthConditionTv.setText(getString(R.string.singleAct_fourthCondition, 4));
			if(!model.description.isEmpty()) {
				fifthConditionTv.setVisibility(View.VISIBLE);
				fifthConditionTv.setText(getString(R.string.singleAct_fifthCondition, 5, model.description));
			}
		} else{
			firstConditionTv.setText(getString(R.string.singleAct_secondCondition, 1, model.days));
			switch(timeDelayInDays){
				case TASK_DELAY_TWO_DAYS:
					secondConditionTv.setText(getString(R.string.singleAct_thirdCondition_48, 2, model.days));
					break;
				case TASK_DELAY_THREE_DAYS:
					secondConditionTv.setText(getString(R.string.singleAct_thirdCondition_72, 2, model.days));
					break;
				default:
					secondConditionTv.setText(getString(R.string.singleAct_thirdCondition, 2, model.days));
					break;
			}
			thirdConditionTv.setText(getString(R.string.singleAct_fourthCondition, 3));
			if(!model.description.isEmpty()) {
				fourthConditionTv.setText(getString(R.string.singleAct_fifthCondition, 4, model.description));
			} else
				fourthConditionTv.setVisibility(View.GONE);
		}
	}

	private static final int RC_PICK_IMAGE = 154;

	private void createReviewAppTaskDialog(boolean isReviewOnly, List<String> commentKeywords, int stars){
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.DefaultDialogStyle);
		View view = LayoutInflater.from(this).inflate(R.layout.dialog_additional_task, null);
		dialogBuilder.setView(view);
		final AlertDialog dialog = dialogBuilder.create();
		dialog.show();
		TextView conditionsTv = (TextView) view.findViewById(R.id.dialog_star_conditions);
		Button sendScreenShotBut = (Button) view.findViewById(R.id.dialog_star_send_screenshot);
		View.OnClickListener onButClickListener = new View.OnClickListener(){
			@Override
			public void onClick(View v){
				try{
					startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) /*Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT).setType("image*//*"), "Choose an image")*/, RC_PICK_IMAGE);
					dialog.dismiss();
				} catch(ActivityNotFoundException e){
					informUser(R.string.registerAct_browserActNotFoundException);
				}
			}
		};
		sendScreenShotBut.setOnClickListener(onButClickListener);
		if(isReviewOnly) {
			conditionsTv.setText(getString(R.string.dialogStarApp_conditions, stars));
		} else{
			conditionsTv.setText(getString(R.string.dialogStarCommentApp_conditions, stars, TextUtils.provideKeywords(commentKeywords)));
		}
	}

	private void downloadApp(){
		if(model != null) {
			if(model.keywords != null && !model.keywords.isEmpty()) {
				createHandyTaskImplementationDialog();
//			try{
//				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q="/* + formattedString + "&c=apps"*/)));
//			} catch(android.content.ActivityNotFoundException anfe){
//				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q="/* + model.app.keywords.get(0) + "&c=apps"*/)));
//			}
			} else if(model.trackingLink != null && !model.trackingLink.isEmpty()) {
				try{
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.trackingLink)));
				} catch(ActivityNotFoundException e){
					Crashlytics.logException(e);
				}
			} else{
				try{
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constantaz.GOOGLE_PLAY_HEADER + model.packageName)));
				} catch(android.content.ActivityNotFoundException anfe){
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + model.packageName)));
				}
			}
		}
	}

	private void createHandyTaskImplementationDialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		DialogInterface.OnClickListener onDismissClick = new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		};
		dialog.setMessage(R.string.detailedTaskAct_askingUserToOpenMarketMessage).setCancelable(false).setPositiveButton(android.R.string.ok, onDismissClick).create();
		if(!isFinishing()) {
			dialog.show();
		}
	}

	private void startApp(){
		if(model != null) {
			if(PackageUtil.isPackageDownloaded(model.packageName, this)) {
				Intent launchIntent = getPackageManager().getLaunchIntentForPackage(model.packageName);
				if(launchIntent != null) {
					ifOpenButClicked = true;
					startActivity(launchIntent);
					startTimer(false);
				}
			} else{
				Toast.makeText(this, getString(R.string.singleTask_noAppException), Toast.LENGTH_LONG).show();
				downloadApp();
			}
		}
	}

	private void startAnim(boolean backward, final LinearLayout translatableLayout){
		if(backward) {
			translatableLayout.animate().translationY(0).setListener(new AnimatorListenerAdapter(){
				@Override
				public void onAnimationEnd(Animator animation){
					super.onAnimationEnd(animation);
					translatableLayout.setVisibility(View.GONE);
				}
			});
			translateBackward = false;
		} else{
			translatableLayout.animate().translationY(translatableLayout.getLayoutParams().height).setListener(new AnimatorListenerAdapter(){
				@Override
				public void onAnimationEnd(Animator animation){
					super.onAnimationEnd(animation);
					translatableLayout.setVisibility(View.VISIBLE);
				}
			});
			translateBackward = true;
		}
	}

}
