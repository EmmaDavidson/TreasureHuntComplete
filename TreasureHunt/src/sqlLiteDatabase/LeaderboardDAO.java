package sqlLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import Utilities.MySQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LeaderboardDAO {
	 private SQLiteDatabase database;
	  private MySQLiteHelper dbHelper;
	  private String[] allColumns = { MySQLiteHelper.COLUMN_LEADERBOARD_USERNAME, MySQLiteHelper.COLUMN_LEADERBOARD_TALLY, MySQLiteHelper.COLUMN_LEADERBOARD_ELAPSED_TIME };
	  public LeaderboardDAO(Context context) {
	    dbHelper = new MySQLiteHelper(context);
	  }

	  public void open() throws SQLException {
		  database = dbHelper.getWritableDatabase();
		  //Unclean - drops the database each time and re-adds the table.
		  updateDatabaseLocally();
	  }
	  
	  public void updateDatabaseLocally()
	  {
		  dbHelper.onUpgrade(database, database.getVersion(), database.getVersion());
	  }

	  public void close() {
	    dbHelper.close();
	  }
	  
	  public Leaderboard addLeaderboardResult(String username, int tally, float elapsedTime) {
		    ContentValues values = new ContentValues();
		    values.put(MySQLiteHelper.COLUMN_LEADERBOARD_USERNAME, username);
		    values.put(MySQLiteHelper.COLUMN_LEADERBOARD_TALLY, tally);
		    values.put(MySQLiteHelper.COLUMN_LEADERBOARD_ELAPSED_TIME, elapsedTime);
		    long insertId = database.insert(MySQLiteHelper.TABLE_HUNT_LEADERBOARD, null,
		        values);
		    Cursor cursor = database.query(MySQLiteHelper.TABLE_HUNT_LEADERBOARD,
		        allColumns, null, null,
		        null, null, null);
		    cursor.moveToFirst();
		    Leaderboard newComment = cursorToLeaderboardEntry(cursor);
		    cursor.close();
		    return newComment;
		  }

	  public List<Leaderboard> getAllResults() {
	    List<Leaderboard> leaderboardEntries = new ArrayList<Leaderboard>();
	    //http://stackoverflow.com/questions/12339121/multiple-orderby-in-sqlitedatabase-query-method
	    String orderBy = MySQLiteHelper.COLUMN_LEADERBOARD_TALLY + " DESC, " + MySQLiteHelper.COLUMN_LEADERBOARD_ELAPSED_TIME + " DESC";
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_HUNT_LEADERBOARD,
	    		allColumns, null, null, null, null, orderBy);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Leaderboard leaderboardEntry = cursorToLeaderboardEntry(cursor);
	      leaderboardEntries.add(leaderboardEntry);
	     
	      cursor.moveToNext();
	    }
	    
	    cursor.close();
	    return leaderboardEntries;
	  }
	  
	  //http://stackoverflow.com/questions/6781954/android-3-0-couldnt-read-row-column-from-cursor-window
	  private Leaderboard cursorToLeaderboardEntry(Cursor cursor) {
		  Leaderboard leaderboardResult = new Leaderboard();
		    leaderboardResult.setUserName(cursor.getString(0));
		    leaderboardResult.setUserTally(cursor.getInt(1));
		    leaderboardResult.setUserElapsedTime(cursor.getFloat(2));
		    return leaderboardResult;
	  }
}
