package com.application.treasurehunt;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.MapData;
import sqlLiteDatabase.MapDataDAO;

import com.application.treasurehunt.RegisterActivity.UserRegisterTask;

import Utilities.JSONParser;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RegisterWithHuntActivity extends Activity{
	
	//Home
	private static final String getHuntIdUrl =  "http://lowryhosting.com/emmad/returnCurrentHuntId.php";
	private static final String registerUserWithHuntUrl =  "http://lowryhosting.com/emmad/huntParticipantSave.php";
	private static final String getHuntDescriptionUrl = "http://lowryhosting.com/emmad/getHuntDescription.php";
	private static final String checkIfUserRegisteredUrl = "http://lowryhosting.com/emmad/checkUserHuntRegistration.php";
	private static final String saveStartTimeUrl = "http://lowryhosting.com/emmad/saveHuntStartTime.php";
	private static final String checkIfHuntStartedUrl = "http://lowryhosting.com/emmad/checkIfHuntStarted.php";
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";

	Intent scanQRCodeActivity;
	
	GetParticipantIdTask mGetHuntParticipantIdTask;
	private static final String getHuntParticipantIdUrl = "http://lowryhosting.com/emmad/getHuntParticipantId.php";
	boolean huntParticipantIdReturned;
	private JSONParser jsonParser = new JSONParser();
	
	boolean startTimeSaved;
	
	private static JSONObject huntParticipantIdResult;
	private static JSONObject huntIdResult;
	private static JSONObject currentHuntDescriptionResult;
	private static JSONObject saveStartTimeResult;
	private static JSONObject startTimeResult;
	private static JSONObject saveHuntParticipantResult;
	private static JSONObject checkIfHuntOverResult;
	
	private SaveStartTimeTask mSaveStartTimeTask = null;
	private UserRegisterWithHuntTask mAuthTask = null;
	private GetHuntDescriptionTask mHuntDescriptionTask = null;
	private CheckIfUserRegisteredTask mUserRegisteredTask = null;
	private CheckIfHuntStartedTask mHuntStartedTask = null;
	private ProgressDialog pDialog;
	
	ProgressBar descriptionProgressBar;
	
	private MapData mMap;
	private MapManager mMapManager;
	
	int userId;
	int currentParticipantId;
	GetHuntIdTask mHuntIdTask;
	
	String currentHunt;
	String currentHuntDescription;
	
	boolean currentHuntIdReturned = false;
	boolean registrationSuccessful = false;
	boolean huntDescriptionreturned = false;
	boolean userAlreadyRegistered = false;
	boolean huntAlreadyStarted = false;
	boolean huntAlreadyFinished = false;
	
	long startTime;
	
	TextView mhuntDescriptionView;
	TextView mHuntNameLabelView;
	
	Button mBeginHuntButton;
	Button mRegisterButton;
	
	SharedPreferences.Editor editor;
	SharedPreferences settings;
	
	int huntId;
	int currentUserId;
	
	Builder alertForHuntOver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_with_hunt);
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		//mPasswordView = (EditText) findViewById(R.id.register_hunt_password);
		mhuntDescriptionView = (TextView) findViewById(R.id.hunt_description_box);
		mHuntNameLabelView = (TextView) findViewById(R.id.hunt_name_label);
		
		mBeginHuntButton = (Button) findViewById(R.id.start_treasure_hunt_button);
		mBeginHuntButton.setEnabled(false);
		
		mRegisterButton = (Button) findViewById(R.id.register_hunt_button);
		mRegisterButton.setEnabled(false);
		
		mMapManager = MapManager.get(this);
		
		mRegisterButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
	
						if(currentHuntIdReturned){
							attemptRegisterWithHunt();
						}
						else
						{
							Toast.makeText(RegisterWithHuntActivity.this, "Internal error", Toast.LENGTH_LONG).show();
						}
						
					}
				});
		
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Register with a hunt");
		}
		
		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		settings = getSharedPreferences("UserPreferencesFile", 0);

		//http://www.mkyong.com/android/android-alert-dialog-example/
		alertForHuntOver = new Builder(this);
		alertForHuntOver.setTitle("Hunt over");
		alertForHuntOver.setMessage("This hunt is no longer available. You can no longer participate in this hunt.");
		alertForHuntOver.setCancelable(false);
		alertForHuntOver.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RegisterWithHuntActivity.this.finish();
				
			}
		});
		
		alertForHuntOver.create();
		
		
		descriptionProgressBar = (ProgressBar) findViewById(R.id.hunt_description_progress_bar);
		descriptionProgressBar.setIndeterminate(true);
		descriptionProgressBar.setVisibility(ProgressBar.INVISIBLE);

		currentHunt = settings.getString("currentHuntName", "");
		currentUserId = settings.getInt("currentUserId", 0);
		mHuntNameLabelView.setText(currentHunt);
		
		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		userId = settings.getInt("currentUserId", 0);
		
		/*if(savedInstanceState != null)
		{
			mhuntDescriptionView.setText(savedInstanceState.getString("HUNT_REGISTRATION_CURRENT_HUNT_DESCRIPTION"));
			userAlreadyRegistered = savedInstanceState.getBoolean("HUNT_REGISTRATION_HAS_USER_REGISTERED");
			currentHuntIdReturned = savedInstanceState.getBoolean("HUNT_REGISTRATION_HAS_HUNT_ID_RETURNED");
			huntDescriptionreturned = savedInstanceState.getBoolean("HUNT_REGISTRATION_HAS_HUNT_DESCRIPTION_RETURNED");
			//huntAlreadyStarted = savedInstanceState.getBoolean("HUNT_REGISTRATION_HAS_HUNT_ALREADY_STARTED");
			Log.d("leaderboard", "Value saved in SavedInstanceState for huntAlreadyStarted is " + huntAlreadyStarted);
			
			if(userAlreadyRegistered)
			{
				mBeginHuntButton.setEnabled(true);
				mRegisterButton.setEnabled(false);
			}
		} */
		
	
		if(!currentHuntIdReturned)
		{
			attemptToReturnHuntId();
		}
		if(!huntDescriptionreturned)
		{
			attemptToReturnHuntDescription();
		}
		

		

		
		mBeginHuntButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
					
						getParticipantId();
						
					}
				});
		
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
			Intent loginActivityIntent = new Intent(RegisterWithHuntActivity.this, LoginActivity.class);
			mMapManager.stopLocationUpdates();
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void attemptToSaveStartTime()
	{
		if (mSaveStartTimeTask != null) {
			return;
		} 	
		mSaveStartTimeTask = new SaveStartTimeTask();
		mSaveStartTimeTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mSaveStartTimeTask!= null)
				{
					if(mSaveStartTimeTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mSaveStartTimeTask.cancel(true);
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}
	
	private void checkIfUserHasAlreadyRegistered()
	{
		if (mUserRegisteredTask != null) {
			return;
		} 	
		
		mUserRegisteredTask = new CheckIfUserRegisteredTask();
		mUserRegisteredTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mUserRegisteredTask!= null)
				{
					if(mUserRegisteredTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mUserRegisteredTask.cancel(true);
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}
	
	private void attemptToReturnHuntDescription()
	{
		if (mHuntDescriptionTask != null) {
			return;
		} 	
		mHuntDescriptionTask = new GetHuntDescriptionTask();
		mHuntDescriptionTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mHuntDescriptionTask!= null)
				{
					if(mHuntDescriptionTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mHuntDescriptionTask.cancel(true);
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}
	
	private void attemptToReturnHuntId()
	{
		if (mHuntIdTask != null) {
			return;
		} 	
		mHuntIdTask = new GetHuntIdTask();
		mHuntIdTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mHuntIdTask!= null)
				{
					if(mHuntIdTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mHuntIdTask.cancel(true);
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}
	
	private void getParticipantId()
	{
		if (mGetHuntParticipantIdTask != null) {
			return;
		} 	
		
		mGetHuntParticipantIdTask = new GetParticipantIdTask();
		mGetHuntParticipantIdTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mGetHuntParticipantIdTask!= null)
				{
					if(mGetHuntParticipantIdTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mGetHuntParticipantIdTask.cancel(true);
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						
					}
				}
			}
		}
		, 10000);	
	}

	private void checkIfHuntAlreadyStarted()
	{
		if (mHuntStartedTask != null) {
			return;
		} 	
		
		mHuntStartedTask = new CheckIfHuntStartedTask();
		mHuntStartedTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mHuntStartedTask!= null)
				{
					if(mHuntStartedTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mHuntStartedTask.cancel(true);
						Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						//mRefreshButton.setVisibility(Button.VISIBLE);
					}
				}
			}
		}
		, 10000);	
	}
	
	private void attemptRegisterWithHunt() {	
		if (mAuthTask != null) {
			return;
		} 		
			
			mAuthTask = new UserRegisterWithHuntTask(); // Do ASYNC way
			mAuthTask.execute((String) null);
			
			//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
			Handler handlerForMAuth = new Handler();
			handlerForMAuth.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					if(mAuthTask!= null)
					{
						if(mAuthTask.getStatus() == AsyncTask.Status.RUNNING)
						{
							mAuthTask.cancel(true);
							pDialog.cancel();
							Toast.makeText(RegisterWithHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						}
					}
				}
			}
			, 100000);			
			
	}
	
	//http://developer.android.com/training/basics/activity-lifecycle/recreating.html
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{	
		savedInstanceState.putString("HUNT_REGISTRATION_CURRENT_HUNT_DESCRIPTION", mhuntDescriptionView.getText().toString());
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_USER_REGISTERED", userAlreadyRegistered);
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_HUNT_ID_RETURNED", currentHuntIdReturned);
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_HUNT_DESCRIPTION_RETURNED", huntDescriptionreturned);
		savedInstanceState.putBoolean("HUNT_REGISTRATION_HAS_HUNT_ALREADY_STARTED", huntAlreadyStarted);
		
		super.onSaveInstanceState(savedInstanceState);
	}	
	
	public class GetHuntIdTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			int huntIdSuccess;		
			
			try {
						
				//GETTING THE HUNT ID
				List<NameValuePair> parametersForHuntId = new ArrayList<NameValuePair>();
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parametersForHuntId.add(new BasicNameValuePair("hunt", currentHunt));
				
				
				
				Log.d("request", "starting");
				JSONObject jsonFindHuntId = jsonParser.makeHttpRequest(getHuntIdUrl, "POST", parametersForHuntId);
				Log.d("Register With Hunt Attempt", jsonFindHuntId.toString());
				huntIdSuccess = jsonFindHuntId.getInt(tagSuccess);
				if(huntIdSuccess == 1)
				{
					huntIdResult = jsonFindHuntId.getJSONObject("result");
					huntId = huntIdResult.getInt("HuntId");	

					Date today = new Date();

					String retrievedEndDate = huntIdResult.getString("EndDate");
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date endDate;
					try {
						//http://stackoverflow.com/questions/15761101/how-to-get-a-date-from-a-json-object
						endDate = format.parse(retrievedEndDate);
						if(today.after(endDate))
						{
							huntAlreadyFinished = true;
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Log.d("leaderboard", "current Hunt Id returned and saved to sharedPref");
					currentHuntIdReturned = true;
						
					return jsonFindHuntId.getString(tagMessage);			
				}
				else
				{
					Log.d("Getting Hunt Id failed!", jsonFindHuntId.getString(tagMessage));
					return jsonFindHuntId.getString(tagMessage);
				}
			}catch (JSONException e) {
				
				}
			return null;	
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mHuntIdTask = null;
			
			if(currentHuntIdReturned)
			{
				editor.putInt("currentHuntId", huntId);
				editor.commit();
			}
			
			if(huntAlreadyFinished)
			{
				mBeginHuntButton.setEnabled(false);
				mRegisterButton.setEnabled(false);
				alertForHuntOver.show();
			}
			else
			{
				if(!userAlreadyRegistered)
				{
					checkIfUserHasAlreadyRegistered();	
				}
				if(!huntAlreadyStarted)
				{
					checkIfHuntAlreadyStarted();
				}	
			}
			
			if (fileUrl != null) {
				Toast.makeText(RegisterWithHuntActivity.this, fileUrl, Toast.LENGTH_LONG).show();
				//mhuntDescriptionView.setText(currentHuntDescription);
				
				
			} else {
				//Toast.makeText(RegisterWithHuntActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
				Toast.makeText(RegisterWithHuntActivity.this, "Couldn't retrieve hunt description", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mHuntIdTask = null;
		}
	}
	
public class UserRegisterWithHuntTask extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(RegisterWithHuntActivity.this);
            pDialog.setMessage("Attempting register...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
			int success;
				//Check to make sure password is correct
				List<NameValuePair> parametersForPasswordCheck = new ArrayList<NameValuePair>();
				
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parametersForPasswordCheck.add(new BasicNameValuePair("huntid", Integer.toString(huntId)));
				parametersForPasswordCheck.add(new BasicNameValuePair("userid", Integer.toString(userId)));
				//parametersForPasswordCheck.add(new BasicNameValuePair("huntname", currentHunt));
				
				try{
				Log.d("request", "starting");
				JSONObject json = jsonParser.makeHttpRequest(registerUserWithHuntUrl, "POST", parametersForPasswordCheck);
				Log.d("Register With Hunt Attempt", json.toString());
					
				success = json.getInt(tagSuccess);
				if(success == 1)
				{							
					Log.d("Registration Successful!", json.toString());
					Log.d("leaderboard", "User has just registered with: " + huntId);
					
					saveHuntParticipantResult = json.getJSONObject("result");
					currentParticipantId = saveHuntParticipantResult.getInt("HuntParticipantId");
					registrationSuccessful = true;
					return json.getString(tagMessage);
				}
				else
				{
					Log.d("Registration failed!", json.getString(tagMessage));
					return json.getString(tagMessage);
				}
				
			} catch (JSONException e) {
			
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mAuthTask = null;
			pDialog.dismiss();
			
			if(registrationSuccessful)
			{
				mBeginHuntButton.setEnabled(true);
				mRegisterButton.setEnabled(false);
				editor.putInt("userParticipantId", currentParticipantId);
				editor.commit(); 
				
			}

			if (fileUrl != null) {
				Toast.makeText(RegisterWithHuntActivity.this, fileUrl, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(RegisterWithHuntActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}

public class GetHuntDescriptionTask extends AsyncTask<String, String, String> {

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		descriptionProgressBar.setVisibility(ProgressBar.VISIBLE);
	}
	
	@Override
	protected String doInBackground(String... args) {
		//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
		
			int success;
			//Check to make sure password is correct
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			
			//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
			parameters.add(new BasicNameValuePair("huntId", Integer.toString(huntId)));
			
			try{
			Log.d("request", "starting");
			JSONObject json = jsonParser.makeHttpRequest(getHuntDescriptionUrl, "POST", parameters);
			Log.d("Return hunt description attempt", json.toString());
				
			success = json.getInt(tagSuccess);
			if(success == 1)
			{							
				Log.d("Got hunt description!", json.toString());
				currentHuntDescriptionResult = json.getJSONObject("result");
				currentHuntDescription = currentHuntDescriptionResult.getString("HuntDescription");
				huntDescriptionreturned = true;
				return json.getString(tagMessage);
			}
			else
			{
				Log.d("Return hunt description failed!", json.getString(tagMessage));
				return json.getString(tagMessage);
			}
			
		} catch (JSONException e) {
		
		}

		return null;
	}

	@Override
	protected void onPostExecute(final String fileUrl) {
		mHuntDescriptionTask = null;
		descriptionProgressBar.setVisibility(ProgressBar.GONE);
		descriptionProgressBar.clearAnimation();
		
		if (fileUrl != null) {
			Toast.makeText(RegisterWithHuntActivity.this, fileUrl, Toast.LENGTH_LONG).show();	
			if(huntDescriptionreturned)
			{
				mhuntDescriptionView.setText(currentHuntDescription);
				
			}
			else
			{
				mhuntDescriptionView.setText("Hunt description could not be retrieved");
			}
		} else {
			Toast.makeText(RegisterWithHuntActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
		}
		
		
	}

	@Override
	protected void onCancelled() {
		mHuntDescriptionTask = null;
	}
}

public class CheckIfUserRegisteredTask extends AsyncTask<String, String, String> {
	
	@Override
	protected String doInBackground(String... args) {
		//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
		
			int success;

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			
			//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
			parameters.add(new BasicNameValuePair("huntId", Integer.toString(huntId)));
			parameters.add(new BasicNameValuePair("userId", Integer.toString(userId)));
			
			try{
			Log.d("request", "starting");
			JSONObject json = jsonParser.makeHttpRequest(checkIfUserRegisteredUrl, "POST", parameters);
			Log.d("Check user registered attempt", json.toString());
				
			success = json.getInt(tagSuccess);
			if(success == 1)
			{							
				Log.d("leaderboard","User is already registered");
				
				userAlreadyRegistered = true;
				return json.getString(tagMessage);
			}
			else
			{
				Log.d("leaderboard","User has not registered previously");
				userAlreadyRegistered = false;
				return json.getString(tagMessage);
			}
			
		} catch (JSONException e) {
		
		}

		return null;
	}

	@Override
	protected void onPostExecute(final String fileUrl) {
		mUserRegisteredTask = null;
		
		if(!userAlreadyRegistered)
		{
			mBeginHuntButton.setEnabled(false);
			mRegisterButton.setEnabled(true);
		}
		else
		{
			mBeginHuntButton.setEnabled(true);
			mRegisterButton.setEnabled(false);
		}
		
		if (fileUrl != null) {
			Toast.makeText(RegisterWithHuntActivity.this, fileUrl, Toast.LENGTH_LONG).show();	
		} else {
			Toast.makeText(RegisterWithHuntActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
		}		
	}

	@Override
	protected void onCancelled() {
		mUserRegisteredTask = null;
	}
}

public class SaveStartTimeTask extends AsyncTask<String, String, String> {

	@Override
	protected String doInBackground(String... arg0) {
		int startTimeSuccess;		
		
		try {
			Log.d("leaderboard", "SaveStartTimeTask has begun");
			startTime = System.currentTimeMillis();
					
			//GETTING THE HUNT ID
			List<NameValuePair> parametersForHuntId = new ArrayList<NameValuePair>();
			parametersForHuntId.add(new BasicNameValuePair("userId", Long.toString(userId)));
			parametersForHuntId.add(new BasicNameValuePair("huntId", Long.toString(huntId)));
			parametersForHuntId.add(new BasicNameValuePair("startTime", Long.toString(startTime)));
			
			Log.d("request", "starting");
			JSONObject saveStartTimeObject = jsonParser.makeHttpRequest(saveStartTimeUrl, "POST", parametersForHuntId);
			Log.d("Attempt to save original start time", saveStartTimeObject.toString());
			
			startTimeSuccess = saveStartTimeObject.getInt(tagSuccess);
			
			if(startTimeSuccess == 1)
			{
				Log.d("leaderboard", "start time for " + huntId + " is " + startTime );
				saveStartTimeResult = saveStartTimeObject.getJSONObject("result");
				huntAlreadyStarted = true;
				
				return saveStartTimeObject.getString(tagMessage);			
			}
			else
			{
				Log.d("Saving start time failed!", saveStartTimeObject.getString(tagMessage));
				return saveStartTimeObject.getString(tagMessage);
			}
		}catch (JSONException e) {
			
			}
		return null;	
	}

	@Override
	protected void onPostExecute(final String fileUrl) {
		mSaveStartTimeTask = null;
	
			editor.putBoolean(huntId + " isHuntAlreadyStarted", huntAlreadyStarted);
			editor.putLong(huntId + " startTime", startTime);
			editor.commit(); 
			startTimeSaved = true;
		
		if (fileUrl != null) {
			
			Toast.makeText(RegisterWithHuntActivity.this, fileUrl, Toast.LENGTH_LONG).show();
		} else {
			//Toast.makeText(RegisterWithHuntActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
			Toast.makeText(RegisterWithHuntActivity.this, "Couldn't save start time", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onCancelled() {
		mSaveStartTimeTask = null;
	}
}

public class CheckIfHuntStartedTask extends AsyncTask<String, String, String> {
	
	@Override
	protected String doInBackground(String... args) {
		//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			Log.d("leaderboard", "CheckIfHuntStartedTask has begun");
			int success;
			
			try{
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parameters.add(new BasicNameValuePair("huntId", Integer.toString(huntId)));
				parameters.add(new BasicNameValuePair("userId", Integer.toString(userId)));
			Log.d("request", "starting");
			Log.d("leaderboard", "Checking if hunt started - Current Hunt Id:" + huntId);
			Log.d("leaderboard", "Checking if hunt started - Current Hunt Id:" + userId);
			
			Log.d("leaderboard", "Attempting to see if hunt has already started");
			JSONObject checkIfAlreadyStartedJson = jsonParser.makeHttpRequest(checkIfHuntStartedUrl, "POST", parameters);
			Log.d("Check hunt started attempt", checkIfAlreadyStartedJson.toString());
				
			success = checkIfAlreadyStartedJson.getInt(tagSuccess);
			Log.d("leaderboard","Check hunt started attempt SUCCESS:" + success);
			if(success == 1)
			{	//http://stackoverflow.com/questions/1576243/php-check-for-null				
				Log.d("leaderboard", huntId + " has already started");
				huntAlreadyStarted = true;
				Log.d("leaderboard", huntAlreadyStarted + " huntAlreadyStarted value");
				startTimeResult = checkIfAlreadyStartedJson.getJSONObject("result");
				Log.d("leaderboard", checkIfAlreadyStartedJson.getJSONObject("result")+"");
				startTime = startTimeResult.getLong("StartTime");
				
				//Cant do shared pref in here because its ui thread
				return checkIfAlreadyStartedJson.getString(tagMessage);
			}
			else
			{
				Log.d("leaderboard", huntId + " has not already started");
				huntAlreadyStarted = false;
				//save that hunt has already started with the shared pref
				return checkIfAlreadyStartedJson.getString(tagMessage);
			}
			
		} catch (JSONException e) {
			Log.d("leaderboard", e.toString());
		}

		return null;
	}

	@Override
	protected void onPostExecute(final String fileUrl) {
		mHuntStartedTask = null;

		editor.putBoolean(huntId + " isHuntAlreadyStarted", huntAlreadyStarted);
		editor.putLong(huntId + " startTime", (long) startTime);
		editor.commit(); 
		
		
		
		if (fileUrl != null) {
			Toast.makeText(RegisterWithHuntActivity.this, fileUrl, Toast.LENGTH_LONG).show();	
		} else {
			Toast.makeText(RegisterWithHuntActivity.this, "Couldn't retrieve if hunt started or not", Toast.LENGTH_LONG).show();
		}		
	}

	@Override
	protected void onCancelled() {
		mHuntStartedTask = null;
	}
}
	
public class GetParticipantIdTask extends AsyncTask<String, String, String> {
		
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
				int success;

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parameters.add(new BasicNameValuePair("huntId", Integer.toString(huntId)));
				parameters.add(new BasicNameValuePair("userId", Integer.toString(userId)));
				
				try{
					Log.d("request", "starting");
					JSONObject jsonGetHuntParticipantId = jsonParser.makeHttpRequest(getHuntParticipantIdUrl, "POST", parameters);
					Log.d("Get User Id Attempt", jsonGetHuntParticipantId.toString());
					success = jsonGetHuntParticipantId.getInt(tagSuccess);
					
					if(success == 1)
					{
						huntParticipantIdResult = jsonGetHuntParticipantId.getJSONObject("result");
						currentParticipantId = huntParticipantIdResult.getInt("HuntParticipantId");
						huntParticipantIdReturned = true;
						Log.d("leaderboard", "hunt participant id is: " + currentParticipantId);
						return jsonGetHuntParticipantId.getString(tagMessage);
						
					}
					else
					{
						Log.d("Getting hunt participant Id failed!", jsonGetHuntParticipantId.getString(tagMessage));
						return jsonGetHuntParticipantId.getString(tagMessage);
					}
				
			} catch (JSONException e) {
			
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mGetHuntParticipantIdTask = null;
			
			if(huntParticipantIdReturned)
			{
				editor.putInt(huntId + "userParticipantId", currentParticipantId);
				editor.putBoolean(huntId + " userParticipantIdReturned", true);
				editor.commit(); 
				
				Log.d("leaderboard", huntAlreadyStarted+ " = has hunt already started by the time of the check on the button");
				if(!huntAlreadyStarted)
				{
					//http://stackoverflow.com/questions/3944344/how-to-display-the-time-elapsed-onto-ui-screen-of-the-android
					attemptToSaveStartTime();
					
					mMap = mMapManager.startNewMap(currentParticipantId);
					
					Log.d("leaderboard", startTime+ " = start time");
					scanQRCodeActivity = new Intent(RegisterWithHuntActivity.this, ScanQRCodeActivity.class);
					startActivity(scanQRCodeActivity);
				}	
				else
				{
					Log.d("leaderboard", "should change Begin Hunt to Continue");							
					editor.putLong(huntId + " startTime", (long) startTime);
					editor.putBoolean(huntId + " isHuntAlreadyStarted", huntAlreadyStarted);
					editor.commit();
					Log.d("leaderboard", "START TIME HAS BEEN SAVED TO SHAREDPREF");
					
					//Set up map with existing details
					mMap = mMapManager.getMapData(currentParticipantId);
					
					//If if the user has deleted the app and started again
					if(mMap == null)
					{
						mMap = mMapManager.startNewMap(currentParticipantId);
					}
					mMapManager.startTrackingMap(mMap);
					
					scanQRCodeActivity = new Intent(RegisterWithHuntActivity.this, ScanQRCodeActivity.class);
					startActivity(scanQRCodeActivity);
				}
			}
			if (fileUrl != null) {
				Toast.makeText(RegisterWithHuntActivity.this, fileUrl, Toast.LENGTH_LONG).show();	
			} else {
				Toast.makeText(RegisterWithHuntActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
			}		
		}

		@Override
		protected void onCancelled() {
			mGetHuntParticipantIdTask = null;
		}
	}
	
	//HUNT FINISHED CHECK
	//mRunManager.stopLocationUpdates();

}




