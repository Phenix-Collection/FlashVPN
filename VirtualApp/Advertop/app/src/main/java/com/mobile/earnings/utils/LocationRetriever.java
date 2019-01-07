package com.mobile.earnings.utils;

import android.content.Context;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mobile.earnings.splash.SplashScreenActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.util.List;
import java.util.Locale;



public class LocationRetriever implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

	private Context                     context;
	private GoogleApiClient             client;
	private LocationRequest             locationRequest;
	private OnLocationRetrievedListener listener;
	private             boolean mIfLocationCheckedAlready = false;
	public static final int     LOCATION_REQUEST_CODE     = 1000;

	public LocationRetriever(Context context){
		this.context = context;
		client = new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
		setUpLocationRequest();
	}

	public interface OnLocationRetrievedListener{
		void onLocationRetrieved(Location location);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle){
		Log.i("GPS", "onConnected: Google client connected");
	}

	@Override
	public void onConnectionSuspended(int i){
		client.connect();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
		Log.e("GPS", "Google client connection failed");
		listener.onLocationRetrieved(null);
	}

	@Override
	public void onLocationChanged(Location location){
		if(location != null) {
			listener.onLocationRetrieved(location);
			removeUpdates();
		} else if(mIfLocationCheckedAlready) {
			listener.onLocationRetrieved(null);
			Log.e("GPS", "Cannot handle location");
		}
		mIfLocationCheckedAlready = true;
	}

	public void connect(){
		if(client != null && !client.isConnected()) {
			client.connect();
		}
	}

	public void disconnect(){
		if(client != null && client.isConnected()) {
			client.disconnect();
			removeUpdates();
		}
	}

	private void setUpLocationRequest(){
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(5 * 1000);
		locationRequest.setFastestInterval(5 * 1000);
		locationRequest.setNumUpdates(1);
	}

	private void removeUpdates(){
		if(client != null && client.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
		}
	}

	public void setOnLocationRetrievedListener(OnLocationRetrievedListener listener){
		this.listener = listener;
	}

	public void createGoogleDialog(){
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().setAlwaysShow(true).addLocationRequest(locationRequest);
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
		if(result != null) {
			result.setResultCallback(new ResultCallback<LocationSettingsResult>(){
				@Override
				public void onResult(@NonNull LocationSettingsResult locationSettingsResult){
					final Status status = locationSettingsResult.getStatus();
					switch(status.getStatusCode()){
						case LocationSettingsStatusCodes.SUCCESS:
							Log.e("GPS", "Success");
							requestUpdates();
							break;
						case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
							Log.e("GPS", "Resolution");
							try{
								if(status.hasResolution()) {
									status.startResolutionForResult((SplashScreenActivity) context, LOCATION_REQUEST_CODE);
								}
							} catch(IntentSender.SendIntentException e){
								Log.e("GPS", "ResolutionDialogFailed");
							}
							break;
						case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
							// Location settings are not satisfied. However, we have no way to fix the
							// settings so we won't show the optionsDialog.
							Log.e("GPS", "Cannot show dialog");
							break;
					}
				}
			});
		}
	}

	@SuppressWarnings("MissingPermission")
	public void requestUpdates(){
		removeUpdates();
		if(client.isConnected()) {
			LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
		}
	}

	@Nullable
	public String getCityName(@NonNull Location location){
		Locale locale = new Locale("ru");
		Geocoder gcd = new Geocoder(context, locale);
		List<Address> addresses = null;
		try{
			addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		} catch(IOException e){
			Log.e("GPS", "Cannot retrieve geo data");
		}
		if(addresses != null && addresses.size() > 0) {
			String cityName = addresses.get(0).getLocality();
			if(cityName != null && cityName.contentEquals("Кропивницький")) {
				cityName = cityName.replace("ь", "");
			}
			Log.d("GPS", "City: " + cityName);
			return cityName;
		} else{
			Log.e("GPS", "Cannot get city");
			return null;
		}
	}

}
