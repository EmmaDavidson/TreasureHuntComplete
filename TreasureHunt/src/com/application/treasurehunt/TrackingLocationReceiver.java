package com.application.treasurehunt;

import android.content.Context;
import android.location.Location;

import Mapping.LocationReceiver;
import Mapping.MapManager;

/* Android Programming Nerd Ranch Guide - Entire class taken from here*/

/* The purpose of this class is to receive the location broadcasted by a device's location services, regardless of the user
 * interface being visible [Page 1378]*/
public class TrackingLocationReceiver extends LocationReceiver {
	
	//DONT NEED THIS CLASS BECAUSE DONT WANT TO SAVE LOCATION ALL OF THE TIME...
	/* Method that takes the location received by the location services and enters this location into the local SQLite database.*/
	@Override
	protected void onLocationReceived(Context c, Location loc) {
		MapManager.get(c).insertLocation(loc);
	}

}
