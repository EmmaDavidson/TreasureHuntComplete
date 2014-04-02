package com.application.treasurehunt.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import Utilities.JSONParser;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;

import com.application.treasurehunt.ChooseCompanyActivity;
import com.application.treasurehunt.RegisterWithHuntActivity;
import com.application.treasurehunt.ChooseCompanyActivity.ReturnCompaniesTask;

public class RegisterWithHuntTest extends ActivityInstrumentationTestCase2<RegisterWithHuntActivity> {
	
	Class mRegisterWithHuntClass;
	
	Method mCheckIfHuntFinishedMethod; 
	
	Field mEndDateField;
	Field mHuntAlreadyFinishedField;
	Field mUserAlreadyRegisteredField;
	Field mHuntAlreadyStartedField;
	Field mHuntIdField;
	Field mUserIdField;
	Field sHuntParticipantIdResultField;
	Field mHuntParticipantIdReturnedField;
	Field mRegistrationSuccessfulField;
	Field sStartTimeResultField;
	Field mStartTimeField;
	
	//The classes to be tested
	RegisterWithHuntActivity mRegisterWithHuntActivity;
	RegisterWithHuntActivity.SaveStartTimeTask mSaveStartTimeTask;
	RegisterWithHuntActivity.GetParticipantIdTask mGetParticipantIdTask;
	RegisterWithHuntActivity.UserRegisterWithHuntTask mUserRegisterWithHuntTask;
	RegisterWithHuntActivity.CheckIfHuntStartedTask mCheckIfHuntStartedTask;
	RegisterWithHuntActivity.CheckIfUserRegisteredTask mCheckIfUserRegisteredTask;
	
	Object mCheckIfHuntFinishedMethodObject;
	
	//The JSON parser to be mocked
	@Mock private JSONParser jsonParserMock;
	
	public RegisterWithHuntTest(Class<RegisterWithHuntActivity> activityClass) {
		super(activityClass);
		// TODO Auto-generated constructor stub
	}
	
	public RegisterWithHuntTest() {
		super(RegisterWithHuntActivity.class);
	}
	
	@Before
	public void setUp() throws Exception {
		
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		
		//Grabbing the classes
		mRegisterWithHuntActivity = getActivity();
		mSaveStartTimeTask = mRegisterWithHuntActivity.new SaveStartTimeTask();
		mGetParticipantIdTask = mRegisterWithHuntActivity.new GetParticipantIdTask();
		mUserRegisterWithHuntTask = mRegisterWithHuntActivity.new UserRegisterWithHuntTask();
		mCheckIfHuntStartedTask = mRegisterWithHuntActivity.new CheckIfHuntStartedTask();
		mCheckIfUserRegisteredTask = mRegisterWithHuntActivity.new CheckIfUserRegisteredTask();
		
		//Setting up the mock
		jsonParserMock = Mockito.mock(JSONParser.class);
		mRegisterWithHuntActivity.jsonParser = jsonParserMock;
		
		//Using reflection to access the JSON result variable
		//http://stackoverflow.com/questions/1555658/is-it-possible-in-java-to-access-private-fields-via-reflection
		mEndDateField = RegisterWithHuntActivity.class.getDeclaredField("mEndDate");
		mEndDateField.setAccessible(true);
		
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 8, 02);
		String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
		mEndDateField.set(mRegisterWithHuntActivity, formattedDate);
		
		mHuntAlreadyFinishedField = RegisterWithHuntActivity.class.getDeclaredField("mHuntAlreadyFinished");
		mHuntAlreadyFinishedField.setAccessible(true);
		
		mUserAlreadyRegisteredField = RegisterWithHuntActivity.class.getDeclaredField("mUserAlreadyRegistered");
		mUserAlreadyRegisteredField.setAccessible(true);
		
		mHuntAlreadyStartedField = RegisterWithHuntActivity.class.getDeclaredField("mHuntAlreadyStarted");
		mHuntAlreadyStartedField.setAccessible(true);
		
		mHuntIdField = RegisterWithHuntActivity.class.getDeclaredField("mHuntId");
		mHuntIdField.setAccessible(true);
		mHuntIdField.set(mRegisterWithHuntActivity, 1);
		
		mUserIdField = RegisterWithHuntActivity.class.getDeclaredField("mUserId");
		mUserIdField.setAccessible(true);
		mUserIdField.set(mRegisterWithHuntActivity, 1);
		
		sHuntParticipantIdResultField = RegisterWithHuntActivity.class.getDeclaredField("sHuntParticipantIdResult");
		sHuntParticipantIdResultField.setAccessible(true);
		
		mHuntParticipantIdReturnedField = RegisterWithHuntActivity.class.getDeclaredField("mHuntParticipantIdReturned");
		mHuntParticipantIdReturnedField.setAccessible(true);
		
		mRegistrationSuccessfulField = RegisterWithHuntActivity.class.getDeclaredField("mRegistrationSuccessful");
		mRegistrationSuccessfulField.setAccessible(true);
		
		sStartTimeResultField = RegisterWithHuntActivity.class.getDeclaredField("sStartTimeResult");
		sStartTimeResultField.setAccessible(true);
		
