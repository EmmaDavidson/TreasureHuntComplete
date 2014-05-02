/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package com.application.treasurehunt;

import Mapping.GoogleMapFragment;
import Mapping.MapManager;
import Utilities.SingleFragmentActivity;
import android.os.Build;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

/* Activity based on the Nerd Ranch Guide*/

/* The purpose of this Activity is to create a 'holder' for an instance of a GoogleMapFragment to
 *  'sit in' when it has been created i.e. this Activity will display the relevant Fragment. 
 * [See Dissertation Section 2.4.2.9]*/
public class GoogleMapActivity extends SingleFragmentActivity {
	
	/*
	 * Global variables used within GoogleMapActivity.
	 */
	private MapManager mMapManager;
	
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSettings;

	/* Method called when Activity created (as part of the Android Life Cycle) to create and display an instance of GoogleMapFragment
	 * for the associated participant and treasure hunt. Based on Nerd Ranch Guide Page 1456 */
	@Override
	protected Fragment createFragment() {
			
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My current hunt map");
		}
		
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		mMapManager = MapManager.get(this);
		
		int huntParticipantId = getIntent().getIntExtra("userParticipantIdForMap", -1);
	
		//If the HuntParticipantId is a valid number then create an instance.
		if(huntParticipantId != -1) {
			return GoogleMapFragment.newInstance(huntParticipantId);
		}
		return new GoogleMapFragment();
	}
	
	/* Methods to set up the on screen menu. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		//http://mobileorchard.com/android-app-development-menus-part-1-options-menu/
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		menu.add(Menu.NONE, 1, Menu.NONE, "Home");
		menu.add(Menu.NONE, 2, Menu.NONE, "Log out");
		return true;
	} 
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		
		switch(item.getItemId()) {
			case 1: {
					
				Intent homepageActivityIntent = new Intent(GoogleMapActivity.this, HomepageActivity.class);
				startActivity(homepageActivityIntent);
				
				return true;
			}
			case 2: {
				
				mEditor.clear();
				mEditor.commit();
				
				mMapManager.stopLocationUpdates();
				
				Intent loginActivityIntent = new Intent(GoogleMapActivity.this, LoginActivity.class);
				startActivity(loginActivityIntent);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
}
