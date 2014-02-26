package com.application.treasurehunt;

import sqlLiteDatabase.SQLiteCursorLoader;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

public class LocationListCursorLoader extends SQLiteCursorLoader {
	
	private int mParticipantId;

	public LocationListCursorLoader(Context context, int participantId) {
		super(context);
		mParticipantId = participantId;
	}

	@Override
	protected Cursor loadCursor() {
		return MapManager.get(getContext()).queryLocationsForMapsAsync(mParticipantId);
	}
}
