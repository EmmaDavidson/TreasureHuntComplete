/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package sqlLiteDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/* This singleton class handles the interaction between the application and SQLite (local) database for 
 * the Hunt and UserHunt tables. Based originally on //http://www.vogella.com/articles/AndroidSQLite/article.html*/

public class HuntDAO {

  /* Global variables for HuntDAO*/
  private static HuntDAO mHuntDAO;
  private SQLiteDatabase mDatabase;
  private MySQLiteHelper mDbHelper;
  private String[] mAllColumns = { MySQLiteHelper.COLUMN_HUNT_ID, MySQLiteHelper.COLUMN_HUNTNAME, 
		  MySQLiteHelper.COLUMN_HUNT_DESCRIPTION, MySQLiteHelper.COLUMN_END_DATE };
  private String[] mAllColumnsUserHunt  = { MySQLiteHelper.COLUMN_USER_HUNTS_ID, MySQLiteHelper.COLUMN_USER_HUNTS_HUNTNAME, 
		  MySQLiteHelper.COLUMN_USER_HUNTS_DESCRIPTION, MySQLiteHelper.COLUMN_USER_HUNTS_END_DATE };

  /* Constructor*/
  private HuntDAO(Context context) {
    mDbHelper = new MySQLiteHelper(context);
  }
  
  /* Method that returns an instance of MapManager.*/
  public static HuntDAO getInstance(Context c) {
		
		if(mHuntDAO == null) {
			mHuntDAO = new HuntDAO(c.getApplicationContext());
		}
		return mHuntDAO;
   }

  /* Method handling what happens when the helper is first opened. It retrieves access to the SQLite (local) database. */
  public void open() throws SQLException {
	  mDatabase = mDbHelper.getWritableDatabase();
	  refreshCompanyHunts();
	  refreshMyHunts();
  }
  
  /* Method handling what happens when the helper is closed. It closes access to the SQLite (local) database.*/
  public void close() {
	    mDbHelper.close();
	  }

  /* Method that removes all of the current data stored in the Hunt table.*/
  public void refreshCompanyHunts() {
	  mDbHelper.refreshTable(mDatabase, MySQLiteHelper.TABLE_HUNTS);
  }
  
  /* Method that removes all of the current data stored in the UserHunt table.*/
  public void refreshMyHunts() {
	  mDbHelper.refreshTable(mDatabase, MySQLiteHelper.TABLE_USER_HUNTS);
  }
  
  /* Method that inserts a new row into the Hunt table with the data supplied. It returns the Hunt that has just been created. */
  public Hunt addHunt(int huntId, String huntName, String huntDescription, String endDate) {
	    ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_HUNT_ID, huntId);
	    values.put(MySQLiteHelper.COLUMN_HUNTNAME, huntName);
	    values.put(MySQLiteHelper.COLUMN_HUNT_DESCRIPTION, huntDescription);
	    values.put(MySQLiteHelper.COLUMN_END_DATE, endDate);
	    long insertId = mDatabase.insert(MySQLiteHelper.TABLE_HUNTS, null,
	        values);
	    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_HUNTS,
	        mAllColumns, null, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Hunt newComment = cursorToHunt(cursor);
	    cursor.close();
	    return newComment;
	  }

  /* Method returns a list of all Hunts currently present in the Hunt table.*/
  public List<Hunt> getAllHunts() {
    List<Hunt> listOfHunts = new ArrayList<Hunt>();

    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_HUNTS,
        mAllColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Hunt hunt = cursorToHunt(cursor);
      listOfHunts.add(hunt);
      cursor.moveToNext();
    }
    
    cursor.close();
    return listOfHunts;
  }
  
  /* Method that inserts a new row into the UserHunt table with the data supplied. 
   * It returns the Hunt that has just been created. */
  public Hunt addUserHunt(int huntId, String huntName, String huntDescription, String endDate) {
	  ContentValues values = new ContentValues();
	  	values.put(MySQLiteHelper.COLUMN_USER_HUNTS_ID, huntId);
	    values.put(MySQLiteHelper.COLUMN_USER_HUNTS_HUNTNAME, huntName);
	    values.put(MySQLiteHelper.COLUMN_USER_HUNTS_DESCRIPTION, huntDescription);
	    values.put(MySQLiteHelper.COLUMN_USER_HUNTS_END_DATE, endDate);
	    long insertId = mDatabase.insert(MySQLiteHelper.TABLE_USER_HUNTS, null,
	        values);
	    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_USER_HUNTS,
	    		mAllColumnsUserHunt, null, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Hunt newComment = cursorToUserHunt(cursor);
	    cursor.close();
	    return newComment;
  }
  
  /* Method that returns a list of all Hunts currently present in the UserHunt table */
  public List<Hunt> getAllUserHunts() {
	    List<Hunt> listOfHunts = new ArrayList<Hunt>();

	    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_USER_HUNTS,
	    		mAllColumnsUserHunt, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Hunt hunt = cursorToUserHunt(cursor);
	      listOfHunts.add(hunt);
	      cursor.moveToNext();
	    }
	    
	    cursor.close();
	    return listOfHunts;
	  }
  
  /* Method that returns details of a Hunt for a given hunt name. */
  public Hunt getParticularHunt(String huntName) {
	  String arguments =  "HuntName = '"+huntName + "'";
	  Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_USER_HUNTS, mAllColumnsUserHunt , arguments, null , null , null, null);
	  cursor.moveToFirst();
	  if(!cursor.isAfterLast()) {
		  Hunt hunt = cursorToUserHunt(cursor);
		  return hunt;
	  }
	  return null;
	  
  }

  /* A cursor that creates a Hunt instance from a given Cursor i.e. retrieves the data found in a single row
   * returned by a query for the Hunt table.*/
  //http://stackoverflow.com/questions/6781954/android-3-0-couldnt-read-row-column-from-cursor-window
  private Hunt cursorToHunt(Cursor cursor) {
    Hunt hunt = new Hunt();
    hunt.setHuntId(cursor.getInt(0));
    hunt.setHuntName(cursor.getString(1));
    hunt.setHuntDescription(cursor.getString(2));
    hunt.setEndDate(cursor.getString(3));
    return hunt;
  }
  
  /* A cursor that creates a Hunt instance from a given Cursor i.e. retrieves the data found in a single row
   * returned by a query for the UserHunt table.*/
  private Hunt cursorToUserHunt(Cursor cursor) {
	    Hunt hunt = new Hunt();
	    hunt.setHuntId(cursor.getInt(0));
	    hunt.setHuntName(cursor.getString(1));
	    hunt.setHuntDescription(cursor.getString(2));
	    hunt.setEndDate(cursor.getString(3));
	    return hunt;
	  }
} 
