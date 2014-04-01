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
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.Hunt;
import sqlLiteDatabase.HuntDAO;

import Mapping.MapManager;

import Utilities.ChooseHuntListAdapter;
import Utilities.InternetUtility;
import Utilities.JSONParser;
import Utilities.PHPHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

//http://net.tutsplus.com/tutorials/php/php-database-access-are-you-doing-it-correctly/

/* The purpose of this Activity is to allow a participant to select from a list of treasure hunts for a particular
 * company. [See dissertation Section 2.4.2.4].*/

public class ChooseHuntActivity extends Activity {

	/* Global variables used within ChooseHuntActivity.*/
	private static final String CHOOSE_HUNT_URL =  "http://lowryhosting.com/emmad/chooseHunts.php";
	
	private ListView mListView;
	private ProgressDialog mReturnHuntsDialog;
	
	public JSONParser jsonParser = new JSONParser();
	public static JSONArray sTagResult;
	private ReturnHuntsTask mReturnHuntsTask = null;
	 
	private HuntDAO mHuntDataSource;
	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSettings;
	
	private InternetUtility mInternetUtility;
	private MapManager mMapManager;
	
	private Handler mHandlerForUpdatingHuntList;
	
	//http://stackoverflow.com/questions/12220239/repeat-task-in-android
	//http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
	
	/* This Runnable  will periodically update the list of treasure hunts available from 
	 * the database for the given company.*/
	
	//QUESTION TO SELF - AGAIN, SHOULD I JUST IT REPETITIVELY RUN WHEN THE SCREEN HAS BEEN TILTED?
	private final Runnable mUpdateHuntsList = new Runnable() {
		
		@Override
		public void run() {
			mHuntDataSource.refreshCompanyHunts();
			
			if(mInternetUtility.isInternetConnected()) {
				attemptReturnHunts();
			}
			else {
				Toast.makeText(ChooseHuntActivity.this, InternetUtility.INTERNET_DISCONNECTED, Toast.LENGTH_LONG).show();
			}
			
			mHandlerForUpdatingHuntList.postDelayed(mUpdateHuntsList, 60000);
		}
	};
	
	private int mCurrentAdministratorId;
	
	/*
	 * Method called when the Activity is created (as part of the Android Life Cycle) which sets up this Activity's variables.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_hunt);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Choose a hunt");
		}
		
		mListView = (ListView) findViewById(R.id.hunt_list_view);	
		
		mMapManager = MapManager.get(this);
		mInternetUtility = InternetUtility.getInstance(this);
		
		mHuntDataSource = HuntDAO.getInstance(this);
		mHuntDataSource.open();
		
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		mCurrentAdministratorId = mSettings.getInt("administratorId", 0);
		
		////http://stackoverflow.com/questions/12220239/repeat-task-in-android
		mHandlerForUpdatingHuntList = new Handler();
		mHandlerForUpdatingHuntList.post(mUpdateHuntsList);
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
					
				Intent homepageActivityIntent = new Intent(ChooseHuntActivity.this, HomepageActivity.class);
				startActivity(homepageActivityIntent);
				
				return true;
			}
			case 2: {
				
				mEditor.clear();
				mEditor.commit();
				
				mMapManager.stopLocationUpdates();
				
				Intent loginActivityIntent = new Intent(ChooseHuntActivity.this, LoginActivity.class);
				startActivity(loginActivityIntent);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Method called when the activity is paused by the participant (as part of the Android Life Cycle).
	 * It prevents the list of treasure hunts from being updated when it is not visible on screen.
	 * */
	@Override
	protected void onPause() {
		
		super.onPause();
		mHandlerForUpdatingHuntList.removeCallbacks(mUpdateHuntsList);
	}
		
	/* Method to call the asynchronous class 'ReturnHuntsTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptReturnHunts() {
		
		if (mReturnHuntsTask != null) {
			return;
		} 			
			mReturnHuntsTask = new ReturnHuntsTask();
			mReturnHuntsTask.execute((String) null);	
			//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mReturnHuntsTask != null) {
						if(mReturnHuntsTask.getStatus() == AsyncTask.Status.RUNNING) {
							mReturnHuntsTask.cancel(true);
							mReturnHuntsDialog.cancel();
							Toast.makeText(ChooseHuntActivity.this, mConnectionTimeout, 
									Toast.LENGTH_LONG).show();
						}
					}
				}
			}
			
			, 10000);
			try {
				mReturnHuntsTask.get();
			} catch (InterruptedException e) {
				try {
					throw new InterruptedException();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	}
	

	/* This internal class attempts to return from the database a list of treasure hunts that are 
	 * available for a participant to choose from for a given company. */
	public class ReturnHuntsTask extends AsyncTask<String, String, String> {
	
