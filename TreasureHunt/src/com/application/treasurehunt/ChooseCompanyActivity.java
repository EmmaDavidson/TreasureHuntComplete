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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.Company;
import sqlLiteDatabase.CompanyDAO;

import Mapping.MapManager;

import Utilities.ChooseCompanyListAdapter;
import Utilities.InternetUtility;
import Utilities.JSONParser;
import Utilities.PHPHelper;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ExecutionException;

/*
 * The purpose of this Activity (class) is to allow a participant to choose a from a list of companies on screen in order to see that 
 * particular company's list of treasure hunts. [See dissertation Section 2.4.2.3]
 * */

public class ChooseCompanyActivity extends Activity {
	
	/*
	 * Global variables used within ChooseCompanyActivity.
	 */
	private static final String CHOOSE_COMPANIES_URL =  "http://lowryhosting.com/emmad/chooseCompanies.php";
	
	private ListView mCompanyListView;
	private ProgressDialog mChooseCompanyDialog;
	
	public JSONParser jsonParser = new JSONParser();
	
	private static JSONArray sTagResult;
	private ReturnCompaniesTask mCompaniesTask = null;

	private InternetUtility mInternetUtility;
	private MapManager mMapManager;
	private CompanyDAO mCompanyDataSource;
	
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSettings;

	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	private Handler mHandlerForUpdatingCompanyList;
	
	/*This Runnable  will periodically update the list of companies available from the database. Will only
	 * do so as long as the screen is not tilted on its side. */
	
	//NOTE TO SELF - SHOULD I JUST ALLOW THE DB CALL ANYWAY TO RUN WHEN THE SCREEN HAS BEEN TILTED?
	private final Runnable mUpdateCompanyList = new Runnable() {
		
		//http://stackoverflow.com/questions/12220239/repeat-task-in-android
		//http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
		@Override
		public void run() {
			mCompanyDataSource.refreshCompanies();
			
			if(mInternetUtility.isInternetConnected()) {
				attemptReturnCompanies();
			}
			else {
				Toast.makeText(ChooseCompanyActivity.this, InternetUtility.INTERNET_DISCONNECTED, 
						Toast.LENGTH_LONG).show();
					}
			
			mHandlerForUpdatingCompanyList.postDelayed(mUpdateCompanyList, 60000);
			}
		};
	
	private int mCurrentUserId;
	private List<Company> mListOfCompanies;
	private ChooseCompanyListAdapter mAdapter;
	
