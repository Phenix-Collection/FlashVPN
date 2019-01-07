package com.mobile.earnings.single.task;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.mobile.earnings.single.task.ReviewState.AVAILABLE;
import static com.mobile.earnings.single.task.ReviewState.FAILED;
import static com.mobile.earnings.single.task.ReviewState.MODERATING;
import static com.mobile.earnings.single.task.ReviewState.PAID;



@StringDef({AVAILABLE,
		MODERATING,
		PAID,
		FAILED
})
@Retention(RetentionPolicy.SOURCE)
public @interface ReviewState{

	String AVAILABLE = "available";
	String MODERATING = "moderating";
	String PAID = "paid";
	String FAILED = "failed";
}
