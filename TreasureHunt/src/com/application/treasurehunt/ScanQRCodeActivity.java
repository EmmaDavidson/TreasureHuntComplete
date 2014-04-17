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

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.MapDataDAO;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import Mapping.LocationReceiver;
import Mapping.MapManager;

import Utilities.JSONParser;
import Utilities.PHPHelper;

import java.util.ArrayList;
import java.util.List;

//http://mobile.tutsplus.com/tutorials/android/android-sdk-create-a-barcode-reader/ - ZXING
//https://github.com/DushyanthMaguluru/ZBarScanner for ZBAR - USING THIS CURRENTLY, referencing the source forge project
//Whole Activity taken from this either of these websites - now with my own added features for timing and saving to the database

/* The purpose of this Activity is to allow a participant to scan a QR Code and view on screen the question that the code
 * represents in order to complete the treasure hunt.
 * [See Dissertation Section 2.4.2.6]*/

//MIT Licence - http://opensource.org/licenses/MIT
//is released under the MIT Open Source Initiative license (MIT) which is unrestricted usage rights providing
//the copyright notice and permission notice at the link below should be included in the source code as comments.
public class ScanQRCodeActivity extends Activity {

	/*
	 * Global variables used within ScanQRCodeActivity.
	 */
	private static final String SCAN_RESULT_URL = "http://lowryhosting.com/emmad/updateScanResults.php";
	
	private ProgressDialog mScanDialog;
	
	public JSONParser jsonParser = new JSONParser();
	private SaveScanResultTask mScanResultTask; 
	
	private Button mScanButton;
	private TextView mQuestionReturned;
	
	private Builder mAlertForNoLocationServices;
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	
	private MapDataDAO mMapDataSource;
	private MapManager mMapManager;
	
	private Location mLastLocation;
	private LocationManager mLocationManager;

	private long mStartTime;
	private int mCurrentHuntId;
	private int mCurrentParticipantId;
	
