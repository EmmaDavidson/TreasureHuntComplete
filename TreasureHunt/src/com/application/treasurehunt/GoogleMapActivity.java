package com.application.treasurehunt;

import Utilities.SingleFragmentActivity;
import android.os.Build;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

public class GoogleMapActivity extends SingleFragmentActivity {
	
	MapManager mMapManager;

	@Override
	protected Fragment createFragment() {
		int participantId = getIntent().getIntExtra("userParticipantIdForMap", -1);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My current hunt map");
		}
		
		mMapManager = MapManager.get(this);
		
		if(participantId != 1)
		{
			return GoogleMapFragment.newInstance(participantId);
		}
			return new GoogleMapFragment();
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
				Intent loginActivityIntent = new Intent(GoogleMapActivity.this, LoginActivity.class);
				mMapManager.stopLocationUpdates();
				startActivity(loginActivityIntent);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
}
