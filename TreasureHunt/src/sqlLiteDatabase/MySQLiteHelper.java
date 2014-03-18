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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/* The purpose of this class is to aid the setting up of the SQLite (local) database, where the data is stored locally on 
 * a participants device. Based on //http://www.vogella.com/articles/AndroidSQLite/article.html*/
public class MySQLiteHelper extends SQLiteOpenHelper {

  /* Global variables used by MySQLiteHelper*/
  public static final String TABLE_HUNTS = "Hunt";
  public static final String COLUMN_HUNT_ID = "HuntId";
  public static final String COLUMN_HUNTNAME = "HuntName";
  public static final String COLUMN_HUNT_DESCRIPTION = "HuntDescription";
  public static final String COLUMN_END_DATE = "EndDate";
  
  public static final String TABLE_USER_HUNTS = "UserHunt";
  public static final String COLUMN_USER_HUNTS_HUNTNAME = "HuntName";
  public static final String COLUMN_USER_HUNTS_ID = "HuntId";
  public static final String COLUMN_USER_HUNTS_DESCRIPTION = "HuntDescription";
  public static final String COLUMN_USER_HUNTS_END_DATE = "HuntEndDate";
  
  public static final String TABLE_HUNT_LEADERBOARD = "Leaderboard";
  public static final String COLUMN_LEADERBOARD_USERNAME = "UserName";
  public static final String COLUMN_LEADERBOARD_TALLY = "Tally";
  public static final String COLUMN_LEADERBOARD_ELAPSED_TIME = "ElapsedTime";
  
  public static final String TABLE_COMPANIES = "Company";
  public static final String COLUMN_COMPANY_ID = "CompanyId";
  public static final String COLUMN_COMPANY_NAME = "CompanyName";
  public static final String COLUMN_COMPANY_PASSWORD = "CompanyPassword";
  
  public static final String TABLE_LOCATIONS = "Locations";
  public static final String COLUMN_MAPS_LATITUDE = "Latitude";
  public static final String COLUMN_MAPS_LONGTITUDE = "Longtitude";
  public static final String COLUMN_MAPS_ALTITUDE = "Altitude";
  public static final String COLUMN_MAPS_TIME_STAMP= "TimeStamp";
  
  public static final String TABLE_MAPS = "Map";
  public static final String COLUMN_MAPS_START_TIME= "StartTime";
  public static final String COLUMM_MAPS_PARTICIPANT_ID = "HuntParticipantId";
  
  public static final String TABLE_MARKERS = "Markers";
  
  private static final String DATABASE_NAME = "TreasureHunt.db";
  private static final int DATABASE_VERSION = 1;

 
  /* Variables aiding the creation of each database table within the local database.*/
  //http://stackoverflow.com/questions/14137622/confused-regarding-sqliteopenhelper-and-creating-multiple-tables
  private static final String DATABASE_CREATE = "create table "
      + TABLE_HUNTS + "("  + COLUMN_HUNT_ID + " INTEGER NOT NULL, "  +
		  COLUMN_HUNTNAME + " text not null, " + 
          COLUMN_HUNT_DESCRIPTION + " text not null, " + 
		  COLUMN_END_DATE + " text not null);" ;
  
  private static final String DATABASE_CREATE_USER_HUNTS =  "create table " 
		  + TABLE_USER_HUNTS + " ("  + COLUMN_USER_HUNTS_ID + " INTEGER NOT NULL, " + 
		  COLUMN_USER_HUNTS_HUNTNAME + " text not null, " +
		  COLUMN_USER_HUNTS_DESCRIPTION + " text not null, " + 
		  COLUMN_USER_HUNTS_END_DATE  + " text not null); " ;
  
  //http://stackoverflow.com/questions/15424382/sqlite-database-android-create-table
  //http://stackoverflow.com/questions/19619418/android-sqlite-sqliteexception-near-syntax-error
  private static final String DATABASE_CREATE_LEADERBOARD =  "create table " 
		  + TABLE_HUNT_LEADERBOARD + " (" + COLUMN_LEADERBOARD_USERNAME + " name not null, " +
	        COLUMN_LEADERBOARD_TALLY + " INTEGER NOT NULL, " + COLUMN_LEADERBOARD_ELAPSED_TIME + " FLOAT NOT NULL); ";
  
  private static final String DATABASE_CREATE_COMPANY = "create table "
	      + TABLE_COMPANIES + "("  + COLUMN_COMPANY_ID + " INTEGER NOT NULL, " + 
	      COLUMN_COMPANY_NAME + " text not null, " + COLUMN_COMPANY_PASSWORD + " text not null); ";
  
  private static final String DATABASE_CREATE_LOCATIONS = "create table " + TABLE_LOCATIONS + " (" + COLUMM_MAPS_PARTICIPANT_ID + " INTEGER NOT NULL, " 
		  									+ COLUMN_MAPS_LATITUDE + " real, " + COLUMN_MAPS_LONGTITUDE + " real, " 
		  									 + COLUMN_MAPS_ALTITUDE + " real, " + COLUMN_MAPS_TIME_STAMP + " INTEGER NOT NULL); " ;

  private static final String DATABASE_CREATE_MAPS = "create table " + TABLE_MAPS + " (" + COLUMM_MAPS_PARTICIPANT_ID + " INTEGER NOT NULL, " 
		  								+ COLUMN_MAPS_START_TIME + " LONG); " ;
  
  private static final String DATABASE_CREATE_MARKERS = "create table " + TABLE_MARKERS + " (" + COLUMM_MAPS_PARTICIPANT_ID + " INTEGER NOT NULL, " 
		  + COLUMN_MAPS_LATITUDE + " real, " + COLUMN_MAPS_LONGTITUDE + " real); " ;
  
	  
  /* Constructor.*/
  public MySQLiteHelper(Context context) {
	  
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  /* Method called when the database is first created - generates all tables.*/
  @Override
  public void onCreate(SQLiteDatabase database) {
	  
    database.execSQL(DATABASE_CREATE);
    database.execSQL(DATABASE_CREATE_USER_HUNTS);
    database.execSQL(DATABASE_CREATE_LEADERBOARD);
    database.execSQL(DATABASE_CREATE_COMPANY);
    database.execSQL(DATABASE_CREATE_MAPS);
    database.execSQL(DATABASE_CREATE_LOCATIONS);
    database.execSQL(DATABASE_CREATE_MARKERS);
  }
  
  /* Should get this to sync... but for the moment clears the table and drags the data back from the database each time.*/
  /* Method that clears all of the data from a given table. */
  //http://stackoverflow.com/questions/9599741/how-to-delete-all-record-from-table-in-sqlite
  public void refreshTable(SQLiteDatabase db, String tableName) {
	  
	  db.execSQL("delete from " + tableName);
  }

  /* Method that recreates the database i.e. drops all of the tables and re-adds them. */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	  
    Log.i(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_HUNTS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_HUNTS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_HUNT_LEADERBOARD);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPANIES);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS);
    onCreate(db);
  }
} 
