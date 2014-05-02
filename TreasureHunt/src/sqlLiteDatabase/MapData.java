/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package sqlLiteDatabase;
import java.sql.Date;

/* The purpose of this class is to hold data about participant maps; used when dealing with the 
 * mapping aspect of the application. This data is to be held locally on a participant's device. */
public class MapData {

	/* Global Variables for MapData*/
	private Date mStartDate;
	private int mHuntParticipantId;
	
	/* Constructor*/
	public MapData() {
		mHuntParticipantId = -1;
		mStartDate = new Date(System.currentTimeMillis());
	}

	/* Getters and setters*/
	public Date getStartDate() {
		return mStartDate;
	}
	
	public void setStartDate(Date start) {
		mStartDate = start;
	}

	public int getParticipantId() {
		return mHuntParticipantId;
	}
	
	public void setParticipantId(int id) {
		 mHuntParticipantId = id;
	}
	
	public int getDurationSeconds(long endMillis) {
		return (int)((endMillis - mStartDate.getTime()) / 1000);
	}
}
