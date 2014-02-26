package sqlLiteDatabase;

public class Leaderboard {
	private int tally;
	  private String userName;
	  private float elapsedTime;
	  
	  public int getUserTally() {
		    return tally;
		  }
	  
	  public void setUserTally(int score) {
		    this.tally = score;
		  }

		  public String getUserName() {
		    return userName;
		  }

		  public void setUserName(String user) {
		    this.userName = user;
		  }
		  
		  public float getUserElapsedTime() {
			    return elapsedTime;
			  }
		  
		  public void setUserElapsedTime(float time) {
			  this.elapsedTime = time;
		  }

}
