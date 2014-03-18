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

/* The purpose of this class is to hold data about companies; used when a participant is searching for a particular
 * company in order to view and register with that company's treasure hunts. 
 * This data is to be held locally on a participant's device. */
public class Company {
	
   	  /* Global Variables for Company*/
	  private String mCompanyName;
	  private int mAdministratorId; 
	  private String mCompanyPassword;

	  /* Getter and setters*/
	  public String getCompanyName() {
	    return mCompanyName;
	  }
	  
	  public void setCompanyName(String name) {
	    this.mCompanyName = name;
	  }
	  
	  public int getAdministratorId() {
		    return mAdministratorId;
	  }
		  
	  public void setAdministratorId(int id) {
		    this.mAdministratorId = id;
	  }
	  
	  public String getCompanyPassword() {
		  return mCompanyPassword;
	  }
	  
	  public void setCompanyPassword(String password) {
		   this.mCompanyPassword = password;
	  }	  
}
