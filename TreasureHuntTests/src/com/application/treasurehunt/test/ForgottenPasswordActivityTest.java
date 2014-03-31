package com.application.treasurehunt.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import Utilities.JSONParser;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.TextView;

import com.application.treasurehunt.ChooseCompanyActivity;
import com.application.treasurehunt.ForgottenPasswordActivity;
import com.application.treasurehunt.ForgottenPasswordActivity.SaveNewPasswordTask;
import com.application.treasurehunt.LoginActivity;
import com.application.treasurehunt.ChooseCompanyActivity.ReturnCompaniesTask;
import com.application.treasurehunt.LoginActivity.UserLoginTask;

public class ForgottenPasswordActivityTest extends ActivityInstrumentationTestCase2<ForgottenPasswordActivity> {
	
	//PROBLEM WITH THE TESTING OF THE VALIDATION CONDITIONS
	
	//The classes to be tested
	ForgottenPasswordActivity mForgottenPasswordActivity;
	ForgottenPasswordActivity.SaveNewPasswordTask mSavePasswordTask;
	
	private EditText mAnswerView;
	private EditText mNewPasswordView;
	
	private String mAnswer;
	private String mNewPassword;
	
	Class mForgottenPasswordActivityClass;
	Class mSavePasswordTaskClass;
	
	Method mIsValidAnswerMethod;
	Method mIsValidNewPasswordMethod;
	
	Object mIsValidAnswerMethodObject;
	Object mIsValidNewPasswordMethodObject;
	
	Field mExistingAnswerField;
	Field mSavePasswordSuccessField;
	
	Method doInBackgroundMethod;
	Object doInBackgroundMethodObject;
	
	//The JSON parser to be mocked
	@Mock private JSONParser jsonParserMock;

	public ForgottenPasswordActivityTest(
			Class<ForgottenPasswordActivity> activityClass) {
		super(activityClass);
	}
	
