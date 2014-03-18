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

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.Leaderboard;
import sqlLiteDatabase.LeaderboardDAO;

import Mapping.MapManager;

import Utilities.InternetUtility;
import Utilities.JSONParser;
import Utilities.LeaderboardListAdapter;
import Utilities.PHPHelper;

import java.util.ArrayList;
import java.util.List;

/*
 * The purpose of this Activity is to display all of the current participants of a given treasure hunt on screen
 * on a 'leader board', where the participants are displayed in descending order according to their tally then elapsed time. 
 * [See dissertation section 2.4.2.8]
 * */

public class LeaderboardActivity extends Activity {
	 
	/*
	 * Global variables used within LeaderboardActivity.
	 */
	private static final String RETURN_LEADERBOARD_URL =  "http://lowryhosting.com/emmad/returnHuntLeaderboardResults.php";
	
	private ListView mListView;
	private ProgressDialog mGetLeaderboardDialog;
	
	public JSONParser jsonParser = new JSONParser();
	private static JSONArray sTagResult;
	private static JSONArray sTagNameResult;
	private ReturnLeaderboardTask mLeaderboardTask;
	
	private LeaderboardDAO mLeaderboardDataSource;
	
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSettings;
	
	private MapManager mMapManager;
	private InternetUtility mInternetUtility;
	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	private Handler mHandlerForUpdatingLeaderboard;

	/*Runnable that will periodically update the list of participants and their scores for a given treasure hunt.*/
	private final Runnable mUpdateLeaderboardList = new Runnable(){
		
		//http://stackoverflow.com/questions/12220239/repeat-task-in-android
		//http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
		@Override
		public void run() {
			mLeaderboardDataSource.refreshLeaderboard();
			if(mInternetUtility.isInternetConnected()) {
				attemptToReturnLeaderboard();
			}
			else {
				Toast.makeText(LeaderboardActivity.this, InternetUtility.INTERNET_DISCONNECTED, Toast.LENGTH_LONG).show();
			}
			mHandlerForUpdatingLeaderboard.postDelayed(mUpdateLeaderboardList, 10000);
		}
	};
	
	private int mCurrentHuntId;
	
	/*
	 * Method called when the Activity is created (as part of the android life cycle) which sets up this Activity's variables.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leaderboard);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Leaderboard");
		}
		
		mListView = (ListView) findViewById(R.id.leaderboard_list_view);
		
		mLeaderboardDataSource = new LeaderboardDAO(this);
		mLeaderboardDataSource.open();
		
		mInternetUtility = InternetUtility.getInstance(this);
		mMapManager = MapManager.get(this);
		
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		mCurrentHuntId = mSettings.getInt("currentHuntId", 0);
		
		mHandlerForUpdatingLeaderboard = new Handler();
		//http://stackoverflow.com/questions/12220239/repeat-task-in-android
		mHandlerForUpdatingLeaderboard.post(mUpdateLeaderboardList);
	}
	
	/*
	 * Method called when the Activity is paused (as part of the Android Life Cycle).
	 * Prevents the leader board updating when it is not visible on screen.
	 * */
	@Override
	protected void onPause() {
		
		super.onPause();
		mHandlerForUpdatingLeaderboard.removeCallbacks(mUpdateLeaderboardList);
	}
	
	/*
	 * Method saves the current HuntId when the Activity is paused.
	 * */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {	
		
		mCurrentHuntId = mSettings.getInt("currentHuntId", 0);
		savedInstanceState.putInt("currentHuntId", mCurrentHuntId);
		super.onSaveInstanceState(savedInstanceState);
	}	
	
