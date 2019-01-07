package com.mobile.earnings.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.mobile.earnings.R;
import com.crashlytics.android.Crashlytics;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity{

	@Override
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState){
		super.onCreate(savedInstanceState, persistentState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	protected void attachBaseContext(Context newBase){
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

	@Override
	protected void onResume(){
		super.onResume();
	}

	@Override
	protected void onPause(){
		super.onPause();
	}

	@Override
	protected void onStop(){
		super.onStop();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
	}

	public void startFinishingActivity(Intent intent){
		startActivity(intent);
		finish();
	}

	public void displayHomeAsUpEnabled(boolean enable){
		if(getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
	}

	/**
	 * Replace current screen with fragment
	 *
	 * @param fragment
	 */
	public void replaceFragment(final Fragment fragment){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if(isDestroyed() || isFinishing()) {
				return;
			}
		} else{
			if(isFinishing()) {
				return;
			}
		}
		try{
			getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, fragment).commitAllowingStateLoss();
		} catch(IllegalStateException e){
			Crashlytics.logException(e);
		}
	}

	private TextView titleTV;

	public interface ToolbarClickHelper{
		void onElementClick(int tabPosition);
	}

	public Toolbar setToolBar(String title){
		Toolbar toolBar = (Toolbar) findViewById(R.id.tool_bar);
		titleTV = (TextView) toolBar.findViewById(R.id.toolBarTitle);
		titleTV.setText(title);
		return toolBar;
	}

	public void updateToolbarTitle(String title){
		titleTV.setVisibility(View.VISIBLE);
		if(titleTV != null)
			titleTV.setText(title);
	}

	/**
	 * Hides the soft keyboard
	 */
	public void hideSoftKeyboard(){
		if(getCurrentFocus() != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private ProgressDialog progressDialog;

	public void showProgressDialog(){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(progressDialog == null) {
					progressDialog = new ProgressDialog(BaseActivity.this);
					progressDialog.setCancelable(false);
					if(isFinishing()) {
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

	public void showProgressDialog(final int title){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(progressDialog == null) {
					progressDialog = new ProgressDialog(BaseActivity.this, R.style.LoadingProgressDialog);
					progressDialog.setTitle(title);
					progressDialog.setCancelable(false);
					if(isFinishing()) {
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
				if(progressDialog != null && progressDialog.isShowing()) {
					if(isFinishing()) {
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

}
