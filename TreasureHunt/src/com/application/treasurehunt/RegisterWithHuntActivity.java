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
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.MapData;

import Mapping.MapManager;

import Utilities.InternetUtility;
import Utilities.JSONParser;
import Utilities.PHPHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* The purpose of this Activity is to allow a participant to do one of the following things:
 * 1. Register with a given treasure hunt;
 * 2. Play a given treasure hunt.
 * [See Dissertation Section 2.4.2.5]*/
public class RegisterWithHuntActivity extends Activity {
	
	/*
	 * Global variables used within RegisterWithHuntActivity.
	 */
	private static final String REGISTER_WITH_HUNT_URL =  "http://lowryhosting.com/emmad/huntParticipantSave.php";
	private static final String CHECK_REGISTRATION_URL = "http://lowryhosting.com/emmad/checkUserHuntRegistration.php";
	private static final String SAVE_START_TIME_URL = "http://lowryhosting.com/emmad/saveHuntStartTime.php";
	private static final String CHECK_IF_HUNT_STARTED_URL = "http://lowryhosting.com/emmad/checkIfHuntStarted.php";
	private static final String GET_HUNT_PARTICIPANT_ID_URL = "http://lowryhosting.com/emmad/getHuntParticipantId.php";
	
	private TextView mhuntDescriptionView;
	private TextView mHuntNameLabelView;
	
	private Button mBeginHuntButton;
	private Button mRegisterButton;
	
	private ProgressDialog mRegisterDialog;
	private ProgressDialog mCheckStatusDialog;
	private ProgressDialog mSavingStartTimeDialog;
	private ProgressDialog mPreparationDialog;
	private ProgressBar mDescriptionProgressBar;

	private Intent mScanQRCodeIntent;
	
	public JSONParser jsonParser = new JSONParser();
	
	private static JSONObject sHuntParticipantIdResult;
	private static JSONObject sHuntIdResult;
	private static JSONObject sStartTimeResult;
	
	private SaveStartTimeTask mSaveStartTimeTask = null;
	private UserRegisterWithHuntTask mRegisterWithHuntTask = null;
	private CheckIfUserRegisteredTask mUserRegisteredTask = null;
	private CheckIfHuntStartedTask mHuntStartedTask = null;
	private GetParticipantIdTask mGetHuntParticipantIdTask;
	
	private InternetUtility mInternetUtility;
	private MapData mMap;
	private MapManager mMapManager;
	
	private int mUserId;
	private int mHuntParticipantId;
	
	private String mCurrentHunt;
	private String mCurrentHuntDescription;
	private String mEndDate;
	
	private boolean mHuntParticipantIdReturned = false;
	private boolean mCurrentHuntIdReturned = false;
	private boolean mRegistrationSuccessful = false;
	private boolean mHuntDescriptionreturned = false;
	private boolean mUserAlreadyRegistered = false;
	private boolean mHuntAlreadyStarted = false;
	private boolean mHuntAlreadyFinished = false;
	private boolean startTimeSaved = false;
	
	private long mStartTime;
	
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSettings;
	
	private int mHuntId;
	
	private Builder mAlertForHuntOver;

