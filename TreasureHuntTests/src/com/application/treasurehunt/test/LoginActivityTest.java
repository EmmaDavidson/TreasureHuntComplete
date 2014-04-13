package com.application.treasurehunt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
	
	//http://stackoverflow.com/questions/9053864/no-enclosing-instance-of-type-error-while-calling-method-from-another-class-in
		
	LoginActivity.UserLoginTask loginTask;
	
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
		mLoginActivity = getActivity();
	
		mEmailView = (TextView) mLoginActivity.findViewById(com.application.treasurehunt.R.id.login_email_address);
		mPasswordView = (TextView) mLoginActivity.findViewById(com.application.treasurehunt.R.id.login_password);
		mRegisterButton = (Button) mLoginActivity.findViewById(com.application.treasurehunt.R.id.register_on_login_button);
		mLoginButton = (Button) mLoginActivity.findViewById(com.application.treasurehunt.R.id.sign_in_button);
		
		loginTask = mLoginActivity.new UserLoginTask();
		
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
		
		doInBackgroundMethod = mUserLoginTaskClass.getDeclaredMethod("doInBackground", null);
		doInBackgroundMethod.setAccessible(true);
		
		//http://stackoverflow.com/questions/9694282/activityunittestcase-and-activityrunonuithread
		mLoginActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mEmailView.setText(null);
				mPasswordView.setText(null);
			};
			
		});
	}
	
	@Test
	public void testPreconditions()
	{
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		
		assertNotNull("mLoginActivity is null", mLoginActivity);
		assertEquals("mPassword is empty","", mPassword);
		assertEquals("Email is empty", "", mEmail);
	}
	
	@Test
	public void emailInvalidIfEmpty()
	{	
		mLoginActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
				//http://blog.octo.com/en/android-testing-testing-private-methods/
				//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
				mEmailView.setText(null);
				
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
				
				assertEquals("Email is empty", false, isValidEmailMethodObject);  
			}
		});
	}
	
	@Test
	public void emailInvalidIfInvalidLength()
	{
		mLoginActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mEmailView.setText("emma@com");
				
				try {
					isValidEmailMethodObject = isValidEmailMethod.invoke(mLoginActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Email is invalid length", false, isValidEmailMethodObject);
			}
		});
		
	}
	
	@Test
	public void emailInvalidIfIncorrectFormat()
	{
		mLoginActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
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
	
	@Test
	public void passwordInvalidIfEmpty()
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
	
	@Test
	public void passwordInvalidIfInvalidLength()
	{
		mLoginActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mPasswordView.setText("em");
				
				try {
					isValidPasswordMethodObject = isValidPasswordMethod.invoke(mLoginActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Password is invalid length", false, isValidPasswordMethodObject);
			}
		});
			
	}

	//http://stackoverflow.com/questions/9405561/test-if-a-button-starts-a-new-activity-in-android-junit-pref-without-robotium
	@Test
	public void registerActivityShouldStartIfRegisterButtonPressed()
	{
    	ActivityMonitor activityMonitor = getInstrumentation().addMonitor(RegisterActivity.class.getName(), null, false);
		
		mLoginActivity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mRegisterButton.performClick();
			}
			
		});
		
		RegisterActivity nextActivity = (RegisterActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);
		assertNotNull(nextActivity);
		nextActivity.finish();
	}
	
	//I wanted it to test that an activity was started afterwards but issues (See below)
	@Test
	public void chooseHuntActivityBeginsIfSuccessfullyLoggedIn() throws JSONException
	{	
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String SUCCESSFULLY_LOGGED_IN = "Successfully logged in!";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFULLY_LOGGED_IN);
		fakeObject.put("success", "1");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			doInBackgroundMethodObject = doInBackgroundMethod.invoke(loginTask, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());		
			
		//test here to see if the activity has changed
		//Problem is, the test is running on the wrong thread - despite the screen appearing
		//UI thread?
		//Should be acceptance tested instead?
	}
	
	@Test
	public void loginFailsIfIncorrectDetails() throws JSONException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String UNSUCCESSFUL_LOGIN = "Login details incorrect.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_LOGIN);
		fakeObject.put("success", "0");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			doInBackgroundMethodObject = doInBackgroundMethod.invoke(loginTask, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}	
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());
		
		//test here to see if the activity has not changed
		//Problem is, the test is running on the wrong thread - despite the screen appearing on the device...its not checking the ui thread
		//UI thread?
		//Should be acceptance tested instead?
		//Maybe start the activity on the postExecuteMethod and check there?		
	}
	
	@Test
	public void loginFailsIfUserDoesNotExist() throws JSONException
	{		
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String UNSUCCESSFUL_LOGIN = "Login details incorrect.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_LOGIN);
		fakeObject.put("success", "0");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			doInBackgroundMethodObject = doInBackgroundMethod.invoke(loginTask, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());
		
		//test here to see if the activity has not changed
		//Problem is, the test is running on the wrong thread - despite the screen appearing
		//UI thread?
		//Should be acceptance tested instead?
		
	}
}

