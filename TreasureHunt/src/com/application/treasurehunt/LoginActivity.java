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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import Utilities.InternetUtility;
import Utilities.JSONParser;
import Utilities.PHPHelper;

import java.util.ArrayList;
import java.util.List;

/*
 * The purpose of this activity is to allow a participant to log into the mobile application.
 * [See dissertation Section 2.4.1.2].
 * */

public class LoginActivity extends Activity {
	//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
	//http://stackoverflow.com/questions/5806220/how-to-connect-to-my-http-localhost-web-server-from-android-emulator-in-eclips
	//COLOUR CHANGE IN THE BACKGROUND ON THE VIEWS - http://stackoverflow.com/questions/2748830/how-to-change-background-color-in-android-app
	 //FILE GENERATED AUTOMATICALLY WHEN CREATING NEW LOGIN ACTIVITY, although altered to suit my needs
	 
	/*
	 * Global variables used within LoginActivity.
	 */
	private static final String LOGIN_URL =  "http://lowryhosting.com/emmad/login.php";
	private static final String RETRIEVE_PASSWORD_RESET_DETAILS_URL = 
			"http://lowryhosting.com/emmad/checkValidEmailAddress.php";
	
	private String mEmail;
	private String mPassword;
	private EditText mResetEmailAddress;
	private String mSubmittedEmail;
	
	private Button mForgotPasswordButton;
	private Button mLicenceButton;
	private Button mLoginButton;
	private Button mRegisterButton;
	private EditText mEmailView;
	private EditText mPasswordView;
	private ProgressDialog mLoginDialog; 
	private ProgressDialog mResetPasswordDialog;
	
	public JSONParser jsonParser;
	private VerifyEmailTask mVerifyEmailTask = null;
	private UserLoginTask mLoginTask = null;
	private static JSONObject sUserIdResult;
	private static JSONObject sResetEmailAddressResult;
	
	private InternetUtility mInternetUtility;
	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	private boolean mExistingEmailAddress;
	private boolean mLoginSuccessful = false;
	private boolean mCurrentUserIdReturned = false;
	private boolean mGettingUserIdSuccessful;
	
	private Intent mHomepageActivityIntent;
	
	private int mUserId = 0;
	private String mUserName;
	private String mSecurityQuestion;
	private String mSecurityAnswer;
	private int mResetPasswordId;