	/*
	 * Method called when the Activity is created (as part of the Android Life Cycle) which sets up this Activity's variables.
	 * It also decides what to do when on screen buttons are pressed. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_with_hunt);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Register with hunt");
		}
		
		mhuntDescriptionView = (TextView) findViewById(R.id.hunt_description_box);
		mHuntNameLabelView = (TextView) findViewById(R.id.hunt_name_label);
		
		mBeginHuntButton = (Button) findViewById(R.id.start_treasure_hunt_button);
		mBeginHuntButton.setEnabled(false);
		mRegisterButton = (Button) findViewById(R.id.register_hunt_button);
		mRegisterButton.setEnabled(false);

		mDescriptionProgressBar = (ProgressBar) findViewById(R.id.hunt_description_progress_bar);
		mDescriptionProgressBar.setIndeterminate(true);
		mDescriptionProgressBar.setVisibility(ProgressBar.INVISIBLE);
		
		mInternetUtility = InternetUtility.getInstance(this);
		mMapManager = MapManager.get(this);

		showHuntNoLongerAvailableMessage();

		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		retrieveHuntDetails();	
		checkIfHuntFinished();
			
		mRegisterButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
	
						if(mCurrentHuntIdReturned){
							if(mInternetUtility.isInternetConnected()) {
								attemptRegisterWithHunt();
							}
							else {
								Toast.makeText(RegisterWithHuntActivity.this, InternetUtility.INTERNET_DISCONNECTED, 
											Toast.LENGTH_LONG).show();
							}
						}
						else {
							Log.w("RegisterWithHunt", "Current Hunt Id not returned.");
						}	
					}
				});
		
		mBeginHuntButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(mInternetUtility.isInternetConnected()) {
								getHuntParticipantId();
							
						}
						else {
							Toast.makeText(RegisterWithHuntActivity.this, InternetUtility.INTERNET_DISCONNECTED, Toast.LENGTH_LONG).show();
						}
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
			
			Intent loginActivityIntent = new Intent(RegisterWithHuntActivity.this, LoginActivity.class);
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Method saves the details about the current treasure hunt when the activity is paused to prevent a database recall.
	 * */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {	
		
		savedInstanceState.putString("HUNT_REGISTRATION_CURRENT_HUNT_DESCRIPTION", mhuntDescriptionView.getText().toString());
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_USER_REGISTERED", mUserAlreadyRegistered);
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_HUNT_ID_RETURNED", mCurrentHuntIdReturned);
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_HUNT_DESCRIPTION_RETURNED", mHuntDescriptionreturned);
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_HUNT_ALREADY_STARTED", mHuntAlreadyStarted);
		
		//http://developer.android.com/training/basics/activity-lifecycle/recreating.html
		super.onSaveInstanceState(savedInstanceState);
	}	
	
