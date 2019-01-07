package com.mobile.earnings.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.mobile.earnings.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;



public class Demander{

	private final RxPermissions                rxPermissions;
	private       String[]                     permissions;
	private final Activity                     activity;
	private       OnPermissionsGrantedListener listener;

	public interface OnPermissionsGrantedListener{
		void isAllPermissionsGranted();
	}

	/**
	 * Should be called from onCreate() or View.onFinishInflate() methods
	 * @param activity
	 */
	public Demander(Activity activity){
		this.rxPermissions = new RxPermissions(activity);
		this.activity = activity;
	}

	/**
	 * Ask user to confirm displayed permissions
	 *
	 * @param permissions all needed permissions
	 */
	public void demand(final String... permissions){
		copyToArray(permissions);
		rxPermissions.request(permissions).subscribe(new Action1<Boolean>(){
			@Override
			public void call(Boolean granted){
				if(!granted) {
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.startActivity(intent);
					Toast.makeText(activity, activity.getString(R.string.permissionError), Toast.LENGTH_LONG).show();
				} else {
					listener.isAllPermissionsGranted();
				}
			}
		});
	}

	public void setOnPermissionsGrantedListener(OnPermissionsGrantedListener listener){
		this.listener = listener;
	}

	/**
	 * Checking if all permissions granted
	 *
	 * @return true if granted otherwise false
	 */
	public boolean isGranted(){
		boolean granted = true;
		for(String permission : permissions){
			if(!rxPermissions.isGranted(permission))
				granted = false;
		}
		return granted;
	}

	private void copyToArray(String... permissions){
		this.permissions = new String[permissions.length];
		System.arraycopy(permissions, 0, this.permissions, 0, permissions.length);
	}
}