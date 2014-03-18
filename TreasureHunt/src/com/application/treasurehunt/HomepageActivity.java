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

import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import Mapping.MapManager;

/*
 * The purpose of this Activity is to be the main home page for a participant.
 * From here participants can navigate either to their hunts or to a view where they can browse other treasure hunts. 
 * */

public class HomepageActivity extends Activity {
	
	/*
	 * Global variables used within HomepageActivity.
	 */
	private Button mMyHuntsButton;
	private Button mBrowseHuntsButton;
	private TextView mNameView;
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	
	private MapManager mMapManager;
	
	private String mUserName;
	
	/*
	 * Method called when the Activity is created (as part of the Android Life Cycle) which sets up this Activity's variables.
	 * It also handles what happens when the participant presses one of the buttons to screen.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homepage);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My Homepage");
		}
		
		mMyHuntsButton = (Button) findViewById(R.id.my_hunts_button);
		mBrowseHuntsButton = (Button) findViewById(R.id.browse_hunts_button);
		mNameView = (TextView) findViewById(R.id.homepage_username);
		
		mMapManager = MapManager.get(this);
		
		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();

		mUserName = mSettings.getString("currentUserName", " user ");
		mNameView.setText("Welcome, " + mUserName);

		mMyHuntsButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent myHuntsActivity = new Intent(HomepageActivity.this, ChooseTypeOfMyHuntActivity.class);	
						startActivity(myHuntsActivity);
					}
				});
		
		mBrowseHuntsButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent chooseCompanyActivity = new Intent(HomepageActivity.this, ChooseCompanyActivity.class);	
						startActivity(chooseCompanyActivity);
					}
				});	
	}

	/* Methods to set up the on screen menu. This particular menu only contains an option to log out. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		menu.add(Menu.NONE, 1, Menu.NONE, "Log out");
		return true;
	} 
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		//http://mobileorchard.com/android-app-development-menus-part-1-options-menu/
		switch(item.getItemId()) {
		case 1:
			mEditor.clear();
			mEditor.commit();
			
			mMapManager.stopLocationUpdates();
			
			Intent loginActivityIntent = new Intent(HomepageActivity.this, LoginActivity.class);
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
