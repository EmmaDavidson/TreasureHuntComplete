/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
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
