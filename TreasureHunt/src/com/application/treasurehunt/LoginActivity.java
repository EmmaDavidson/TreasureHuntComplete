package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import Utilities.InternetUtility;
import Utilities.JSONParser;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
	
	private static final String getResetPasswordDetails = "http://lowryhosting.com/emmad/checkValidEmailAddress.php";
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	
	private GetUserIdTask mUserTask = null;
	private ResetPasswordTask mPasswordTask = null;
	
	private UserLoginTask mAuthTask = null;
	private ProgressDialog pDialog; 
	private ProgressDialog resetPasswordDialog;
	
	private boolean existingEmailAddress;
	
	private String mEmail;
	private String mPassword;
	
	private Button mForgotPasswordButton;
	private Button mLicenceButton;
	
	private boolean loginSuccessful = false;

	private EditText mEmailView;
	private EditText mPasswordView;
	
	private static JSONObject userIdResult;
	private static JSONObject resetEmailAddressResult;
	private static String resetEmailAddressQuestionResult;
	
	Button mLoginButton;
	Button mRegisterButton;
	
	boolean currentUserIdReturned;
	boolean gettingUserIdSuccessful;
	
	EditText resetEmailAddress;
	String submittedEmail;
	
	int userId = 0;
	String userName;
	Intent homepageActivityIntent;
	public JSONParser jsonParser;
	
	InternetUtility internetUtility;
	
	String securityQuestion;
	String securityAnswer;
	int resetPasswordId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		internetUtility = InternetUtility.getInstance(this);

		setContentView(R.layout.activity_login);
		mEmailView = (EditText) findViewById(R.id.login_email_address);
		mPasswordView = (EditText) findViewById(R.id.login_password);
		mForgotPasswordButton = (Button) findViewById(R.id.forgotten_password_button);
		mLicenceButton = (Button) findViewById(R.id.licence_button);
		
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
						if(internetUtility.isInternetConnected())
						{
							pDialog = new ProgressDialog(LoginActivity.this);
					        pDialog.setMessage("Attempting login...");
							pDialog.setIndeterminate(false);
							pDialog.setCancelable(false);
							pDialog.show();
							
							attemptLogin();
						}
						else
						{
							Toast.makeText(LoginActivity.this, "Internet is required", Toast.LENGTH_LONG).show();
						}
					}
				});
		
		//http://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
		mLicenceButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent licenceIntent = new Intent(LoginActivity.this, LicenceActivity.class);
						startActivity(licenceIntent);
					}
				});
		
		findViewById(R.id.register_on_login_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						goToRegister();
					}
				});
		
		mForgotPasswordButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						LayoutInflater li = LayoutInflater.from(LoginActivity.this);
						final View emailView = li.inflate(R.layout.email_address_dialog, null);
						
						//Alert pop up to enter email address 
						Builder builder = new Builder(LoginActivity.this);
						builder.setView(emailView);
						builder.setCancelable(false);
						builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								resetEmailAddress = (EditText) emailView.findViewById(R.id.email_address_reset_text_field);
								submittedEmail = resetEmailAddress.getText().toString();
								//dialog.cancel();
								if(internetUtility.isInternetConnected())
								{
									attemptResetPassword();
								}
								else
								{
									Toast.makeText(LoginActivity.this, "Internet is required", Toast.LENGTH_LONG).show();
								}
								
								
							}
						});
						
						builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
					
					
						AlertDialog emailDialog = builder.create();
						emailDialog.show();
					}
				});
		
		
	}

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
				Log.i("Login Successful!", retrievedJsonObject.toString());
				//finish();
				loginSuccessful = true;
				
				return retrievedJsonObject.getString(tagMessage);
			}
			else
			{
				Log.e("Login failed!", retrievedJsonObject.getString(tagMessage));
				return retrievedJsonObject.getString(tagMessage);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return "";
	}

	@Override
	protected void onPostExecute(final String fileUrl) {
		mAuthTask = null;
		
		if(loginSuccessful)
		{
			Log.i("Login", "Login was successful");
			attemptToReturnUserId();
		}
		else
		{	
			Builder alertForFailedLogin = new Builder(LoginActivity.this);
			alertForFailedLogin.setTitle("Login unsuccessful");
			alertForFailedLogin.setMessage("Login was unsuccessful. Please try again.");
			alertForFailedLogin.setCancelable(false);
			alertForFailedLogin.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			alertForFailedLogin.create();
			alertForFailedLogin.show();
			
			mEmailView.setText("");
			mPasswordView.setText(""); 
			
			Log.e("Login", "Login failure for email address: " + mEmailView.getText());
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

public void attemptResetPassword()
{
	if (mPasswordTask != null) {
		return;
	} 	
	mPasswordTask = new ResetPasswordTask();
	mPasswordTask.execute((String) null);

	Handler handlerForUserTask = new Handler();
	handlerForUserTask.postDelayed(new Runnable()
	{
		@Override
		public void run()
		{
			if(mPasswordTask!= null)
			{
				if(mPasswordTask.getStatus() == AsyncTask.Status.RUNNING)
				{
					mPasswordTask.cancel(true);
					resetPasswordDialog.cancel();
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
				gettingUserIdSuccessful = true;
				homepageActivityIntent = new Intent(LoginActivity.this, HomepageActivity.class);
				//homepageActivityIntent.putExtra("userName", userName);
				
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
		pDialog.cancel();
		
		if(gettingUserIdSuccessful)
		{
			SharedPreferences settings = getSharedPreferences("UserPreferencesFile", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("currentUserId", userId);
			editor.putString("currentUserName", userName);
			editor.commit();
			
			startActivity(homepageActivityIntent);
		}
		else
		{
			Builder alertForNoData = new Builder(LoginActivity.this);
			alertForNoData.setTitle("Login unsuccessful");
			alertForNoData.setMessage("Login was unsuccessful. Please try again.");
			alertForNoData.setCancelable(false);
			alertForNoData.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			alertForNoData.create();
			alertForNoData.show();

			Log.e("Login", "Login failure for email address: " + mEmailView.getText());
		}
	}

	@Override
	protected void onCancelled() {
		mUserTask = null;
	}
}

public class ResetPasswordTask extends AsyncTask<String, String, String> {
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		resetPasswordDialog = new ProgressDialog(LoginActivity.this);
		resetPasswordDialog.setMessage("Attempting to reset password");
		resetPasswordDialog.setIndeterminate(false);
		resetPasswordDialog.setCancelable(false);
		resetPasswordDialog.show();
	}

	@Override
	protected String doInBackground(String... arg0) {
		
		int success;
		//GETTING THE USER ID
		List<NameValuePair> parametersForUserId = new ArrayList<NameValuePair>();
		//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
		parametersForUserId.add(new BasicNameValuePair("email", submittedEmail));
		
		try{
			Log.d("request", "starting");
			JSONObject jsonFindUserId = jsonParser.makeHttpRequest(getResetPasswordDetails, "POST", parametersForUserId);
			Log.d("Get email address for password reset attempt", jsonFindUserId.toString());
			success = jsonFindUserId.getInt(tagSuccess);
			
			if(success == 1)
			{
				resetEmailAddressResult = jsonFindUserId.getJSONObject("result");
				securityAnswer = resetEmailAddressResult.getString("Answer");
				securityQuestion = jsonFindUserId.getString("securityquestion");
				resetPasswordId = resetEmailAddressResult.getInt("UserId");
				existingEmailAddress = true;
				return jsonFindUserId.getString(tagMessage);	
			}
			else
			{
				Log.d("Failed attempt at reset password", jsonFindUserId.getString(tagMessage));
				existingEmailAddress = false;
				return jsonFindUserId.getString(tagMessage);
			}
		}
		catch (JSONException e) {
			Log.d("ResetPassword", e.toString());
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(final String fileUrl) {
		mPasswordTask = null;
		resetPasswordDialog.cancel();
		
		if(existingEmailAddress)
		{
			Intent forgottenPasswordIntent = new Intent(LoginActivity.this, ForgottenPasswordActivity.class);
			forgottenPasswordIntent.putExtra("EmailAddress", resetEmailAddress.getText().toString());
			forgottenPasswordIntent.putExtra("SecurityQuestion", securityQuestion);
			forgottenPasswordIntent.putExtra("SecurityAnswer", securityAnswer);
			forgottenPasswordIntent.putExtra("UserId", resetPasswordId);
			startActivity(forgottenPasswordIntent);
		}
		else
		{	
			Builder incorrectEmailBuilder = new Builder(LoginActivity.this);									
			incorrectEmailBuilder.setTitle("Invalid email");
			incorrectEmailBuilder.setMessage("You entered an invalid email address");
			incorrectEmailBuilder.setCancelable(false);
			incorrectEmailBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			incorrectEmailBuilder.create();
			incorrectEmailBuilder.show();
			
		}
	}

	@Override
	protected void onCancelled() {
		mPasswordTask = null;
	}
}
}



