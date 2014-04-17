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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.Hunt;
import sqlLiteDatabase.HuntDAO;

import Mapping.MapManager;

import Utilities.ExpandableListAdapter;
import Utilities.InternetUtility;
import Utilities.JSONParser;
import Utilities.PHPHelper;
import Utilities.TypeOfHunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * The purpose of this Activity is to display a list of treasure hunts that a participant has registered with according to its type
 * i.e. 'Not Started', 'Current' and 'Completed'.
 * [See dissertation Section 2.4.2.7]
 * */
public class MyHuntsActivity extends Activity {

	/*
	 * Global variables used within MyHuntsActivity.
	 */
	private static final String USER_HUNTS_NOT_STARTED_URL =  "http://lowryhosting.com/emmad/chooseUserHuntNotStarted.php";
	private static final String USER_HUNTS_COMPLETED_URL =  "http://lowryhosting.com/emmad/chooseUserHuntCompleted.php";
	private static final String USER_HUNTS_CURRENT_URL =  "http://lowryhosting.com/emmad/chooseUserHuntCurrent.php";
	private static final String GET_HUNT_PARTICIPANT_ID_URL = "http://lowryhosting.com/emmad/getHuntParticipantId.php";
	
	private ExpandableListView mListView;
	private ProgressDialog mRetrievalDialog;
	private ProgressDialog mMapDataDialog;
	
	private static JSONObject sHuntParticipantIdResult;
	private ReturnUserHuntsTask mReturnUserHuntsTask;
	private GetHuntParticipantIdTask mGetHuntParticipantIdTask;
	public JSONParser jsonParser = new JSONParser();
	
	private static JSONArray sTagResult;
	private static JSONArray sTagIdResult;
	
	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSettings;
	
	private MapManager mMapManager;
	private InternetUtility mInternetUtility;
	
	private HuntDAO mHuntDataSource;
	private Hunt mChosenHunt;

	private String mTypeOfHunt;
	private int mCurrentUserId;
	private int mHuntParticipantId;
	private boolean mHuntParticipantIdReturned;
	private boolean mHuntsReturned;
	
	/*
	 * Method called when the Activity is created (as part of the android life cycle) which sets up this Activity's variables.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_hunts);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My hunts");
		}
		
		mListView = (ExpandableListView) findViewById(R.id.list_of_user_hunts_id);
		
		mHuntDataSource = HuntDAO.getInstance(this);
		mHuntDataSource.open();
		
		mInternetUtility = InternetUtility.getInstance(this);
		mMapManager = MapManager.get(this);
		
		mTypeOfHunt = getIntent().getStringExtra("Type");
		
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		mCurrentUserId = mSettings.getInt("currentUserId", 0);
		
		if(mInternetUtility.isInternetConnected()) {
			attemptToReturnUserHunts();
		}
		else {
			Toast.makeText(MyHuntsActivity.this, InternetUtility.INTERNET_DISCONNECTED, Toast.LENGTH_LONG).show();
		}
	}
	
	/*
	 * Method called when the Activity is resumed (as part of the Android Life Cycle).
	 * Updates the list of treasure hunts for the given section.
	 * */
	/*@Override
	protected void onResume() {
		
		super.onResume();
		mHuntDataSource.refreshMyHunts();
		
		if(mInternetUtility.isInternetConnected()) {
			attemptToReturnUserHunts();
		}
		else {
			Toast.makeText(MyHuntsActivity.this, InternetUtility.INTERNET_DISCONNECTED, Toast.LENGTH_LONG).show();
		}
	}*/
	
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
					
				Intent homepageActivityIntent = new Intent(MyHuntsActivity.this, HomepageActivity.class);
				startActivity(homepageActivityIntent);
				
