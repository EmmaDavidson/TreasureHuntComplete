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

public class HomepageActivity extends Activity {
	
	Button mMyHuntsButton;
	Button mBrowseHuntsButton;
	TextView mNameView;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	MapManager mMapManager;
	
	String userName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homepage);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My Homepage");
		}
		
		mMapManager = MapManager.get(this);
		
		mMyHuntsButton = (Button) findViewById(R.id.my_hunts_button);
		mBrowseHuntsButton = (Button) findViewById(R.id.browse_hunts_button);
		mNameView = (TextView) findViewById(R.id.homepage_username);
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();

		userName = settings.getString("currentUserName", " user ");

		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		mNameView.setText("Welcome, " + userName);
		
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
						Intent chooseHuntActivity = new Intent(HomepageActivity.this, ChooseHuntActivity.class);	
						startActivity(chooseHuntActivity);
					}
				});	
	}

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
			Intent loginActivityIntent = new Intent(HomepageActivity.this, LoginActivity.class);
			mMapManager.stopLocationUpdates();
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
