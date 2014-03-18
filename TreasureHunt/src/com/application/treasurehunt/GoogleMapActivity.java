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

/* The purpose of this Activity is to create a 'holder' for an instance of a GoogleMapFragment to 'sit in' when it has been created i.e.
 * this Activity will display the relevant Fragment. Based on the Nerd Ranch Guide.
 * [See Dissertation Section 2.4.2.9]*/
public class GoogleMapActivity extends SingleFragmentActivity {
	
	/*
	 * Global variables used within GoogleMapActivity.
	 */
	private MapManager mMapManager;

	/* Method called when Activity created (as part of the Android Life Cycle) to create and display an instance of GoogleMapFragment
	 * for the associated participant and treasure hunt. Based on Page 1456 */
	@Override
	protected Fragment createFragment() {
			
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My current hunt map");
		}
		
		mMapManager = MapManager.get(this);
		
		int huntParticipantId = getIntent().getIntExtra("userParticipantIdForMap", -1);
	
		//If the HuntParticipantId is a valid number then create an instance.
		if(huntParticipantId != -1) {
			return GoogleMapFragment.newInstance(huntParticipantId);
		}
		return new GoogleMapFragment();
	}
	
	/* Methods to set up the on screen menu. This particular menu only contains an option to log out. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		menu.add(Menu.NONE, 1, Menu.NONE, "Log out");
		return true;
	} 
	
	//http://mobileorchard.com/android-app-development-menus-part-1-options-menu/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case 1:
				SharedPreferences settings = getSharedPreferences("UserPreferencesFile", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.clear();
				editor.commit();
				
				mMapManager.stopLocationUpdates();
				
				Intent loginActivityIntent = new Intent(GoogleMapActivity.this, LoginActivity.class);
				startActivity(loginActivityIntent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
