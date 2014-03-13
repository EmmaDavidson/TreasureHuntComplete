package sqlLiteDatabase;

import com.application.treasurehunt.MapManager;
import android.content.Context;
import android.location.Location;

public class LastLocationLoader extends DataLoader<Location> {

	private int mParticipantId;
	
	public LastLocationLoader(Context context, int participantId) {
		super(context);
		mParticipantId = participantId;
	}

	@Override
	public Location loadInBackground() {
		
		return MapManager.get(getContext()).getLastLocationForMap(mParticipantId);
	}


}
