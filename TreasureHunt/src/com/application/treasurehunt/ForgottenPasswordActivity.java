package com.application.treasurehunt;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.application.treasurehunt.RegisterActivity.SetUserRoleTask;

import Utilities.JSONParser;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ForgottenPasswordActivity extends Activity {
	
	String securityQuestion;
	String securityAnswer;
	int userId;
	
	TextView securityQuestionText;
	EditText securityAnswerText;
	EditText newPasswordText;
	Button submitAnswerButton;
	Button saveNewPasswordButton;
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	
	boolean setNewPasswordSuccess;
	
	private static final String saveNewPasswordUrl =  "http://lowryhosting.com/emmad/resetpassword.php";
	
	SaveNewPasswordTask saveNewPasswordTask;
	
	public JSONParser jsonParser = new JSONParser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgotten_password);
		
		Intent intent = getIntent();
		
		securityQuestion = intent.getStringExtra("SecurityQuestion");
		securityAnswer = intent.getStringExtra("SecurityAnswer");
		userId = intent.getIntExtra("UserId", 0);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Treasure Hunt");
			actionBar.setSubtitle("Reset Password");
		}
		
		securityQuestionText = (TextView) findViewById(R.id.security_question);
		securityAnswerText = (EditText) findViewById(R.id.security_answer);
		newPasswordText = (EditText) findViewById(R.id.new_password);
		submitAnswerButton = (Button) findViewById(R.id.submit_security_answer_button);
		saveNewPasswordButton = (Button) findViewById(R.id.save_new_password_button);
		
		submitAnswerButton.setEnabled(true);
		saveNewPasswordButton.setEnabled(false);
		
		securityQuestionText.setText(securityQuestion);
		
		submitAnswerButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (TextUtils.isEmpty(securityAnswerText.getText().toString())) {
							securityAnswerText.setError("You must enter an answer");
						}
						else
						{
							if(securityAnswerText.getText().toString().equals(securityAnswer))
							{
								submitAnswerButton.setEnabled(false);
								saveNewPasswordButton.setEnabled(true);
							}
							else
							{
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
								securityAnswerText.setText("");
							}
						}
					}
				});
		
		saveNewPasswordButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (isValidPassword()) {
				
							attemptSaveNewPassword();
						}
					}
				});
	}
	
	private void attemptSaveNewPassword()
	{
		if (saveNewPasswordTask != null) {
			return;
		} 	
		saveNewPasswordTask = new SaveNewPasswordTask();
		saveNewPasswordTask.execute((String) null);

		Handler handlerForUserRoleTask = new Handler();
		handlerForUserRoleTask.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(saveNewPasswordTask!= null)
				{
					if(saveNewPasswordTask.getStatus() == AsyncTask.Status.RUNNING)
					{
						saveNewPasswordTask.cancel(true);
						Toast.makeText(ForgottenPasswordActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		, 10000);	
		
	}
	
	private boolean isValidPassword()
	{
		if (TextUtils.isEmpty(newPasswordText.getText().toString())) {
			newPasswordText.setError(getString(R.string.error_password_null));
			return false;
			
		}
		else if(newPasswordText.getText().toString().length() < 6 || newPasswordText.getText().toString().length() >= 10)
		{
			newPasswordText.setError(getString(R.string.error_password_too_short));	
			return false;
		}
		
		return true;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.forgotten_password, menu);
		return true;
	}
	
	public class SaveNewPasswordTask extends AsyncTask<String, String, String> {
		
		@Override
		protected String doInBackground(String... arg0) {
			
			int success;
			//GETTING THE USER ID
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
			parameters.add(new BasicNameValuePair("userid", Integer.toString(userId)));
			parameters.add(new BasicNameValuePair("password", newPasswordText.getText().toString()));

			try
			{
				Log.d("request", "starting");
				JSONObject jsonSetNewPassword = jsonParser.makeHttpRequest(saveNewPasswordUrl, "POST", parameters);
				Log.d("Set new password attempt", jsonSetNewPassword.toString());
				
				success = jsonSetNewPassword.getInt(tagSuccess);
				
				if(success == 1)
				{
					Log.d("Setting user role was successful!", jsonSetNewPassword.getString(tagMessage));
					setNewPasswordSuccess = true;
					return jsonSetNewPassword.getString(tagMessage);
				}
				else
				{
					setNewPasswordSuccess = false;
					Log.d("Setting user role failed!", jsonSetNewPassword.getString(tagMessage));
					return jsonSetNewPassword.getString(tagMessage);
				}
			}
			catch (JSONException e) 
			{
				
			}
			
			
			return null;
		}
		
		@Override
		protected void onPostExecute(final String fileUrl) {
			saveNewPasswordTask = null;
			
			if (fileUrl != null) {
				if(setNewPasswordSuccess)
				{
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
				Toast.makeText(ForgottenPasswordActivity.this, "Couldn't set the new password", Toast.LENGTH_LONG).show();
			}
		

		}
		
		@Override
		protected void onCancelled() {
			saveNewPasswordTask = null;
		}
	}

}
