package com.mobile.earnings.api;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mobile.earnings.R;
import com.crashlytics.android.Crashlytics;

import static com.unity3d.ads.misc.Utilities.runOnUiThread;

public class BaseFragment extends Fragment{

	private ProgressDialog progressDialog;
	private BaseActivity        activityContext;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activityContext = (BaseActivity) getActivity();
	}

	/**
	 * Replace fragment on screen
	 *
	 * @param fragment your fragment
	 */
	public void replaceReferralFragment(Fragment fragment){
		getChildFragmentManager().beginTransaction().replace(R.id.referalContainer, fragment).commitAllowingStateLoss();
	}

	public void showProgressDialog(){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(progressDialog == null){
					progressDialog = new ProgressDialog(activityContext);
					progressDialog.setCancelable(false);
					if(activityContext.isFinishing()) {
						return;
					}
					try{
						progressDialog.show();
					} catch(IllegalArgumentException e){
						Crashlytics.logException(e);
					}
				}
			}
		});
	}

	public void hideProgressDialog(){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(progressDialog != null){
					if(activityContext.isFinishing()) {
						return;
					}
					try{
						progressDialog.dismiss();
					} catch(IllegalArgumentException e){
						Crashlytics.logException(e);
					}
					progressDialog = null;
				}
			}
		});
	}

	@Override
	public void onDestroy(){
		hideProgressDialog();
		super.onDestroy();
	}
}
