package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import sqlLiteDatabase.MapData;
import sqlLiteDatabase.MapDataDAO;

import com.application.treasurehunt.RegisterWithHuntActivity.CheckIfUserRegisteredTask;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;

import Utilities.JSONParser;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

//http://mobile.tutsplus.com/tutorials/android/android-sdk-create-a-barcode-reader/ - ZXING
//https://github.com/DushyanthMaguluru/ZBarScanner for ZBAR - USING THIS CURRENTLY, referencing the source forge project
//Whole Activity taken from this either of these websites - now with my own added features for timing and saving to the database

public class ScanQRCodeActivity extends Activity implements OnClickListener {

	private static final String getHuntParticipantIdUrl = "http://lowryhosting.com/emmad/getHuntParticipantId.php";
	private static final String scanResultUrl = "http://lowryhosting.com/emmad/updateScanResults.php";
	
	private JSONParser jsonParser = new JSONParser();
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	
	private static JSONObject huntParticipantIdResult;
	
	private Button scanButton;
	private TextView contentText;
	
	Builder alertForNoPin;
	
	int userId;
	int huntId;
	//private int huntParticipantId;
	int currentParticipantId;
	
	boolean huntParticipantIdReturned;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	MapDataDAO mMapDataSource;
	MapManager mMapManager;
	
	long startTime;
	
	private Location mLastLocation;
	
	GetHuntParticipantIdTask mGetHuntParticipantIdTask;
	ScanResultTask mScanResultTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_qrcode);
		
		if(savedInstanceState != null)
		{
			currentParticipantId = savedInstanceState.getInt(huntId + "HUNT_PARTICIPANT_ID");
			mLastLocation = savedInstanceState.getParcelable("lastLocation");
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Scan a QR code");
		}
		
		mMapManager = MapManager.get(this);
		
		mMapDataSource = new MapDataDAO(this);
		mMapDataSource.open();
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		userId = settings.getInt("currentUserId", 0);
		huntId = settings.getInt("currentHuntId", 0);
		
		currentParticipantId = settings.getInt("userParticipantId", 0);
		
		if(currentParticipantId!= 0)
		{
			huntParticipantIdReturned = true;
		}
		
		Log.d("leaderboard", "The hunt retrieved from the editor is: " + huntId);

		scanButton = (Button)findViewById(R.id.scan_qr_code_button);
		contentText = (TextView)findViewById(R.id.scan_content_received);
		scanButton.setOnClickListener(this);
		
		/*if(!huntParticipantIdReturned)
		{
			getParticipantId();
		}
		else
		{
			huntParticipantId = settings.getInt(huntId + " userParticipantId", 0) ; 
		}*/
		
		ScanQRCodeActivity.this.registerReceiver(mLocationReceiver, new IntentFilter(MapManager.ACTION_LOCATION));
		
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
			Intent loginActivityIntent = new Intent(ScanQRCodeActivity.this, LoginActivity.class);
			mMapManager.stopLocationUpdates();
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void getParticipantId()
	{
		if (mGetHuntParticipantIdTask != null) {
			return;
		} 	
		
		mGetHuntParticipantIdTask = new GetHuntParticipantIdTask();
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
						Toast.makeText(ScanQRCodeActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						
					}
				}
			}
		}
		, 10000);	
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{	
		savedInstanceState.putInt(huntId + "HUNT_PARTICIPANT_ID", currentParticipantId);
		savedInstanceState.putParcelable("lastLocation", mLastLocation);
		super.onSaveInstanceState(savedInstanceState);
	}	
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		
		currentParticipantId = savedInstanceState.getInt(huntId + "HUNT_PARTICIPANT_ID");
		mLastLocation = savedInstanceState.getParcelable("lastLocation");
	}
	
	//For now - deciding to use ZBar within application so user does not have to have another application running to scan
	@Override
	public void onClick(View v) {	
		Intent intent = new Intent(this, ZBarScannerActivity.class);
		startActivityForResult(intent, 1);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		if (resultCode == RESULT_OK) 
	    {	
			if(intent.getStringExtra(ZBarConstants.SCAN_RESULT).contains(huntId+""))
			{
				String questionReturned = intent.getStringExtra(ZBarConstants.SCAN_RESULT);
				//http://stackoverflow.com/questions/8694984/remove-part-of-string
				String questionReturnedWithoutHuntId = questionReturned.replace(huntId+"", "");
				contentText.setText(questionReturnedWithoutHuntId);
				saveScanResult();
			}
			else
			{
				Toast.makeText(this, "This was an invalid scan for this hunt", Toast.LENGTH_SHORT).show();
			}

	    } else if(resultCode == RESULT_CANCELED) {
	        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
	    }
	}
	
	public void saveScanResult()
	{
		if (mScanResultTask != null) {
			return;
		} 	
		
		mScanResultTask = new ScanResultTask();
		mScanResultTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mScanResultTask!= null)
				{
					if(mScanResultTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mScanResultTask.cancel(true);
						Toast.makeText(ScanQRCodeActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						
					}
				}
			}
		}
		, 10000);	
	}
	
