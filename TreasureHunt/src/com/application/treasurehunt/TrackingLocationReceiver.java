package com.application.treasurehunt;


import android.content.Context;
import android.location.Location;

public class TrackingLocationReceiver extends LocationReceiver {
	
	@Override
	protected void onLocationReceived(Context c, Location loc)
	{
		MapManager.get(c).insertLocation(loc);
	}

}
