package com.application.treasurehunt;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import Utilities.InternetUtility;
import Utilities.JSONParser;
import Utilities.PHPHelper;
import Utilities.ValidationHelper;

import java.util.ArrayList;
import java.util.List;

/*The purpose of this Activity is to allow a participant to register with the application.
 * [See Dissertation Section 2.4.2.1]*/

public class RegisterActivity extends Activity {

	/*
	 * Global variables used within RegisterActivity.
	 */
	private static final String REGISTER_URL =  "http://lowryhosting.com/emmad/register.php";
	
	private ProgressDialog mRegisterDialog;
	
	private InternetUtility mInternetUtility;
	private ValidationHelper mValidationHelper;
	
	public JSONParser jsonParser = new JSONParser();
	private UserRegisterTask mRegisterTask = null;

	private boolean mUserSucessfullyRegistered = false;
	
	private String mEmail;
	private String mPassword;
	private String mName;
	private String mAnswer;

	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mNameView;
	private EditText mAnswerView;
	
	private Spinner mSecurityQuestionSpinner;
	
	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	private int mMinEmailLength = 10;
	private int mMaxEmailLength = 30;
	private int mMinAnswerLength = 3;
	private int mMaxAnswerLength = 30;
	private int mMinPasswordLength = 6;
	private int mMaxPasswordLength = 10;
	private int mMinNameLength = 3;
	private int mMaxNameLength = 30;
	
	/*
	 * Method called when the Activity is created (as part of the android life cycle) which sets up this Activity's variables.
	 * It also handles what happens when a participant selects the 'Save' button on screen.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Register");
		}
		
		mEmailView = (EditText) findViewById(R.id.register_email_address);
		mPasswordView = (EditText) findViewById(R.id.register_password);
		mNameView = (EditText) findViewById(R.id.register_name);
		mAnswerView = (EditText) findViewById(R.id.security_question_answer);
		
		mInternetUtility = InternetUtility.getInstance(this);
		mValidationHelper = ValidationHelper.getInstance(this);
		
		//http://developer.android.com/guide/topics/ui/controls/spinner.html
		mSecurityQuestionSpinner = (Spinner) findViewById(R.id.security_question_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.security_choices_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSecurityQuestionSpinner.setAdapter(adapter);
							
		findViewById(R.id.register_save_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(mInternetUtility.isInternetConnected()) {
							attemptRegister();
						}
						else {
							//-http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
							Toast.makeText(RegisterActivity.this, InternetUtility.INTERNET_DISCONNECTED, Toast.LENGTH_LONG).show();
						}
					}
				});
	}
	
	/*
	 * Method saves the current details that have been entered on screen when the Activity is paused.
	 * */
	//http://developer.android.com/training/basics/activity-lifecycle/recreating.html
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {	
		savedInstanceState.putString("REGISTER_EMAIL_ADDRESS", mEmailView.getText().toString());
		savedInstanceState.putString("REGISTER_NAME", mNameView.getText().toString());
		savedInstanceState.putString("REGISTER_PASSWORD", mPasswordView.getText().toString());	
		
		super.onSaveInstanceState(savedInstanceState);
	}

