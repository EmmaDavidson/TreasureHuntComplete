/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */

package com.application.treasurehunt.test;

import java.lang.reflect.Field;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import Utilities.JSONParser;
import android.test.ActivityInstrumentationTestCase2;

import com.application.treasurehunt.ChooseCompanyActivity;
import com.application.treasurehunt.LeaderboardActivity;
import com.application.treasurehunt.ChooseCompanyActivity.ReturnCompaniesTask;

public class LeaderboardActivityTest extends ActivityInstrumentationTestCase2<LeaderboardActivity> {
	
	//The classes to be tested
	LeaderboardActivity mLeaderboardActivity;
	LeaderboardActivity.ReturnLeaderboardTask mReturnLeaderboardTask;
	
	//Field within class to access using reflection
	Field mReturnedLeaderboardResultField;
	Field mReturnedListOfParticipantsResultField;
	
	//The JSON parser to be mocked
	@Mock private JSONParser jsonParserMock;
	
	public LeaderboardActivityTest() {
		super(LeaderboardActivity.class);
	}
	
	public LeaderboardActivityTest(Class<LeaderboardActivity> activityClass) {
		super(activityClass);
	}
	
	@Before
	public void setUp() throws Exception {
		
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		
		//Grabbing the classes
		mLeaderboardActivity = getActivity();
		mReturnLeaderboardTask = mLeaderboardActivity.new ReturnLeaderboardTask();
		
		//Setting up the mock
		jsonParserMock = Mockito.mock(JSONParser.class);
		mLeaderboardActivity.jsonParser = jsonParserMock;
		
		//Using reflection to access the JSON result variable
		//http://stackoverflow.com/questions/1555658/is-it-possible-in-java-to-access-private-fields-via-reflection
		mReturnedLeaderboardResultField = LeaderboardActivity.class.getDeclaredField("sTagResult");
		
		mReturnedLeaderboardResultField.setAccessible(true);
		
		mReturnedListOfParticipantsResultField = LeaderboardActivity.class.getDeclaredField("sTagNameResult");
		mReturnedListOfParticipantsResultField.setAccessible(true);
		
	}
	
	public void testlistOfParticipantsEmptyIfUnsuccessful() throws JSONException, 
							IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		
		//Set up the JSONObject to be returned by the mock
		String UNSUCCESSFUL_MESSAGE = "Leaderboard not returned.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_MESSAGE);
		fakeObject.put("success", "0");
		fakeObject.put("results", null);
		
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), 
		Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		try {
			mReturnLeaderboardTask.doInBackground("");
		} catch (IllegalArgumentException e) {
		e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
		Matchers.anyString(), Matchers.anyList());	
		
		//Having problems here asserting that the result should be empty
		Object actualResult = mReturnedLeaderboardResultField.get(mLeaderboardActivity);
		assertEquals(null, actualResult);	
		
		Object listResult = mReturnedListOfParticipantsResultField.get(mLeaderboardActivity);
		assertEquals(null, listResult);
	}
		
	public void testListOfParticipantsGeneratedIfSuccessful()
			  throws JSONException, IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully returned companies!";
		
		JSONArray array = new JSONArray();
		array.put("Emma results");
		
		JSONArray resultNames = new JSONArray();
		resultNames.put("Emma");
		resultNames.put("Adam");
		resultNames.put("Kathryn");
		
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
		fakeObject.put("results", array);
		fakeObject.put("resultNames", resultNames);
		
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
		Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		//Want to use reflection here! Not happy about making 'doInBackground' public.
		try {
			mReturnLeaderboardTask.doInBackground("");
		} catch (IllegalArgumentException e) {
		e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
		Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		Object actualResult = mReturnedLeaderboardResultField.get(mLeaderboardActivity);
		assertEquals(array, actualResult);	
		
		Object listResult = mReturnedListOfParticipantsResultField.get(mLeaderboardActivity);
		assertEquals(resultNames, listResult);
	}

}