	/*
	 * Method called when the Activity is created (as part of the Android Life Cycle) which sets up this Activity's variables.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_company);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Choose company");
		}
		
		mCompanyListView = (ListView) findViewById(R.id.company_list_view);
		
		mInternetUtility = InternetUtility.getInstance(this);
		mMapManager = MapManager.get(this);
	
		mCompanyDataSource = CompanyDAO.getInstance(this);
		mCompanyDataSource.open();	
			
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		mCurrentUserId = mSettings.getInt("currentUserId", 0);

		mHandlerForUpdatingCompanyList = new Handler();
		
		mHandlerForUpdatingCompanyList.post(mUpdateCompanyList);
		
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
					
				Intent homepageActivityIntent = new Intent(ChooseCompanyActivity.this, HomepageActivity.class);
				startActivity(homepageActivityIntent);
				
				return true;
			}
			case 2: {
				
				mEditor.clear();
				mEditor.commit();
				
				mMapManager.stopLocationUpdates();
				
				Intent loginActivityIntent = new Intent(ChooseCompanyActivity.this, LoginActivity.class);
				startActivity(loginActivityIntent);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Method restoring the companies first retrieved to prevent a second database call when
	 * the screen has been tilted.
	 * */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);
		
		//mListOfCompanies = mCompanyDataSource.getAllCompanies();
		//mAdapter = new ChooseCompanyListAdapter(ChooseCompanyActivity.this, mListOfCompanies);
		//mCompanyListView.setAdapter(mAdapter);	
	}
	
	/*
	 * Method called when the activity is paused by the participant (as part of the Android Life Cycle).
	 * It prevents the list of companies from being updated when it is not visible on screen.
	 * */
	@Override
	protected void onPause() {
		
		super.onPause();
		mHandlerForUpdatingCompanyList.removeCallbacks(mUpdateCompanyList);
	}
		
	/* Method to call the asynchronous class 'ReturnCompaniesTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptReturnCompanies() {
		
		if (mCompaniesTask != null) {
			return;
		} 
		
		mCompaniesTask = new ReturnCompaniesTask();
		mCompaniesTask.execute((String) null);	
			
		//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
		Handler timeoutHandler = new Handler();
		timeoutHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mCompaniesTask != null) {
					if(mCompaniesTask.getStatus() == AsyncTask.Status.RUNNING) {
						mCompaniesTask.cancel(true);
						mChooseCompanyDialog.cancel();
						Toast.makeText(ChooseCompanyActivity.this, 
								mConnectionTimeout, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
			
		, 10000);
		
		try {
			mCompaniesTask.get();
		} catch (InterruptedException e) {
			try {
				throw new InterruptedException(e.toString());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (ExecutionException e) {
				e.printStackTrace();
		}
	}
	
	/* Method to check whether or not a password has been saved previously for the selected treasure hunt (prevents 
	 * user re-entry each time). If password has already been saved, the participant is directed to its list of treasure hunts.
	 * Else, the participant must enter a password. Associated with ReturnCompaniesTask onPostExecute() method.*/
	private void checkHasPasswordBeenSaved(final ChooseCompanyListAdapter adapter, int position) {
		
		//http://stackoverflow.com/questions/4508979/android-listview-get-selected-item
		final Company selectedCompany = adapter.getItem(position);
		boolean hasPasswordAlreadyBeenSaved = mSettings.getBoolean(selectedCompany.getAdministratorId() + 	
				"companyIdPasswordSavedFor" + mCurrentUserId, false);
		
		if(!hasPasswordAlreadyBeenSaved) {
			checkPassword(adapter, position);
		}
		else {
			Intent chooseHuntsIntent = new Intent(ChooseCompanyActivity.this, ChooseHuntActivity.class);
			mEditor.putInt("administratorId", selectedCompany.getAdministratorId()); 	
			mEditor.commit(); 
			startActivity(chooseHuntsIntent);		
		}
	}
	
	/* Method to allow a participant to enter a password for a selected treasure hunt, and compare the password entered on the dialog
	 * with the password of the selected company. If the password is correct the the participant is directed to that company's
	 * associated list of treasure hunts. Else, they are notified with an error on screen. 
	 * Associated with ReturnCompaniesTask onPostExecute() method.*/
	private void checkPassword(final ChooseCompanyListAdapter adapter, int position) {
		
		//http://www.mkyong.com/android/android-prompt-user-input-dialog-example/
		LayoutInflater li = LayoutInflater.from(ChooseCompanyActivity.this);
		final View passwordView = li.inflate(R.layout.company_password_dialog, null);						
		
		Builder builder = new Builder(ChooseCompanyActivity.this);
		builder.setView(passwordView);
		builder.setCancelable(false);
		final int thisPosition = position;
		
		builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
			
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			EditText password = (EditText) passwordView.findViewById(R.id.company_password_textbox);
			final Company selectedCompany = adapter.getItem(thisPosition);
			final CheckBox mCheckBox = (CheckBox) passwordView.findViewById(R.id.save_password_check_box);
			
			if(password.getText().toString().equals(selectedCompany.getCompanyPassword().toString())) {
				
				Intent chooseHuntsIntent = new Intent(ChooseCompanyActivity.this, ChooseHuntActivity.class);
				mEditor.putInt("administratorId", selectedCompany.getAdministratorId()); 
				
				if(mCheckBox.isChecked()) {
					
					mEditor.putBoolean(selectedCompany.getAdministratorId() + "companyIdPasswordSavedFor" + mCurrentUserId, true);
				}
				else {
					mEditor.putBoolean(selectedCompany.getAdministratorId() + "companyIdPasswordSavedFor" + mCurrentUserId, false);
				}
				
				mEditor.commit(); 
				dialog.cancel();
				startActivity(chooseHuntsIntent);
			}
			else {
				dialog.cancel();	
				showFailedPasswordAttemptMessage();
			}			
		}
		});
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	
		AlertDialog passwordDialog = builder.create();
		passwordDialog.show();
	}
	
	/* Method to display dialog if the password entered by a participant for the selected hunt is incorrect.
	 * Associated with ReturnCompaniesTask onPostExecute() method.*/
	private void showFailedPasswordAttemptMessage() {
		
		Builder incorrectPasswordBuilder = new Builder(ChooseCompanyActivity.this);									
		incorrectPasswordBuilder.setTitle("Incorrect password");
		incorrectPasswordBuilder.setMessage("You entered the wrong password for this company");
		incorrectPasswordBuilder.setCancelable(false);
		incorrectPasswordBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		incorrectPasswordBuilder.create();
		incorrectPasswordBuilder.show();
	}
	
	/* This internal class attempts to return from the database a list of companies that are 
	 * available for a participant to select from. */
	public class ReturnCompaniesTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant that a search is being made.*/
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mChooseCompanyDialog = new ProgressDialog(ChooseCompanyActivity.this);
	        mChooseCompanyDialog.setMessage("Attempting to get treasure hunts...");
			mChooseCompanyDialog.setIndeterminate(false);
			mChooseCompanyDialog.setCancelable(false);
			mChooseCompanyDialog.show();
		}
		
		/* Method calling the database to return all companies present.*/
		@Override
		public String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			//http://www.php.net/manual/en/pdostatement.fetchall.php
			//http://stackoverflow.com/questions/14491430/using-pdo-to-echo-display-all-rows-from-a-table
		
			int success;
			
			try {
				//PHP CURRENT DATE TIME CHECK http://stackoverflow.com/questions/470617/get-current-date-and-time-in-php
				Log.i("ChooseCompany", "starting");
				
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				JSONObject jsonResult = jsonParser.makeHttpRequest(CHOOSE_COMPANIES_URL, "POST", parameters);
				
				Log.i("ChooseCompany", jsonResult.toString());
				
				success = jsonResult.getInt(PHPHelper.SUCCESS);
				
				//Successful return from database
				if(success == 1) {
					Log.i("ChooseCompany", jsonResult.toString());
					sTagResult = jsonResult.getJSONArray(PHPHelper.RESULTS);
					
					//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
					for(int i=0; i < sTagResult.length(); i++) {
						mCompanyDataSource.addCompany(sTagResult.getJSONObject(i).getInt("UserId"),
								sTagResult.getJSONObject(i).getString("CompanyName"), 
									sTagResult.getJSONObject(i).getString("CompanyPassword"));
					}
					
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				else {
					
					Log.w("ChooseCompany", jsonResult.getString(PHPHelper.MESSAGE));
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

		/* Method called after the database call has been made. If a list of companies has been returned
		 * then this list is displayed on screen else participant is notified of an error on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			mCompaniesTask = null;
			mChooseCompanyDialog.cancel();
			
			mListOfCompanies = mCompanyDataSource.getAllCompanies();

			if (mListOfCompanies.size() != 0) {	
				
				mAdapter = new ChooseCompanyListAdapter(ChooseCompanyActivity.this, mListOfCompanies);
				mCompanyListView.setAdapter(mAdapter);	
				
				//http://stackoverflow.com/questions/16189651/android-listview-selected-item-stay-highlighted
				mCompanyListView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {					
						checkHasPasswordBeenSaved(mAdapter, position);
					}
				}); 
			} 
			else {
				
				showFailedCompanyDataReturnMessage();
			}
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mCompaniesTask = null;
			mChooseCompanyDialog.cancel();
		}
		
		/* Method to display a dialog if no company data is returned from the database call.
		 * Associated with ReturnCompaniesTask onPostExecute() method.*/
		public void showFailedCompanyDataReturnMessage() {
			
			Builder alertForNoData = new Builder(ChooseCompanyActivity.this);
			alertForNoData.setTitle("Companies");
			alertForNoData.setMessage("There are currently no companies to show. Please check back later.");
			alertForNoData.setCancelable(false);
			alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent homepageIntent = new Intent(ChooseCompanyActivity.this, HomepageActivity.class);
					startActivity(homepageIntent);
				}
			});
			
			alertForNoData.create();
			alertForNoData.show();
			
			Log.w("ChooseCompany", "No companies returned from the database");
		}
	}
}
