package com.mobile.earnings.autorization.presentersImpl;

import com.mobile.earnings.autorization.presenters.LoginPresenter;
import com.mobile.earnings.autorization.views.LoginView;

public class LoginPresenterImpl implements LoginPresenter{

	private LoginView view;

	public LoginPresenterImpl(LoginView view){
		this.view = view;
	}

}
