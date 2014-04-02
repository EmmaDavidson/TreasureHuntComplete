package com.application.treasurehunt.test;

import java.lang.reflect.Field;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import sqlLiteDatabase.Hunt;

import com.application.treasurehunt.ChooseCompanyActivity;
import com.application.treasurehunt.MyHuntsActivity;
import com.application.treasurehunt.ChooseCompanyActivity.ReturnCompaniesTask;

import Utilities.JSONParser;
import android.test.ActivityInstrumentationTestCase2;


public class MyHuntsActivityTest extends ActivityInstrumentationTestCase2<MyHuntsActivity> {
	
	//When debugged through these tests run clear
	
	//The classes to be tested
	MyHuntsActivity mMyHuntsActivity;
	MyHuntsActivity.ReturnUserHuntsTask mReturnHuntsTask;
	MyHuntsActivity.GetHuntParticipantIdTask mGetHuntParticipantIdTask;
	//Class mChooseCompanyActivityClass;
	//Class mReturnCompaniesTaskClass;	
	//Method doInBackgroundMethod;
	//Object doInBackgroundMethodObject;
	
	//Field within class to access using reflection
	Field mReturnedHuntsResultField;
	Field mReturnedIdField;
	Field mTypeOfHunt;
	Field mCurrentUserId;
	Field sHuntParticipantIdResultField;
	Field mChosenHuntField;
	Field mHuntParticipantIdReturnedField;
	
	JSONObject fakeObject;
	JSONArray sTagResult;
	JSONObject participantIdObject;
	
	//The JSON parser to be mocked
	@Mock private JSONParser jsonParserMock;
	
	public MyHuntsActivityTest() {
		super(MyHuntsActivity.class);
	}
	
	public MyHuntsActivityTest(Class<MyHuntsActivity> name) {
		super(name);
	}
	
	//ALL TESTS include...THERE IS NO VALIDATION
	//GetHuntParticipantIdTask
	//ReturnUserHuntsTask

	@Before
	public void setUp() throws Exception {
		
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		
		//Grabbing the classes
		mMyHuntsActivity = getActivity();
		mReturnHuntsTask = mMyHuntsActivity.new ReturnUserHuntsTask();
		mGetHuntParticipantIdTask = mMyHuntsActivity.new GetHuntParticipantIdTask();
		
		//Using reflection to access the JSON result variable
		//http://stackoverflow.com/questions/1555658/is-it-possible-in-java-to-access-private-fields-via-reflection
		mReturnedHuntsResultField = MyHuntsActivity.class.getDeclaredField("sTagResult");
		mReturnedHuntsResultField.setAccessible(true);
		
		mReturnedIdField = MyHuntsActivity.class.getDeclaredField("sTagIdResult");
		mReturnedHuntsResultField.setAccessible(true);
				
		mTypeOfHunt = MyHuntsActivity.class.getDeclaredField("mTypeOfHunt");
		mTypeOfHunt.setAccessible(true);
		
		mCurrentUserId = MyHuntsActivity.class.getDeclaredField("mCurrentUserId");
		mCurrentUserId.setAccessible(true);
		
		sHuntParticipantIdResultField = MyHuntsActivity.class.getDeclaredField("sHuntParticipantIdResult");
		sHuntParticipantIdResultField.setAccessible(true);
		
		mHuntParticipantIdReturnedField = MyHuntsActivity.class.getDeclaredField("mHuntParticipantIdReturned");
		mHuntParticipantIdReturnedField.setAccessible(true);
		
		mChosenHuntField = MyHuntsActivity.class.getDeclaredField("mChosenHunt");
		mChosenHuntField.setAccessible(true);
		
		Hunt newHunt = new Hunt();
		newHunt.setHuntId(1);
		
		mChosenHuntField.set(mMyHuntsActivity, newHunt);
		
		mTypeOfHunt.set(mMyHuntsActivity, "NotStarted");
		mCurrentUserId.set(mMyHuntsActivity, 1);
		
		String SUCCESSFUL_MESSAGE = "Sucessfully returned hunts!";
		
		//RESULTS ARRAY
		JSONObject results = new JSONObject();
		results.put("HuntName", "My Hunt");
		results.put("HuntDescription", "Description here");
		results.put("EndDate", "28/06/2014");
		
		JSONArray myresults = new JSONArray();
		myresults.put(results);
		
		sTagResult = new JSONArray();
		sTagResult.put(myresults);
		
		participantIdObject = new JSONObject();
		participantIdObject.put("success", "1");
		
		participantIdObject.put("message", SUCCESSFUL_MESSAGE);
		 		
		//ID ARRAY
		JSONObject sTagIdResult = new JSONObject();
		sTagIdResult.put("HuntId", 1);
		
		JSONArray sTagIdResults = new JSONArray();
		sTagIdResults.put(sTagIdResult);
		
		fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
		fakeObject.put("results", sTagResult);
		fakeObject.put("huntIds", sTagIdResults);
		fakeObject.put("HuntParticipantId", 1);
		
		//Setting up the mock
		jsonParserMock = Mockito.mock(JSONParser.class);
		mMyHuntsActivity.jsonParser = jsonParserMock;
		
	}
	
