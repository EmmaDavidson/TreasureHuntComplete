package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sqlLiteDatabase.Hunt;
import sqlLiteDatabase.HuntDAO;
import Utilities.ExpandableListAdapter;
import Utilities.InternetUtility;
import Utilities.JSONParser;
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

public class MyHuntsActivity extends Activity implements OnChildClickListener{

	ExpandableListView mListView;
	private static JSONObject huntParticipantIdResult;
	private int huntParticipantId;
	
	boolean huntParticipantIdReturned;
	
	
	
	ReturnUserHuntsTask mReturnUserHuntsTask;
	GetHuntParticipantIdTask mGetHuntParticipantIdTask;
	private JSONParser jsonParser;
	
	//private static final String returnUserHuntsUrl =  "http://lowryhosting.com/emmad/chooseUserHunt.php";
	private static final String returnUserHuntsNotStartedUrl =  "http://lowryhosting.com/emmad/chooseUserHuntNotStarted.php";
	private static final String returnUserHuntsCompletedUrl =  "http://lowryhosting.com/emmad/chooseUserHuntCompleted.php";
	private static final String returnUserHuntsCurrentUrl =  "http://lowryhosting.com/emmad/chooseUserHuntCurrent.php";
	private static final String getHuntParticipantIdUrl = "http://lowryhosting.com/emmad/getHuntParticipantId.php";
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	private static JSONArray tagResult;
	private static JSONArray tagIdResult;
	
	SharedPreferences.Editor editor;
	SharedPreferences settings;
	
	MapManager mMapManager;
	
	private ProgressDialog pDialog;
	private ProgressDialog mapDataDialog;
	
	int currentUserId;
	
	private HuntDAO huntDataSource;
	
	Hunt chosenHunt;
	
	InternetUtility internetUtility;
	
