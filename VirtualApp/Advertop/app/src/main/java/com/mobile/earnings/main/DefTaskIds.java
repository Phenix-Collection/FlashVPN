package com.mobile.earnings.main;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.mobile.earnings.main.DefTaskIds.DEF_AD_MOB;
import static com.mobile.earnings.main.DefTaskIds.DEF_BONUS;
import static com.mobile.earnings.main.DefTaskIds.DEF_COMMEND;
import static com.mobile.earnings.main.DefTaskIds.DEF_OFFERWALL;
import static com.mobile.earnings.main.DefTaskIds.DEF_REFERRAL;
import static com.mobile.earnings.main.DefTaskIds.DEF_ROULETTE;
import static com.mobile.earnings.main.DefTaskIds.DEF_VIDEO_ZONE;
import static com.mobile.earnings.main.DefTaskIds.DEF_VK;


@IntDef({
		DEF_AD_MOB,
		DEF_OFFERWALL,
		DEF_ROULETTE,
		DEF_VIDEO_ZONE,
		DEF_REFERRAL,
		DEF_VK,
		DEF_BONUS,
		DEF_COMMEND
})
@Retention(RetentionPolicy.SOURCE)
public @interface DefTaskIds{

	int DEF_AD_MOB = 0;
	int DEF_OFFERWALL = -1;
	int DEF_ROULETTE = -2;
	int DEF_VIDEO_ZONE = -3;
	int DEF_REFERRAL = -4;
	int DEF_VK = -5;
	int DEF_BONUS = -6;
	int DEF_COMMEND = -7;
}
