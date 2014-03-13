package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import sqlLiteDatabase.MapDataDAO;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import Utilities.JSONParser;
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

//http://mobile.tutsplus.com/tutorials/android/android-sdk-create-a-barcode-reader/ - ZXING
//https://github.com/DushyanthMaguluru/ZBarScanner for ZBAR - USING THIS CURRENTLY, referencing the source forge project
//Whole Activity taken from this either of these websites - now with my own added features for timing and saving to the database


//MIT Licence - http://opensource.org/licenses/MIT
//is released under the MIT Open Source Initiative license (MIT) which is unrestricted usage rights providing
//the copyright notice and permision notice at the link below should be included in the source code as comments.
public class ScanQRCodeActivity extends Activity implements OnClickListener {

	private static final String getHuntParticipantIdUrl = "http://lowryhosting.com/emmad/getHuntParticipantId.php";
	private static final String scanResultUrl = "http://lowryhosting.com/emmad/updateScanResults.php";
	
	private JSONParser jsonParser = new JSONParser();
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	
	private ProgressDialog pDialog; 
	
	private static JSONObject huntParticipantIdResult;
	
	private Button scanButton;
	private TextView contentText;
	
	Builder alertForNoPin;
	Builder alertForNoLocationServices;
	
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
	
	private LocationManager mLocationManager;
	
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
		
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
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
		
		//check here to see if location services are turned on... pop up warning
		
		//http://stackoverflow.com/questions/10311834/android-dev-how-to-check-if-location-services-are-enabled
		if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			alertForNoLocationServices = new Builder(ScanQRCodeActivity.this);
			alertForNoLocationServices.setTitle("Location Services");
			alertForNoLocationServices.setMessage("Location services are not turned on. Turn on to track your treasure hunt.");
			alertForNoLocationServices.setCancelable(false);
			alertForNoLocationServices.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
			
			alertForNoLocationServices.create();
			alertForNoLocationServices.show();
		}
		
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
		contentText.setText("");
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
				
				Log.e("ScanQRCode", "Invalid scan for hunt: " + huntId);
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
						pDialog.cancel();
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
	protected void onPreExecute()
	{
		super.onPreExecute();
		pDialog = new ProgressDialog(ScanQRCodeActivity.this);
        pDialog.setMessage("Attempting to retrieve scan result");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}
	
		
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
			pDialog.dismiss();
			
			if(mLastLocation !=null)
			{
				mMapDataSource.insertMarker(currentParticipantId, mLastLocation);
			}		
			else
			{
				Toast.makeText(ScanQRCodeActivity.this, "Pin not saved.", Toast.LENGTH_LONG).show();
				//http://www.mkyong.com/android/android-alert-dialog-example/
				/*
				alertForNoPin = new Builder(ScanQRCodeActivity.this);
				alertForNoPin.setTitle("Pin not saved");
				alertForNoPin.setMessage("No pin was saved to the map for this scan. This is probably due to location service issues.");
				alertForNoPin.setCancelable(false);
				alertForNoPin.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
					}
				});
				
				alertForNoPin.create();
				alertForNoPin.show();
				*/
			}
			
			if (fileUrl != null) 
			{
				Log.i("ScanQRCode", fileUrl);
			} 
			else 
			{
				Log.d("ScanQRCode", "Nothing returned");
			}		
		}

		@Override
		protected void onCancelled() {
			mScanResultTask = null;
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
