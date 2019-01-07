package com.mobile.earnings.autorization;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.api.responses.AuthorizationResponse;
import com.mobile.earnings.api.listeners.OnRegisterListener;
import com.mobile.earnings.api.modules.RegisterModule;
import com.mobile.earnings.autorization.presentersImpl.LoginPresenterImpl;
import com.mobile.earnings.autorization.views.LoginView;
import com.mobile.earnings.utils.Constantaz;
import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.vk.sdk.VKSdk;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mobile.earnings.main.MainActivity.getMainIntent;
import static com.mobile.earnings.tutorial.IntroActivity.getIntroIntent;
import static com.mobile.earnings.utils.Constantaz.EXTRA_CITY_NAME;
import static com.mobile.earnings.utils.Constantaz.EXTRA_LAT;
import static com.mobile.earnings.utils.Constantaz.EXTRA_LON;
import static com.mobile.earnings.utils.Constantaz.GOOGLE_REQUEST_CODE;

public class LoginActivity extends BaseActivity implements LoginView, GoogleApiClient.OnConnectionFailedListener, OnRegisterListener{

	@BindView(R.id.loginFacebook)
	ImageView loginFB;
	@BindView(R.id.loginPromoET)
	EditText  promoET;

	private CallbackManager    facebookCallBackManager;
	private GoogleApiClient    client;
	private LoginPresenterImpl presenter;
	private RegisterModule     registerModule;
	private String             cityName;
	private float lat, lon;

	public static Intent getLoginIntent(@NonNull Context context,
										@NonNull String cityName,
										float lat, float lon){
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(EXTRA_CITY_NAME, cityName);
		intent.putExtra(EXTRA_LAT, lat);
		intent.putExtra(EXTRA_LON, lon);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);

		presenter = new LoginPresenterImpl(this);
		registerModule = new RegisterModule();
		registerModule.setOnRegisterSuccessListener(this);

		setSupportActionBar(setToolBar(getResources().getString(R.string.login_toolbar_title)));
		displayHomeAsUpEnabled(true);
		cityName = getIntent().getStringExtra(EXTRA_CITY_NAME);
		lat = getIntent().getFloatExtra(EXTRA_LAT, 0f);
		lon = getIntent().getFloatExtra(EXTRA_LON, 0f);
		///////////***********FACEBOOK****************////////
		facebookCallBackManager = CallbackManager.Factory.create();
		//////*****************GOOGLE**********//////
		initGoogleClient();
	}

	@Override
	protected void onResume(){
		super.onResume();
		AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onPause(){
		super.onPause();
		AppEventsLogger.deactivateApp(this);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
		Log.e("GOOGLE", "Sign-in error: \n" + connectionResult.toString());
	}

	@Override
	public void onSuccess(AuthorizationResponse model){
		if(App.getPrefs().getBoolean(Constantaz.PREFS_TUTORIAL, false)) {
			startFinishingActivity(getMainIntent(this));
		} else{
			startFinishingActivity(getIntroIntent(this));
		}
	}

	@Override
	public void onError(@StringRes int resourceId){
		DialogInterface.OnClickListener onPositiveButClicked = new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		};
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DefaultDialogStyle);
		dialog.setMessage(resourceId).setPositiveButton(android.R.string.ok, onPositiveButClicked).create();
		if(!isFinishing()) {
			dialog.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		facebookCallBackManager.onActivityResult(requestCode, resultCode, data);
		////////////**********VK*******////////////
		VkLoginCallback vkLoginCallback = new VkLoginCallback(registerModule, promoET.getText().toString(), cityName, lat, lon);
		if(!VKSdk.onActivityResult(requestCode, resultCode, data, vkLoginCallback)) {
			super.onActivityResult(requestCode, resultCode, data);
		}
		////****************GOOGLE**********///////
		if(requestCode == GOOGLE_REQUEST_CODE) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			handleSignInResult(result);
		}
	}

	@Override
	public void informUser(int resourceId){
		Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
	}

	@OnClick(R.id.loginVk)
	void loginUsingVk(){
		VKSdk.login(this, "wall");
	}

	@OnClick(R.id.loginGoogle)
	void loginUsingGoogle(){
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
		startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);
	}

	@OnClick(R.id.loginFacebook)
	void onFacebookButClick(){
		FacebookResultCallback callback = new FacebookResultCallback(registerModule, promoET.getText().toString(), cityName, lat, lon);
		LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
		LoginManager.getInstance().registerCallback(facebookCallBackManager, callback);
	}

	private void initGoogleClient(){
		GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
		client = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();
	}

	private void handleSignInResult(GoogleSignInResult result){
		if(result != null && result.isSuccess()) {
			GoogleSignInAccount acct = result.getSignInAccount();
			if(acct != null) {
				String promoCode = promoET.getText().toString();
				String name = acct.getDisplayName();
				String login = acct.getGivenName();
				String email = acct.getEmail();
				registerModule.registerUser(promoCode.isEmpty() ? null : promoCode, name, login, cityName, lat, lon, email, null, 0, "");
			} else{
				Toast.makeText(LoginActivity.this, R.string.registerDialogMessage, Toast.LENGTH_LONG).show();
				Crashlytics.log("GoogleSignIn_Account==null");
			}
		} else{
			Toast.makeText(LoginActivity.this, R.string.registerDialogMessage, Toast.LENGTH_LONG).show();
		}
	}

}
