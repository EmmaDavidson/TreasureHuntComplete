package com.application.treasurehunt.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
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

	//HAVING A PROBLEM WITH THE VALIDATION TESTS OF THE CLASS 
	
	private RegisterActivity mRegisterActivity;
	private TextView mNameView;
	private TextView mEmailView;
	private TextView mPasswordView;
	private TextView mAnswerView;
	private Button mSaveButton;
	private String mName;
	private String mEmail;
	private String mPassword;
	private String mAnswer;
	
	@Mock private JSONParser jsonParserMock;
	
	Class mRegisterActivityClass;
	Class mUserRegisterTaskClass;
	
	Method isValidEmailMethod;
	Method isValidPasswordMethod;
	Method isValidAnswerMethod;
	Method isValidNameMethod;
	
	Method doInBackgroundMethod;
	
	Object isValidEmailMethodObject;
	Object isValidPasswordMethodObject;
	Object isValidNameMethodObject;
	Object isValidAnswerMethodObject;
	Object doInBackgroundMethodObject;
	
	Field mUserSucessfullyRegisteredField;
	
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
		
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath() );		
		
		mRegisterActivity = getActivity();
		mNameView = (TextView) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_name);
		mEmailView = (TextView) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_email_address);
		mPasswordView = (TextView) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_password);
		mSaveButton = (Button) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.register_save_button);
		mAnswerView = (TextView) mRegisterActivity.findViewById(com.application.treasurehunt.R.id.security_question_answer);
		
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
		
		isValidAnswerMethod = mRegisterActivityClass.getDeclaredMethod("isValidAnswer", null);
		isValidAnswerMethod.setAccessible(true);

		mUserSucessfullyRegisteredField = RegisterActivity.class.getDeclaredField("mUserSucessfullyRegistered");
		mUserSucessfullyRegisteredField.setAccessible(true);
	}

	public void testPreconditions()
	{
		mName = mNameView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mAnswer = mAnswerView.getText().toString();
			
		assertNotNull("mLoginActivity is null", mRegisterActivity);
		assertEquals("mName is empty","", mName);
		assertEquals("Email is empty", "", mEmail);
		assertEquals("mPassword is empty","", mPassword);
		assertEquals("mAnswer is empty", "", mAnswer);
	}
	
	public void testNameInvalidIfEmpty()
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
	
	public void testNameInvalidIfTooShort()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mNameView.setText("e");

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
	
	public void testNameInvalidIfTooLong()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mNameView.setText("emmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");

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
	
	public void testNameInvalidIfInvalidCharacters() {
		
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mNameView.setText("emma!");

				try {
					isValidNameMethodObject = isValidNameMethod.invoke(mRegisterActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Name has invalid characters", false, isValidNameMethodObject);
			}
			
		});	
	}
	
	public void testEmailInvalidIfEmpty()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mEmailView.setText(null);
				try {
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
	
	public void testEmailInvalidIfTooShort()
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
	
	public void testEmailInvalidIfTooLong()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mEmailView.setText("emmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmma@com");try {
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
	
	public void testEmailInvalidIfIncorrectFormat()
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
	
	public void testPasswordInvalidIfEmpty()
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
	
	public void testPasswordInvalidIfTooShort()
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
	
	public void testPasswordInvalidIfTooLong()
	{
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mPasswordView.setText("emmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");

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

	public void testPasswordInvalidIfInvalidFormat () {
		
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mPasswordView.setText("password!");

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
	
	public void testAnswerInvalidIfEmpty() {
		
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mAnswerView.setText(null);

				try {
					isValidAnswerMethodObject = isValidAnswerMethod.invoke(mRegisterActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Answer is null", false, isValidAnswerMethodObject);
			}
		});
	}
	
	public void testAnswerInvalidIfInvalidFormat() {
	
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mAnswerView.setText("answer!");

				try {
					isValidAnswerMethodObject = isValidAnswerMethod.invoke(mRegisterActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Answer has invalid characters", false, isValidAnswerMethodObject);
			}
		});
	}
	
	public void testAnswerInvalidIfTooLong() {
		
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mAnswerView.setText("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

				try {
					isValidAnswerMethodObject = isValidAnswerMethod.invoke(mRegisterActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Answer is too long", false, isValidAnswerMethodObject);
			}
		});
	}
	
	public void testAnswerInvalidIfTooShort() {
		
		mRegisterActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				mAnswerView.setText("a");	
				
				try {
					isValidAnswerMethodObject = isValidAnswerMethod.invoke(mRegisterActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				assertEquals("Answer is too short", false, isValidAnswerMethodObject);
			}
		});
			
		
	}
	
	
	public void testSuccessfulRegistrationDatabaseCallMade() throws JSONException, IllegalAccessException, IllegalArgumentException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String SUCCESSFULLY_REGISTERED = "Registration successful!";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFULLY_REGISTERED);
		fakeObject.put("success", "1");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {

			registerTask.doInBackground();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());		

		Object actualResult = mUserSucessfullyRegisteredField.get(mRegisterActivity);
		assertEquals(true, actualResult);	
	}
	
	public void testRegisterFailsIfEmailAlreadyInUse() throws JSONException, IllegalAccessException, IllegalArgumentException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String UNSUCCESSFUL_REGISTER = "This Email Address is already in use";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_REGISTER);
		fakeObject.put("success", "0");
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			registerTask.doInBackground();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());
		
		Object actualResult = mUserSucessfullyRegisteredField.get(mRegisterActivity);
		assertEquals(false, actualResult);
	}
}
