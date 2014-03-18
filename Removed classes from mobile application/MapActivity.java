package com.application.treasurehunt;

import Mapping.MapFragment;
import Mapping.MapManager;
import Utilities.SingleFragmentActivity;
import android.os.Build;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

public class MapActivity extends SingleFragmentActivity {

	MapManager mMapManager;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	@Override
	protected Fragment createFragment()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My hunt map's details");
		}
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		mMapManager = MapManager.get(this);
		
		int participantId =  settings.getInt("userParticipantId", 0);
		if(participantId != -1)
		{
			return MapFragment.newInstance(participantId);
		}
		else
		{
			return new MapFragment();
		}
	}
	
	//http://mobileorchard.com/android-app-development-menus-part-1-options-menu/
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			getMenuInflater().inflate(R.menu.login, menu);
			menu.add(Menu.NONE, 1, Menu.NONE, "Log out");
			return true;
		} 
		
		//http://mobileorchard.com/android-app-development-menus-part-1-options-menu/
		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			switch(item.getItemId())
			{
			case 1:
				SharedPreferences settings = getSharedPreferences("UserPreferencesFile", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.clear();
				editor.commit();
				Intent loginActivityIntent = new Intent(MapActivity.this, LoginActivity.class);
				mMapManager.stopLocationUpdates();
				startActivity(loginActivityIntent);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

}
