package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sqlLiteDatabase.Company;
import sqlLiteDatabase.CompanyDAO;
import Utilities.ChooseCompanyListAdapter;
import Utilities.InternetUtility;
import Utilities.JSONParser;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseCompanyActivity extends Activity {

	private ListView mCompanyListView;
	
	public JSONParser jsonParser = new JSONParser();
	private static final String myChooseCompaniesUrl =  "http://lowryhosting.com/emmad/chooseCompanies.php";
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	private static JSONArray tagResult;
	
	private InternetUtility internetChecker;
	
	private ReturnCompaniesTask mCompaniesTask = null;
	
	//private ReturnCompaniesTask mCompaniesTask = null;
	private ProgressDialog pDialog; 
	
	InternetUtility internetUtility;
	SharedPreferences.Editor editor;
	SharedPreferences settings;
	
	MapManager mMapManager;
	
	private CompanyDAO companyDataSource;
	
	Handler handlerForUpdatingCompanyList;
	
	int currentUserId;
	
	//http://stackoverflow.com/questions/12220239/repeat-task-in-android
			//http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
			final Runnable updateCompanyList = new Runnable(){
				@Override
				public void run()
				{
					companyDataSource.updateDatabaseLocally();
					
					if(internetUtility.isInternetConnected())
					{
						attemptReturnCompanies();
					//handlerForUpdatingCompanyList.postDelayed(updateCompanyList, 10000);
					}
					else
					{
						Toast.makeText(ChooseCompanyActivity.this, "Internet is required", Toast.LENGTH_LONG).show();
					}
				}
			};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_company);
		
		mCompanyListView = (ListView) findViewById(R.id.company_list_view);
		
		internetUtility = InternetUtility.getInstance(this);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Choose company");
		}
		
		companyDataSource = new CompanyDAO(this);
		companyDataSource.open();
		
		companyDataSource.updateDatabaseLocally();

		handlerForUpdatingCompanyList = new Handler();
		handlerForUpdatingCompanyList.post(updateCompanyList);
		
		settings = getSharedPreferences("UserPreferencesFile", 0);
		editor = settings.edit();
		
		mMapManager = MapManager.get(this);
		
		currentUserId = settings.getInt("currentUserId", 0);
		
		internetChecker = InternetUtility.getInstance(this);
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
				Intent loginActivityIntent = new Intent(ChooseCompanyActivity.this, LoginActivity.class);
				mMapManager.stopLocationUpdates();
				startActivity(loginActivityIntent);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
		private void attemptReturnCompanies() {
			if (mCompaniesTask != null) {
				return;
			} 
			
				mCompaniesTask = new ReturnCompaniesTask(); // Do ASYNC way
				mCompaniesTask.execute((String) null);	
					//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
					Handler handler = new Handler();
					handler.postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							if(mCompaniesTask != null)
							{
								if(mCompaniesTask.getStatus() == AsyncTask.Status.RUNNING)
								{
									mCompaniesTask.cancel(true);
									pDialog.cancel();
									Toast.makeText(ChooseCompanyActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
								}
							}
						}
					}
					
					, 10000);
					try {
						mCompaniesTask.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
		}
		
		public class ReturnCompaniesTask extends AsyncTask<String, String, String> {
			
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				pDialog = new ProgressDialog(ChooseCompanyActivity.this);
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
					JSONObject json = jsonParser.makeHttpRequest(myChooseCompaniesUrl, "POST", parameters);
					
					Log.d("Get companies attempt", json.toString());
					
					success = json.getInt(tagSuccess);
					if(success == 1)
					{
						Log.d("Returning of companies successful!", json.toString());
						tagResult = json.getJSONArray("results");
						
						//-http://stackoverflow.com/questions/8411154/null-pointer-exception-while-inserting-json-array-into-sqlite-database
						for(int i=0; i < tagResult.length(); i++)
						{
							companyDataSource.addCompany(tagResult.getJSONObject(i).getInt("UserId"), tagResult.getJSONObject(i).getString("CompanyName"), tagResult.getJSONObject(i).getString("CompanyPassword"));
						}
						
						return tagMessage.toString();
					}
					else
					{
						Log.d("Returning of companies failed! ", json.getString(tagMessage));
						
						return json.getString(tagMessage);
					}
					
				} catch (JSONException e) {
				Log.d("Company", e.toString());
				}

				return null;
			}

			@Override
			protected void onPostExecute(final String fileUrl) {
				mCompaniesTask = null;
				pDialog.dismiss();

				if (fileUrl != null) 
				{	
					List<Company> listOfCompanies = companyDataSource.getAllCompanies();
					final ChooseCompanyListAdapter adapter = new ChooseCompanyListAdapter(ChooseCompanyActivity.this, listOfCompanies);
					mCompanyListView.setAdapter(adapter);	
					
					//http://stackoverflow.com/questions/16189651/android-listview-selected-item-stay-highlighted
					mCompanyListView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							
							//check here to make sure that user hasn't already registered with this hunt
							//http://stackoverflow.com/questions/4508979/android-listview-get-selected-item
							final Company selectedCompany = adapter.getItem(position);
							boolean hasPasswordAlreadyBeenSaved = settings.getBoolean(selectedCompany.getCompanyId() + "companyIdPasswordSavedFor" + currentUserId, false);
							if(!hasPasswordAlreadyBeenSaved)
							{
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
										
										if(password.getText().toString().equals(selectedCompany.getCompanyPassword().toString()))
										{
											//Go to the choose hunts page
											Intent chooseHuntsIntent = new Intent(ChooseCompanyActivity.this, ChooseHuntActivity.class);
											editor.putInt("currentCompanyId", selectedCompany.getCompanyId()); //USER ID
											if(mCheckBox.isChecked())
											{
												
												editor.putBoolean(selectedCompany.getCompanyId() + "companyIdPasswordSavedFor" + currentUserId, true);
											}
											else
											{
												editor.putBoolean(selectedCompany.getCompanyId() + "companyIdPasswordSavedFor" + currentUserId, false);
											}
											editor.commit(); 
											dialog.cancel();
											startActivity(chooseHuntsIntent);
										}
										else
										{
											//Should pop up another to say that this is an incorrect password!!!	
											dialog.cancel();
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
						else
							{
								Intent chooseHuntsIntent = new Intent(ChooseCompanyActivity.this, ChooseHuntActivity.class);
								editor.putInt("currentCompanyId", selectedCompany.getCompanyId()); //USER ID	
								editor.commit(); 
								startActivity(chooseHuntsIntent);		
							}
						}
					}); 
						
					
					
				} 
				else 
				{
					Builder alertForNoData = new Builder(ChooseCompanyActivity.this);
					alertForNoData.setTitle("Companies");
					alertForNoData.setMessage("There are currently no companies to show. Please check back later.");
					alertForNoData.setCancelable(false);
					alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					
					alertForNoData.create();
					alertForNoData.show();
					
					Log.e("Companies", "No companies returned from the database");
				}
			}

			@Override
			protected void onCancelled() {
				mCompaniesTask = null;
			}
		}
}
