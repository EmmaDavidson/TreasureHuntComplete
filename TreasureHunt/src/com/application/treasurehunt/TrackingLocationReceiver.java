/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package com.application.treasurehunt;

import android.content.Context;
import android.location.Location;

import Mapping.LocationReceiver;
import Mapping.MapManager;

/* Android Programming Nerd Ranch Guide - Entire class taken from here, I do not own this class. */

/* The purpose of this class is to receive the location broadcasted by a device's location services, 
 * regardless of the user interface being visible [Based upon Nerd Ranch Guide Page 1378]*/
public class TrackingLocationReceiver extends LocationReceiver {
	
	
	/* Method that takes the location received by the location services and enters this location into the local SQLite database.
	 * Not currently being used but could be utilised for later expansion.*/
	@Override
	protected void onLocationReceived(Context c, Location loc) {
		MapManager.get(c).insertLocation(loc);
	}

}