	String typeOfHunt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_hunts);
		
		huntDataSource = new HuntDAO(this);
		huntDataSource.open();
		
		internetUtility = InternetUtility.getInstance(this);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("My hunts");
		}
		
		typeOfHunt = getIntent().getStringExtra("Type");
		
		mMapManager = MapManager.get(this);
		
		jsonParser = new JSONParser();
		
		mListView = (ExpandableListView) findViewById(R.id.list_of_user_hunts_id);
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		currentUserId = settings.getInt("currentUserId", 0);
		
		if(internetUtility.isInternetConnected())
		{
			attemptToReturnUserHunts();
		}
		else
		{
			Toast.makeText(MyHuntsActivity.this, "Internet is required", Toast.LENGTH_LONG).show();
		}
		
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		huntDataSource.updateDatabaseLocally();
		
		if(internetUtility.isInternetConnected())
		{
			attemptToReturnUserHunts();
		}
		else
		{
			Toast.makeText(MyHuntsActivity.this, "Internet is required", Toast.LENGTH_LONG).show();
		}
	}
	
	private void attemptToReturnUserHunts()
	{
		if (mReturnUserHuntsTask != null) {
			return;
		} 	
		mReturnUserHuntsTask = new ReturnUserHuntsTask();
		mReturnUserHuntsTask.execute((String) null);

		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mReturnUserHuntsTask!= null)
				{
					if(mReturnUserHuntsTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						pDialog.cancel();
						mReturnUserHuntsTask.cancel(true);
						Toast.makeText(MyHuntsActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
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
						mapDataDialog.cancel();
						mGetHuntParticipantIdTask.cancel(true);
						Toast.makeText(MyHuntsActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();	
					}
				}
			}
		}
		, 10000);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		menu.add(Menu.NONE, 1, Menu.NONE, "Log out");
		return true;
	} 
	

	
	//http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
	public void updateUI(final List<String> listDataHeader, HashMap<String, List<String>> listDataChild)
	{
	
		final ExpandableListAdapter adapter = new ExpandableListAdapter(MyHuntsActivity.this, listDataHeader, listDataChild);
		mListView.setAdapter(adapter);
		int noValuesList = mListView.getCount();
		
		mListView.setOnChildClickListener(new OnChildClickListener()
		{

			@Override
			public boolean onChildClick(ExpandableListView parent,
					View v, int groupPosition, int childPosition,
					long id) {
				
				//check here to make sure that user hasn't already registered with this hunt
				//http://stackoverflow.com/questions/4508979/android-listview-get-selected-item
				chosenHunt = huntDataSource.getParticularHunt(listDataHeader.get(groupPosition));
			
				if(chosenHunt != null)
				{	
					editor.putInt("currentHuntId", chosenHunt.getHuntId()); //NEEDS FIXED
					editor.putString("currentHuntName", chosenHunt.getHuntName());
					editor.commit(); 
					
					if(childPosition == 0)
					{
						Intent registerWithHuntIntent = new Intent(MyHuntsActivity.this, RegisterWithHuntActivity.class);
						startActivity(registerWithHuntIntent);
					
					}
					else if (childPosition == 1)
					{
						Intent leaderboardActivity = new Intent(MyHuntsActivity.this, LeaderboardActivity.class);
						startActivity(leaderboardActivity);
					}
					else if(childPosition ==2)
					{
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
				editor.putString("currentHuntName", selectedHunt.getHuntName());
				editor.commit(); 

				//startActivity(registerWithHuntintent);
			}
		});
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
			Intent loginActivityIntent = new Intent(MyHuntsActivity.this, LoginActivity.class);
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class ReturnUserHuntsTask extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(MyHuntsActivity.this);
	        pDialog.setMessage("Attempting to get treasure hunts...");
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
					parameters.add(new BasicNameValuePair("userId", Integer.toString(currentUserId)));
					
					JSONObject json = new JSONObject();
					
					//look for hunts that have start times that are 0 i.e. registered but not started
					if(typeOfHunt.equals("NotStarted"))
					{
						json = jsonParser.makeHttpRequest(returnUserHuntsNotStartedUrl, "POST", parameters);
					}
					else if(typeOfHunt.equals("Current"))
					{
						json = jsonParser.makeHttpRequest(returnUserHuntsCurrentUrl, "POST", parameters);
					}
					else
					{	//look for hunts where hunt end date is less than current date
						json = jsonParser.makeHttpRequest(returnUserHuntsCompletedUrl, "POST", parameters);
					}
					
					Log.d("Get hunts attempt", json.toString());
					
					success = json.getInt(tagSuccess);
					if(success == 1)
					{
						Log.d("Returning of hunts successful!", json.toString());
						tagResult = json.getJSONArray("results");
						tagIdResult = json.getJSONArray("huntIds");
						
						//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
						for(int i=0; i < tagResult.length(); i++)
						{
							huntDataSource.addUserHunt(tagIdResult.getJSONObject(i).getInt("HuntId"), tagResult.getJSONArray(i).getJSONObject(0).getString("HuntName"));
						}
						
						return tagMessage.toString();
					}
					else
					{
						Log.d("Returning of hunts failed! ", json.getString(tagMessage));
						
						return json.getString(tagMessage);
					}
				
			} catch (JSONException e) {
				Log.d("Leaderboard", e.toString());
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mReturnUserHuntsTask = null;
			pDialog.dismiss();

			if (fileUrl != null) 
			{	
				List<String> listDataHeader;
				//http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
				HashMap<String, List<String>> listDataChild;
				
		        listDataHeader = new ArrayList<String>();
		        listDataChild = new HashMap<String, List<String>>();
				
		        Log.i("ReturnedHunts","fileUrl");
		        
				List<Hunt> listOfHunts = huntDataSource.getAllUserHunts();
				
				//For every listOfHunts, add the header with its name
				for(int i=0; i< listOfHunts.size(); i++)
				{
					listDataHeader.add(listOfHunts.get(i).getHuntName().toString());
					List<String> huntOptions = new ArrayList<String>();
					huntOptions.add("Continue");
					huntOptions.add("Leaderboard");
					huntOptions.add("Map list");
					
					listDataChild.put(listDataHeader.get(i), huntOptions);		
				}
				
				updateUI(listDataHeader, listDataChild);
			} 
			else 
			{
				Builder alertForNoData = new Builder(MyHuntsActivity.this);
				alertForNoData.setTitle("Treasure Hunts");
				alertForNoData.setMessage("There are currently no treasure hunts to show. Please check back later.");
				alertForNoData.setCancelable(false);
				alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				
				alertForNoData.create();
				alertForNoData.show();
				
				Log.e("MyHunts", "No treasure hunts returned from the database");
			}
		}

		@Override
		protected void onCancelled() {
			mReturnUserHuntsTask = null;
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		return false;
	}
	
public class GetHuntParticipantIdTask extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			mapDataDialog = new ProgressDialog(MyHuntsActivity.this);
			mapDataDialog.setMessage("Attempting to return data for map");
			mapDataDialog.setIndeterminate(false);
			mapDataDialog.setCancelable(true);
			mapDataDialog.show();
		}
	
		@Override
		protected String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
				int success;

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
				parameters.add(new BasicNameValuePair("huntId", Integer.toString(chosenHunt.getHuntId())));
				parameters.add(new BasicNameValuePair("userId", Integer.toString(currentUserId)));
				
				try{
					Log.d("request", "starting");
					JSONObject jsonGetHuntParticipantId = jsonParser.makeHttpRequest(getHuntParticipantIdUrl, "POST", parameters);
					Log.i("MyHunts", "Get User Id Attempt: " + jsonGetHuntParticipantId.toString());
					success = jsonGetHuntParticipantId.getInt(tagSuccess);
					
					if(success == 1)
					{
						huntParticipantIdResult = jsonGetHuntParticipantId.getJSONObject("result");
						huntParticipantId = huntParticipantIdResult.getInt("HuntParticipantId");
						huntParticipantIdReturned = true;
						Log.i("MyHunts", "hunt participant id is: " + huntParticipantId);
						return jsonGetHuntParticipantId.getString(tagMessage);
						
					}
					else
					{
						Log.d("MyHunts", jsonGetHuntParticipantId.getString(tagMessage));
						return jsonGetHuntParticipantId.getString(tagMessage);
					}
				
			} catch (JSONException e) {
			
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mGetHuntParticipantIdTask = null;
			mapDataDialog.cancel();
			
			if(huntParticipantIdReturned)
			{
				editor.putInt("userParticipantId", huntParticipantId);
				editor.commit(); 
		
				Intent googleMapActivity = new Intent(MyHuntsActivity.this, GoogleMapActivity.class);
				googleMapActivity.putExtra("userParticipantIdForMap", huntParticipantId);
				startActivity(googleMapActivity);
				
				Log.i("MyHunts", fileUrl);
			}
			else
			{
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
				
				Log.d("MyHunts", "No Hunt Participant Id has been returned from the database for user " + currentUserId + " to use with map.");	
			}			
		}

		@Override
		protected void onCancelled() {
			mGetHuntParticipantIdTask = null;
		}
	}

	



}
