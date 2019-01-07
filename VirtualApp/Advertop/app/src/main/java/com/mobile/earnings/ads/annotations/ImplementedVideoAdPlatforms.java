package com.mobile.earnings.ads.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.mobile.earnings.ads.annotations.ImplementedVideoAdPlatforms.AD_COLONY;
import static com.mobile.earnings.ads.annotations.ImplementedVideoAdPlatforms.APP_NEXT;
import static com.mobile.earnings.ads.annotations.ImplementedVideoAdPlatforms.FACEBOOK;
import static com.mobile.earnings.ads.annotations.ImplementedVideoAdPlatforms.FYBER_REWARDED_VIDEO;
import static com.mobile.earnings.ads.annotations.ImplementedVideoAdPlatforms.UNITY_AD_VIDEO;


@IntDef({UNITY_AD_VIDEO, AD_COLONY, APP_NEXT, FYBER_REWARDED_VIDEO, FACEBOOK})
@Retention(RetentionPolicy.SOURCE)
public @interface ImplementedVideoAdPlatforms{

	int UNITY_AD_VIDEO           = 0;
	int AD_COLONY                = 1;
	int APP_NEXT                 = 2;
	int FYBER_REWARDED_VIDEO     = 3;
	int FACEBOOK                 = 4;
	int VIDEO_AD_PLATFORMS_COUNT = 5;
}
