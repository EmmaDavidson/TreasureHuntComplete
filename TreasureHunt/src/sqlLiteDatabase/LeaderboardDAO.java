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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/* This singleton class handles the interaction between the application and SQLite (local) database for the Leaderboard table.
 * Based originally on //http://www.vogella.com/articles/AndroidSQLite/article.html*/

public class LeaderboardDAO {
	
	  /* Global variables for LeaderboardDAO*/
	  private static LeaderboardDAO mLeaderboardDAO;
	  private SQLiteDatabase mDatabase;
	  private MySQLiteHelper mDbHelper;
	  private String[] mAllColumns = { MySQLiteHelper.COLUMN_LEADERBOARD_USERNAME, MySQLiteHelper.COLUMN_LEADERBOARD_TALLY, MySQLiteHelper.COLUMN_LEADERBOARD_ELAPSED_TIME };
	  
	  /* Constructor*/
	  private LeaderboardDAO(Context context) {
	    mDbHelper = new MySQLiteHelper(context);
	  }
	  
	  /* Method that returns an instance of MapManager.*/
	  public static LeaderboardDAO getInstance(Context c) {
			
			if(mLeaderboardDAO == null) {
				mLeaderboardDAO = new LeaderboardDAO(c.getApplicationContext());
			}
			return mLeaderboardDAO;
	   }

	  /* Method handling what happens when the helper is first opened. It retrieves access to the SQLite (local) database. */
	  public void open() throws SQLException {
		  mDatabase = mDbHelper.getWritableDatabase();
	  }
	  
	  /* Method handling what happens when the helper is closed. It closes access to the SQLite (local) database.*/
	  public void close() {
	    mDbHelper.close();
	  }
	   
	  /* Method that removes all of the current data stored in the Leaderboard table.*/
	  public void refreshLeaderboard() {
		  mDbHelper.refreshTable(mDatabase, MySQLiteHelper.TABLE_HUNT_LEADERBOARD);
	  }

	  /* Method that inserts a new row into the Leaderboard table with the data supplied. It returns the Leaderboard instance 
	   * that has just been created. */
	  public Leaderboard addLeaderboardResult(String username, int tally, float elapsedTime) {
		    ContentValues values = new ContentValues();
		    values.put(MySQLiteHelper.COLUMN_LEADERBOARD_USERNAME, username);
		    values.put(MySQLiteHelper.COLUMN_LEADERBOARD_TALLY, tally);
		    values.put(MySQLiteHelper.COLUMN_LEADERBOARD_ELAPSED_TIME, elapsedTime);
		    long insertId = mDatabase.insert(MySQLiteHelper.TABLE_HUNT_LEADERBOARD, null,
		        values);
		    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_HUNT_LEADERBOARD,
		        mAllColumns, null, null,
		        null, null, null);
		    cursor.moveToFirst();
		    Leaderboard newComment = cursorToLeaderboardEntry(cursor);
		    cursor.close();
		    return newComment;
		  }

	  /* Method returns a list of all Leaderboard data currently present in the Leaderboard table.*/
	  public List<Leaderboard> getAllResults() {
	    List<Leaderboard> leaderboardEntries = new ArrayList<Leaderboard>();
	    //http://stackoverflow.com/questions/12339121/multiple-orderby-in-sqlitedatabase-query-method
	    String orderBy = MySQLiteHelper.COLUMN_LEADERBOARD_TALLY + " DESC, " + MySQLiteHelper.COLUMN_LEADERBOARD_ELAPSED_TIME + " ASC";
	    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_HUNT_LEADERBOARD,
	    		mAllColumns, null, null, null, null, orderBy);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Leaderboard leaderboardEntry = cursorToLeaderboardEntry(cursor);
	      leaderboardEntries.add(leaderboardEntry);
	     
	      cursor.moveToNext();
	    }
	    
	    cursor.close();
	    return leaderboardEntries;
	  }
	  
	  /* A cursor that creates a Leaderboard instance from a given Cursor i.e. retrieves the data found in a single row
	   * returned by a query for the Leaderboard table.*/
	  //http://stackoverflow.com/questions/6781954/android-3-0-couldnt-read-row-column-from-cursor-window
	  private Leaderboard cursorToLeaderboardEntry(Cursor cursor) {
		  Leaderboard leaderboardResult = new Leaderboard();
		    leaderboardResult.setUserName(cursor.getString(0));
		    leaderboardResult.setUserTally(cursor.getInt(1));
		    leaderboardResult.setUserElapsedTime(cursor.getFloat(2));
		    return leaderboardResult;
	  }
}