				return true;
			}
			case 2: {
				
				mEditor.clear();
				mEditor.commit();
				
				mMapManager.stopLocationUpdates();
				
				Intent loginActivityIntent = new Intent(MyHuntsActivity.this, LoginActivity.class);
				startActivity(loginActivityIntent);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/* Method to call the asynchronous class 'ReturnUserHuntsTask'. If call to the database takes too 
	 * long then a timeout should occur.*/
	private void attemptToReturnUserHunts() {
		
		if (mReturnUserHuntsTask != null) {
			return;
		} 	
		mReturnUserHuntsTask = new ReturnUserHuntsTask();
		mReturnUserHuntsTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mReturnUserHuntsTask!= null) {
					if(mReturnUserHuntsTask.getStatus() == AsyncTask.Status.RUNNING) {
						mRetrievalDialog.cancel();
						mReturnUserHuntsTask.cancel(true);
						Toast.makeText(MyHuntsActivity.this, mConnectionTimeout, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 20000);
	}
	
	/* Method to call the asynchronous class 'GetParticipantIdTask'. If call to the database takes too long then a timeout should occur.*/
	private void getParticipantId() {
		
		if (mGetHuntParticipantIdTask != null) {
			return;
		} 	
		
		mGetHuntParticipantIdTask = new GetHuntParticipantIdTask();
		mGetHuntParticipantIdTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mGetHuntParticipantIdTask!= null) {
					if(mGetHuntParticipantIdTask.getStatus() == AsyncTask.Status.RUNNING) {
						mMapDataDialog.cancel();
						mGetHuntParticipantIdTask.cancel(true);
						Toast.makeText(MyHuntsActivity.this, mConnectionTimeout, Toast.LENGTH_LONG).show();	
					}
				}
			}
		}
		, 20000);	
	}

	/* Method that will display the refreshed list of treasure hunts on screen. It will also handle what happens when the participant 
	 * selects an item and one of its options on screen. Options include 'continue', 'leader board' and 'map' for each hunt displayed.*/
	public void updateUI(final List<String> listDataHeader, HashMap<String, List<String>> listDataChild) {
		
		//http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
		final ExpandableListAdapter adapter = new ExpandableListAdapter(MyHuntsActivity.this, listDataHeader, listDataChild);
		mListView.setAdapter(adapter);
		mListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent,
					View v, int groupPosition, int childPosition,
					long id) {
				//http://stackoverflow.com/questions/4508979/android-listview-get-selected-item
				mChosenHunt = mHuntDataSource.getParticularHunt(listDataHeader.get(groupPosition));
			
				if(mChosenHunt != null) {	
					mEditor.putInt("currentHuntId", mChosenHunt.getHuntId()); 
					mEditor.putString("currentHuntName", mChosenHunt.getHuntName());
					mEditor.putString("currentHuntDescription", mChosenHunt.getHuntDescription());
					mEditor.putString("currentEndDate", mChosenHunt.getEndDate());
					mEditor.commit(); 
					
					if(childPosition == 0) {
						Intent registerWithHuntIntent = new Intent(MyHuntsActivity.this, RegisterWithHuntActivity.class);
						startActivity(registerWithHuntIntent); 
					}
					else if (childPosition == 1) {
						Intent leaderboardActivity = new Intent(MyHuntsActivity.this, LeaderboardActivity.class);
						startActivity(leaderboardActivity);
					}
					else if(childPosition ==2) {
						getParticipantId();
					}
				}
				return true;
			}
		});
		
		//http://stackoverflow.com/questions/16189651/android-listview-selected-item-stay-highlighted
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//check here to make sure that user hasn't already registered with this hunt
				//http://stackoverflow.com/questions/4508979/android-listview-get-selected-item
				Hunt selectedHunt = (Hunt) ((Menu) adapter).getItem(position);
			    
				//Instead of starting a new activity, it only starts if the stats or map button pressed
				//Intent registerWithHuntintent = new Intent(MyHuntsActivity.this, RegisterWithHuntActivity.class);
				mEditor.putString("currentHuntName", selectedHunt.getHuntName());
				mEditor.commit(); 
			}
		});
	}
	
	/* Method that displays a dialog to tell the participant that map data could not be retrieved.
	 * Associated with GetHuntParticipantIdTask OnPostExecute() method */
	public void showNoMapDataMessage() {
	
		Builder alertForNoIdRetrieved = new Builder(MyHuntsActivity.this);
		alertForNoIdRetrieved.setTitle("Failure");
		alertForNoIdRetrieved.setMessage("Could not retrieve data for map. Please check back later.");
		alertForNoIdRetrieved.setCancelable(false);
		alertForNoIdRetrieved.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		alertForNoIdRetrieved.create();
		alertForNoIdRetrieved.show();
		
		Log.w("MyHunts", "No Hunt Participant Id has been returned from the database for user "
				+ mCurrentUserId + " to use with map.");	
	}
	
	/* Method that displays a dialog to tell the participant that no treasure hunts could be retrieved to be displayed.
	 * Associated with ReturnUserHuntsTask OnPostExecute() method */
	public void showNoTreasureHuntsMessage() {
		Builder alertForNoData = new Builder(MyHuntsActivity.this);
		alertForNoData.setTitle("Treasure Hunts");
		alertForNoData.setMessage("There are currently no treasure hunts to show. Please check back later.");
		alertForNoData.setCancelable(false);
		alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent chooseTypeOfHuntActivity = new Intent(MyHuntsActivity.this, ChooseTypeOfMyHuntActivity.class);
				startActivity(chooseTypeOfHuntActivity);
			}
		});
		
		alertForNoData.create();
		alertForNoData.show();
		
		Log.w("MyHunts", "No treasure hunts returned from the database");
	}
	
	
	/* This internal class attempts to return from the database a list of treasure hunts for a given choice i.e. 'Not Started', 'Current'
	 * or 'Completed' */
	public class ReturnUserHuntsTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a search is being made.*/
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mRetrievalDialog = new ProgressDialog(MyHuntsActivity.this);
	        mRetrievalDialog.setMessage("Attempting to get treasure hunts...");
			mRetrievalDialog.setIndeterminate(false);
			mRetrievalDialog.setCancelable(true);
			mRetrievalDialog.show();
		}
		
		/* Method calling the database to return all relevant treasure hunts.*/
		@Override
		public String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			//http://www.php.net/manual/en/pdostatement.fetchall.php
			//http://stackoverflow.com/questions/14491430/using-pdo-to-echo-display-all-rows-from-a-table
		
			int success;
			
			try {
					Log.i("MyHunts", "starting");
					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
					parameters.add(new BasicNameValuePair("userId", Integer.toString(mCurrentUserId)));
					
					JSONObject jsonResult = new JSONObject();
					
					//look for hunts that have start times that are 0 i.e. registered but not started
					if(mTypeOfHunt.equals("NotStarted")) {
						jsonResult = jsonParser.makeHttpRequest(USER_HUNTS_NOT_STARTED_URL, "POST", parameters);
					}
					else if(mTypeOfHunt.equals("Current")) {
						jsonResult = jsonParser.makeHttpRequest(USER_HUNTS_CURRENT_URL, "POST", parameters);
					}
					else {	//look for hunts where hunt end date is less than current date
						jsonResult = jsonParser.makeHttpRequest(USER_HUNTS_COMPLETED_URL, "POST", parameters);
					}
					
					Log.i("MyHunts", jsonResult.toString());
					
					success = jsonResult.getInt(PHPHelper.SUCCESS);
					if(success == 1) {
						Log.i("MyHunts", jsonResult.toString());
						sTagResult = jsonResult.getJSONArray("results");
						sTagIdResult = jsonResult.getJSONArray("huntIds");
						mHuntsReturned = true;
						//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
						//For each treasure hunt returned it is displayed by name on the list.
						for(int i=0; i < sTagResult.length(); i++) {
							mHuntDataSource.addUserHunt(sTagIdResult.getJSONObject(i).getInt("HuntId"), 
									sTagResult.getJSONArray(i).getJSONObject(0).getString("HuntName"),
									sTagResult.getJSONArray(i).getJSONObject(0).getString("HuntDescription"),
									sTagResult.getJSONArray(i).getJSONObject(0).getString("EndDate"));
						}
						
						return jsonResult.getString(PHPHelper.MESSAGE);
					}
					else {
						Log.w("MyHunts", jsonResult.getString(PHPHelper.MESSAGE));
						mHuntsReturned = false;
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
		 * then this list is displayed, else the participant is alerted of an error on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			mReturnUserHuntsTask = null;
			mRetrievalDialog.dismiss();

			if (mHuntsReturned)  {	
				List<String> listDataHeader;
				//http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
				HashMap<String, List<String>> listDataChild;
				
		        listDataHeader = new ArrayList<String>();
		        listDataChild = new HashMap<String, List<String>>();
				
		        Log.i("MyHunts","fileUrl");
		        
				List<Hunt> listOfHunts = mHuntDataSource.getAllUserHunts();
				
				//For every listOfHunts, add the header with its name
				for(int i=0; i< listOfHunts.size(); i++) {
					listDataHeader.add(listOfHunts.get(i).getHuntName().toString());
					List<String> huntOptions = new ArrayList<String>();
					huntOptions.add("Continue");
					huntOptions.add("Leaderboard");
					huntOptions.add("Map");
					
					listDataChild.put(listDataHeader.get(i), huntOptions);		
				}
				
				updateUI(listDataHeader, listDataChild);
			} 
			else {
				showNoTreasureHuntsMessage();
			}
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mReturnUserHuntsTask = null;
		}
	}
	
	/* This internal class attempts to return from the database the HuntParticipantId associated with the given participant. 
	 * In particular this Id is used for the maps functionality. */
	public class GetHuntParticipantIdTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a search is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mMapDataDialog = new ProgressDialog(MyHuntsActivity.this);
			mMapDataDialog.setMessage("Attempting to return data for map");
			mMapDataDialog.setIndeterminate(false);
			mMapDataDialog.setCancelable(true);
			mMapDataDialog.show();
		}
	
		/* Method calling the database to return the HuntParticipantId associated with the given participant.*/
		@Override
		public String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
				int success;

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parameters.add(new BasicNameValuePair("huntId", Integer.toString(mChosenHunt.getHuntId())));
				parameters.add(new BasicNameValuePair("userId", Integer.toString(mCurrentUserId)));
				
				try{
					Log.i("MyHunts", "starting");
					JSONObject jsonResult = jsonParser.makeHttpRequest(GET_HUNT_PARTICIPANT_ID_URL, "POST", parameters);
					Log.i("MyHunts", "Get User Id Attempt: " + jsonResult.toString());
					success = jsonResult.getInt(PHPHelper.SUCCESS);
					
					if(success == 1) {
						sHuntParticipantIdResult = jsonResult.getJSONObject(PHPHelper.RESULTS);
						mHuntParticipantId = sHuntParticipantIdResult.getInt("HuntParticipantId");
						mHuntParticipantIdReturned = true;
						Log.i("MyHunts", "hunt participant id is: " + mHuntParticipantId);
						return jsonResult.getString(PHPHelper.MESSAGE);
						
					}
					else {
						Log.w("MyHunts", jsonResult.getString(PHPHelper.MESSAGE));
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

		/* Method called after the database call has been made. If a HuntParticipantId has been returned
		 * then the GoogleMapActivity is started, else, the participant is notified of the error on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			mGetHuntParticipantIdTask = null;
			mMapDataDialog.cancel();
			
			if(mHuntParticipantIdReturned) {
				mEditor.putInt("userParticipantId", mHuntParticipantId);
				mEditor.commit(); 
		
				//Based loosely on Nerd Ranch Guide Page 1462
				Intent googleMapActivity = new Intent(MyHuntsActivity.this, GoogleMapActivity.class);
				googleMapActivity.putExtra("userParticipantIdForMap", mHuntParticipantId);
				startActivity(googleMapActivity);
				
				Log.i("MyHunts", fileUrl);
			}
			else {
				showNoMapDataMessage();
			}			
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mGetHuntParticipantIdTask = null;
		}
	}
}
