package com.application.treasurehunt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Matchers;
import org.mockito.runners.*;
import org.robolectric.Robolectric;

import Utilities.JSONParser;
import android.R;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.application.treasurehunt.ChooseCompanyActivity;
import com.application.treasurehunt.ChooseHuntActivity;
import com.application.treasurehunt.LoginActivity;
import com.application.treasurehunt.LoginActivity.UserLoginTask;
import com.application.treasurehunt.RegisterActivity;
import com.application.treasurehunt.R.id;
//http://developer.android.com/training/activity-testing/activity-basic-testing.html

/*
 * The following parts of LoginActivity are tested:
 * Validation on both the email address and password using reflection
 * RegisterActivity is started when button pressed
 * JSON request made to database when login button pressed
 * 		- issues here include not being able to check if a new activity has started 
 * 		- see comments below for the appropriate tests
 */

public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

	private LoginActivity mLoginActivity;
	private LoginActivity.UserLoginTask mLoginTask;
	private LoginActivity.VerifyEmailTask mVerifyEmailTask;
	
	private TextView mEmailView;
	private TextView mPasswordView;
	private Button mRegisterButton;
	private Button mLoginButton;
	private String mEmail;
	private String mPassword;
	
	@Mock private JSONParser jsonParserMock;
	
	Class mLoginActivityClass;
	Class mUserLoginTaskClass;
	
	Method isValidEmailMethod;
	Method isValidPasswordMethod;
	Method doInBackgroundMethod;
	
	Object isValidEmailMethodObject;
	Object isValidPasswordMethodObject;
	Object doInBackgroundMethodObject;
	
	Field mLoginSuccessfulField;
	Field mExistingEmailField;
	
	//http://stackoverflow.com/questions/9053864/no-enclosing-instance-of-type-error-while-calling-method-from-another-class-in
		
	public LoginActivityTest(Class<LoginActivity> name) {
		super(name);
	}
	
	public LoginActivityTest()
	{
		super(LoginActivity.class);
	}

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath() );		
		mLoginActivity = getActivity();
	
		mEmailView = (TextView) mLoginActivity.findViewById(com.application.treasurehunt.R.id.login_email_address);
		mPasswordView = (TextView) mLoginActivity.findViewById(com.application.treasurehunt.R.id.login_password);
		mRegisterButton = (Button) mLoginActivity.findViewById(com.application.treasurehunt.R.id.register_on_login_button);
		mLoginButton = (Button) mLoginActivity.findViewById(com.application.treasurehunt.R.id.sign_in_button);
		
		mLoginTask = mLoginActivity.new UserLoginTask();
		mVerifyEmailTask = mLoginActivity.new VerifyEmailTask();
		
		jsonParserMock = Mockito.mock(JSONParser.class);
		mLoginActivity.jsonParser = jsonParserMock;
		
		//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
		//http://blog.octo.com/en/android-testing-testing-private-methods/
		//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
		mLoginActivityClass = LoginActivity.class;
		mUserLoginTaskClass = UserLoginTask.class;
		
		isValidEmailMethod = mLoginActivityClass.getDeclaredMethod("isValidEmailAddress", null);
		isValidEmailMethod.setAccessible(true);
					
		isValidPasswordMethod = mLoginActivityClass.getDeclaredMethod("isValidPassword", null);
		isValidPasswordMethod.setAccessible(true);
		
		mLoginSuccessfulField = LoginActivity.class.getDeclaredField("mLoginSuccessful");
		mLoginSuccessfulField.setAccessible(true);
		
		mExistingEmailField = LoginActivity.class.getDeclaredField("mExistingEmailAddress");
		mExistingEmailField.setAccessible(true);
	
	}
	
	public void testPreconditions() {
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		
		assertNotNull("mLoginActivity is null", mLoginActivity);
		assertEquals("mPassword is empty","", mPassword);
		assertEquals("Email is empty", "", mEmail);
	}
	
	public void testEmailInvalidIfEmpty()
	{	
		mLoginActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
				//http://blog.octo.com/en/android-testing-testing-private-methods/
				//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
				
				try {
					//OR IS IT .invoke(mLoginActivityClass)????
					isValidEmailMethodObject = isValidEmailMethod.invoke(mLoginActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Please enter an email address", false, isValidEmailMethodObject);  
			}
		});
	}
	
	public void testEmailInvalidIfIncorrectFormat() {
		
		mLoginActivity.runOnUiThread(new Runnable() {
			public void run() {
				mEmailView.setText("emmadotcom");
				
				try {
					isValidEmailMethodObject = isValidEmailMethod.invoke(mLoginActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Email is invalid format", false, isValidEmailMethodObject);
			}	
		});
	}
	
	public void testPasswordInvalidIfEmpty()
	{
		mLoginActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mPasswordView.setText(null);
				
				try {
					isValidPasswordMethodObject = isValidPasswordMethod.invoke(mLoginActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Password is empty", false, isValidPasswordMethodObject);
			}
		});
	}

	public void testLoginSuccessfulIfValidDetails() throws JSONException, 
						IllegalArgumentException, IllegalAccessException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String SUCCESSFUL_LOGIN = "Successful login";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_LOGIN);
		fakeObject.put("success", "1");
		
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mLoginTask.doInBackground("");
		} catch (IllegalArgumentException e) {
		e.printStackTrace();
		} 
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());
		
		Object actualResult = mLoginSuccessfulField.get(mLoginActivity);
		assertEquals(true, actualResult);	
	}
	
	public void testLoginFailsIfIncorrectDetailsOrUserDoesNotExist() throws JSONException, 
					IllegalArgumentException, IllegalAccessException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String UNSUCCESSFUL_LOGIN = "Login details incorrect.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_LOGIN);
		fakeObject.put("success", "0");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mLoginTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());
			
		Object actualResult = mLoginSuccessfulField.get(mLoginActivity);
		assertEquals(false, actualResult);	
	}

	public void testResetPasswordSuccessfulIfValidEmailAddress() throws JSONException, IllegalArgumentException, IllegalAccessException{
		
		String SUCCESSFUL_RESET = "Valid email address retrieved";
		
		JSONObject array = new JSONObject();
		array.put("email", "email");
		array.put("UserId", 1);
		array.put("securityquestion", "question");
		array.put("Answer", "answer");
		
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_RESET);
		fakeObject.put("success", "1");
		fakeObject.put("result", array);
		fakeObject.put("securityquestion", "question");
		
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mVerifyEmailTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());
			
		boolean actualResult = mExistingEmailField.getBoolean(mLoginActivity);
		assertEquals(true, actualResult);	
	
	}
	
	public void testResetPasswordFailsIfInvalidEmailAddress() throws JSONException, IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String UNSUCCESSFUL_RESET = "User invalid.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_RESET);
		fakeObject.put("success", "0");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mVerifyEmailTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());
			
		Object actualResult = mExistingEmailField.get(mLoginActivity);
		assertEquals(false, actualResult);	
}
	
}

