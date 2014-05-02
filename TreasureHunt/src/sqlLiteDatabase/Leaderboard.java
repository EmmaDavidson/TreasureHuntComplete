/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
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
