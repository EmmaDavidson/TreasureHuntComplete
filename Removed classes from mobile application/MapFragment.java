package Mapping;

import com.application.treasurehunt.GoogleMapActivity;
import com.application.treasurehunt.R;
import com.application.treasurehunt.R.id;
import com.application.treasurehunt.R.layout;

import sqlLiteDatabase.LastLocationLoader;
import sqlLiteDatabase.MapData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MapFragment extends Fragment {

	Button mMapButton;
	TextView mStarted, mLatitude, mLongtitude, mAltitude, mElapsedTime;
	
	private MapData mMap;
	private Location mLastLocation;
	
	private MapManager mRunManager;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mRunManager = MapManager.get(getActivity());
				
		Bundle args = getArguments();
		if(args != null)
		{
			int participantId = args.getInt("userParticipantIdForMap",-1);
			if(participantId != -1)
			{
				LoaderManager lm = getLoaderManager();
				mMap = mRunManager.getMapData(participantId);
				lm.initLoader(0, args, new LocationLoaderCallbacks());
				//mLastLocation = mRunManager.getLastLocationForRun(participantId);
				
			}
		}
		
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		mStarted = (TextView) view.findViewById(R.id.start);
		mLatitude = (TextView) view.findViewById(R.id.latitude);
		mLongtitude = (TextView) view.findViewById(R.id.longtitude);
		mAltitude = (TextView) view.findViewById(R.id.altitude);
		mElapsedTime = (TextView) view.findViewById(R.id.elapsedTime);
		mMapButton = (Button) view.findViewById(R.id.map_button);
	
		
		mMapButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Intent mapIntent = new Intent(getActivity(), GoogleMapActivity.class);
						mapIntent.putExtra("userParticipantIdForMap", mMap.getParticipantId());
						startActivity(mapIntent);
					}
				});
		
		//mRunManager.startTrackingMap(mMap);
		updateUI();

		return view;
	}
	
	public void updateUI()
	{
		boolean started = mRunManager.isTrackingMap();
		boolean trackingThisRun = mRunManager.isTrackingMap(mMap);
		
		if(mMap!= null && mLastLocation !=null)
		{
			mLatitude.setText(Double.toString(mLastLocation.getLatitude()));
			mLongtitude.setText(Double.toString(mLastLocation.getLongitude()));
			mAltitude.setText(Double.toString(mLastLocation.getAltitude()));
			mMapButton.setEnabled(true);
		}
		else
			{
				mMapButton.setEnabled(false);
			}

	}
	
	private BroadcastReceiver mLocationReceiver = new LocationReceiver()
	{
		@Override
		protected void onLocationReceived(Context context, Location loc)
		{
				if(!mRunManager.isTrackingMap(mMap))
				{
					return;
				}
				mLastLocation = loc;
				if(isVisible())
				{
					updateUI();
				}
			
		}
		
		@Override 
		protected void onProviderEnabledChanged(boolean enabled)
		{	//should be int
			String toastText = enabled ? "enabled" : "disabled";
			// R.string.gps_enabled : R.string.gps_disabled
			Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
		}
	};
	
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
	
	public static MapFragment newInstance(int participantId)
	{
		Bundle args = new Bundle();
		args.putInt("userParticipantIdForMap", participantId);
		MapFragment rf = new MapFragment();
		rf.setArguments(args);
		return rf;
	}
	
	public class LocationLoaderCallbacks implements LoaderCallbacks<Location> {

		@Override
		public Loader<Location> onCreateLoader(int id, Bundle args) {
			return new LastLocationLoader(getActivity(), args.getInt("userParticipantIdForMap")); //ARGSRUNID
		}

		@Override
		public void onLoadFinished(Loader<Location> l, Location location) {
			mLastLocation = location;
			updateUI();
			
		}

		@Override
		public void onLoaderReset(Loader<Location> arg0) {
			//DO NOTHINGS
			
		}


	}

	
}


