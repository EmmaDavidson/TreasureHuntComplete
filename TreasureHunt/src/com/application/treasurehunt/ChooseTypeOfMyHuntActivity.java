package com.application.treasurehunt;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ChooseTypeOfMyHuntActivity extends Activity {
	
	MapManager mMapManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_type_of_my_hunt);
		
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
			Intent loginActivityIntent = new Intent(ChooseTypeOfMyHuntActivity.this, LoginActivity.class);
			mMapManager.stopLocationUpdates();
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
