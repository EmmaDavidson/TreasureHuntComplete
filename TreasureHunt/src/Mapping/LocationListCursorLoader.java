package Mapping;

import android.content.Context;
import android.database.Cursor;

import sqlLiteDatabase.SQLiteCursorLoader;

/* The purpose of this class is to handle the loading of a list of locations off the main UI thread of the application. Based on
 * the Nerd Ranch Guide Page 1464. */ 
public class LocationListCursorLoader extends SQLiteCursorLoader {
	
	/*
	 * Global variables used within LocationListCursorLoader.
	 */
	private int mHuntParticipantId;

	/* Constructor for LocationListCursorLoader. Page 1464.*/
	public LocationListCursorLoader(Context context, int participantId) {
		super(context);
		mHuntParticipantId = participantId;
	}

	/* Method that returns a list of locations for a given HuntParticipantId asynchronously and loads a Cursor with the list. Page 1464.*/
	@Override
	protected Cursor loadCursor() {
		return MapManager.get(getContext()).queryLocationsForMapsAsync(mHuntParticipantId);
	}
}
