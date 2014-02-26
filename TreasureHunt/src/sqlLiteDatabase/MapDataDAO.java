package sqlLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.application.treasurehunt.LocationCursor;

import Utilities.MySQLiteHelperForMaps;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;


public class MapDataDAO {

	private SQLiteDatabase database;
	  private MySQLiteHelperForMaps dbHelper;
	  private String[] allColumnsLocations = { MySQLiteHelperForMaps.COLUMM_MAPS_PARTICIPANT_ID, MySQLiteHelperForMaps.COLUMN_MAPS_LATITUDE, MySQLiteHelperForMaps.COLUMN_MAPS_LONGTITUDE, MySQLiteHelperForMaps.COLUMN_MAPS_ALTITUDE , MySQLiteHelperForMaps.COLUMN_MAPS_TIME_STAMP };
	  private String[] allColumnsMaps = { MySQLiteHelperForMaps.COLUMM_MAPS_PARTICIPANT_ID, MySQLiteHelperForMaps.COLUMN_MAPS_START_TIME};
	  private String[] allColumnsMarkers = { MySQLiteHelperForMaps.COLUMM_MAPS_PARTICIPANT_ID, MySQLiteHelperForMaps.COLUMN_MAPS_LATITUDE, MySQLiteHelperForMaps.COLUMN_MAPS_LONGTITUDE };
	  
	  public MapDataDAO(Context context) {
	    dbHelper = new MySQLiteHelperForMaps(context);
	  }

	  public void open() throws SQLException {
		  database = dbHelper.getWritableDatabase();
		  //Unclean - drops the database each time and re-adds the table.
		  //updateDatabaseLocally();
	  }
	  
	  public void updateDatabaseLocally()
	  {
		  dbHelper.onUpgrade(database, database.getVersion(), database.getVersion());
	  }

	  public void close() {
	    dbHelper.close();
	  }
	  
	  public int insertRun(MapData run)
	  {
		  ContentValues values = new ContentValues();
		  values.put(MySQLiteHelperForMaps.COLUMM_MAPS_PARTICIPANT_ID, run.getParticipantId()); //THIS NEEDS CHANGED
		  values.put(MySQLiteHelperForMaps.COLUMN_MAPS_START_TIME, run.getStartDate().toString());
		  long insertId = database.insert(MySQLiteHelperForMaps.TABLE_MAPS, null, values);
		  Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_MAPS,
				  allColumnsMaps, null, null,
			        null, null, null);	
	
