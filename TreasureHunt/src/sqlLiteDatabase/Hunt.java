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

/* The purpose of this class is to hold data about treasure hunts. This data is to be held locally on a participant's device.*/

public class Hunt {
	  
	  /* Global variables for Hunt.*/
	  private int mHuntId;
	  private String mHuntName;
	  private String mHuntDescription;
	  private String mEndDate;

	  /* Getters and setters. */
	  public int getHuntId() {
	    return mHuntId;
	  }

	  public String getHuntName() {
	    return mHuntName;
	  }
	  
	  public String getHuntDescription() {
		    return mHuntDescription;
		  }
	  
	  public String getEndDate() {
		    return mEndDate;
		  }
	  
	  public void setHuntId(int huntId) {
		    this.mHuntId = huntId;
		  }

	  public void setHuntName(String hunt) {
	    this.mHuntName = hunt;
	  }
	  
	  public void setHuntDescription(String description) {
		    this.mHuntDescription = description;
		  }
	  
	  public void setEndDate(String endDate) {
		    this.mEndDate = endDate;
		  }
	} 