	/*
	 * Method restoring the current HuntId when the Activity is restored.
	 * */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);
		mCurrentHuntId = savedInstanceState.getInt("currentHuntId");
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
			
			Intent loginActivityIntent = new Intent(LeaderboardActivity.this, LoginActivity.class);
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* Method to call the asynchronous class 'ReturnLeaderboardTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptToReturnLeaderboard() {
		
		if (mLeaderboardTask != null) {
			return;
		} 	
		
		mLeaderboardTask = new ReturnLeaderboardTask();
		mLeaderboardTask.execute((String) null);

		Handler handlerForLeaderboardTask = new Handler();
		handlerForLeaderboardTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mLeaderboardTask!= null) {
					if(mLeaderboardTask.getStatus() == AsyncTask.Status.RUNNING) {
						mLeaderboardTask.cancel(true);
						mGetLeaderboardDialog.cancel();
						Toast.makeText(LeaderboardActivity.this, mConnectionTimeout, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}
	
	/* Method to show a dialog if no leader board data could be returned from the database. 
	 * Associated with ReturnLeaderboardTask OnPostExecute() method.*/
	public void showNoLeaderboardDataMessage() {
		
		Builder alertForNoData = new Builder(LeaderboardActivity.this);
		alertForNoData.setTitle("Leader board");
		alertForNoData.setMessage("There is currently no data to show for this leader board. Please check back later.");
		alertForNoData.setCancelable(false);
		alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		alertForNoData.create();
		alertForNoData.show();
		
		Log.w("Leaderboard", "Nothing returned from the database for the leader board participants");
	}

	
	/* This internal class attempts to return from the database a list of participants for a given treasure hunt and their
	 * current hunt details including tally and elapsed time. */
	public class ReturnLeaderboardTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a search is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mGetLeaderboardDialog = new ProgressDialog(LeaderboardActivity.this);
	        mGetLeaderboardDialog.setMessage("Attempting to get leaderboard results...");
			mGetLeaderboardDialog.setIndeterminate(false);
			mGetLeaderboardDialog.setCancelable(true);
			mGetLeaderboardDialog.show();
		}
		
		/* Method calling the database to return all participants present for a given treasure hunt.*/
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			//http://www.php.net/manual/en/pdostatement.fetchall.php
			//http://stackoverflow.com/questions/14491430/using-pdo-to-echo-display-all-rows-from-a-table
		
			int success;
			
			try {
				Log.i("Leaderboard", "starting");
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("huntid", Integer.toString(mCurrentHuntId)));
				
				JSONObject jsonResult = jsonParser.makeHttpRequest(RETURN_LEADERBOARD_URL, "POST", parameters);
				
				Log.i("Leaderboard", jsonResult.toString());
				
				success = jsonResult.getInt(PHPHelper.SUCCESS);
				if(success == 1) {
					Log.i("Leaderboard", jsonResult.toString());
					sTagResult = jsonResult.getJSONArray("results");
					sTagNameResult = jsonResult.getJSONArray("resultNames");
					
					//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
					for(int i=0; i < sTagResult.length(); i++) {
						String elapsedTimeResult = sTagResult.getJSONObject(i).getString("ElapsedTime");
						mLeaderboardDataSource.addLeaderboardResult(sTagNameResult.getJSONArray(i).getJSONObject(0).getString("Name"),
								sTagResult.getJSONObject(i).getInt("Tally"),  Float.parseFloat(elapsedTimeResult));
					}
					
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				else {
					Log.w("Leaderboard", jsonResult.getString(PHPHelper.MESSAGE));
					
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				
			} catch (JSONException e) {
				try {
					throw new JSONException(e.toString());
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}

			return null;
		}

		/* Method called after the database call has been made. If a list of participants has been returned
		 * then this list is displayed else the participant is alerted of an error on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mLeaderboardTask = null;
			mGetLeaderboardDialog.dismiss();

			if (fileUrl != null)  {	
				List<Leaderboard> listOfLeaderboardResults = mLeaderboardDataSource.getAllResults();
				
				//Android book - The Big Nerd Ranch Guide
				LeaderboardListAdapter adapter = new LeaderboardListAdapter(LeaderboardActivity.this, listOfLeaderboardResults);
				mListView.setAdapter(adapter);	
			
			} 
			else {
				showNoLeaderboardDataMessage();
			}
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			
			mLeaderboardTask = null;
		}
	}
}
