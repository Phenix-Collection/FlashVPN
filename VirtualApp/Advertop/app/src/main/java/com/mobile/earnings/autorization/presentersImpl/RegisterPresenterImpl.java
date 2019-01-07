package com.mobile.earnings.autorization.presentersImpl;

import com.mobile.earnings.autorization.presenters.RegisterPresenter;
import com.mobile.earnings.autorization.views.RegisterView;

public class RegisterPresenterImpl implements RegisterPresenter{

	private RegisterView view;

	public RegisterPresenterImpl(RegisterView view){
		this.view = view;
	}

}
