package com.application.treasurehunt;

import java.util.List;

import sqlLiteDatabase.MapData;
import sqlLiteDatabase.MapDataDAO;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

//Complete class from tutorial by Big Nerd Ranch
public class MapManager {
	
	private static final String TAG = "MapManager";
	public static final String ACTION_LOCATION = "com.application.treasurehunt.ACTION_LOCATION";
	
	MapDataDAO mHelper;
	
	SharedPreferences.Editor editor;
	SharedPreferences settings;
	
	private static MapManager sRunManager;
	private Context mAppContext;
	private LocationManager mLocationManager;
	private int currentHuntParticipantId;

	private MapManager(Context appContext)
	{
		mAppContext = appContext;
		mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);
		
		mHelper = new MapDataDAO(appContext);
		mHelper.open();
		
		
		
		settings = appContext.getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		currentHuntParticipantId = settings.getInt("userParticipantIdForMap", -1);
	}
	
	public static MapManager get(Context c)
	{
		if(sRunManager == null)
		{
			sRunManager = new MapManager(c.getApplicationContext());
		}
		return sRunManager;
	}
	
	private PendingIntent getLocationPendingIntent(boolean shouldCreate)
	{
		Intent broadcast = new Intent(ACTION_LOCATION);
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
	}
	
	public void startLocationUpdates()
	{
		String provider = LocationManager.GPS_PROVIDER;
		
		Location lastKnown = mLocationManager.getLastKnownLocation(provider);
		if(lastKnown != null)
		{
			lastKnown.setTime(System.currentTimeMillis());
			broadcastLocation(lastKnown);
		}
		
		PendingIntent pi = getLocationPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
	}
	
	private void broadcastLocation(Location location)
	{
		Intent broadcast = new Intent(ACTION_LOCATION);
		broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
		mAppContext.sendBroadcast(broadcast);
	}
	
	public void stopLocationUpdates()
	{
		PendingIntent pi = getLocationPendingIntent(false);
		if(pi != null)
		{
			mLocationManager.removeUpdates(pi);
			pi.cancel();
		}
	}
	
	public boolean isTrackingMap()
	{
		return getLocationPendingIntent(false) != null;
	}
	
	
	//Actually creates and inserts a new run into the database
	public MapData startNewMap(int participantId)
	{
		MapData run = insertMap(participantId);
		startTrackingMap(run);
		return run;
	}
	
	//startNewRun passes the id to this method to begin tracking it 
	//RunFragment will use this directly to when it restarts tracking on an existing run
	//The participantId is saved and allows it to be retrieved later 
	public void startTrackingMap(MapData run)
	{
		currentHuntParticipantId = run.getParticipantId();
		editor.putInt("userParticipantIdForMap", currentHuntParticipantId);
		editor.commit();
		startLocationUpdates();
	}
	
	//Stops location updates and clears out the id of the current run
	public void stopMap()
	{
		stopLocationUpdates();
		currentHuntParticipantId = -1;
		editor.remove("userParticipantIdForMap");
		editor.commit();
	}
	
	public MapData insertMap(int participantId)
	{
		MapData run = new MapData();
		run.setParticipantId(participantId);
		mHelper.insertRun(run);
		Log.i("Mapping", "Run entered into the database");
		return run;
	}
	
	public void insertLocation(Location loc)
	{	//I added this in
		currentHuntParticipantId = settings.getInt("userParticipantIdForMap", -1);
		
		if(currentHuntParticipantId != -1)
		{
			mHelper.insertLocation(currentHuntParticipantId, loc);
			Log.i("Mapping", "A location has been entered into the database for participantId: " + currentHuntParticipantId);
		}
		else
		{
			Log.e("Mapping", "Location received with no tracking run; ignoring");
		}
	}
	
	public List<MapData> queryMapData()
	{
		return mHelper.queryRuns();
	}
	
	public MapData getMapData(int participantId)
	{
		MapData run = null;
		run = mHelper.queryRun(participantId);
		return run;
	}
	
	public boolean isTrackingMap(MapData run)
	{
		return run!=null && run.getParticipantId() == currentHuntParticipantId;
	}
	
	public Location getLastLocationForMap(int participantId)
	{
		Location location = mHelper.queryLastLocationForRun(participantId);
		return location;
	}
	
	public List<Location> queryLocationForMaps(int participantId)
	{
		return mHelper.queryLocationsForMap(participantId);
	}
	
	public LocationCursor queryLocationsForMapsAsync(int participant)
	{
		return mHelper.queryLocationsForMapAsync(participant);
	}
	
	public List<Location> queryLocationsForMarkers(int participantId)
	{
		return mHelper.queryMarkersForMap(participantId);
	}
}