		mStartTimeField = RegisterWithHuntActivity.class.getDeclaredField("mStartTime");
		mStartTimeField.setAccessible(true);
		
		mRegisterWithHuntClass = RegisterWithHuntActivity.class;
		
		//mCheckIfHuntFinishedMethod = RegisterWithHuntActivity.class.getDeclaredMethod("checkIfHuntFinished", null);
		//mCheckIfHuntFinishedMethod.setAccessible(true);
		
	}
	
	//Runs green if debugged
	public void testHuntFinishedIfHuntOutOfDate() throws IllegalAccessException, InvocationTargetException {
		
		mRegisterWithHuntActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Bundle fakeBundle = new Bundle();
				
				try {
					mRegisterWithHuntActivity.checkIfHuntFinished(fakeBundle);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} 
				
				Object actualResult = new Object();
				try {
					actualResult = mHuntAlreadyFinishedField.get(mRegisterWithHuntActivity);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				assertEquals(true, actualResult);
			}
		});		
	}
	
	//Runs green if debugged
	public void testDatabaseCallMadeWhenAttemptingToSaveStartTime() throws JSONException {
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully saved start time!";
		 
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		//Want to use reflection here! Not happy about making 'doInBackground' public.
		try {
			mSaveStartTimeTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());			
	}
	
	public void testHuntParticipantIdReturnedWhenSuccessfulDatabaseCallMade() throws IllegalAccessException, IllegalArgumentException, JSONException {
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully returned participant id!";
		 
		JSONObject results = new JSONObject();
		results.put("HuntParticipantId", 1);
		
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
		fakeObject.put("results", results);
				
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mGetParticipantIdTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		String actualResult = sHuntParticipantIdResultField.get(mRegisterWithHuntActivity).toString();
		assertEquals(results.toString(), actualResult);	
		
		Object returnedResult = mHuntParticipantIdReturnedField.get(mRegisterWithHuntActivity);
		assertEquals(true, returnedResult);
	}
	
	public void testHuntParticipantIdNotReturnedWhenUnsuccessfulDatabaseCallMade() throws JSONException, IllegalAccessException, IllegalArgumentException {
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Not successfully returned participant id!";
		
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "0");
				
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mGetParticipantIdTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		Object actualResult = sHuntParticipantIdResultField.get(mRegisterWithHuntActivity);
		assertEquals(null, actualResult);	
		
		Object returnedResult = mHuntParticipantIdReturnedField.get(mRegisterWithHuntActivity);
		assertEquals(false, returnedResult);	
	}
	
	public void testDatabaseCallMadeWhenUserSuccessfullyRegistersWithHunt() throws JSONException, IllegalAccessException, IllegalArgumentException {
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully saved user!";
		 
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		//Want to use reflection here! Not happy about making 'doInBackground' public.
		try {
			mUserRegisterWithHuntTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());	
		
		Object returnedResult = mRegistrationSuccessfulField.get(mRegisterWithHuntActivity);
		assertEquals(true, returnedResult);	
	}
	
	public void testStartTimeReturnedIfHuntAlreadyStarted() throws IllegalAccessException, IllegalArgumentException, JSONException {
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully returned start time!";
		 
		JSONObject results = new JSONObject();
		results.put("StartTime", 13934211);
		
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
		fakeObject.put("results", results);
				
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mCheckIfHuntStartedTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());	
		
		String startTime = mStartTimeField.get(mRegisterWithHuntActivity).toString();
		assertNotNull("Start time is null", Integer.parseInt(startTime));	
		
		Object returnedResult = mHuntAlreadyStartedField.get(mRegisterWithHuntActivity);
		assertEquals(true, returnedResult);
		
		//Assert that the array passed back the correct data
		String actualResult = sStartTimeResultField.get(mRegisterWithHuntActivity).toString();
		assertEquals(results.toString(), actualResult);	
	}

	public void testStartTimeNotReturnedIfHuntNotAlreadyStarted () throws JSONException, IllegalAccessException, IllegalArgumentException {
		//Set up the JSONObject to be returned by the mock
		String UNSUCCESSFUL_MESSAGE = "Start time not returned!";
		
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_MESSAGE);
		fakeObject.put("success", "0");
		fakeObject.put("results", null);
				
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mCheckIfHuntStartedTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());	
		
		String startTime = mStartTimeField.get(mRegisterWithHuntActivity).toString();
		assertEquals(0, Integer.parseInt(startTime));	
		
		//Assert that the array passed back the correct data
		Object actualResult = sStartTimeResultField.get(mRegisterWithHuntActivity);
		assertEquals(null, actualResult);
		
		Object returnedResult = mHuntAlreadyStartedField.get(mRegisterWithHuntActivity);
		assertEquals(false, returnedResult);			
	}
	
	public void testDatabaseCallMadeWhenAttemptingToDetermineIfUserHasAlreadyRegistered() throws JSONException, IllegalAccessException, IllegalArgumentException {
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "User has already registered!";
		 
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		//Want to use reflection here! Not happy about making 'doInBackground' public.
		try {
			mCheckIfUserRegisteredTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		Object returnedResult = mUserAlreadyRegisteredField.get(mRegisterWithHuntActivity);
		assertEquals(true, returnedResult);
	}


}