public class ScanResultTask extends AsyncTask<String, String, String> {
		
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
				int success;

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				//NEED TO FIND REFERENCES
				//http://stackoverflow.com/questions/10862845/how-to-set-android-chronometer-base-time-from-date-object
				startTime = settings.getLong(huntId + " startTime", 0);
				Log.d("leaderboard", "The start time retrieved from the editor is: " + startTime);
				Log.d("leaderboard", "Current millis time: " + System.currentTimeMillis());
				long elapsedTime = System.currentTimeMillis() - startTime;
				Log.d("leaderboard", "elapsed time from last scan: " + elapsedTime);
				
				//http://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java
				//http://stackoverflow.com/questions/10593834/displaying-seconds-in-3-decimal-places-java
				long timeElapsedSeconds = (elapsedTime / 1000);
				Log.d("leaderboard","elapsedTime in seconds: "+ timeElapsedSeconds);

				double timeElapsedHours = (elapsedTime /(1000.0f*60.0f*60.0f));
				Log.d("leaderboard","elapsedTime in hours with calc "+ timeElapsedHours);
				
	
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				//Chose elapsed hours because it may take some time to complete the hunt
				parameters.add(new BasicNameValuePair("huntParticipantId", Integer.toString(currentParticipantId)));
				parameters.add(new BasicNameValuePair("timeElapsed", ""+ timeElapsedHours));
				//Don't need to save the tally as it will update in the php call
				//http://stackoverflow.com/questions/3866524/mysql-update-column-1
				
				try{
					Log.d("request", "starting");
					JSONObject json = jsonParser.makeHttpRequest(scanResultUrl, "POST", parameters);
					Log.d("Attempt to update scan results tally", json.toString());
					success = json.getInt(tagSuccess);
					
					if(success == 1)
					{
						Log.d("leaderboard", "SCAN: huntParticipantId is: " + currentParticipantId + ", timeElapsed= " + timeElapsedHours);
						return json.getString(tagMessage);			
					}
					else
					{
						Log.d("Updating scan results failed!", json.getString(tagMessage));
						return json.getString(tagMessage);
					}
				
			} catch (JSONException e) {
			
				Log.d("leaderboard", e+"");
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mScanResultTask = null;
			
			if(mLastLocation !=null)
			{
				mMapDataSource.insertMarker(currentParticipantId, mLastLocation);
			}		
			else
			{
				//http://www.mkyong.com/android/android-alert-dialog-example/
				alertForNoPin = new Builder(ScanQRCodeActivity.this);
				alertForNoPin.setTitle("Pin not saved");
				alertForNoPin.setMessage("No pin was saved to the map. This is probably due to location service issues.");
				alertForNoPin.setCancelable(false);
				alertForNoPin.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
					}
				});
				
				alertForNoPin.create();
				alertForNoPin.show();
			}
			
			if (fileUrl != null) {
				Toast.makeText(ScanQRCodeActivity.this, fileUrl, Toast.LENGTH_LONG).show();	
			} else {
				Toast.makeText(ScanQRCodeActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
			}		
		}

		@Override
		protected void onCancelled() {
			mScanResultTask = null;
		}
	}
	
	public class GetHuntParticipantIdTask extends AsyncTask<String, String, String> {
		
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
				editor.putInt(huntId + " userParticipantId", currentParticipantId);
				editor.putBoolean(huntId + " userParticipantIdReturned", true);
				editor.commit(); 
			}
			if (fileUrl != null) {
				Toast.makeText(ScanQRCodeActivity.this, fileUrl, Toast.LENGTH_LONG).show();	
			} else {
				Toast.makeText(ScanQRCodeActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
			}		
		}

		@Override
		protected void onCancelled() {
			mGetHuntParticipantIdTask = null;
		}
	}

	
	private BroadcastReceiver mLocationReceiver = new LocationReceiver()
	{
		@Override
		protected void onLocationReceived(Context context, Location loc)
		{
			mLastLocation = loc;		
		}
		
		@Override 
		protected void onProviderEnabledChanged(boolean enabled)
		{	//should be int
			String toastText = enabled ? "enabled" : "disabled";
			// R.string.gps_enabled : R.string.gps_disabled
			Toast.makeText(ScanQRCodeActivity.this, toastText, Toast.LENGTH_LONG).show();
		}
	};
}
