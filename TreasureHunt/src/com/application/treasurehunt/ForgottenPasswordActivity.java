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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
 * The purpose of this Activity is to allow a participant to reset their login password should they forget it.
 * [See dissertation section 2.4.2.10]
 * */

public class ForgottenPasswordActivity extends Activity {
	
	/*
	 * Global variables used within ForgottenPasswordActivity.
	 */
	
	private static final String SAVE_NEW_PASSWORD_URL =  "http://lowryhosting.com/emmad/resetpassword.php";
	
	private SaveNewPasswordTask mSaveNewPasswordTask;
	public JSONParser jsonParser = new JSONParser();
	
	private String mSecurityQuestion;
	private String mSecurityAnswer;
	private ProgressDialog mSavePasswordDialog; 
	
	private TextView mSecurityQuestionText;
	private EditText mSecurityAnswerText;
	private EditText mNewPasswordText;
	private Button mSubmitAnswerButton;
	private Button mSaveNewPasswordButton;
	
	private InternetUtility mInternetUtility;
	private String mConnectionTimeout = "Connection timeout. Please try again.";
	
	boolean mSetNewPasswordSuccess;
	private int mUserId;

	/*
	 * Method called when the Activity is created (as part of the Android Life Cycle) which sets up this Activity's variables.
	 * It also handles what happens when the on screen buttons are pressed.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgotten_password);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Reset Password");
		}
		
		Intent intent = getIntent();
		
		mSecurityQuestionText = (TextView) findViewById(R.id.security_question);
		mSecurityAnswerText = (EditText) findViewById(R.id.security_answer);
		mNewPasswordText = (EditText) findViewById(R.id.new_password);
		mSubmitAnswerButton = (Button) findViewById(R.id.submit_security_answer_button);
		mSaveNewPasswordButton = (Button) findViewById(R.id.save_new_password_button);
		
		mSubmitAnswerButton.setEnabled(true);
		mSaveNewPasswordButton.setEnabled(false);
		
		mSecurityQuestionText.setText(mSecurityQuestion);
		
		mInternetUtility = InternetUtility.getInstance(this);
		
		mSecurityQuestion = intent.getStringExtra("SecurityQuestion");
		mSecurityAnswer = intent.getStringExtra("SecurityAnswer");
		mUserId = intent.getIntExtra("UserId", 0);
		
		mSubmitAnswerButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						submitAnswer();
					}
				});
		
		mSaveNewPasswordButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (isValidPassword()) {
				
							if(mInternetUtility.isInternetConnected()) {
								attemptSaveNewPassword();
							}
							else {
								Toast.makeText(ForgottenPasswordActivity.this, InternetUtility.INTERNET_DISCONNECTED, 
										Toast.LENGTH_LONG).show();
							}
						}
					}
				});
	}
	
	/* Method to call the asynchronous class 'SaveNewPasswordTask'. If call to the database takes too long then a timeout should occur.*/
	private void attemptSaveNewPassword() {
		
		if (mSaveNewPasswordTask != null) {
			return;
		} 	
		mSaveNewPasswordTask = new SaveNewPasswordTask();
		mSaveNewPasswordTask.execute((String) null);

		Handler handlerForUserRoleTask = new Handler();
		handlerForUserRoleTask.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mSaveNewPasswordTask!= null) {
					if(mSaveNewPasswordTask.getStatus() == AsyncTask.Status.RUNNING) {
						mSavePasswordDialog.cancel();
						mSaveNewPasswordTask.cancel(true);
						Toast.makeText(ForgottenPasswordActivity.this, mConnectionTimeout, 
								Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);		
	}
	
	/* Method that handles submission of a participant's security question in comparison to their answer stored in the database.
	 * If successful, they are allowed to enter a new password. Else, they are notified of their error on screen.*/
	public void submitAnswer() {
		if (TextUtils.isEmpty(mSecurityAnswerText.getText().toString())) {
			
			mSecurityAnswerText.setError("You must enter an answer");
		}
		else {				
			
			if(mSecurityAnswerText.getText().toString().equals(mSecurityAnswer)) {
				
				mSubmitAnswerButton.setEnabled(false);
				mSaveNewPasswordButton.setEnabled(true);
			}
			else {
				
				Builder alertForIncorrectAnswer = new Builder(ForgottenPasswordActivity.this);
				alertForIncorrectAnswer.setTitle("Incorrect answer");
				alertForIncorrectAnswer.setMessage("Your answer did not match our records!");
				alertForIncorrectAnswer.setCancelable(false);
				alertForIncorrectAnswer.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();					
					}
				});
				
				alertForIncorrectAnswer.create();
				alertForIncorrectAnswer.show();
				mSecurityAnswerText.setText("");
			}
		}
	}
	
	/* Method to check if the new password (to be saved) conforms with the validation rules for participant passwords.*/
	private boolean isValidPassword() {
		
		if (TextUtils.isEmpty(mNewPasswordText.getText().toString())) {
			mNewPasswordText.setError(getString(R.string.error_password_null));
			return false;
			
		}
		else if(mNewPasswordText.getText().toString().length() < 6	
				|| mNewPasswordText.getText().toString().length() >= 10) {
			mNewPasswordText.setError(getString(R.string.error_password_too_short));	
			return false;
		}
		
		return true;	
	}
	
	/* Method to display dialog if password could not be reset.
	 * Associated with SaveNewPasswordTask OnPostExecute() method. */
	public void showFailedSaveMessage() {
		
		Builder alertForPasswordNotSaved = new Builder(ForgottenPasswordActivity.this);
		alertForPasswordNotSaved.setTitle("Password reset");
		alertForPasswordNotSaved.setMessage("Your password could not be reset. Please try again.");
		alertForPasswordNotSaved.setCancelable(false);
		alertForPasswordNotSaved.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();	
			}
		});
		
		alertForPasswordNotSaved.create();
		alertForPasswordNotSaved.show();
		
		Log.w("PasswordReset", "Password for user identified by id " +  mUserId + " could not be reset.");
	}
	
	/* This internal class attempts to save into the database the new password for the given participant. */
	public class SaveNewPasswordTask extends AsyncTask<String, String, String> {
		
		/* A dialog will appear on screen to show the participant the save is in progress.*/
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			mSavePasswordDialog = new ProgressDialog(ForgottenPasswordActivity.this);
	        mSavePasswordDialog.setMessage("Attempting to save new password");
			mSavePasswordDialog.setIndeterminate(false);
			mSavePasswordDialog.setCancelable(false);
			mSavePasswordDialog.show();
		}
		
		/* Method calling the database to save the new password.*/
		@Override
		protected String doInBackground(String... arg0) {
			
			int success;

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
			parameters.add(new BasicNameValuePair("userid", Integer.toString(mUserId)));
			parameters.add(new BasicNameValuePair("password", mNewPasswordText.getText().toString()));

			try {
				Log.i("PasswordReset", "starting");
				JSONObject jsonResult = jsonParser.makeHttpRequest(SAVE_NEW_PASSWORD_URL, "POST", parameters);
				Log.i("PasswordReset", jsonResult.toString());
				
				success = jsonResult.getInt(PHPHelper.SUCCESS);
				
				if(success == 1) {
					Log.i("PasswordReset", jsonResult.getString(PHPHelper.MESSAGE));
					mSetNewPasswordSuccess = true;
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
				else {
					mSetNewPasswordSuccess = false;
					Log.w("PasswordReset", jsonResult.getString(PHPHelper.MESSAGE));
					return jsonResult.getString(PHPHelper.MESSAGE);
				}
			}
			catch (JSONException e)  {
				try {
					throw new JSONException(e.toString());
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			
			return null;
		}
		
		/* Method called after the database call has been made. Whatever the out come of the save (either
		 * successful or failed) the participant is notified on screen via a dialog.*/
		@Override
		protected void onPostExecute(final String fileUrl) {
			
			mSavePasswordDialog.dismiss();
			mSaveNewPasswordTask = null;
			
			if (fileUrl != null) {
				if(mSetNewPasswordSuccess) {
					Builder alertForResetPassword = new Builder(ForgottenPasswordActivity.this);
					alertForResetPassword.setTitle("Password reset");
					alertForResetPassword.setMessage("Your password has been reset.");
					alertForResetPassword.setCancelable(false);
					alertForResetPassword.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ForgottenPasswordActivity.this.finish();					
						}
					});
					
					alertForResetPassword.create();
					alertForResetPassword.show();
					
				}
				
			} else {
				showFailedSaveMessage();
			}
		}
		
		/*Method to cancel the current task.*/
		@Override
		protected void onCancelled() {
			
			mSaveNewPasswordTask = null;
		}
	}
}
