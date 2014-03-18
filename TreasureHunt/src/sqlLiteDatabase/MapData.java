/*
 * Copyright (C) 2013 The Android Open Source Project 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
