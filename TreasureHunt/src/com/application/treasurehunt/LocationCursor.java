package com.application.treasurehunt;

import Utilities.MySQLiteHelperForMaps;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;

public class LocationCursor extends CursorWrapper {

	public LocationCursor(Cursor cursor) {
		super(cursor);
	}
	
	public Location getLocation()
	{
		if(isBeforeFirst() || isAfterLast())
		{
			return null;
		}
		
		Location loc = new Location("GPS");
		loc.setLatitude(getDouble(getColumnIndex(MySQLiteHelperForMaps.COLUMN_MAPS_LATITUDE)));
		loc.setLongitude(getDouble(getColumnIndex(MySQLiteHelperForMaps.COLUMN_MAPS_LONGTITUDE)));
		loc.setAltitude(getDouble(getColumnIndex(MySQLiteHelperForMaps.COLUMN_MAPS_ALTITUDE)));
		return loc;
	}


}
