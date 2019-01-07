package com.mobile.earnings.ads.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.mobile.earnings.ads.annotations.ImplementedOfferWallPlatforms.ADS_CENT_OFFER_WALL;
import static com.mobile.earnings.ads.annotations.ImplementedOfferWallPlatforms.AD_TRIAL;
import static com.mobile.earnings.ads.annotations.ImplementedOfferWallPlatforms.FYBER_OFFER_WALL;
import static com.mobile.earnings.ads.annotations.ImplementedOfferWallPlatforms.OFFER_TORO;

@IntDef({FYBER_OFFER_WALL,
		ADS_CENT_OFFER_WALL,
		AD_TRIAL,
		OFFER_TORO})
@Retention(RetentionPolicy.SOURCE)
public @interface ImplementedOfferWallPlatforms{

	int FYBER_OFFER_WALL = 0;
	int ADS_CENT_OFFER_WALL = 1;
	int AD_TRIAL = 2;
	int OFFER_TORO = 3;
	int OFFER_WALL_PLATFORMS_COUNT = 4;
}
