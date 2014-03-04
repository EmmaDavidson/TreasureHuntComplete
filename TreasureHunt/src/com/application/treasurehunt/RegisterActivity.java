package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.application.treasurehunt.LoginActivity.UserLoginTask;

import Utilities.JSONParser;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private static final String myRegisterUrl =  "http://lowryhosting.com/emmad/register.php";
	private static final String getUserIdUrl =  "http://lowryhosting.com/emmad/returnCurrentUserId.php";
	private static final String setUserRoleUrl =  "http://lowryhosting.com/emmad/setUserRole.php";

	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	
	public JSONParser jsonParser = new JSONParser();
	
	private UserRegisterTask mAuthTask = null;
	private SetUserRoleTask mUserRoleTask = null;
	
	private ProgressDialog pDialog; 
	
	private static JSONObject userIdResult;

	String currentUser;
	boolean userSucessfullyRegistered = false;
	boolean userIdSucessfullyReturned = false;
	boolean userRoleSuccessful = false;
	int userId = 0;
	
	private String mEmail;
	private String mPassword;
	private String mName;
	private String mAnswer;

	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mNameView;
	private EditText mAnswerView;
	
	Spinner securityQuestionSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mEmailView = (EditText) findViewById(R.id.register_email_address);
		mPasswordView = (EditText) findViewById(R.id.register_password);
		mNameView = (EditText) findViewById(R.id.register_name);
		mAnswerView = (EditText) findViewById(R.id.security_question_answer);
							
		findViewById(R.id.register_save_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegister();
						
					}
				});
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Register");
		}
		
		//http://developer.android.com/guide/topics/ui/controls/spinner.html
		securityQuestionSpinner = (Spinner) findViewById(R.id.security_question_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.security_choices_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		securityQuestionSpinner.setAdapter(adapter);
		
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	} */
	
	//PUT IN A CHECK THAT IT NEEDS TO CHECK IF THERES AN INTERNET CONNECTION
	//-http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
	
	private void attemptRegister() {
		if (mAuthTask != null) {
			return;
		} 		
		
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mNameView.setError(null);

		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mName = mNameView.getText().toString();
		mAnswer = mAnswerView.getText().toString();
		
		if((isValidEmailAddress() && isValidPassword() && isValidName() && isValidAnswer()))
		{		
			mAuthTask = new UserRegisterTask(); // Do ASYNC way
			mAuthTask.execute((String) null);
			
			//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
			Handler handler = new Handler();
			handler.postDelayed(new Runnable()
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
							Toast.makeText(RegisterActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
						}
					}
				}
			}
			, 10000);
		}
		
	}
	
	private boolean isValidEmailAddress()
	{	
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_email_null));
			return false;
			
		}
		else if(mEmail.length() < 10 || mEmail.length() >= 30)
		{
			mEmailView.setError(getString(R.string.error_email_too_short));	
			return false;
		}
		else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail.toString()).matches())
		{
			mEmailView.setError(getString(R.string.error_email_incorrect_format));	
			return false;
		}
		
		return true;
		
	}
	
	private boolean isValidAnswer()
	{
		if (TextUtils.isEmpty(mAnswer)) {
			mAnswerView.setError(getString(R.string.error_answer_null));
			return false;
		}
		
		//MUST BE LONGER THAN 3 AND SHORTER THAN 30
		return true;
	}
	
	private boolean isValidPassword()
	{
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_password_null));
			return false;
			
		}
		else if(mPassword.length() < 6 || mPassword.length() >= 10)
		{
			mPasswordView.setError(getString(R.string.error_password_too_short));	
			return false;
		}
		
		return true;
		
	}
	
	private boolean isValidName()
	{
		if (TextUtils.isEmpty(mName)) {
			mNameView.setError(getString(R.string.error_name_null));
			return false;
			
		}
		else if(mName.length() < 3 || mName.length() > 30)
		{
			mNameView.setError(getString(R.string.error_name_too_short));	
			return false;
		}
		
		//THIS DOESNT CHECK FORMAT HERE
		
		return true;
		
	}
	
	private void attemptUserRoleRegister()
	{
		if (mUserRoleTask != null) {
			return;
		} 	
		mUserRoleTask = new SetUserRoleTask();
		mUserRoleTask.execute((String) null);

		Handler handlerForUserRoleTask = new Handler();
		handlerForUserRoleTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(mUserRoleTask!= null)
				{
					if(mUserRoleTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						mUserRoleTask.cancel(true);
						Toast.makeText(RegisterActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
		
	}
	
		//http://developer.android.com/training/basics/activity-lifecycle/recreating.html
		@Override
		public void onSaveInstanceState(Bundle savedInstanceState)
		{	
			savedInstanceState.putString("REGISTER_EMAIL_ADDRESS", mEmailView.getText().toString());
			savedInstanceState.putString("REGISTER_NAME", mNameView.getText().toString());
			savedInstanceState.putString("REGISTER_PASSWORD", mPasswordView.getText().toString());	
			
			super.onSaveInstanceState(savedInstanceState);
		}

		@Override
		public void onRestoreInstanceState(Bundle savedInstanceState)
		{
			super.onRestoreInstanceState(savedInstanceState);
			
			mEmailView.setText(savedInstanceState.getString("REGISTER_EMAIL_ADDRESS"));
			mNameView.setText(savedInstanceState.getString("REGISTER_NAME"));
			mPasswordView.setText(savedInstanceState.getString("REGISTER_PASSWORD"));	
		}
	

	
public class UserRegisterTask extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Attempting register...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... args) {
//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
			int success;
			
			try {
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				parameters.add(new BasicNameValuePair("email", mEmail));
				parameters.add(new BasicNameValuePair("password", mPassword));
				parameters.add(new BasicNameValuePair("name", mName));
				parameters.add(new BasicNameValuePair("securityQuestionId", Long.toString(securityQuestionSpinner.getSelectedItemId())));
				parameters.add(new BasicNameValuePair("answer", mAnswer));
				
				Log.d("request", "starting");
				JSONObject json = jsonParser.makeHttpRequest(myRegisterUrl, "POST", parameters);
				Log.d("Login attempt", json.toString());
				
				success = json.getInt(tagSuccess);
				if(success == 1)
				{
					Log.d("Login Successful!", json.toString());
					userSucessfullyRegistered = true;
					currentUser = mEmail;
					userIdResult = json.getJSONObject("result");
					userIdSucessfullyReturned = true;
					userId = userIdResult.getInt("UserId");
					return json.getString(tagMessage);
				}
				else
				{
					Log.d("Login failed!", json.getString(tagMessage));
					return json.getString(tagMessage);
				}
				
			} catch (JSONException e) {
			
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String fileUrl) {
			mAuthTask = null;
			

			if (fileUrl != null) {
				Toast.makeText(RegisterActivity.this, fileUrl, Toast.LENGTH_LONG).show();
				if(userSucessfullyRegistered)
				{
					attemptUserRoleRegister();
				}
			} else {
				Toast.makeText(RegisterActivity.this, "Nothing returned from the database", Toast.LENGTH_LONG).show();
			}
			

		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			pDialog.cancel();
		}
	}

public class SetUserRoleTask extends AsyncTask<String, String, String> {
	
	@Override
	protected String doInBackground(String... arg0) {
		
		int success;
		//GETTING THE USER ID
		List<NameValuePair> parametersForUserRoleId = new ArrayList<NameValuePair>();
		//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
		parametersForUserRoleId.add(new BasicNameValuePair("roleid", Integer.toString(2)));
		parametersForUserRoleId.add(new BasicNameValuePair("userid", Integer.toString(userId)));

		try
		{
			Log.d("request", "starting");
			JSONObject jsonSetUserRoleId = jsonParser.makeHttpRequest(setUserRoleUrl, "POST", parametersForUserRoleId);
			Log.d("Set userrole Attempt", jsonSetUserRoleId.toString());
			success = jsonSetUserRoleId.getInt(tagSuccess);
			
			if(success == 1)
			{
				Log.d("Setting user role was successful!", jsonSetUserRoleId.getString(tagMessage));
				userRoleSuccessful = true;
				return jsonSetUserRoleId.getString(tagMessage);
			}
			else
			{
				Log.d("Setting user role failed!", jsonSetUserRoleId.getString(tagMessage));
				return jsonSetUserRoleId.getString(tagMessage);
			}
		}
		catch (JSONException e) 
		{
			
		}
		
		
		return null;
	}
	
	@Override
	protected void onPostExecute(final String fileUrl) {
		mUserRoleTask = null;
		pDialog.cancel();
		
		if (fileUrl != null) {
			if(userRoleSuccessful)
			{
			Intent loginActivityIntent = new Intent(RegisterActivity.this, LoginActivity.class);
			finish();
			startActivity(loginActivityIntent);
			}
			
		} else {
			Toast.makeText(RegisterActivity.this, "Couldn't set up user role", Toast.LENGTH_LONG).show();
		}
	

	}
	
	@Override
	protected void onCancelled() {
		mUserRoleTask = null;
		pDialog.cancel();
	}

	

	}
}