	private boolean mLocationServicesChecked = false;
	
	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	/*
	 * Method called when the Activity is created (as part of the android life cycle) which sets up this Activity's variables.
	 * It also checks to see if the location services of the participant's device has been turned on in order for 
	 * pins to be dropped on the participants map for the given treasure hunt.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_qrcode);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Scan a QR code");
		}
		
		mScanButton = (Button)findViewById(R.id.scan_qr_code_button);
		mQuestionReturned = (TextView)findViewById(R.id.scan_content_received);
		
		mMapManager = MapManager.get(this);
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		mMapDataSource = MapDataDAO.getInstance(this);
		mMapDataSource.open();
		
		mSettings = getSharedPreferences("UserPreferencesFile", 0);
		mEditor = mSettings.edit();
		
		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		mCurrentHuntId = mSettings.getInt("currentHuntId", 0);
		mCurrentParticipantId = mSettings.getInt("huntParticipantId", 0);
		
		Log.i("ScanQRCode", "The hunt retrieved from the editor is: " + mCurrentHuntId);
			
		if(savedInstanceState == null){
			checkLocationServices();	
		}
		
		ScanQRCodeActivity.this.registerReceiver(mLocationReceiver, new IntentFilter(MapManager.ACTION_LOCATION));
		
		mScanButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mQuestionReturned.setText("");
						Intent intent = new Intent(ScanQRCodeActivity.this, ZBarScannerActivity.class);
						startActivityForResult(intent, 1);
					}
				});
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
					
				Intent homepageActivityIntent = new Intent(ScanQRCodeActivity.this, HomepageActivity.class);
				startActivity(homepageActivityIntent);
				
				return true;
			}
			case 2: {
				
				mEditor.clear();
				mEditor.commit();
				
				mMapManager.stopLocationUpdates();
				
				Intent loginActivityIntent = new Intent(ScanQRCodeActivity.this, LoginActivity.class);
				startActivity(loginActivityIntent);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Method saves the current HuntId and the last location that has been saved by the device when the Activity is paused.
	 * */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	
		savedInstanceState.putParcelable("lastLocation", mLastLocation);
		savedInstanceState.putBoolean("mLocationServicesChecked", mLocationServicesChecked);
		super.onSaveInstanceState(savedInstanceState);
	}	
	
	/*
	 * Method restoring the current HuntId and last know location when the Activity is restored.
	 * */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);
		mLastLocation = savedInstanceState.getParcelable("lastLocation");
		mLocationServicesChecked = savedInstanceState.getBoolean("mLocationServicesChecked");
	}
	
	/* Method to deal with the result of a scan. If the scan was valid for the given treasure hunt (i.e. the participant
	 * did not scan a QR code unrelated to this particular treasure hunt) their scan is saved. Else, a dialog appears 
	 * to inform the participant of an error.*/
	public void onActivityResult(int requestCode, int resultCode, Intent intent)  {
		
		if (resultCode == RESULT_OK)  {	
			if(intent.getStringExtra(ZBarConstants.SCAN_RESULT).contains(mCurrentHuntId+"")) {
				String questionReturned = intent.getStringExtra(ZBarConstants.SCAN_RESULT);
				//http://stackoverflow.com/questions/8694984/remove-part-of-string
				String questionReturnedWithoutHuntId = questionReturned.replace(mCurrentHuntId+"", "");
				mQuestionReturned.setText(questionReturnedWithoutHuntId);
				saveScanResult();
			}
			else {
				showFailedScanMessage();
			}

	    } else if(resultCode == RESULT_CANCELED) {
	        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
	    }
	}
	
	/* Method to call the asynchronous class 'ScanResultTask'. If call to the database takes too long then a timeout should occur.*/
	private void saveScanResult() {
		
		if (mScanResultTask != null) {
			return;
		} 	
		
		mScanResultTask = new SaveScanResultTask();
		mScanResultTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mScanResultTask!= null) {
					if(mScanResultTask.getStatus() == AsyncTask.Status.RUNNING) {
						mScanDialog.cancel();
						mScanResultTask.cancel(true);
						Toast.makeText(ScanQRCodeActivity.this, mConnectionTimeout, Toast.LENGTH_LONG).show();	
					}
				}
			}
		}
		, 20000);	
	}
	
	/* Method to check whether location services are turned on for a device. If turned off then
	 * the current participant is unable to drop a pin on the map associated with this treasure hunt for a given scan.*/
	private void checkLocationServices() {
		
		//http://stackoverflow.com/questions/10311834/android-dev-how-to-check-if-location-services-are-enabled
		if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			
			mAlertForNoLocationServices = new Builder(ScanQRCodeActivity.this);
			mAlertForNoLocationServices.setTitle("Location Services");
			mAlertForNoLocationServices.setMessage("Location services are not turned on. Turn on to track your treasure hunt.");
			mAlertForNoLocationServices.setCancelable(false);
			mAlertForNoLocationServices.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();			
				}
			});
			
			mAlertForNoLocationServices.create();
			mAlertForNoLocationServices.show();
		}
		
		mLocationServicesChecked = true;
	}
	
	/* Method to show a dialog if the current scan has failed e.g. if the participant has scanned a QR Code that
	 * is not associated with the given treasure hunt.*/
	private void showFailedScanMessage() {
		
		Builder alertForNoData = new Builder(ScanQRCodeActivity.this);
		alertForNoData.setTitle("Invalid Code");
		alertForNoData.setMessage("This was an invalid scan for this particular treasure hunt. Please try again.");
		alertForNoData.setCancelable(false);
		alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		alertForNoData.create();
		alertForNoData.show();
		
		Log.w("ScanQRCode", "Invalid scan for hunt: " + mCurrentHuntId);
	}
	
	/* This BroadcastRecevier will listen out for location services updates. If a new location has been noted, the last 
	 * known location is recorded. This is to be used in conjunction with saving a pin to the associated map for the 
	 * given participant. [See SaveScanResultTask OnPostExecute() method.] */
	private BroadcastReceiver mLocationReceiver = new LocationReceiver() {
		
		@Override
		protected void onLocationReceived(Context context, Location loc) {
			mLastLocation = loc;		
		}
		
		@Override 
		protected void onProviderEnabledChanged(boolean enabled) {
			String toastText = enabled ? "enabled" : "disabled";
			Toast.makeText(ScanQRCodeActivity.this, toastText, Toast.LENGTH_LONG).show();
		}
	};
	
	/* This internal class attempts to save in the database a participant's elapsed time for each valid scan made i.e. it takes
	 * the participant's start time and compares it with the current time (of the scan) and date to work out the elapsed time. */
	public class SaveScanResultTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant that a save is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mScanDialog = new ProgressDialog(ScanQRCodeActivity.this);
	        mScanDialog.setMessage("Attempting to retrieve scan result");
			mScanDialog.setIndeterminate(false);
			mScanDialog.setCancelable(false);
			mScanDialog.show();
		}
			
		/* Method calling the database a participant's elapsed time for each valid scan made.*/
		@Override
		public String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
			int success;
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			
			//NEED TO FIND REFERENCES
			//http://stackoverflow.com/questions/10862845/how-to-set-android-chronometer-base-time-from-date-object
			mStartTime = mSettings.getLong(mCurrentHuntId + " startTime", 0);
			Log.i("ScanQRCode", "The start time retrieved from the editor is: " + mStartTime);
			Log.i("ScanQRCode", "Current millis time: " + System.currentTimeMillis());
			long elapsedTime = System.currentTimeMillis() - mStartTime;
			Log.i("ScanQRCode", "elapsed time from last scan: " + elapsedTime);
			
			//http://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java
			//http://stackoverflow.com/questions/10593834/displaying-seconds-in-3-decimal-places-java
			long timeElapsedSeconds = (elapsedTime / 1000);
			Log.i("ScanQRCode","elapsedTime in seconds: "+ timeElapsedSeconds);

			double timeElapsedHours = (elapsedTime /(1000.0f*60.0f*60.0f));
			Log.i("ScanQRCode","elapsedTime in hours with calc "+ timeElapsedHours);
			
			//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
			//Chose elapsed hours because it may take some time to complete the hunt
			parameters.add(new BasicNameValuePair("huntParticipantId", Integer.toString(mCurrentParticipantId)));
			parameters.add(new BasicNameValuePair("timeElapsed", ""+ timeElapsedHours));
			//Don't need to save the tally as it will update in the php call
			//http://stackoverflow.com/questions/3866524/mysql-update-column-1
			
			try{
				Log.i("ScanQRCode", "starting");
				JSONObject json = jsonParser.makeHttpRequest(SCAN_RESULT_URL, "POST", parameters);
				Log.i("ScanQRCode", json.toString());
				success = json.getInt(PHPHelper.SUCCESS);
				
				if(success == 1) {
					Log.i("ScanQRCode", "SCAN: huntParticipantId is: " + mCurrentParticipantId + ", timeElapsed= " + timeElapsedHours);
					return json.getString(PHPHelper.MESSAGE);			
				}
				else {
					Log.w("ScanQRCode", json.getString(PHPHelper.MESSAGE));
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

		/* Method called after the database call has been made. If location services have picked up the location of the device during 
		 * the scan, the location is recorded against this participant for a pin to be dropped on the associated map. Else, they 
		 * are notified of an error on screen. */
		@Override
		protected void onPostExecute(final String fileUrl) {
			mScanResultTask = null;
			mScanDialog.dismiss();
			
			if(mLastLocation !=null) {
				mMapDataSource.insertMarker(mCurrentParticipantId, mLastLocation);
			}		
			else {
				Toast.makeText(ScanQRCodeActivity.this, "Pin not saved.", Toast.LENGTH_LONG).show();
				//http://www.mkyong.com/android/android-alert-dialog-example/
			}
			
			if (fileUrl != null)  {
				Log.i("ScanQRCode", fileUrl);
			} 
			else {
				Log.w("ScanQRCode", "Nothing returned");
			}		
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mScanResultTask = null;
		}
	}
}
