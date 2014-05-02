/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
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

import Mapping.MapManager;
import Utilities.TypeOfHunt;

/*
 * The purpose of this Activity is to allow a participant to navigate to the correct type of treasure hunt that they 
 * have registered with i.e. 'Not Started', 'Current' and 'Completed'.
 *  */

public class ChooseTypeOfMyHuntActivity extends Activity {
	
	/*
	 * Global variables used within ChooseTypeOfMyHuntActivity.
	 */
	private MapManager mMapManager;
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;

	/*
	 * Method called when the Activity is created (as part of the Android Life Cycle) which sets up this Activity's variables.
	 * It also decides which new Activity to navigate to depending on what button was pressed on screen.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_type_of_my_hunt);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Choose hunt type");
		}
		
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		mMapManager = MapManager.get(this);
		
		findViewById(R.id.not_started_hunts_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent myHuntsActivity = new Intent(ChooseTypeOfMyHuntActivity.this, MyHuntsActivity.class);
						myHuntsActivity.putExtra("Type", "NotStarted");
						startActivity(myHuntsActivity);
					}
				});
		
		findViewById(R.id.current_hunts_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent myHuntsActivity = new Intent(ChooseTypeOfMyHuntActivity.this, MyHuntsActivity.class);
						myHuntsActivity.putExtra("Type", "Current");
						startActivity(myHuntsActivity);
					}
				});
		
		findViewById(R.id.completed_hunts_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent myHuntsActivity = new Intent(ChooseTypeOfMyHuntActivity.this, MyHuntsActivity.class);
						myHuntsActivity.putExtra("Type", "Completed");
						startActivity(myHuntsActivity);
					}
				});
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
					
				Intent homepageActivityIntent = new Intent(ChooseTypeOfMyHuntActivity.this, HomepageActivity.class);
				startActivity(homepageActivityIntent);
				
				return true;
			}
			case 2: {
				
				mEditor.clear();
				mEditor.commit();
				
				mMapManager.stopLocationUpdates();
				
				Intent loginActivityIntent = new Intent(ChooseTypeOfMyHuntActivity.this, LoginActivity.class);
				startActivity(loginActivityIntent);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}

}
