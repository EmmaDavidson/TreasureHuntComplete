package Mapping;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/* Entire class taken from the Nerd Ranch Guide - 1343 and 1344.*/

/* Class whose purpose is to 'log out the locations it receives' - Page 1343. */
public class LocationReceiver extends BroadcastReceiver {

	/*
	 * Global variables used within LocationReceiver.
	 */
	private static final String TAG = "LocationReceiver";
	
	/* Method that will handle a location being received by the broadcast receiver. */
	@Override
	public void onReceive(Context context, Intent intent) {
		Location loc = (Location) intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
		
		if(loc != null) {
			onLocationReceived(context, loc);
			return;
		}
		
		if(intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
			boolean enabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
			onProviderEnabledChanged(enabled);
		}	
	}
	
	/* Method to log a debug message if a location has been received. */
	protected void onLocationReceived(Context context, Location loc) {
		Log.d(TAG, this + " Got location from " + loc.getProvider() + " : " + loc.getLatitude() + ", " + loc.getLongitude());
	}
	
	/* Method to log a debug message if the provider (e.g. originally GPS) has changed. */
	protected void onProviderEnabledChanged(boolean enabled) {
		Log.d(TAG, "Provider " + (enabled ? "enabled" : "disabled"));
	} 
}
