package com.application.treasurehunt.test;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.application.treasurehunt.LoginActivity;
import com.application.treasurehunt.RegisterActivity;
import com.application.treasurehunt.LoginActivity.UserLoginTask;
import com.application.treasurehunt.RegisterActivity.UserRegisterTask;

import Utilities.JSONParser;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.TextView;

public class RegisterActivityTest extends ActivityInstrumentationTestCase2<RegisterActivity>{

	private RegisterActivity mRegisterActivity;
	private TextView mNameView;
	private TextView mEmailView;
	private TextView mPasswordView;
	private Button mSaveButton;
	private String mName;
	private String mEmail;
	private String mPassword;
	
	@Mock private JSONParser jsonParserMock;
	
	Class mRegisterActivityClass;
	Class mUserRegisterTaskClass;
	
	Method isValidEmailMethod;
	Method isValidPasswordMethod;
	Method isValidNameMethod;
	
	Method doInBackgroundMethod;
	
	Object isValidEmailMethodObject;
	Object isValidPasswordMethodObject;
	Object isValidNameMethodObject;
	Object doInBackgroundMethodObject;
	
	RegisterActivity.UserRegisterTask registerTask;
	
	public RegisterActivityTest(Class<RegisterActivity> name) {
		super(name);
	}
	
	public RegisterActivityTest()
	{
		super(RegisterActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		
		mRegisterActivity = getActivity();
		mNameView = (TextView) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_name);
		mEmailView = (TextView) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_email_address);
		mPasswordView = (TextView) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_password);
		mSaveButton = (Button) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_save_button);
		
		registerTask = mRegisterActivity.new UserRegisterTask();
		
		jsonParserMock = Mockito.mock(JSONParser.class);
		mRegisterActivity.jsonParser = jsonParserMock;
		
		//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
		//http://blog.octo.com/en/android-testing-testing-private-methods/
		//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
		mRegisterActivityClass = RegisterActivity.class;
		mUserRegisterTaskClass = UserRegisterTask.class;
		
		isValidEmailMethod = mRegisterActivityClass.getDeclaredMethod("isValidEmailAddress", null);
		isValidEmailMethod.setAccessible(true);
					
		isValidPasswordMethod = mRegisterActivityClass.getDeclaredMethod("isValidPassword", null);
		isValidPasswordMethod.setAccessible(true);
		
		isValidNameMethod = mRegisterActivityClass.getDeclaredMethod("isValidName", null);
		isValidNameMethod.setAccessible(true);
		
		doInBackgroundMethod = mUserRegisterTaskClass.getDeclaredMethod("doInBackground", null);
		doInBackgroundMethod.setAccessible(true);
		
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mNameView.setText(null);
				mEmailView.setText(null);
				mPasswordView.setText(null);
			};
			
		});
	}
	
	@Test
	public void testPreconditions()
	{
		mName = mNameView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
			
		assertNotNull("mLoginActivity is null", mRegisterActivity);
		assertEquals("mName is empty","", mName);
		assertEquals("Email is empty", "", mEmail);
		assertEquals("mPassword is empty","", mPassword);
	}
	
	@Test
	public void nameInvalidIfEmpty()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mNameView.setText(null);
				try {
					isValidNameMethodObject = isValidNameMethod.invoke(mRegisterActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Name is empty", false, isValidNameMethodObject);
			}
			
		});
	}
	
	@Test
	public void nameInvalidIfInvalidLength()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mNameView.setText("em");

				try {
					isValidNameMethodObject = isValidNameMethod.invoke(mRegisterActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Name is invalid length", false, isValidNameMethodObject);
			}
			
		});
			
	}

	@Test
	public void emailInvalidIfEmpty()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mEmailView.setText(null);try {
					isValidEmailMethodObject = isValidEmailMethod.invoke(mRegisterActivity, null);
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
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mEmailView.setText("emma@com");try {
					isValidEmailMethodObject = isValidEmailMethod.invoke(mRegisterActivity, null);
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
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mEmailView.setText("emmadotcom");

				try {
					isValidEmailMethodObject = isValidEmailMethod.invoke(mRegisterActivity, null);
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
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mPasswordView.setText(null);

				try {
					isValidPasswordMethodObject = isValidPasswordMethod.invoke(mRegisterActivity, null);
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
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mPasswordView.setText("em");

				try {
					isValidPasswordMethodObject = isValidPasswordMethod.invoke(mRegisterActivity, null);
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

	@Test
	public void LoginActivityBeginsIfSuccessfullyLoggedIn() throws JSONException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String SUCCESSFULLY_REGISTERED = "Registration successful!";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFULLY_REGISTERED);
		fakeObject.put("success", "1");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			doInBackgroundMethodObject = doInBackgroundMethod.invoke(registerTask, null);
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
	public void RegisterFailsIfEmailAlreadyInUse() throws JSONException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String UNSUCCESSFUL_REGISTER = "This Email Address is already in use";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_REGISTER);
		fakeObject.put("success", "0");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			doInBackgroundMethodObject = doInBackgroundMethod.invoke(registerTask, null);
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
}
