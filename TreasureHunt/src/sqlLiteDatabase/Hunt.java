package sqlLiteDatabase;

public class Hunt {
	  private int huntId;
	  private String huntName;

	  public int getHuntId() {
	    return huntId;
	  }

	  public String getHuntName() {
	    return huntName;
	  }
	  
	  public void setHuntId(int huntId) {
		    this.huntId = huntId;
		  }

	  public void setHuntName(String hunt) {
	    this.huntName = hunt;
	  }
	  
	  public String getHuntPassword() {
		    return huntName;
		  }


	  // Will be used by the ArrayAdapter in the ListView
	  @Override
	  public String toString() {
	    return huntName;
	  }
	} 