	/* Method to call the asynchronous class 'CheckIfUserRegisteredTask'. If call to the database takes too long then a timeout should occur.*/
	private void checkIfUserHasAlreadyRegistered() {
		if (mUserRegisteredTask != null) {
			return;
		} 	
		
		mUserRegisteredTask = new CheckIfUserRegisteredTask();
		mUserRegisteredTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mUserRegisteredTask!= null) {
					if(mUserRegisteredTask.getStatus() == AsyncTask.Status.RUNNING) {
						mUserRegisteredTask.cancel(true);
						mCheckStatusDialog.cancel();
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}

	/* Method to call the asynchronous class 'CheckIfHuntStartedTask'. If call to the database takes too long then a timeout should occur.*/
	private void checkIfHuntAlreadyStarted() {
		if (mHuntStartedTask != null) {
			return;
		} 	
		
		mHuntStartedTask = new CheckIfHuntStartedTask();
		mHuntStartedTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mHuntStartedTask!= null) {
					if(mHuntStartedTask.getStatus() == AsyncTask.Status.RUNNING) {
						mHuntStartedTask.cancel(true);
						mPreparationDialog.cancel();
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						//mRefreshButton.setVisibility(Button.VISIBLE);
					}
				}
			}
		}
		, 10000);	
	}
	
	/* Method to call the asynchronous class 'UserRegisterWithHuntTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptRegisterWithHunt() {	
		if (mRegisterWithHuntTask != null) {
			return;
		} 		
			
			mRegisterWithHuntTask = new UserRegisterWithHuntTask(); // Do ASYNC way
			mRegisterWithHuntTask.execute((String) null);
			
			//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
			Handler handlerForMAuth = new Handler();
			handlerForMAuth.postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mRegisterWithHuntTask!= null) {
						if(mRegisterWithHuntTask.getStatus() == AsyncTask.Status.RUNNING) {
							mRegisterWithHuntTask.cancel(true);
							mRegisterDialog.cancel();
							Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						}
					}
				}
			}
			, 100000);				
	}
	
	/* Method to call the asynchronous class 'GetParticipantIdTask'. If call to the database takes too long then a timeout should occur.*/
	private void getHuntParticipantId() {
		if (mGetHuntParticipantIdTask != null) {
			return;
		} 	
		
		mGetHuntParticipantIdTask = new GetParticipantIdTask();
		mGetHuntParticipantIdTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mGetHuntParticipantIdTask!= null) {
					if(mGetHuntParticipantIdTask.getStatus() == AsyncTask.Status.RUNNING) {
						mSavingStartTimeDialog.cancel();
						mGetHuntParticipantIdTask.cancel(true);
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();	
					}
				}
			}
		}
		, 10000);	
	}
	
	/* Method to call the asynchronous class 'SaveStartTimeTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptToSaveStartTime() {
		if (mSaveStartTimeTask != null) {
			return;
		} 	
		mSaveStartTimeTask = new SaveStartTimeTask();
		mSaveStartTimeTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mSaveStartTimeTask!= null) {
					if(mSaveStartTimeTask.getStatus() == AsyncTask.Status.RUNNING) {
						mSaveStartTimeTask.cancel(true);
						mSavingStartTimeDialog.cancel();
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}
	
	/* Method that retrieves the data sent by the previous Activity and assigns it to the relevant variables. */
	public void retrieveHuntDetails() {
		
		mHuntId = mSettings.getInt("currentHuntId", 0);
		mCurrentHunt = mSettings.getString("currentHuntName", "");
		mCurrentHuntDescription = mSettings.getString("currentHuntDescription", "No description retrieved.");
		mEndDate = mSettings.getString("currentEndDate", "");
		mUserId = mSettings.getInt("currentUserId", 0); //http://developer.android.com/guide/topics/data/data-storage.html#pref
		
		if(mHuntId != 0) {
			mEditor.putInt("currentHuntId", mHuntId);
			mEditor.commit();
			mCurrentHuntIdReturned = true;
		}
		else {
			mRegisterButton.setEnabled(false);
			showCouldNotRetrieveDetailsMessage();
		}
		
		mHuntNameLabelView.setText(mCurrentHunt);
		mhuntDescriptionView.setText(mCurrentHuntDescription);
	}
	
	/* Method that will check if the current treasure hunt is out of date i.e. it is no longer playable. If so,
	 * prevent the participant from registering or playing the given hunt. Else, check if the hunt has already
	 * been registered with or started. */
	public void checkIfHuntFinished() {
		
		Date today = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date endDate;
		
		try {
			//http://stackoverflow.com/questions/15761101/how-to-get-a-date-from-a-json-object
			endDate = format.parse(mEndDate);
			if(today.after(endDate)) {
				mHuntAlreadyFinished = true;
			}
		} catch (ParseException e) {
			try {
				throw new ParseException(mEndDate, 0);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		
		if(mHuntAlreadyFinished) {
			mBeginHuntButton.setEnabled(false);
			mRegisterButton.setEnabled(false);
			mAlertForHuntOver.show();
		}
		else {
			if(!mUserAlreadyRegistered) {
				checkIfUserHasAlreadyRegistered();	
			}
			if(!mHuntAlreadyStarted) {
				checkIfHuntAlreadyStarted();
			}	
		}	
	}
	
	/* Method to display a dialog to alert the participant if the treasure hunt they seek to access is no longer available i.e. 
	 * it is out of date and can no longer be played.*/
	private void showHuntNoLongerAvailableMessage() {
		//http://www.mkyong.com/android/android-alert-dialog-example/
		mAlertForHuntOver = new Builder(this);
		mAlertForHuntOver.setTitle("Hunt over");
		mAlertForHuntOver.setMessage("This hunt is no longer available. You can no longer participate in this hunt.");
		mAlertForHuntOver.setCancelable(false);
		mAlertForHuntOver.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RegisterWithHuntActivity.this.finish();
				
			}
		});
		
		mAlertForHuntOver.create();
	}
	
	/* Method to display a dialog to alert the participant that the start time could not be saved when attempting to begin the treasure hunt 
	 * for the first time. */
	private void showSaveTimeFailureMessage() {
		//http://www.mkyong.com/android/android-alert-dialog-example/
		Builder mAlertForFailedStartTimeSave = new Builder(this);
		mAlertForFailedStartTimeSave.setTitle("Application Error");
		mAlertForFailedStartTimeSave.setMessage("Application error. Please try again.");
		mAlertForFailedStartTimeSave.setCancelable(false);
		mAlertForFailedStartTimeSave.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
		
		mAlertForFailedStartTimeSave.create();
		mAlertForFailedStartTimeSave.show();
	}
	
	private void showCouldNotRetrieveDetailsMessage() {
		
		Builder mAlertForFailedDetails = new Builder(this);
		mAlertForFailedDetails.setTitle("Application Error");
		mAlertForFailedDetails.setMessage("Application error. Could not retrieve details");
		mAlertForFailedDetails.setCancelable(false);
		mAlertForFailedDetails.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();	
			}
		});
		
		mAlertForFailedDetails.create();
		mAlertForFailedDetails.show();
		
		mRegisterButton.setEnabled(false);
		mBeginHuntButton.setEnabled(false);
	}
	
	/* Method to display a dialog if registration with the given treasure hunt has failed.*/
	private void showFailedRegistrationMessage() {
		Builder alertForFailedRegistration = new Builder(RegisterWithHuntActivity.this);
		alertForFailedRegistration.setTitle("Registration");
		alertForFailedRegistration.setMessage("Registration for this treasure hunt failed. Please try again later.");
		alertForFailedRegistration.setCancelable(false);
		alertForFailedRegistration.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		alertForFailedRegistration.create();
		alertForFailedRegistration.show();
		
		Log.w("RegisterWithHunt", "Failed registration with hunt: " + mHuntId + "for participant: " + mUserId);
	}
	
	/* This internal class attempts to check whether or not a given participant has already registered with a given application. */
	public class CheckIfUserRegisteredTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a check is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mCheckStatusDialog = new ProgressDialog(RegisterWithHuntActivity.this);
			mCheckStatusDialog.setMessage("Attempting status check...");
			mCheckStatusDialog.setIndeterminate(false);
			mCheckStatusDialog.setCancelable(true);
			mCheckStatusDialog.show();
		}
		
		/* Method calling the database to check if the given participant's details have already been saved for the given treasure hunt.*/
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
				int success;
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parameters.add(new BasicNameValuePair("huntId", Integer.toString(mHuntId)));
				parameters.add(new BasicNameValuePair("userId", Integer.toString(mUserId)));
				
				try{
				Log.i("RegisterWithHunt", "starting");
				JSONObject json = jsonParser.makeHttpRequest(CHECK_REGISTRATION_URL, "POST", parameters);
				Log.i("RegisterWithHunt", json.toString());
					
				success = json.getInt(PHPHelper.SUCCESS);
				if(success == 1) {							
					Log.i("RegisterWithHunt","User is already registered");
					
					mUserAlreadyRegistered = true;
					return json.getString(PHPHelper.MESSAGE);
				}
				else {
					Log.w("RegisterWithHunt","User has not registered previously");
					mUserAlreadyRegistered = false;
					return json.getString(PHPHelper.MESSAGE);
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
	
		/* Method called after the database call has been made. If the participant has not already registered then the 'Register' button becomes available
		 * whilst the 'Play' button becomes unavailable, else, vice versa for a registered participant. */
		@Override
		protected void onPostExecute(final String fileUrl) {
			mUserRegisteredTask = null;
			mCheckStatusDialog.cancel();
			
			if(!mUserAlreadyRegistered) {
				mBeginHuntButton.setEnabled(false);
				mRegisterButton.setEnabled(true);
				Log.i("RegisterWithHunt", "user: " + mUserId + " has not yet registered with hunt: " + mHuntId);
			}
			else {
				mBeginHuntButton.setEnabled(true);
				mRegisterButton.setEnabled(false);
			}
			
			if (fileUrl != null) {
				Log.i("RegisterWithHunt",  fileUrl);
			} else {
				Log.w("RegisterWithHunt", "Nothing returned from the database");
			}		
		}
	
		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mUserRegisteredTask = null;
			mCheckStatusDialog.cancel();
		}
	}

	/* This internal class attempts to check whether or not a participant has begun playing the given treasure hunt 
	 * i.e. it may be the case where a participant
	 * has registered but not yet decided to play.*/
	public class CheckIfHuntStartedTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a check is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mPreparationDialog = new ProgressDialog(RegisterWithHuntActivity.this);
			mPreparationDialog.setMessage("Preparation...");
			mPreparationDialog.setIndeterminate(false);
			mPreparationDialog.setCancelable(true);
			mPreparationDialog.show();
		}
		
		/* Method calling the database to check if a start time has been associated with the given participant and treasure hunt.*/
		@Override
		protected String doInBackground(String... args) {
			
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
				Log.i("RegisterWithHunt", "CheckIfHuntStartedTask has begun");
				int success;
				
				try{
					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
					
					//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
					parameters.add(new BasicNameValuePair("huntId", Integer.toString(mHuntId)));
					parameters.add(new BasicNameValuePair("userId", Integer.toString(mUserId)));
					Log.i("RegisterWithHunt", "starting");
					Log.i("RegisterWithHunt", "Checking if hunt started - Current Hunt Id:" + mHuntId);
					Log.i("RegisterWithHunt", "Checking if hunt started - Current User Id:" + mUserId);
					
					Log.i("RegisterWithHunt", "Attempting to see if hunt has already started");
					JSONObject jsonResult = jsonParser.makeHttpRequest(CHECK_IF_HUNT_STARTED_URL, "POST", parameters);
					Log.i("RegisterWithHunt", jsonResult.toString());
						
					success = jsonResult.getInt(PHPHelper.SUCCESS);
					
					if(success == 1) {	//http://stackoverflow.com/questions/1576243/php-check-for-null				
						Log.i("RegisterWithHunt", mHuntId + " has already started");
						
						mHuntAlreadyStarted = true;
						sStartTimeResult = jsonResult.getJSONObject(PHPHelper.RESULTS);
						Log.i("RegisterWithHunt", jsonResult.getJSONObject(PHPHelper.RESULTS)+"");
						mStartTime = sStartTimeResult.getLong("StartTime");
			
						return jsonResult.getString(PHPHelper.MESSAGE);
					}
					else {
						Log.w("RegisterWithHunt", mHuntId + " has not already started");
						mHuntAlreadyStarted = false;
						//save that hunt has already started with the shared pref
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
	
		/* Method called after the database call has been made. Indicates in shared preferences if the hunt has already been started by the given participant.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			mHuntStartedTask = null;
			mPreparationDialog.cancel();
	
			mEditor.putBoolean(mHuntId + " isHuntAlreadyStarted", mHuntAlreadyStarted);
			mEditor.putLong(mHuntId + " startTime", (long) mStartTime);
			mEditor.commit(); 
			
			if (fileUrl != null)  {
				Log.i("RegisterWithHunt", fileUrl);	
			} 
			else {
				Log.w("RegisterWithHunt", "Could not determine if hunt started or not");
			}		
		}
	
		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mHuntStartedTask = null;
			mPreparationDialog.cancel();
		}
	}
	
	/* This internal class attempts to register the given participant with the given treasure hunt.  */
	public class UserRegisterWithHuntTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a save is being made.*/
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mRegisterDialog = new ProgressDialog(RegisterWithHuntActivity.this);
            mRegisterDialog.setMessage("Attempting registration...");
			mRegisterDialog.setIndeterminate(false);
			mRegisterDialog.setCancelable(true);
			mRegisterDialog.show();
		}
		
		/* Method calling the database to register the participant with the given hunt.*/
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
			int success;
				List<NameValuePair> parametersForPasswordCheck = new ArrayList<NameValuePair>();
				
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parametersForPasswordCheck.add(new BasicNameValuePair("huntid", Integer.toString(mHuntId)));
				parametersForPasswordCheck.add(new BasicNameValuePair("userid", Integer.toString(mUserId)));
				
				try{
				Log.i("RegisterWithHunt", "starting");
				JSONObject json = jsonParser.makeHttpRequest(REGISTER_WITH_HUNT_URL, "POST", parametersForPasswordCheck);
				Log.i("RegisterWithHunt", json.toString());
					
				success = json.getInt(PHPHelper.SUCCESS);
				if(success == 1) {							
					Log.i("RegisterWithHunt", json.toString());
					Log.i("RegisterWithHunt", "User has just registered with: " + mHuntId);
		
					mRegistrationSuccessful = true;
					return json.getString(PHPHelper.MESSAGE);
				}
				else {
					Log.w("RegisterWithHunt", json.getString(PHPHelper.MESSAGE));
					return json.getString(PHPHelper.MESSAGE);
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

		/* Method called after the database call has been made. If registration has been successful
		 * then the 'Play' button becomes available to use whilst the participant can no longer press the 'Register' button, else,
		 * the participant is notified of an error on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			mRegisterWithHuntTask = null;
			mRegisterDialog.dismiss();
			
			if(mRegistrationSuccessful) {
				mBeginHuntButton.setEnabled(true);
				mRegisterButton.setEnabled(false);
				
				//mEditor.putInt("userParticipantId", mCurrentParticipantId);
				//mEditor.commit(); 
				
				Log.i("RegisterWithHunt", fileUrl);
			}
			else {
				showFailedRegistrationMessage();
			}
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mRegisterWithHuntTask = null;
		}
	}
	/* This internal class attempts to retrieve the given participant's HuntParticipantId for a given treasure hunt. 
	 * This id is utilised when the participant is 
	 * directed to the ScanQRCodeActivity.*/
	public class GetParticipantIdTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a save is being made.*/
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mSavingStartTimeDialog = new ProgressDialog(RegisterWithHuntActivity.this);
			mSavingStartTimeDialog.setMessage("Preparing hunt...");
			mSavingStartTimeDialog.setIndeterminate(false);
			mSavingStartTimeDialog.setCancelable(true);
			mSavingStartTimeDialog.show();
		}
		
		/* Method calling the database to return the HuntParticipantId associated with the given participant and treasure hunt.*/
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
				int success;
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parameters.add(new BasicNameValuePair("huntId", Integer.toString(mHuntId)));
				parameters.add(new BasicNameValuePair("userId", Integer.toString(mUserId)));
				
				try{
					Log.i("RegisterWithHunt", "starting");
					JSONObject jsonResult = jsonParser.makeHttpRequest(GET_HUNT_PARTICIPANT_ID_URL, "POST", parameters);
					Log.i("RegisterWitHunt", jsonResult.toString());
					success = jsonResult.getInt(PHPHelper.SUCCESS);
					
					if(success == 1) {
						sHuntParticipantIdResult = jsonResult.getJSONObject(PHPHelper.RESULTS);
						mHuntParticipantId = sHuntParticipantIdResult.getInt("HuntParticipantId");
						mHuntParticipantIdReturned = true;
						Log.i("RegisterWithHunt", "hunt participant id is: " + mHuntParticipantId);
						
						return jsonResult.getString(PHPHelper.MESSAGE);
					}
					else {
						Log.w("RegisterWithHunt", jsonResult.getString(PHPHelper.MESSAGE));
						mHuntParticipantIdReturned = false;
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

		/* Method called after the database call has been made. If an ID has been returned then a check is made to see if the treasure hunt has already
		 * been started by the participant.
		 * If so, i.e. a start time has already been saved for this participant, the participant proceeds to the ScanQRCodeActivity.
		 * Otherwise, i.e. the participant has not yet began playing this hunt, an attempt to save a new start time is made.
		 * */
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mGetHuntParticipantIdTask = null;
			
			if(mHuntParticipantIdReturned) {
				mEditor.putInt("huntParticipantId", mHuntParticipantId);
				mEditor.putBoolean(mHuntId + " userParticipantIdReturned", true);
				mEditor.commit(); 
				
				Log.i("RegisterWithHunt", mHuntAlreadyStarted+ " = has hunt already started by the time of the check on the button");
				
				if(!mHuntAlreadyStarted) {
					//http://stackoverflow.com/questions/3944344/how-to-display-the-time-elapsed-onto-ui-screen-of-the-android
					attemptToSaveStartTime();
				}	
				else {
					mSavingStartTimeDialog.cancel();						
					mEditor.putLong(mHuntId + " startTime", (long) mStartTime);
					mEditor.putBoolean(mHuntId + " isHuntAlreadyStarted", mHuntAlreadyStarted);
					mEditor.commit();
					Log.i("RegisterWithHunt", "START TIME HAS BEEN SAVED TO SHARED PREF");
					
					//NOT CURRENTLY BEING USED - TAKEN OUT?
					//Set up associated map with existing details.
					mMap = mMapManager.getMapData(mHuntParticipantId);
					
					//If if the participant has deleted the application and reinstalled it then create a new map for this treasure hunt.
					if(mMap == null) {
						mMap = mMapManager.startNewMap(mHuntParticipantId);
					}
					mMapManager.startTrackingMap(mMap); 
					
					mScanQRCodeIntent = new Intent(RegisterWithHuntActivity.this, ScanQRCodeActivity.class);
					startActivity(mScanQRCodeIntent);
				}
			}
			if (fileUrl != null) {
				Log.i("RegisterWithHunt", fileUrl);
			} 
			else  {
				Log.w("RegisterWithHunt", "Nothing returned from the database");
			}		
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mGetHuntParticipantIdTask = null;
			mSavingStartTimeDialog.cancel();
		}
	}
	
	/* This internal class attempts to save the time at which the 'Play' button was pressed by a participant who 
	 * has just begun playing a given treasure hunt. */
	public class SaveStartTimeTask extends AsyncTask<String, String, String> {
		
		/* Method calling the database to save the start time associated with the given participant and treasure hunt when the 'Play' button is first clicked after
		 * registration.*/
		@Override
		protected String doInBackground(String... arg0) {
			int startTimeSuccess;		
			
			try {
					Log.i("RegisterWithHunt", "SaveStartTimeTask has begun");
					mStartTime = System.currentTimeMillis();
							
					List<NameValuePair> parametersForHuntId = new ArrayList<NameValuePair>();
					parametersForHuntId.add(new BasicNameValuePair("userId", Long.toString(mUserId)));
					parametersForHuntId.add(new BasicNameValuePair("huntId", Long.toString(mHuntId)));
					parametersForHuntId.add(new BasicNameValuePair("startTime", Long.toString(mStartTime)));
					
					Log.i("RegisterWithHunt", "starting");
					JSONObject saveStartTimeObject = jsonParser.makeHttpRequest(SAVE_START_TIME_URL, "POST", parametersForHuntId);
					Log.i("RegisterWithHunt", saveStartTimeObject.toString());
					
					startTimeSuccess = saveStartTimeObject.getInt(PHPHelper.SUCCESS);
					
					if(startTimeSuccess == 1) {
						Log.i("RegisterWithHunt", "start time for " + mHuntId + " is " + mStartTime );
						mHuntAlreadyStarted = true;
						startTimeSaved = true;
						return saveStartTimeObject.getString(PHPHelper.MESSAGE);			
					}
					else {
						Log.w("RegisterWithHunt", saveStartTimeObject.getString(PHPHelper.MESSAGE));
						startTimeSaved = false;
						return saveStartTimeObject.getString(PHPHelper.MESSAGE);
					}
				}catch (JSONException e) {
					try {
						throw new JSONException(e.toString());
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			return null;	
		}
	
		/* Method called after the database call has been made. If the start time was successfully saved then it is saved in the shared preferences.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mSaveStartTimeTask = null;
			mSavingStartTimeDialog.cancel();
		
			if(startTimeSaved) {
				mEditor.putBoolean(mHuntId + " isHuntAlreadyStarted", mHuntAlreadyStarted);
				mEditor.putLong(mHuntId + " startTime", mStartTime);
				mEditor.commit(); 
				
				mMap = mMapManager.startNewMap(mHuntParticipantId);
				
				Log.i("RegisterWithHunt", mStartTime+ " = start time");
				mScanQRCodeIntent = new Intent(RegisterWithHuntActivity.this, ScanQRCodeActivity.class);
				startActivity(mScanQRCodeIntent);
			}
			else {
				Log.w("RegisterWithHunt", "Start time was not saved for the user: " + mUserId);
				showSaveTimeFailureMessage();
			}
		}
		
		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			
			mSaveStartTimeTask = null;
			mSavingStartTimeDialog.cancel();
		}
	}
}