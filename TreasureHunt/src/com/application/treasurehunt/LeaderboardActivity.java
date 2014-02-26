package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.Leaderboard;
import sqlLiteDatabase.LeaderboardDAO;
import Utilities.ExpandableListAdapter;
import Utilities.JSONParser;
import Utilities.LeaderboardListAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LeaderboardActivity extends Activity {

	 
	private JSONParser jsonParser;
	
	private static final String returnLeaderboardUrl =  "http://lowryhosting.com/emmad/returnHuntLeaderboardResults.php";
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	
	private static JSONArray tagResult;
	private static JSONArray tagNameResult;
	
	ReturnLeaderboardTask mLeaderboardTask;
	ListView mListView;
	LeaderboardDAO leaderboardDataSource;
		
	Handler handlerForUpdatingLeaderboard;
	
	PendingIntent mPendingIntent;
	
	int currentHuntId;
	
	private ProgressDialog pDialog;
	
	SharedPreferences.Editor editor;
	SharedPreferences settings;
	
	MapManager mMapManager;
	
	//http://stackoverflow.com/questions/12220239/repeat-task-in-android
	//http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
	final Runnable updateLeaderboardList = new Runnable(){
		@Override
		public void run()
		{
			leaderboardDataSource.updateDatabaseLocally();
			attemptToReturnLeaderboard();
			handlerForUpdatingLeaderboard.postDelayed(updateLeaderboardList, 10000);
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leaderboard);
		
		leaderboardDataSource = new LeaderboardDAO(this);
		leaderboardDataSource.open();
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Leaderboard");
		}
		
		mMapManager = MapManager.get(this);
		
		jsonParser = new JSONParser();
		
		mListView = (ListView) findViewById(R.id.leaderboard_list_view);
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		handlerForUpdatingLeaderboard = new Handler();
	
		currentHuntId = settings.getInt("currentHuntId", 0);
		
		//http://stackoverflow.com/questions/12220239/repeat-task-in-android
		handlerForUpdatingLeaderboard.post(updateLeaderboardList);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		handlerForUpdatingLeaderboard.removeCallbacks(updateLeaderboardList);
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{	
		currentHuntId = settings.getInt("currentHuntId", 0);
		
		savedInstanceState.putInt("currentHuntId", currentHuntId);
		//PUT STUFF BEFORE
		super.onSaveInstanceState(savedInstanceState);
	}	
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		//PUT STUFF AFTER
		currentHuntId = savedInstanceState.getInt("currentHuntId");

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
			Intent loginActivityIntent = new Intent(LeaderboardActivity.this, LoginActivity.class);
			mMapManager.stopLocationUpdates();
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void attemptToReturnLeaderboard()
	{
		if (mLeaderboardTask != null) {
			return;
		} 	
		
		mLeaderboardTask = new ReturnLeaderboardTask();
		mLeaderboardTask.execute((String) null);

		Handler handlerForLeaderboardTask = new Handler();
		handlerForLeaderboardTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mLeaderboardTask!= null)
				{
					if(mLeaderboardTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mLeaderboardTask.cancel(true);
						pDialog.cancel();
						Toast.makeText(LeaderboardActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}
	
	public class ReturnLeaderboardTask extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(LeaderboardActivity.this);
	        pDialog.setMessage("Attempting to get leaderboard results...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			//http://www.php.net/manual/en/pdostatement.fetchall.php
			//http://stackoverflow.com/questions/14491430/using-pdo-to-echo-display-all-rows-from-a-table
		
			int success;
			
			try {
				
				Log.d("request", "starting");
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("huntid", Integer.toString(currentHuntId)));
				
				JSONObject jsonLeaderboardCall = jsonParser.makeHttpRequest(returnLeaderboardUrl, "POST", parameters);
				
				Log.d("Get leaderboard attempt", jsonLeaderboardCall.toString());
				
				success = jsonLeaderboardCall.getInt(tagSuccess);
				if(success == 1)
				{
					Log.d("Return of leaderboard successful!", jsonLeaderboardCall.toString());
					tagResult = jsonLeaderboardCall.getJSONArray("results");
					tagNameResult = jsonLeaderboardCall.getJSONArray("resultNames");
					
					//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
					for(int i=0; i < tagResult.length(); i++)
					{
						String elapsedTimeResult = tagResult.getJSONObject(i).getString("ElapsedTime");
						leaderboardDataSource.addLeaderboardResult(tagNameResult.getJSONArray(i).getJSONObject(0).getString("Name"),
								tagResult.getJSONObject(i).getInt("Tally"),  Float.parseFloat(elapsedTimeResult));
					}
					
					return tagMessage.toString();
				}
				else
				{
					Log.d("Returning of leaderboard failed! ", jsonLeaderboardCall.getString(tagMessage));
					
					return jsonLeaderboardCall.getString(tagMessage);
				}
				
			} catch (JSONException e) {
				Log.d("leaderboard", e.toString());
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mLeaderboardTask = null;
			pDialog.dismiss();

			if (fileUrl != null) 
			{	
				List<Leaderboard> listOfLeaderboardResults = leaderboardDataSource.getAllResults();
				
				//Need to change this back to leaderboard for displaying all of the values... displaying in a table instead!
				//Android book - The Big Nerd Ranch Guide
				LeaderboardListAdapter adapter = new LeaderboardListAdapter(LeaderboardActivity.this, listOfLeaderboardResults);
				mListView.setAdapter(adapter);	
			
			} 
			else 
			{
				Toast.makeText(LeaderboardActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mLeaderboardTask = null;
		}
	}

}
