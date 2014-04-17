package Mapping;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
/* Based on the Android Programming book Nerd Ranch Guide*/

import sqlLiteDatabase.MapDataDAO.LocationCursor;

/* The purpose of this Fragment is to display a Google Map for a given participant and a given treasure hunt. It should display
 * pin drops at the locations of successful QR Code scans to allow a participant to view where they have travelled on their treasure hunt.
 * [See Dissertation Section 2.4.2.9] */

public class GoogleMapFragment extends SupportMapFragment implements LoaderCallbacks<Cursor> {

	/*
	 * Global variables used within GoogleMapActivity.
	 */
	private GoogleMap mGoogleMap;
	private static final int LOAD_LOCATIONS=0;
	private LocationCursor mLocationCursor;
	private MapManager mMapManager;
	private int mParticipantId;
	
	/* Constructor to create a new instance of a map Fragment. Based on Nerd Ranch Guide Page 1454. */
	public static GoogleMapFragment newInstance(int participantId) {
		Bundle args = new Bundle();
		args.putInt("userParticipantId", participantId);
		GoogleMapFragment mf = new GoogleMapFragment();
		mf = new GoogleMapFragment();
		mf.setArguments(args);
		return mf;
	}
	
	/*
	 * Method called when the Fragment is created (as part of the Android Life Cycle) which sets up this Fragments's variables.
	 * Page 1465.
	 *
	 * */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args != null) {
			mParticipantId = args.getInt("userParticipantId", -1);
			if(mParticipantId != -1) {
				LoaderManager lm = getLoaderManager();
				lm.initLoader(LOAD_LOCATIONS, args, this);
			}
		}
		
		mMapManager = MapManager.get(getActivity());
	}
	
	/* Method called when the Fragment is first created (as part of the Android Life cycle) to set up the screen appropriately.
	 * Based on Nerd Ranch Guide Page 1454. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		mGoogleMap = getMap();
		mGoogleMap.setMyLocationEnabled(true);
		return v;
	}
	
	/* Method called when the list of pin locations saved in the SQLite database for the given HuntParticipantId has finished loading
	 * (see method onLoadFinished()). The pins are drawn on screen to show the given participant where they have made a successful 
	 * QR Code scan for the given treasure hunt. The first pin dropped is highlighted in a different colour
	 * than the other pins. Page 1476. */
	private void updateUI() {
		
		if(mGoogleMap == null || mLocationCursor == null ) {
			return;
		}
			
		List<Location> locationsForMarkers = mMapManager.queryLocationsForMarkers(mParticipantId);
		//http://www.mkyong.com/java/how-do-loop-iterate-a-list-in-java/

		for(int i = 0 ; i < locationsForMarkers.size(); i++) {
			
			MarkerOptions marker = new MarkerOptions().position(new LatLng(locationsForMarkers.get(i).getLatitude(), locationsForMarkers.get(i).getLongitude()));
			marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
			mGoogleMap.addMarker(marker);
		}
	}

	/* Method that creates a new instance of the LocationListCursorLoader for the given HuntParticipantId. Page 1465. */
	@Override
	public Loader<Cursor> onCreateLoader(int value, Bundle args) {
		int huntParticipantId = args.getInt("userParticipantId", -1);
		return new LocationListCursorLoader(getActivity(), huntParticipantId);
	}

	/* Method handling what happens when the LocationListCursorLoader has finished loading. The map shown on screen
	 * will be updated with the new data gathered. Page 1465. */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mLocationCursor = (LocationCursor) cursor;
		updateUI();
	}

	/* Method handling what happens when the LocationListCursorLoader is reset. Page 1466. */
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mLocationCursor.close();
		mLocationCursor = null;	
	}
}
