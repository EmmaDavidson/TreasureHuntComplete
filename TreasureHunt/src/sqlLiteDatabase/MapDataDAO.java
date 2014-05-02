/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package sqlLiteDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/* This singleton class handles the interaction between the application and SQLite (local) 
 * database for the Locations, Markers and Map tables.*/
public class MapDataDAO {

	  /* Global variables for MapDataDAO. */
	  private static MapDataDAO mMapDataDAO;
	  private SQLiteDatabase mDatabase;
	  private MySQLiteHelper mDbHelper;
	  private String[] mAllColumnsLocations = { MySQLiteHelper.COLUMM_MAPS_PARTICIPANT_ID, MySQLiteHelper.COLUMN_MAPS_LATITUDE, MySQLiteHelper.COLUMN_MAPS_LONGTITUDE, MySQLiteHelper.COLUMN_MAPS_ALTITUDE , MySQLiteHelper.COLUMN_MAPS_TIME_STAMP };
	  private String[] mAllColumnsMaps = { MySQLiteHelper.COLUMM_MAPS_PARTICIPANT_ID, MySQLiteHelper.COLUMN_MAPS_START_TIME};
	  private String[] mAllColumnsMarkers = { MySQLiteHelper.COLUMM_MAPS_PARTICIPANT_ID, MySQLiteHelper.COLUMN_MAPS_LATITUDE, MySQLiteHelper.COLUMN_MAPS_LONGTITUDE };
	  
	  /* Constructor */
	  private MapDataDAO(Context context) {
	    mDbHelper = new MySQLiteHelper(context);
	  }
	  
	  /* Method that returns an instance of MapManager.*/
	  public static MapDataDAO getInstance(Context c) {
			
			if(mMapDataDAO == null) {
				mMapDataDAO = new MapDataDAO(c.getApplicationContext());
			}
			return mMapDataDAO;
	   }

	  /* Method handling what happens when the helper is first opened. It retrieves access to the SQLite (local) database. */
	  public void open() throws SQLException {
		  mDatabase = mDbHelper.getWritableDatabase();
	  }

	  /* Method handling what happens when the helper is closed. It closes access to the SQLite (local) database.*/
	  public void close() {
	    mDbHelper.close();
	  }
	  
	  /* Method that inserts a new row into the Map table with the MapData supplied. 
	   * Based upon Nerd Ranch Guide Page 1370. */
	  public void insertMap(MapData map) {
		  ContentValues values = new ContentValues();
		  values.put(MySQLiteHelper.COLUMM_MAPS_PARTICIPANT_ID, map.getParticipantId());
		  values.put(MySQLiteHelper.COLUMN_MAPS_START_TIME, map.getStartDate().toString());
		  long insertId = mDatabase.insert(MySQLiteHelper.TABLE_MAPS, null, values);
	  }
	  
	 /* Method that inserts a row into the Location table for a given HuntParticipantId. */
	 public void insertLocation(int huntParticipantId, Location location) {
		  ContentValues values = new ContentValues(); 
		  values.put(MySQLiteHelper.COLUMM_MAPS_PARTICIPANT_ID, huntParticipantId);
		  values.put(MySQLiteHelper.COLUMN_MAPS_LATITUDE, location.getLatitude());
		  values.put(MySQLiteHelper.COLUMN_MAPS_LONGTITUDE, location.getLongitude());
		  values.put(MySQLiteHelper.COLUMN_MAPS_ALTITUDE, location.getAltitude());
		  values.put(MySQLiteHelper.COLUMN_MAPS_TIME_STAMP, location.getTime());
		  long insertId = mDatabase.insert(MySQLiteHelper.TABLE_LOCATIONS, null, values);
	  }
	  
	  /* Method that inserts a row into the Marker table for a given HuntParticipantId i.e. it stores the location of
	   * where the given participant has successfully scanned a QR Code for a particular treasure hunt. */
	  public void insertMarker(int participantId, Location location) {
		  ContentValues values = new ContentValues(); 
		  values.put(MySQLiteHelper.COLUMM_MAPS_PARTICIPANT_ID, participantId);
		  values.put(MySQLiteHelper.COLUMN_MAPS_LATITUDE, location.getLatitude());
		  values.put(MySQLiteHelper.COLUMN_MAPS_LONGTITUDE, location.getLongitude());
		  long insertId = mDatabase.insert(MySQLiteHelper.TABLE_MARKERS, null, values);
	  }
	  
