package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sqlLiteDatabase.Hunt;
import sqlLiteDatabase.HuntDAO;
import Utilities.ChooseHuntListAdapter;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;


//http://net.tutsplus.com/tutorials/php/php-database-access-are-you-doing-it-correctly/
public class ChooseHuntActivity extends Activity{

	public JSONParser jsonParser = new JSONParser();
	private static final String myChooseHuntUrl =  "http://lowryhosting.com/emmad/chooseHunts.php";
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	private static JSONArray tagResult;
	
	private ReturnHuntsTask mAuthTask = null;
	private ProgressDialog pDialog; 
	
	private HuntDAO huntDataSource;
	
	SharedPreferences.Editor editor;
	SharedPreferences settings;
	
	private ListView mListView;
	
	InternetUtility internetUtility;
	
	Handler handlerForUpdatingHuntList;
	
	MapManager mMapManager;
	
	int currentCompanyId;
	
	//http://stackoverflow.com/questions/12220239/repeat-task-in-android
		//http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
		final Runnable updateHuntsList = new Runnable(){
			@Override
			public void run()
			{
				
				huntDataSource.updateDatabaseLocally();
				if(internetUtility.isInternetConnected())
				{
					attemptReturnHunts();
				}
				else
				{
					Toast.makeText(ChooseHuntActivity.this, "Internet is required", Toast.LENGTH_LONG).show();
				}
				handlerForUpdatingHuntList.postDelayed(updateHuntsList, 10000);
			}
		};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_hunt);
		huntDataSource = new HuntDAO(this);
		huntDataSource.open();
		internetUtility = InternetUtility.getInstance(this);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Choose a hunt");
		}
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		mListView = (ListView) findViewById(R.id.hunt_list_view);	
		
		huntDataSource.updateDatabaseLocally();
		
		////http://stackoverflow.com/questions/12220239/repeat-task-in-android
		handlerForUpdatingHuntList = new Handler();
		handlerForUpdatingHuntList.post(updateHuntsList);
		
		mMapManager = MapManager.get(this);
		
		currentCompanyId = settings.getInt("currentCompanyId", 0);
		
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
			Intent loginActivityIntent = new Intent(ChooseHuntActivity.this, LoginActivity.class);
			mMapManager.stopLocationUpdates();
			startActivity(loginActivityIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		handlerForUpdatingHuntList.removeCallbacks(updateHuntsList);
	}
	
	/*private void updateListOfHunts()
	{
		//get this to happen automatically at some point // refreshes itself
		huntDataSource.updateDatabaseLocally();
		attemptReturnHunts();
	}*/
	
	private void attemptReturnHunts() {
		if (mAuthTask != null) {
			return;
		} 			
			mAuthTask = new ReturnHuntsTask(); // Do ASYNC way
			mAuthTask.execute((String) null);	
			//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
			Handler handler = new Handler();
			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					if(mAuthTask != null)
					{
						if(mAuthTask.getStatus() == AsyncTask.Status.RUNNING)
						{
							mAuthTask.cancel(true);
							pDialog.cancel();
							Toast.makeText(ChooseHuntActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						}
					}
				}
			}
			
			, 10000);
			try {
				mAuthTask.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	}


public class ReturnHuntsTask extends AsyncTask<String, String, String> {
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		pDialog = new ProgressDialog(ChooseHuntActivity.this);
        pDialog.setMessage("Attempting to get treasure hunts...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}
	
	@Override
	protected String doInBackground(String... args) {
		//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
		//http://www.php.net/manual/en/pdostatement.fetchall.php
		//http://stackoverflow.com/questions/14491430/using-pdo-to-echo-display-all-rows-from-a-table
	
		int success;
		
		try {
			//PHP CURRENT DATE TIME CHECK http://stackoverflow.com/questions/470617/get-current-date-and-time-in-php
			Log.d("request", "starting");
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("companyId", Integer.toString(currentCompanyId)));
			
			JSONObject json = jsonParser.makeHttpRequest(myChooseHuntUrl, "POST", parameters);
			
			Log.d("Get hunts attempt", json.toString());
			
			success = json.getInt(tagSuccess);
			if(success == 1)
			{
				Log.d("Returning of hunts successful!", json.toString());
				tagResult = json.getJSONArray("results");
				
				//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
				for(int i=0; i < tagResult.length(); i++)
				{
					huntDataSource.addHunt(tagResult.getJSONArray(i).getJSONObject(0).getString("HuntName"));
				}
				
				return tagMessage.toString();
			}
			else
			{
				Log.d("Returning of hunts failed! ", json.getString(tagMessage));
				
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

		if (fileUrl != null) 
		{	
			List<Hunt> listOfHunts = huntDataSource.getAllHunts();
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
					editor.putString("currentHuntName", selectedHunt.getHuntName());
					editor.commit(); 

					startActivity(registerWithHuntintent);
				}
			});
			
		} 
		else 
		{
			Builder alertForNoData = new Builder(ChooseHuntActivity.this);
			alertForNoData.setTitle("Treasure Hunts");
			alertForNoData.setMessage("There are currently no hunts to show for this company. Please check back later.");
			alertForNoData.setCancelable(false);
			alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			alertForNoData.create();
			alertForNoData.show();
			
			Log.e("Treasure Hunts", "No treasure hunts returned from the database for the company identified by id " + currentCompanyId);
		}
	}

	@Override
	protected void onCancelled() {
		mAuthTask = null;
	}
}


}