		/*A dialog will appear on screen to show the participant that a search is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mReturnHuntsDialog = new ProgressDialog(ChooseHuntActivity.this);
	        mReturnHuntsDialog.setMessage("Attempting to get treasure hunts...");
			mReturnHuntsDialog.setIndeterminate(false);
			mReturnHuntsDialog.setCancelable(false);
			mReturnHuntsDialog.show();
		}
		
		/* Method calling the database to return all treasure hunts present for a given company.*/
		@Override
		public String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			//http://www.php.net/manual/en/pdostatement.fetchall.php
			//http://stackoverflow.com/questions/14491430/using-pdo-to-echo-display-all-rows-from-a-table
		
			int success;
			
			try {
				//PHP CURRENT DATE TIME CHECK http://stackoverflow.com/questions/470617/get-current-date-and-time-in-php
				Log.i("ChooseHunt", "starting");
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("administratorId", Integer.toString(mCurrentAdministratorId)));
				
				JSONObject jsonResult = jsonParser.makeHttpRequest(CHOOSE_HUNT_URL, "POST", parameters);
				
				Log.i("ChooseHunt", jsonResult.toString());
				
				success = jsonResult.getInt(PHPHelper.SUCCESS);
				if(success == 1) {
					Log.i("ChooseHunt", jsonResult.toString());
					sTagResult = jsonResult.getJSONArray(PHPHelper.RESULTS);
					
					//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
					for(int i=0; i < sTagResult.length(); i++)
					{
						mHuntDataSource.addHunt(sTagResult.getJSONArray(i).getJSONObject(0).getInt("HuntId"),
						sTagResult.getJSONArray(i).getJSONObject(0).getString("HuntName"),
						sTagResult.getJSONArray(i).getJSONObject(0).getString("HuntDescription"),
						sTagResult.getJSONArray(i).getJSONObject(0).getString("EndDate"));
					}
					
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				else {
					Log.w("ChooseHunt", jsonResult.getString(PHPHelper.MESSAGE));
					
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
		
		/* Method called after the database call has been made. If a list of treasure hunts has been returned
		 * then this list is displayed on screen, else the participant is notified with of an error on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mReturnHuntsTask = null;
			mReturnHuntsDialog.dismiss();
	
			if (fileUrl != null) {	
				List<Hunt> listOfHunts = mHuntDataSource.getAllHunts();
				final ChooseHuntListAdapter adapter = new ChooseHuntListAdapter(ChooseHuntActivity.this, listOfHunts);
				mListView.setAdapter(adapter);	
				
				//http://stackoverflow.com/questions/16189651/android-listview-selected-item-stay-highlighted
				mListView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						
						//check here to make sure that user hasn't already registered with this hunt
						//http://stackoverflow.com/questions/4508979/android-listview-get-selected-item
						Hunt selectedHunt = adapter.getItem(position);
					    
						Intent registerWithHuntintent = new Intent(ChooseHuntActivity.this, RegisterWithHuntActivity.class);
						mEditor.putInt("currentHuntId", selectedHunt.getHuntId());
						mEditor.putString("currentHuntName", selectedHunt.getHuntName());
						mEditor.putString("currentHuntDescription", selectedHunt.getHuntDescription());
						mEditor.putString("currentEndDate", selectedHunt.getEndDate());
						mEditor.commit(); 
	
						startActivity(registerWithHuntintent);
					}
				});
				
			} 
			else {
				
				showFailedTreasureHuntDataReturnMessage();
			}
		}
	
		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			
			mReturnHuntsTask = null;
		}
		
		/* Method to display a dialog if no treasure hunt data is returned from the database call.
		 * Associated with ReturnHuntsTask onPostExecute() method.*/
		private void showFailedTreasureHuntDataReturnMessage() {
			
			Builder alertForNoData = new Builder(ChooseHuntActivity.this);
			alertForNoData.setTitle("Treasure Hunts");
			alertForNoData.setMessage("There are currently no hunts to show for this company. Please check back later.");
			alertForNoData.setCancelable(false);
			alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent chooseCompanyIntent = new Intent(ChooseHuntActivity.this, ChooseCompanyActivity.class);
					startActivity(chooseCompanyIntent);
				}
			});
			
			alertForNoData.create();
			alertForNoData.show();
			
			Log.w("ChooseHunt", "No treasure hunts returned from the database for the company identified by id " 
					+ mCurrentAdministratorId);
		}
	}
}

