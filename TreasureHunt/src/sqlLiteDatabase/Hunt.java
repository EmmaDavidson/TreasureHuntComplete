/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
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