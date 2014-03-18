package Mapping;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import sqlLiteDatabase.MapData;
import sqlLiteDatabase.MapDataDAO;
import sqlLiteDatabase.MapDataDAO.LocationCursor;

import java.util.List;

/*Based on the Nerd Ranch Guide RunManager class Page 1338. */

/* The purpose of this class is to handle the behaviour of the Google Map aspect of the application. This class is a Singleton
 * i.e. there should only be one MapManager object created.*/

public class MapManager {
	
	/*
	 * Global variables used within MapManager.
	 */
	public static final String ACTION_LOCATION = "com.application.treasurehunt.ACTION_LOCATION";
	
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSettings;
	
	private MapDataDAO mHelper;
	private static MapManager sMapManager;
	private Context mAppContext;
	private LocationManager mLocationManager;
	
	private int mHuntParticipantId;

	/* Private Constructor*/
	private MapManager(Context appContext) {
		
		mAppContext = appContext;
		mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);
		
		mHelper = new MapDataDAO(appContext);
		mHelper.open();
		
		mSettings = appContext.getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		mHuntParticipantId = mSettings.getInt("userParticipantIdForMap", -1);
	}
	
	/* Method that returns an instance of MapManager.*/
	public static MapManager get(Context c) {
		
		if(sMapManager == null) {
			sMapManager = new MapManager(c.getApplicationContext());
		}
		return sMapManager;
	}
	
	/* Method 'creates an Intent to be broadcast when location updates happen.' Page 1339/1341. */
	private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
		
		Intent broadcast = new Intent(ACTION_LOCATION);
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
	}
	
	/* Method that 'you specifically tell LocationManager to give you location updates' Page 1339/1340. */
	public void startLocationUpdates() {
		
		String provider = LocationManager.GPS_PROVIDER;
		
		Location lastKnown = mLocationManager.getLastKnownLocation(provider);
		if(lastKnown != null) {
			lastKnown.setTime(System.currentTimeMillis());
			broadcastLocation(lastKnown);
		}
		
		PendingIntent pi = getLocationPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
	}
	
	//???????????????????????????????
	private void broadcastLocation(Location location) {
		Intent broadcast = new Intent(ACTION_LOCATION);
		broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
		mAppContext.sendBroadcast(broadcast);
	}
	
	/* Method to stop location updates from continuing. Page 1339 */
	public void stopLocationUpdates() {
		PendingIntent pi = getLocationPendingIntent(false);
		if(pi != null) {
			mLocationManager.removeUpdates(pi);
			pi.cancel();
		}
	}
	
	/* Method to call the database and add a new map. Based on Page 1375. */
	public MapData startNewMap(int huntParticipantId) {
		MapData map = insertMap(huntParticipantId);
		startTrackingMap(map);
		return map;
	}
	
	/* Method to track the map for the given HuntParticipantId. Page 1376. */
	public void startTrackingMap(MapData map) {
		mHuntParticipantId = map.getParticipantId();
		mEditor.putInt("userParticipantIdForMap", mHuntParticipantId);
		mEditor.commit();
		startLocationUpdates();
	}
	
	/* Halts the location updates for the current huntParticipantId. Page 1376*/
	public void stopMap() {
		stopLocationUpdates();
		mHuntParticipantId = -1;
		mEditor.remove("userParticipantIdForMap");
		mEditor.commit();
	}
	
	/* Method to physically add new MapData to the SQLite (local) database for the given HuntParticipantId. Page 1376*/
	public MapData insertMap(int huntParticipantId) {
		MapData map = new MapData();
		map.setParticipantId(huntParticipantId);
		mHelper.insertMap(map);
		Log.i("Mapping", "Map entered into the database");
		return map;
	}
	
	/* Method inserting a recorded location into the SQLite (local) database for the given HuntParticipantId. 
	 * Based upon Page 1381.*/
	public void insertLocation(Location loc) {	//I added this in
		mHuntParticipantId = mSettings.getInt("userParticipantIdForMap", -1);
		
		if(mHuntParticipantId != -1) {
			mHelper.insertLocation(mHuntParticipantId, loc);
			Log.i("Mapping", "A location has been entered into the database for participantId: " + mHuntParticipantId);
		}
		else {
			Log.w("Mapping", "Location received with no tracking map; ignoring");
		}
	}
	
	/* Method that will return all of the MapData saved in the SQLite (local) database for the given HuntParticipantId.
	 * Based on Page 1407. */
	public MapData getMapData(int huntParticipantId) {
		MapData map = null;
		map = mHelper.queryMap(huntParticipantId);
		return map;
	}
	
	/* Method that will return all of the MapData saved in the SQLite (local) database for the given HuntParticipantId.
	 * Based on Page 1407. */
	public boolean isTrackingMap(MapData map) {
		return map!=null && map.getParticipantId() == mHuntParticipantId;
	}
	
	/* Method retrieves the last location stored for the given map identified by the HuntParticipantId. Based on Page 1414.*/
	public Location getLastLocationForMap(int huntParticipantId) {
		Location location = mHelper.queryLastLocationForMap(huntParticipantId);
		return location;
	}
	
	/* Method to return the locations stored within the SQLite (local) database asynchronously i.e. outside of the main
	 * UI thread. Based on Nerd Ranch Guide Page 1463. */
	public LocationCursor queryLocationsForMapsAsync(int huntParticipantId) {
		return mHelper.queryLocationsForMapAsync(huntParticipantId);
	}
	
	/* Method that will call the map database helper (MapDataDAO) to query a list of locations for a given HuntParticipantId
	 * and return them. Based on Nerd Ranch Guide Page 1463. */
	//Shouldn't have to be async as it is unlikely that a hunt would contain hundreds of questions thus hundreds of pins for
	//each successful scan.
	public List<Location> queryLocationsForMarkers(int huntParticipantId) {
		return mHelper.queryMarkersForMap(huntParticipantId);	
	}	
}