	/*
	 * Method called when the Activity is created (as part of the Android Life Cycle) which sets up this Activity's variables.
	 * It also decides what to do if an on screen button was pressed.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Login");
		}

		setContentView(R.layout.activity_login);
		mEmailView = (EditText) findViewById(R.id.login_email_address);
		mPasswordView = (EditText) findViewById(R.id.login_password);
		mForgotPasswordButton = (Button) findViewById(R.id.forgotten_password_button);
		mLicenceButton = (Button) findViewById(R.id.licence_button);
		mLoginButton = (Button) findViewById(R.id.sign_in_button);
		mRegisterButton = (Button) findViewById(R.id.register_on_login_button);
		
		mInternetUtility = InternetUtility.getInstance(this);
		jsonParser = new JSONParser();
				
		mLoginButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(mInternetUtility.isInternetConnected()) {
							
							mLoginDialog = new ProgressDialog(LoginActivity.this);
					        mLoginDialog.setMessage("Attempting login...");
							mLoginDialog.setIndeterminate(false);
							mLoginDialog.setCancelable(false);
							mLoginDialog.show();
							
							attemptLogin();
						}
						else {
							Toast.makeText(LoginActivity.this, InternetUtility.INTERNET_DISCONNECTED, Toast.LENGTH_LONG).show();
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
		
		mRegisterButton.setOnClickListener(
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
							handleForgottenPassword();
					}
				});	
	}
	
	/* Method to call the asynchronous class 'UserLoginTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptLogin() {	
		
			mEmail = mEmailView.getText().toString();
			mPassword = mPasswordView.getText().toString();
			
			if((isValidEmailAddress() && isValidPassword())) {		
				mLoginTask = new UserLoginTask(); 
				mLoginTask.execute((String) null);
				
				//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if(mLoginTask != null) {
							if(mLoginTask.getStatus() == AsyncTask.Status.RUNNING) {
								mLoginTask.cancel(true);
								mLoginDialog.cancel();
								Toast.makeText(LoginActivity.this, mConnectionTimeout, 
										Toast.LENGTH_LONG).show();
							}
						}
					}
				}	
				, 100000);
			}	
	}

	/* Method to call the asynchronous class 'VerifyEmailTask'. If call to the database takes too long then a timeout should occur.*/
	public void attemptToVerifyEmail() {
		
		if (mVerifyEmailTask != null) {
			return;
		} 	
		mVerifyEmailTask = new VerifyEmailTask();
		mVerifyEmailTask.execute((String) null);
	
		Handler handlerForUserTask = new Handler();
		handlerForUserTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mVerifyEmailTask!= null) {
					if(mVerifyEmailTask.getStatus() == AsyncTask.Status.RUNNING) {
						mVerifyEmailTask.cancel(true);
						mResetPasswordDialog.cancel();
						Toast.makeText(LoginActivity.this, mConnectionTimeout, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
	}

	/* Method to handle a situation where a participant has forgotten their password. Displays a dialog which allows a participant
	 * to enter their email address (to check its validity before resetting its associated password).*/ 
	public void handleForgottenPassword() {
		
		LayoutInflater li = LayoutInflater.from(LoginActivity.this);
		final View emailView = li.inflate(R.layout.email_address_dialog, null);
		
		Builder builder = new Builder(LoginActivity.this);
		builder.setView(emailView);
		builder.setCancelable(false);
		builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				mResetEmailAddress = (EditText) emailView.findViewById(R.id.email_address_reset_text_field);
				mSubmittedEmail = mResetEmailAddress.getText().toString();
				
				if(mInternetUtility.isInternetConnected()) {
					attemptToVerifyEmail();
				}
				else {
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
	
	/*
	 * Method that directs a user to the RegisterActivity if they wish to register with the application instead.
	 * */
	private void goToRegister() {		
		
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);	
		
		mEmailView.setText(null);
		mPasswordView.setText(null);
	}
	
	/*Method to check if the email address entered conforms with the validation rules for this variable.*/
	private boolean isValidEmailAddress() {	
		
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_email_null));
			return false;
			
		}
		//http://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address-on-android
		else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail.toString()).matches()) {
			mEmailView.setError(getString(R.string.error_email_incorrect_format));	
			return false;
		}
		
		return true;
	}
	
	/* Method to check if the password entered on screen conforms with the validation rules for this field.*/
	private boolean isValidPassword() {
		
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_password_null));
			return false;	
		}	
		return true;	
	}
	
	
	/*
	 * Method saves the currently typed email address and password when the Activity is paused.
	 * */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {	
		
		//http://developer.android.com/training/basics/activity-lifecycle/recreating.html
		savedInstanceState.putString("LOGIN_EMAIL_ADDRESS", mEmailView.getText().toString());
		savedInstanceState.putString("LOGIN_PASSWORD", mPasswordView.getText().toString());
		
		super.onSaveInstanceState(savedInstanceState);
	}

	/*
	 * Method restoring the currently typed email address and password.
	 * */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);
		
		mEmailView.setText(savedInstanceState.getString("LOGIN_EMAIL_ADDRESS"));
		mPasswordView.setText(savedInstanceState.getString("LOGIN_PASSWORD"));
	}
	
	/* This internal class attempts to log the participant into the application with the details submitted on screen. */
	public class UserLoginTask extends AsyncTask<String, String, String>{
		
		/*Method calling the database to attempt to retrieve the data associated with the details entered on screen.*/
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
				
				Log.i("Login", "starting");
				
				JSONObject jsonResult = jsonParser.makeHttpRequest(LOGIN_URL, "POST", parameters);
				Log.i("Login", jsonResult.toString());
				
				success = jsonResult.getInt(PHPHelper.SUCCESS);
				if(success == 1) {
					Log.i("Login", jsonResult.toString());
					//finish();
					mLoginSuccessful = true;
					
					sUserIdResult = jsonResult.getJSONObject(PHPHelper.RESULTS);
					mCurrentUserIdReturned = true;
					mUserId = sUserIdResult.getInt("UserId");
					mUserName = sUserIdResult.getString("Name");
					mGettingUserIdSuccessful = true;
					mHomepageActivityIntent = new Intent(LoginActivity.this, HomepageActivity.class);
					
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				else {
					Log.w("Login", jsonResult.getString(PHPHelper.MESSAGE));
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				
			} catch (JSONException e) {
				try {
					throw new JSONException(e.toString());
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
	
			return "";
		}
	
		/* Method called after the database call has been made. If the data entered on screen matches what is stored in the database
		 * for a given email address, then the participant is logged in. Else, they are notified on screen of any errors.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mLoginTask = null;
			mLoginDialog.cancel();
			
			if(mLoginSuccessful) {
				Log.i("Login", "Login was successful");

				SharedPreferences settings = getSharedPreferences("UserPreferencesFile", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("currentUserId", mUserId);
				editor.putString("currentUserName", mUserName);
				editor.commit();
				
				startActivity(mHomepageActivityIntent);
			}
			else {	
				Builder alertForFailedLogin = new Builder(LoginActivity.this);
				alertForFailedLogin.setTitle("Login unsuccessful");
				alertForFailedLogin.setMessage(fileUrl);
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
				
				Log.w("Login", "Login failure for email address: " + mEmailView.getText());
			}	
		}
	
		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			
			mLoginTask = null;
		}
	}

	/* This internal class attempts to retrieve the details associated with an email address entered on screen for the purpose
	 * of resetting that participants password. */
	public class VerifyEmailTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a retrieval of data is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mResetPasswordDialog = new ProgressDialog(LoginActivity.this);
			mResetPasswordDialog.setMessage("Attempting to reset password");
			mResetPasswordDialog.setIndeterminate(false);
			mResetPasswordDialog.setCancelable(false);
			mResetPasswordDialog.show();
		}
	
		/* Method calling the database to return the data associated with the email address supplied to reset the password.*/
		@Override
		protected String doInBackground(String... arg0) {
			
			int success;
			List<NameValuePair> parametersForUserId = new ArrayList<NameValuePair>();
			//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
			parametersForUserId.add(new BasicNameValuePair("email", mSubmittedEmail));
			
			try{
				Log.i("Login", "starting");
				JSONObject jsonResult = jsonParser.makeHttpRequest(RETRIEVE_PASSWORD_RESET_DETAILS_URL, 
						"POST", parametersForUserId);
				Log.i("Login", jsonResult.toString());
				success = jsonResult.getInt(PHPHelper.SUCCESS);
				
				if(success == 1) {
					Log.i("Login", jsonResult.toString());
					sResetEmailAddressResult = jsonResult.getJSONObject("result");
					mSecurityAnswer = sResetEmailAddressResult.getString("Answer");
					mSecurityQuestion = jsonResult.getString("securityquestion");
					mResetPasswordId = sResetEmailAddressResult.getInt("UserId");
					mExistingEmailAddress = true;
					return jsonResult.getString(PHPHelper.MESSAGE);	
				}
				else {
					Log.w("Login", jsonResult.getString(PHPHelper.MESSAGE));
					mExistingEmailAddress = false;
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
			}
			catch (JSONException e) {
				try {
					throw new JSONException(e.toString());
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			
			return null;
		}
		
		/* Method called after the database call has been made. If the email supplied is valid and exists within the database,
		 * the participant is navigated to a view to allow them to reset their password. Else, they are notified of any 
		 * errors on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mVerifyEmailTask = null;
			mResetPasswordDialog.cancel();
			
			if(mExistingEmailAddress) {
				Intent forgottenPasswordIntent = new Intent(LoginActivity.this, ForgottenPasswordActivity.class);
				forgottenPasswordIntent.putExtra("EmailAddress", mResetEmailAddress.getText().toString());
				forgottenPasswordIntent.putExtra("SecurityQuestion", mSecurityQuestion);
				forgottenPasswordIntent.putExtra("SecurityAnswer", mSecurityAnswer);
				forgottenPasswordIntent.putExtra("UserId", mResetPasswordId);
				startActivity(forgottenPasswordIntent);
			}
			else {	
				Builder incorrectEmailBuilder = new Builder(LoginActivity.this);									
				incorrectEmailBuilder.setTitle("Invalid email");
				incorrectEmailBuilder.setMessage(fileUrl);
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
	
		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mVerifyEmailTask = null;
		}
	}
}



