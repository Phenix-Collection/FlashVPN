package com.mobile.earnings.api.listeners;

import android.support.annotation.StringRes;

import com.mobile.earnings.api.responses.AuthorizationResponse;

public interface OnRegisterListener{

	void onSuccess(AuthorizationResponse model);

	void onError(@StringRes int resourceId);
}
