package com.application.treasurehunt;

import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapFragment extends SupportMapFragment implements LoaderCallbacks<Cursor> {

	private GoogleMap mGoogleMap;
	private Cursor cursor;
	private static final int LOAD_LOCATIONS=0;
	private LocationCursor mLocationCursor;
	private MapManager mMapManager;
	int participantId;
	//private MapData mMap;
	
	public static GoogleMapFragment newInstance(int participantId)
	{
		Bundle args = new Bundle();
		args.putInt("userParticipantId", participantId);
		GoogleMapFragment mf = new GoogleMapFragment();
		mf = new GoogleMapFragment();
		mf.setArguments(args);
		return mf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args != null)
		{
			participantId = args.getInt("userParticipantId", -1);
			if(participantId != -1)
			{
				LoaderManager lm = getLoaderManager();
				lm.initLoader(LOAD_LOCATIONS, args, this);
			}
		}
		
		mMapManager = MapManager.get(getActivity());
		
		//mMap = mMapManager.getMapData(participantId);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		mGoogleMap = getMap();
		mGoogleMap.setMyLocationEnabled(true);
		return v;
	}
	
	private void updateUI()
	{
		if(mGoogleMap == null || mLocationCursor == null )
		{
			return;
		}
			
			/*PolylineOptions line = new PolylineOptions();
			
			LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
			mLocationCursor.moveToFirst();
			while(!mLocationCursor.isAfterLast())
			{
				Location loc = mLocationCursor.getLocation();
				LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
				
				line.add(latLng);
				latLngBuilder.include(latLng);
				mLocationCursor.moveToNext();
				
			}
				mGoogleMap.addPolyline(line);
			*/	
				List<Location> locationsForMarkers = mMapManager.queryLocationsForMarkers(participantId);
				//http://www.mkyong.com/java/how-do-loop-iterate-a-list-in-java/
				/*Iterator<Location> iterator = locationsForMarkers.iterator();
				while(iterator.hasNext())
				{
					MarkerOptions marker = new MarkerOptions().position(new LatLng(iterator.next().getLatitude(), iterator.next().getLongitude()));
					mGoogleMap.addMarker(marker);
				}*/
				
				for(int i = 0 ; i < locationsForMarkers.size(); i++)
				{
					MarkerOptions marker = new MarkerOptions().position(new LatLng(locationsForMarkers.get(i).getLatitude(), locationsForMarkers.get(i).getLongitude()));
					marker.title(i + 1 + "");
					
					if(i == 0) //i.e. the starting point
					{
						marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					}
					else if(i == locationsForMarkers.size() - 1) //i.e. the last point...so far
					{
						marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					}
					else
					{
						marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
					}
					mGoogleMap.addMarker(marker);
				}
				
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				
				//LatLngBounds latlngBounds = latLngBuilder.build();
				//CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latlngBounds, display.getWidth(), display.getHeight(), 5);
				//mGoogleMap.moveCamera(movement);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int value, Bundle args) {
		int participantId = args.getInt("userParticipantId", -1);
		return new LocationListCursorLoader(getActivity(), participantId);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mLocationCursor = (LocationCursor) cursor;
		updateUI();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mLocationCursor.close();
		mLocationCursor = null;	
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		getActivity().registerReceiver(mLocationReceiver, new IntentFilter(MapManager.ACTION_LOCATION));
	}
	
	@Override
	public void onStop()
	{
		getActivity().unregisterReceiver(mLocationReceiver);
		super.onStop();
	}
	
	private BroadcastReceiver mLocationReceiver = new LocationReceiver()
	{
		@Override
		protected void onLocationReceived(Context context, Location loc)
		{
			updateUI();	
		}
		
		@Override 
		protected void onProviderEnabledChanged(boolean enabled)
		{	//should be int
			String toastText = enabled ? "enabled" : "disabled";
			// R.string.gps_enabled : R.string.gps_disabled
			Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
		}
	};

}
