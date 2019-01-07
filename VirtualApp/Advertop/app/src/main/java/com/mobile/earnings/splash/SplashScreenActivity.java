package com.mobile.earnings.splash;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.splash.presenter.SplashPresenterImpl;
import com.mobile.earnings.splash.view.SplashView;
import com.mobile.earnings.utils.ConnectionChecker;
import com.mobile.earnings.utils.Demander;
import com.mobile.earnings.utils.LocationRetriever;
import com.crashlytics.android.Crashlytics;
import com.vansuita.library.CheckNewAppVersion;

import static com.mobile.earnings.autorization.RegisterActivity.getRegisterIntent;
import static com.mobile.earnings.main.MainActivity.getMainIntent;
import static com.mobile.earnings.tutorial.IntroActivity.getIntroIntent;
import static com.mobile.earnings.utils.Constantaz.EXTRA_OPEN_ACTIVE;
import static com.mobile.earnings.utils.Constantaz.EXTRA_OPEN_DETAILED;
import static com.mobile.earnings.utils.LocationRetriever.LOCATION_REQUEST_CODE;

public class SplashScreenActivity extends BaseActivity implements SplashView, Demander.OnPermissionsGrantedListener, LocationRetriever.OnLocationRetrievedListener{

	private SplashPresenterImpl presenter;
	private Demander            demander;
	private String city = "Москва";
	private float  lat  = 55.755f, lon = 37.617f;
	private LocationRetriever locationRetriever;
	private boolean isPermissionSettingsOpen = false;

	@Override
	protected void onCreate(Bundle savedInstanceState){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE //Did this to save code formatting
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION //
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN //
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		presenter = new SplashPresenterImpl(this);
		demander = new Demander(this);
		demander.setOnPermissionsGrantedListener(this);
		locationRetriever = new LocationRetriever(this);
		locationRetriever.setOnLocationRetrievedListener(this);
		disableAllNotifications();
		checkNetworkConnection(this);
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(isPermissionSettingsOpen) {
			isPermissionSettingsOpen = false;
			checkNetworkConnection(this);
		}
	}

	@Override
	protected void onStart(){
		locationRetriever.connect();
		super.onStart();
	}

	@Override
	protected void onStop(){
		locationRetriever.disconnect();
		isPermissionSettingsOpen = true;
		super.onStop();
	}

	@Override
	public void startRegisterAct(@NonNull String cityName, float lat, float lon){
		startFinishingActivity(getRegisterIntent(this, cityName, lat, lon));
	}

	@Override
	public void openMainAct(){
		boolean isReminder = getIntent().getBooleanExtra(EXTRA_OPEN_ACTIVE, false);
		int appId = getIntent().getIntExtra(EXTRA_OPEN_DETAILED, -1);
		Log.e("TAGA", "AppId: " + appId);
		startFinishingActivity(getMainIntent(this, isReminder, appId));
	}

	@Override
	public void openTutorialScreen(){
		startFinishingActivity(getIntroIntent(this));
	}

	@Override
	public void isAllPermissionsGranted(){
		if(isGpsSensorExists()) {
			locationRetriever.createGoogleDialog();
		} else{
			shouldUpdate(city, lat, lon);
		}
	}

	@Override
	public void onLocationRetrieved(Location location){
		if(location != null) {
			shouldUpdate(locationRetriever.getCityName(location), (float) location.getLatitude(), (float) location.getLongitude());
		} else{
			shouldUpdate(city, lat, lon);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == LOCATION_REQUEST_CODE) {
			locationRetriever.requestUpdates();
		}
		if(resultCode != RESULT_OK) {
			shouldUpdate(city, lat, lon);
		}
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	private boolean isGpsSensorExists(){
		PackageManager packageManager = getPackageManager();
		return packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}

	private void checkNetworkConnection(Activity activity){
		if(!ConnectionChecker.isConnectionEnable(activity)) {
			DialogInterface.OnClickListener positiveButListener = new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialogInterface, int i){
					restartScreen();
				}
			};
			DialogInterface.OnClickListener negativeButListener = new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialogInterface, int i){
					finish();
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DefaultDialogStyle);
			builder.setTitle(R.string.splash_dialog_title).setMessage(R.string.splash_dialog_message)
					.setCancelable(false).setPositiveButton(R.string.splash_dialog_positive_but_text, positiveButListener).setNegativeButton(R.string.splash_negative_but_text, negativeButListener).create().show();
		} else{
			permissionCheck();
		}
	}

	private void restartScreen(){
		Intent restartIntent = getIntent();
		finish();
		startActivity(restartIntent);
	}

	private void permissionCheck(){
		demander.demand(android.Manifest.permission.READ_PHONE_STATE,
				android.Manifest.permission.ACCESS_COARSE_LOCATION,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.GET_ACCOUNTS);
	}

	private void shouldUpdate(final String cityName, final float lat, final float lon){
		new CheckNewAppVersion(this).setOnTaskCompleteListener(new CheckNewAppVersion.ITaskComplete(){
			@Override
			public void onTaskComplete(CheckNewAppVersion.Result result){
				//Checks if there is a new version available on Google Play Store.
				if(result.hasNewVersion()) {
					result.openUpdateLink();
				} else{
					presenter.loginUser(cityName, lat, lon);
				}
			}
		}).execute();
	}

	private void disableAllNotifications(){
		try{
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancelAll();
		} catch(SecurityException e){
			Crashlytics.logException(e);
		}
	}

}