	/*
	 * Method restores the current details that were entered on screen when the Activity resumes.
	 * */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		mEmailView.setText(savedInstanceState.getString("REGISTER_EMAIL_ADDRESS"));
		mNameView.setText(savedInstanceState.getString("REGISTER_NAME"));
		mPasswordView.setText(savedInstanceState.getString("REGISTER_PASSWORD"));	
	}
	
	/* Method to call the asynchronous class 'UserRegisterTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptRegister() {
		
		if (mRegisterTask != null) {
			return;
		} 		
		
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mNameView.setError(null);

		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mName = mNameView.getText().toString();
		mAnswer = mAnswerView.getText().toString();
		
		if((isValidEmailAddress() && isValidPassword() && isValidName() && isValidAnswer())) {		
			mRegisterTask = new UserRegisterTask(); // Do ASYNC way
			mRegisterTask.execute((String) null);
			
			//http://stackoverflow.com/questions/7882739/android-setting-a-timeout-for-an-asynctask?rq=1
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mRegisterTask!= null) {
						if(mRegisterTask.getStatus() == AsyncTask.Status.RUNNING) {
							mRegisterTask.cancel(true);
							mRegisterDialog.cancel();
							Toast.makeText(RegisterActivity.this, mConnectionTimeout, Toast.LENGTH_LONG).show();
						}
					}
				}
			}
			, 10000);
		}
	}
	
	/* Method to check if the email address entered on screen conforms with the validation rules for this field.*/
	private boolean isValidEmailAddress() {	
		mEmail = mEmailView.getText().toString();
		if (mValidationHelper.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_email_null));
			return false;
			
		}
		else if(!mValidationHelper.isValidLength(mEmail, mMinEmailLength, mMaxEmailLength)) {
			mEmailView.setError(getString(R.string.error_email_invalid_length));	
			return false;
		}
		else if(!mValidationHelper.isValidEmailFormat(mEmail.toString())) {
			mEmailView.setError(getString(R.string.error_email_incorrect_format));	
			return false;
		}
		
		return true;	
	}
	
	/* Method to check if the security question answer entered on screen conforms with the validation rules for this field.*/
	public boolean isValidAnswer() {
		mAnswer = mAnswerView.getText().toString();
		if (mValidationHelper.isEmpty(mAnswer)) {
			mAnswerView.setError(getString(R.string.error_answer_null));
			return false;
		}
		else if(!mValidationHelper.isValidLength(mAnswer, mMinAnswerLength, mMaxAnswerLength)) {
			mAnswerView.setError(getString(R.string.error_answer_invalid_length));	
			return false;
		}
		else if(!mValidationHelper.isValidPassword(mAnswer)) {
			mAnswerView.setError(getString(R.string.error_invalid_characters));	
			return false;
		}
		return true;
	}
	
	/* Method to check if the password entered on screen conforms with the validation rules for this field.*/
	private boolean isValidPassword() {
		mPassword = mPasswordView.getText().toString();
		if (mValidationHelper.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_password_null));
			return false;
			
		}
		else if(!mValidationHelper.isValidLength(mPassword, mMinPasswordLength, mMaxPasswordLength)) {
			mPasswordView.setError(getString(R.string.error_password_invalid_length));	
			return false;
		}
		else if(!mValidationHelper.isValidPassword(mPassword)) {
			mPasswordView.setError(getString(R.string.error_invalid_characters));	
			return false;
		}
		
		return true;
	}
	
	/* Method to check if the name entered on screen conforms with the validation rules for this field.*/
	private boolean isValidName() {
		mName = mNameView.getText().toString();
		if (mValidationHelper.isEmpty(mName)) {
			mNameView.setError(getString(R.string.error_name_null));
			return false;
			
		}
		else if(!mValidationHelper.isValidLength(mName, mMinNameLength, mMaxNameLength)) {
			mNameView.setError(getString(R.string.error_name_invalid_length));	
			return false;
		}
		else if(!mValidationHelper.isValidCharacters(mName)) {
			mNameView.setError(getString(R.string.error_invalid_characters));	
			return false;
		}
		return true;	
	}
	
	/* Method to show dialog on screen if participant registration fails. 
	 * Associated with UserRegisterTask OnPostExecute() method.*/
	public void showFailedRegistrationMessage() {
		
		Builder alertForFailedRegistration = new Builder(RegisterActivity.this);
		alertForFailedRegistration.setTitle("Register");
		alertForFailedRegistration.setMessage("Could not register with the details provided. Please try again.");
		alertForFailedRegistration.setCancelable(false);
		alertForFailedRegistration.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		alertForFailedRegistration.create();
		alertForFailedRegistration.show();
		
		Log.w("Register", "Failure to register user.");
	}
	

	/* This internal class attempts to register a participant with the application using the details submitted on screen.*/
	public class UserRegisterTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant a save is being made.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mRegisterDialog = new ProgressDialog(RegisterActivity.this);
            mRegisterDialog.setMessage("Attempting to register...");
			mRegisterDialog.setIndeterminate(false);
			mRegisterDialog.setCancelable(false);
			mRegisterDialog.show();
		}
		
		/* Method calling the database to save the details submitted on screen. It will also save the user role of this 
		 * participant as a role of the same name.*/
		@Override
		public String doInBackground(String... args) {
			//http://www.mybringback.com/tutorial-series/13193/android-mysql-php-json-part-5-developing-the-android-application/
			
			int success;
			
			try {
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				parameters.add(new BasicNameValuePair("email", mEmail));
				parameters.add(new BasicNameValuePair("password", mPassword));
				parameters.add(new BasicNameValuePair("name", mName));
				parameters.add(new BasicNameValuePair("securityQuestionId", Long.toString(mSecurityQuestionSpinner.getSelectedItemId())));
				parameters.add(new BasicNameValuePair("answer", mAnswer));
				
				Log.i("Register", "starting");
				JSONObject jsonResult = jsonParser.makeHttpRequest(REGISTER_URL, "POST", parameters);
				Log.i("Register", jsonResult.toString());
				
				success = jsonResult.getInt(PHPHelper.SUCCESS);
				if(success == 1) {
					Log.i("Register", jsonResult.toString());
					mUserSucessfullyRegistered = true;
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				else {
					Log.w("Register", jsonResult.getString(PHPHelper.MESSAGE));
					return jsonResult.getString(PHPHelper.MESSAGE);
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

		/* Method called after the database call has been made. If the participant has been successfully registered,
		 * then they are notified and taken to the login screen; else, the participant is notified of an error on screen.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mRegisterTask = null;
			
			if(mUserSucessfullyRegistered) {
				
				Log.i("Register", fileUrl);
				
					
				Builder mAlertForFailedStartTimeSave = new Builder(RegisterActivity.this);
				mAlertForFailedStartTimeSave.setTitle("Success");
				mAlertForFailedStartTimeSave.setMessage("Registration was successful!");
				mAlertForFailedStartTimeSave.setCancelable(false);
				mAlertForFailedStartTimeSave.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent loginActivityIntent = new Intent(RegisterActivity.this, LoginActivity.class);
						startActivity(loginActivityIntent);
					}
				});
				
				mAlertForFailedStartTimeSave.create();
				mAlertForFailedStartTimeSave.show();		
			}
			else {
				showFailedRegistrationMessage();
			}
		}

		/* Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			mRegisterTask = null;
			mRegisterDialog.cancel();
		}
	}
}