			    cursor.moveToFirst();
			    MapData newRun = cursorToMapEntry(cursor);
			    cursor.close();
			    return newRun.getParticipantId();

	  }
	  
	  public long insertLocation(int participantId, Location location)
	  {
		  ContentValues values = new ContentValues(); 
		  values.put(MySQLiteHelperForMaps.COLUMM_MAPS_PARTICIPANT_ID, participantId);
		  values.put(MySQLiteHelperForMaps.COLUMN_MAPS_LATITUDE, location.getLatitude());
		  values.put(MySQLiteHelperForMaps.COLUMN_MAPS_LONGTITUDE, location.getLongitude());
		  values.put(MySQLiteHelperForMaps.COLUMN_MAPS_ALTITUDE, location.getAltitude());
		  values.put(MySQLiteHelperForMaps.COLUMN_MAPS_TIME_STAMP, location.getTime());
		  long insertId = database.insert(MySQLiteHelperForMaps.TABLE_LOCATIONS, null, values);
		  Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_LOCATIONS,
				  allColumnsLocations, null, null,
			        null, null, null);	
	
			    cursor.moveToFirst();
			    MapData newRun = cursorToMapEntry(cursor);
			    cursor.close();
			    return newRun.getParticipantId();
	  }
	  
	  public void insertMarker(int participantId, Location location)
	  {
		  ContentValues values = new ContentValues(); 
		  values.put(MySQLiteHelperForMaps.COLUMM_MAPS_PARTICIPANT_ID, participantId);
		  values.put(MySQLiteHelperForMaps.COLUMN_MAPS_LATITUDE, location.getLatitude());
		  values.put(MySQLiteHelperForMaps.COLUMN_MAPS_LONGTITUDE, location.getLongitude());
		  long insertId = database.insert(MySQLiteHelperForMaps.TABLE_MARKERS, null, values);
	  }
	 
	  private MapData cursorToMapEntry(Cursor cursor) {
		  MapData runResult = new MapData();
		  	runResult.setParticipantId(cursor.getInt(0));
		  	//runResult.setStartDate(cursor.getDouble(1));
		    
		    return runResult;
	  } 
	  
	  private Location cursorToLocationEntry(Cursor cursor) {
		  Location locationResult = new Location("GPS");
		  locationResult.setLatitude(cursor.getDouble(1));
		  locationResult.setLongitude(cursor.getDouble(2));
		  locationResult.setAltitude(cursor.getDouble(3));
		  locationResult.setTime(cursor.getLong(4));	    
		   
		  return locationResult;
	  }
	  
	  private Location cursorToMarkerEntry(Cursor cursor) {
		  Location locationResult = new Location("GPS");
		  locationResult.setLatitude(cursor.getDouble(1));
		  locationResult.setLongitude(cursor.getDouble(2));	    
		  return locationResult;
	  }
	  
	  public List<MapData> queryRuns() {
		    List<MapData> mapEntries = new ArrayList<MapData>();
		    //http://stackoverflow.com/questions/12339121/multiple-orderby-in-sqlitedatabase-query-method
		   Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_MAPS,
				   allColumnsMaps, null, null, null, null, null);

		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      MapData mapEntry = cursorToMapEntry(cursor);
		      mapEntries.add(mapEntry);
		     
		      cursor.moveToNext();
		    }
		    
		    cursor.close();
		    return mapEntries;
		  }
	  
	  public MapData queryRun(int participantId)
	  {
		  String arguments =  "HuntParticipantId = '"+participantId + "'";
		  Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_MAPS, allColumnsMaps , arguments, null , null , null, null, "1");
		  cursor.moveToFirst();
		  if(!cursor.isAfterLast())
		  {
			  MapData run = cursorToMapEntry(cursor);
			  return run;
		  }
		  return null;
		  
	  }
	  
	  public Location queryLastLocationForRun(int participantId)
	  {
		  String arguments =  "HuntParticipantId = '"+participantId + "'";
		  Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_LOCATIONS, allColumnsLocations , arguments, null , null , null, MySQLiteHelperForMaps.COLUMN_MAPS_TIME_STAMP + " DESC", "1");
		  cursor.moveToFirst();
		  if(!cursor.isAfterLast())
		  {
			  Location location = cursorToLocationEntry(cursor);
			  return location;
		  }
		  return null;
		  
	  }
	  
	  public List<Location> queryLocationsForMap(int participantId)
	  {
		  List<Location> mapEntries = new ArrayList<Location>();
		    //http://stackoverflow.com/questions/12339121/multiple-orderby-in-sqlitedatabase-query-method
		   String arguments =  "HuntParticipantId = '"+participantId + "'";
		   Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_LOCATIONS,
				   allColumnsLocations, arguments, null, null, null, null);

		   cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      Location mapEntry = cursorToLocationEntry(cursor);
		      mapEntries.add(mapEntry);
		     
		      cursor.moveToNext();
		    }
		    
		    cursor.close();
		    return mapEntries;
	  }
	  
	  public List<Location> queryMarkersForMap(int participantId)
	  {
		  List<Location> mapEntries = new ArrayList<Location>();
		    //http://stackoverflow.com/questions/12339121/multiple-orderby-in-sqlitedatabase-query-method
		   String arguments =  "HuntParticipantId = '"+participantId + "'";
		   Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_MARKERS,
				   allColumnsMarkers, arguments, null, null, null, null);

		   cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      Location mapEntry = cursorToMarkerEntry(cursor);
		      mapEntries.add(mapEntry);
		     
		      cursor.moveToNext();
		    }
		    
		    cursor.close();
		    return mapEntries;
	  }
	  
	  public LocationCursor queryLocationsForMapAsync(int participantId)
	  {
		  String arguments =  "HuntParticipantId = '"+participantId + "'";
		   Cursor cursor = database.query(MySQLiteHelperForMaps.TABLE_LOCATIONS,
				   allColumnsLocations, arguments, null, null, null, MySQLiteHelperForMaps.COLUMN_MAPS_TIME_STAMP + " asc");
		   return new LocationCursor(cursor);
		   
	  }
}


