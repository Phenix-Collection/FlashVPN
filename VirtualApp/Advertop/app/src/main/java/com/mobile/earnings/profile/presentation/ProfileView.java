package com.mobile.earnings.profile.presentation;

import com.mobile.earnings.api.BaseView;
import com.mobile.earnings.api.responses.UserResponse;




public interface ProfileView extends BaseView{

	void setProfileData(UserResponse.UserInfo info);
}
