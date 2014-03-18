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

/* The purpose of this class is to hold data about leader boards. This data is to be held locally on a participant's device.*/

public class Leaderboard {
	
	  /* Global variables for Leaderboard.*/
	  private int mTally;
	  private String mUserName;
	  private float mElapsedTime;
	  
	  /* Getters and setters. */
	  public int getUserTally() {
		    return mTally;
		  }
	  
	  public void setUserTally(int score) {
		    this.mTally = score;
		  }

	  public String getUserName() {
	    return mUserName;
	  }

	  public void setUserName(String user) {
	    this.mUserName = user;
	  }
	  
	  public float getUserElapsedTime() {
		    return mElapsedTime;
		  }
	  
	  public void setUserElapsedTime(float time) {
		  this.mElapsedTime = time;
	  }
}