	public ForgottenPasswordActivityTest() {
		super(ForgottenPasswordActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		
		//Grabbing the classes
		mForgottenPasswordActivity = getActivity();
		mSavePasswordTask = mForgottenPasswordActivity.new SaveNewPasswordTask();
		
		mForgottenPasswordActivityClass = ForgottenPasswordActivity.class;
		mSavePasswordTaskClass = SaveNewPasswordTask.class;
		
		//Setting up the mock
		jsonParserMock = Mockito.mock(JSONParser.class);
		mForgottenPasswordActivity.jsonParser = jsonParserMock;
		
		mAnswerView = (EditText) mForgottenPasswordActivity.
				findViewById(com.application.treasurehunt.R.id.security_answer);
		mNewPasswordView = (EditText) mForgottenPasswordActivity.
				findViewById(com.application.treasurehunt.R.id.new_password);
		
		mIsValidAnswerMethod = mForgottenPasswordActivityClass.getDeclaredMethod("isValidAnswer", null);
		mIsValidAnswerMethod.setAccessible(true);
					
		mIsValidNewPasswordMethod = mForgottenPasswordActivityClass.getDeclaredMethod("isValidPassword", null);
		mIsValidNewPasswordMethod.setAccessible(true);
		
		//http://stackoverflow.com/questions/1555658/is-it-possible-in-java-to-access-private-fields-via-reflection
		mExistingAnswerField = ForgottenPasswordActivity.class.getDeclaredField("mSecurityAnswer");
		mExistingAnswerField.setAccessible(true);
		
		mSavePasswordSuccessField = ForgottenPasswordActivity.class.getDeclaredField("mSetNewPasswordSuccess");
		mSavePasswordSuccessField.setAccessible(true);

		//http://stackoverflow.com/questions/9694282/activityunittestcase-and-activityrunonuithread
		
		//doInBackgroundMethod = mSavePasswordTaskClass.getDeclaredMethod("doInBackground", null);
		//doInBackgroundMethod.setAccessible(true);
	}
	
	public void testPreconditions() {
		mAnswer = mAnswerView.getText().toString();
		mNewPassword = mNewPasswordView.getText().toString();
		
		assertNotNull("mForgottenPasswordActivity is null", mForgottenPasswordActivity);
		assertEquals("mPassword is empty","", mNewPassword);
		assertEquals("mAnswer is empty", "", mAnswer);
	}
	
	public void testPasswordSavedIfValidDetails() throws JSONException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {	
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully saved new password!";
		 
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		//Want to use reflection here! Not happy about making 'doInBackground' public.
		try {
			mSavePasswordTask.doInBackground("");
			//doInBackgroundMethodObject = doInBackgroundMethod.invoke(mForgottenPasswordActivity, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());
		
		
		Object actualResult = mSavePasswordSuccessField.get(mForgottenPasswordActivity);
		assertEquals(true, actualResult);
	}
	
	public void testAnswerInvalidIfEmpty() {
		
		mForgottenPasswordActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
				//http://blog.octo.com/en/android-testing-testing-private-methods/
				//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
				
				try {
					//OR IS IT .invoke(mLoginActivityClass)????
					mIsValidAnswerMethodObject = mIsValidAnswerMethod.invoke(mForgottenPasswordActivity, null);
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("You must enter an answer", false, mIsValidAnswerMethodObject);
				
				Object actualResult = new Object();
				try {
					actualResult = mSavePasswordSuccessField.get(mForgottenPasswordActivity);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				assertEquals(false, actualResult);
			}
		});	
	}
	
	public void testAnswerInvalidIfDoesNotMatch() throws IllegalArgumentException, IllegalAccessException {
		
		mExistingAnswerField.set(mForgottenPasswordActivity, "answer");
		mForgottenPasswordActivity.runOnUiThread(new Runnable() {
			public void run() {
				//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
				//http://blog.octo.com/en/android-testing-testing-private-methods/
				//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html	
				mAnswerView.setText("answeranswer");
				try {
					
					//OR IS IT .invoke(mLoginActivityClass)????
					mIsValidAnswerMethodObject = mIsValidAnswerMethod.invoke(mForgottenPasswordActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("You must enter an answer", false, mIsValidAnswerMethodObject);  
			}
		});	
	}
	
	public void testPasswordInvalidIfNullOrEmpty() {
		
		mForgottenPasswordActivity.runOnUiThread(new Runnable() {
			public void run() {
				//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
				//http://blog.octo.com/en/android-testing-testing-private-methods/
				//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
				mNewPasswordView.setText(null);
				
				try {
					//OR IS IT .invoke(mLoginActivityClass)????
					mIsValidNewPasswordMethodObject = mIsValidNewPasswordMethod.invoke(mForgottenPasswordActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Please enter your password", false, mIsValidNewPasswordMethodObject);  
			}
		});	
	}
	
	public void testPasswordInvalidIfLengthTooShort() {
		
		mForgottenPasswordActivity.runOnUiThread(new Runnable() {
			public void run() {
				//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
				//http://blog.octo.com/en/android-testing-testing-private-methods/
				//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
				mNewPasswordView.setText("pa");
				
				try {
					//OR IS IT .invoke(mLoginActivityClass)????
					mIsValidNewPasswordMethodObject = mIsValidNewPasswordMethod.invoke(mForgottenPasswordActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Length must be between 6 and 10 characters", false, mIsValidNewPasswordMethodObject); 			
			}
		});	
	}
	
	public void testPasswordInvalidIfLengthTooLong() {

		mForgottenPasswordActivity.runOnUiThread(new Runnable() {
			public void run() {
				//http://www.xyzws.com/Javafaq/how-to-use-reflection-to-call-methods-in-java/153
				//http://blog.octo.com/en/android-testing-testing-private-methods/
				//http://geekyouup.blogspot.co.uk/2010/01/android-app-optimization-using.html
				mNewPasswordView.setText("passwordpassword");
				
				try {
					mIsValidNewPasswordMethodObject = mIsValidNewPasswordMethod.invoke(mForgottenPasswordActivity, null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				assertEquals("Length must be between 6 and 10 characters", false, mIsValidNewPasswordMethodObject);
			}
		});	
	}
}
