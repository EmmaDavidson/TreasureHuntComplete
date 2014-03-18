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

/* This class handles the interaction between the application and SQLite (local) database for the Hunt and UserHunt tables.
 * Based originally on //http://www.vogella.com/articles/AndroidSQLite/article.html*/

public class HuntDAO {

  /* Global variables for HuntDAO*/
  private SQLiteDatabase mDatabase;
  private MySQLiteHelper mDbHelper;
  private String[] mAllColumns = { MySQLiteHelper.COLUMN_HUNT_ID, MySQLiteHelper.COLUMN_HUNTNAME, 
		  MySQLiteHelper.COLUMN_HUNT_DESCRIPTION, MySQLiteHelper.COLUMN_END_DATE };
  private String[] mAllColumnsUserHunt  = { MySQLiteHelper.COLUMN_USER_HUNTS_ID, MySQLiteHelper.COLUMN_USER_HUNTS_HUNTNAME, 
		  MySQLiteHelper.COLUMN_USER_HUNTS_DESCRIPTION, MySQLiteHelper.COLUMN_USER_HUNTS_END_DATE };

  /* Constructor*/
  public HuntDAO(Context context) {
    mDbHelper = new MySQLiteHelper(context);
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
