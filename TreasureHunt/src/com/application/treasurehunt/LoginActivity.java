package com.application.treasurehunt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import Utilities.JSONParser;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *FILE GENERATED AUTOMATICALLY WHEN CREATING NEW LOGIN ACTIVITY, although altered to suit my needs
 */

//COLOUR CHANGE IN THE BACKGROUND ON THE VIEWS - http://stackoverflow.com/questions/2748830/how-to-change-background-color-in-android-app
public class LoginActivity extends Activity {
	//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
	
	//http://stackoverflow.com/questions/5806220/how-to-connect-to-my-http-localhost-web-server-from-android-emulator-in-eclips
	private static final String myLoginUrl =  "http://lowryhosting.com/emmad/login.php";
	//private static final String myLoginUrl = "http://192.168.1.74:80/webservice/choosehunt.php";
	private static final String getUserIdUrl =  "http://lowryhosting.com/emmad/returnCurrentUserId.php";
	//private static final String getUserIdUrl =  "http://192.168.1.74:80/webservice/returnCurrentUserId.php";
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	
	private GetUserIdTask mUserTask = null;
	
	private UserLoginTask mAuthTask = null;
	private ProgressDialog pDialog; //spinner thing
	
	private String mEmail;
	private String mPassword;
	
	private boolean loginSuccessful = false;

	private EditText mEmailView;
	private EditText mPasswordView;
	
	private static JSONObject userIdResult;
	
	Button mLoginButton;
	Button mRegisterButton;
	
	boolean currentUserIdReturned;
	
	int userId = 0;
	String userName;
	Intent homepageActivityIntent;
	public JSONParser jsonParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		mEmailView = (EditText) findViewById(R.id.login_email_address);
		mPasswordView = (EditText) findViewById(R.id.login_password);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Login");
		}
		
		jsonParser = new JSONParser();
		
		mLoginButton = (Button) findViewById(R.id.sign_in_button);
				
		mLoginButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		findViewById(R.id.register_on_login_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						goToRegister();
					}
				});
	}
	
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	} */

	private void goToRegister()
	{		
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);	
		
		mEmailView.setText(null);
		mPasswordView.setText(null);
	}
	
	
	private void attemptLogin() {		

		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		
		if((isValidEmailAddress() && isValidPassword()))
		{		
			mAuthTask = new UserLoginTask(); 
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
							Toast.makeText(LoginActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						}
					}
				}
			}	
			, 100000);
			
			//mEmailView.setText(null);
			//mPasswordView.setText(null);
		}	
	}
	
	private boolean isValidEmailAddress()
	{	
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_email_null));
			return false;
			
		}
		else if(mEmail.length() < 10)
		{
			mEmailView.setError(getString(R.string.error_email_too_short));	
			return false;
		}
		//http://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address-on-android
		else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail.toString()).matches())
		{
			mEmailView.setError(getString(R.string.error_email_incorrect_format));	
			return false;
		}
		
		return true;
		
	}
	
	private boolean isValidPassword()
	{
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_password_null));
			return false;
			
		}
		else if(mPassword.length() < 6)
		{
			mPasswordView.setError(getString(R.string.error_password_too_short));	
			return false;
		}
		
		return true;
		
	}
	
	//http://developer.android.com/training/basics/activity-lifecycle/recreating.html
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{	
		savedInstanceState.putString("LOGIN_EMAIL_ADDRESS", mEmailView.getText().toString());
		savedInstanceState.putString("LOGIN_PASSWORD", mPasswordView.getText().toString());
		
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		
		mEmailView.setText(savedInstanceState.getString("LOGIN_EMAIL_ADDRESS"));
		mPasswordView.setText(savedInstanceState.getString("LOGIN_PASSWORD"));
	}
	
public class UserLoginTask extends AsyncTask<String, String, String>{
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setMessage("Attempting login...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}
	
	@Override
	protected String doInBackground(String... args) {
		//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
		int success;
		String email = mEmail;
		String password = mPassword;
		
		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			
			parameters.add(new BasicNameValuePair("email", email));
			parameters.add(new BasicNameValuePair("password", password));
			
			Log.d("request", "starting");
			
			JSONObject retrievedJsonObject = jsonParser.makeHttpRequest(myLoginUrl, "POST", parameters);
			Log.d("Login attempt", retrievedJsonObject.toString());
			
			success = retrievedJsonObject.getInt(tagSuccess);
			if(success == 1)
			{
				Log.d("Login Successful!", retrievedJsonObject.toString());
				//finish();
				loginSuccessful = true;
				
				return retrievedJsonObject.getString(tagMessage);
			}
			else
			{
				Log.d("Login failed!", retrievedJsonObject.getString(tagMessage));
				return retrievedJsonObject.getString(tagMessage);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return "This is my return message";
	}

	@Override
	protected void onPostExecute(final String fileUrl) {
		mAuthTask = null;
		pDialog.dismiss();
		
		if(loginSuccessful)
		{
				attemptToReturnUserId();
		}

		if (fileUrl != null) {
			Toast.makeText(LoginActivity.this, fileUrl, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(LoginActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onCancelled() {
		mAuthTask = null;
	}
}

private void attemptToReturnUserId()
{
	if (mUserTask != null) {
		return;
	} 	
	mUserTask = new GetUserIdTask();
	mUserTask.execute((String) null);

	Handler handlerForUserTask = new Handler();
	handlerForUserTask.postDelayed(new Runnable()
	{
		@Override
		public void run()
		{
			if(mUserTask!= null)
			{
				if(mUserTask.getStatus() == AsyncTask.Status.RUNNING)
				{
					mUserTask.cancel(true);
					pDialog.cancel();
					Toast.makeText(LoginActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	, 10000);	
}


public class GetUserIdTask extends AsyncTask<String, String, String> {

	@Override
	protected String doInBackground(String... arg0) {
		
		int success;
		//GETTING THE USER ID
		List<NameValuePair> parametersForUserId = new ArrayList<NameValuePair>();
		//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
		parametersForUserId.add(new BasicNameValuePair("email", mEmail));
		
		try{
			Log.d("request", "starting");
			JSONObject jsonFindUserId = jsonParser.makeHttpRequest(getUserIdUrl, "POST", parametersForUserId);
			Log.d("Get User Id Attempt", jsonFindUserId.toString());
			success = jsonFindUserId.getInt(tagSuccess);
			
			if(success == 1)
			{
				userIdResult = jsonFindUserId.getJSONObject("result");
				currentUserIdReturned = true;
				userId = userIdResult.getInt("UserId");
				userName = userIdResult.getString("Name");
				homepageActivityIntent = new Intent(LoginActivity.this, HomepageActivity.class);
				//homepageActivityIntent.putExtra("userName", userName);
				startActivity(homepageActivityIntent);
				return jsonFindUserId.getString(tagMessage);
				
			}
			else
			{
				Log.d("Getting User Id failed!", jsonFindUserId.getString(tagMessage));
				return jsonFindUserId.getString(tagMessage);
			}
		}
		catch (JSONException e) {
			
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(final String fileUrl) {
		mUserTask = null;

		if (fileUrl != null) {

			SharedPreferences settings = getSharedPreferences("UserPreferencesFile", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("currentUserId", userId);
			editor.putString("currentUserName", userName);
			editor.commit();

		} else {
			Toast.makeText(LoginActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onCancelled() {
		mUserTask = null;
	}
}

}