	//There is a problem when running these tests together. As the calls are made when the activity is first created,
	//before they are being called in the test, the creation is too quick to set up in the inner variables. Therefore,
	//it will fail unless it is debugged through. i.e. the 'mTypeOfHunt' variable has not been set up quickly enough 
	//therefore the test will fail.
	
	//runs clear when debugged
	public void testlistOfHuntsEmptyIfUnsuccessful() throws JSONException, 
		IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		
		//Set up the JSONObject to be returned by the mock
		String UNSUCCESSFUL_MESSAGE = "No hunts were returned.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_MESSAGE);
		fakeObject.put("success", "0");
		fakeObject.put("results", null);
		
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), 
		Matchers.anyList())).thenReturn(fakeObject);
		
		mTypeOfHunt.set(mMyHuntsActivity, "NotStarted");
		mCurrentUserId.set(mMyHuntsActivity, 1);
		
		//Call the method
		try {
			mReturnHuntsTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
		Matchers.anyString(), Matchers.anyList());	
		
		//Having problems here asserting that the result should be empty
		Object actualResult = mReturnedHuntsResultField.get(mMyHuntsActivity);
		assertEquals(null, actualResult);	
	}
	
	//runs clear when debugged
	
	public void testListOfHuntsGeneratedIfSuccessful()
			throws JSONException, IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		
		//Set up the JSONObject to be returned by the mock
		
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mReturnHuntsTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		String actualResult = mReturnedHuntsResultField.get(mMyHuntsActivity).toString();
		assertEquals(sTagResult.toString(), actualResult);	
	}
	
	//Works but issue wanted <huntparticipantid 1> but got <huntparticipantid 1> same thing!
	
	public void testHuntParticipantIdReturnedIfSuccessfulDatabaseCall() throws IllegalAccessException, IllegalArgumentException, JSONException{
			
		//RESULTS ARRAY
		JSONObject idResults = new JSONObject();
		idResults.put("HuntParticipantId", 1);

		JSONObject participantIdResultObject = new JSONObject();
		participantIdResultObject.put("message", "Success!");
		participantIdResultObject.put("success", "1");
		participantIdResultObject.put("results", idResults);
		
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(participantIdResultObject);
		
		try {
			mGetHuntParticipantIdTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		Object actualResult = sHuntParticipantIdResultField.get(mMyHuntsActivity);
		assertEquals(idResults.toString(), actualResult);	
		
		String returnedResult = mHuntParticipantIdReturnedField.get(mMyHuntsActivity).toString();
		assertEquals(true, returnedResult);
	}
	
	//Works when debugging. However, problem <false> but was <false>
	
	public void testHuntParticipantIdNotReturnedIfUnsuccessfulDatabaseCall() throws JSONException, IllegalAccessException, IllegalArgumentException{
		
		//Set up the JSONObject to be returned by the mock
		String UNSUCCESSFUL_MESSAGE = "No id was returned.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_MESSAGE);
		fakeObject.put("success", "0");
		fakeObject.put("results", null);
				
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			mGetHuntParticipantIdTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		Object actualResult = sHuntParticipantIdResultField.get(mMyHuntsActivity);
		assertEquals(null, actualResult);	
		
		String returnedResult = mHuntParticipantIdReturnedField.get(mMyHuntsActivity).toString();
		assertEquals(false, returnedResult);
	}
}
