package sqlLiteDatabase;

import android.content.Context;
import android.location.Location;

import Mapping.MapManager;

/* Whole class taken from the Nerd Ranch Guide. */

/* The purpose of this class is to 'get the loading of the last location off of the main thread' - Page 1442. 
 * */
public class LastLocationLoader extends DataLoader<Location> {

	/*
	 * Global variables used within LastLocationLoader.
	 */
	private int mParticipantId;
	
	/* Constructor*/
	public LastLocationLoader(Context context, int participantId) {
		super(context);
		mParticipantId = participantId;
	}

	/* Method that retrieves from the SQLite (local) database the last Location that has been saved for the 
	 * given HuntParticipantId.*/
	@Override
	public Location loadInBackground() {
		
		return MapManager.get(getContext()).getLastLocationForMap(mParticipantId);
	}
}