	  /* Method that returns all of the data from the SQLite (local) database for a given HuntParticipantId. 
	   * Based upon Nerd Ranch Guide Page 1406 */
	  public MapData queryMap(int huntParticipantId) {
		  String arguments =  "HuntParticipantId = '"+huntParticipantId + "'";
		  Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_MAPS, mAllColumnsMaps , arguments, null , null , null, null, "1");
		  cursor.moveToFirst();
		  if(!cursor.isAfterLast()) {
			  MapData run = cursorToMapEntry(cursor);
			  return run;
		  }
		  return null;  
	  }
	  
	  /*Method retrieves the last location stored for the given map identified by the HuntParticipantId. 
	   * Based upon Nerd Ranch Guide Page 1414.*/
	  public Location queryLastLocationForMap(int participantId) {
		  String arguments =  "HuntParticipantId = '"+participantId + "'";
		  Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_LOCATIONS, mAllColumnsLocations , arguments, null , null , null, MySQLiteHelper.COLUMN_MAPS_TIME_STAMP + " DESC", "1");
		  cursor.moveToFirst();
		  if(!cursor.isAfterLast())
		  {
			  Location location = cursorToLocationEntry(cursor);
			  return location;
		  }
		  return null;  
	  }
	  
	  /* Method returns from the SQLite (local) database data of all locations stored from where a successful QRCode scan has 
	   * been made for a given HuntParticipantId. This is not done asynchronously as it is unlikely that a given treasure
	   * hunt would have e.g. hundreds of QR Codes to scan as part of the hunt and thus displayed on the map
	   * by the participant.  */
	  public List<Location> queryMarkersForMap(int huntParticipantId) {
		  List<Location> mapEntries = new ArrayList<Location>();
		    //http://stackoverflow.com/questions/12339121/multiple-orderby-in-sqlitedatabase-query-method
		   String arguments =  "HuntParticipantId = '"+huntParticipantId + "'";
		   Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_MARKERS,
				   mAllColumnsMarkers, arguments, null, null, null, null);

		   cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      Location mapEntry = cursorToMarkerEntry(cursor);
		      mapEntries.add(mapEntry);
		     
		      cursor.moveToNext();
		    }
		    
		    cursor.close();
		    return mapEntries;
	  }
	  
	  /* Method returns from the SQLite (local) database data of all locations stored for a given HuntParticipantId.
	   * The list is returned into a LocationCursor to ensure that the displaying of data is done off the main UI thread i.e.
	   * in the scenario where there is lots of location data we do not want to slow down the main UI thread. 
	   * Based upon Nerd Ranch Guide Page 1463. */
	  public LocationCursor queryLocationsForMapAsync(int huntParticipantId) {
		  String arguments =  "HuntParticipantId = '"+huntParticipantId + "'";
		   Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_LOCATIONS,
				   mAllColumnsLocations, arguments, null, null, null, MySQLiteHelper.COLUMN_MAPS_TIME_STAMP + " asc");
		   return new LocationCursor(cursor); 
	  }
	  
	  /* A cursor that creates a MapData instance from a given Cursor i.e. retrieves the data found in a single row
	   * returned by a query for the Map table.*/
	  private MapData cursorToMapEntry(Cursor cursor) {
		  MapData runResult = new MapData();
		  runResult.setParticipantId(cursor.getInt(0));
		    
		   return runResult;
	  } 
	  
	  /* A cursor that creates a Location instance from a given Cursor i.e. retrieves the data found in a single row
	   * returned by a query for the Location table.*/
	  private Location cursorToLocationEntry(Cursor cursor) {
		  Location locationResult = new Location("GPS");
		  locationResult.setLatitude(cursor.getDouble(1));
		  locationResult.setLongitude(cursor.getDouble(2));
		  locationResult.setAltitude(cursor.getDouble(3));
		  locationResult.setTime(cursor.getLong(4));	    
		   
		  return locationResult;
	  }
	  
	  /* A cursor that creates a Location instance from a given Cursor i.e. retrieves the data found in a single row
	   * returned by a query for the Marker table.*/
	  private Location cursorToMarkerEntry(Cursor cursor) {
		  Location locationResult = new Location("GPS");
		  locationResult.setLatitude(cursor.getDouble(1));
		  locationResult.setLongitude(cursor.getDouble(2));	    
		  return locationResult;
	  }
	  
	  /* This internal class is for the purpose of wrapping 'a cursor intended to returns rows from the location table
	   * and converts their various fields into properties on the Location object' as quoted from Nerd Ranch Guide Page 1412.
	   * 'CursorWrapper is designed to wrap an existing Cursor and forward along all of the method calls to it' 
	   * as quoted from Nerd Ranch Guide Page 1386.
	   * Taken from Nerd Ranch Guide Page 1412.*/
	  public class LocationCursor extends CursorWrapper {

			/* Constructor */ 
			public LocationCursor(Cursor cursor) {
				super(cursor);
			}
			
			/* Method 'creates and configures an instance of (Location) based on the values of the current row's 
			 * columns' - Nerd Ranch Guide Page 1388.
			 * Based upon Nerd Ranch Guide Page 1412 */
			public Location getLocation() {
				if(isBeforeFirst() || isAfterLast()) {
					return null;
				}
				
				Location loc = new Location("GPS");
				loc.setLatitude(getDouble(getColumnIndex(MySQLiteHelper.COLUMN_MAPS_LATITUDE)));
				loc.setLongitude(getDouble(getColumnIndex(MySQLiteHelper.COLUMN_MAPS_LONGTITUDE)));
				loc.setAltitude(getDouble(getColumnIndex(MySQLiteHelper.COLUMN_MAPS_ALTITUDE)));
				return loc;
			} 
		}
}


