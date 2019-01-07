package com.mobile.earnings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mobile.earnings.fcm.AppFcmDataReceiver;
import com.mobile.earnings.fcm.FcmBackgroundReceiver;
import com.mobile.earnings.fcm.FcmTokenRefresher;
import com.mobile.earnings.utils.Constantaz;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.yandex.metrica.YandexMetrica;

import io.fabric.sdk.android.Fabric;
import rx_fcm.internal.RxFcm;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends MultiDexApplication{

	private volatile static App              instance;
	private static          TelephonyManager tMgr;

	public static Context getContext(){
		return instance.getApplicationContext();
	}

	public static Resources getRes(){
		return getContext().getResources();
	}

	public static SharedPreferences getPrefs(){
		return PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	public static void setInstance(App instance){
		App.instance = instance;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		final Fabric fabric = new Fabric.Builder(this)
				.kits(new Crashlytics())
				.build();
		Fabric.with(fabric);
		setInstance(this);
		tMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		registerGCM();
		VKSdk.initialize(getApplicationContext());
		vkAccessTokenTracker.startTracking();
		FacebookSdk.sdkInitialize(getApplicationContext());
		// Инициализация AppMetrica SDK
		YandexMetrica.activate(getApplicationContext(), Constantaz.YANDEX_METRICA_API_KEY);
		// Отслеживание активности пользователей
		YandexMetrica.enableActivityAutoTracking(this);
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Roboto-Regular.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build()
		);
		//TapJoy
//		connectTapJoy();
	}

	@Override
	protected void attachBaseContext(Context base){
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	private void registerGCM(){
		RxFcm.Notifications.init(this, new AppFcmDataReceiver(), new FcmBackgroundReceiver());
		RxFcm.Notifications.onRefreshToken(new FcmTokenRefresher());
	}

	public static String getDeviceID(){
		String deviceID = null;
		if(tMgr != null) {
			deviceID = tMgr.getDeviceId();
		}
		if(deviceID == null)
			deviceID = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
		return deviceID;
	}

	public static String getPhoneNumber(){
		if(tMgr != null){
			return tMgr.getLine1Number();
		}
		return "";
	}

	VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker(){
		@Override
		public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken){
			if(newToken == null) {
				Log.e("VK", "Token is invalid");
			}
		}
	};
}
