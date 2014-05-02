/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */

package com.application.treasurehunt.test;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.application.treasurehunt.ChooseHuntActivity;
import com.application.treasurehunt.ChooseHuntActivity.ReturnHuntsTask;

import Utilities.JSONParser;
import Utilities.PHPHelper;
import android.test.ActivityInstrumentationTestCase2;

public class ChooseHuntActivityTest extends ActivityInstrumentationTestCase2<ChooseHuntActivity> {
	
	//These tests run green individually but not together. This is because the testListOfHuntsGeneratedIfSuccessful
	//on its run generates a list of tests first, and then in the second test (testlistOfHuntsEmptyIfUnsuccessful)
	//the value of actualResult is not null because the first test has set it.

	//The classes to be tested
	ChooseHuntActivity mChooseHuntActivity;
	ChooseHuntActivity.ReturnHuntsTask mReturnHuntsTask;
	
	//Field within class to access using reflection
	Field mReturnHuntsResultField;
	
	//The JSON parser to be mocked
	@Mock private JSONParser jsonParserMock;
	
	public ChooseHuntActivityTest(Class<ChooseHuntActivity> name) {
		super(name);
	}
	
	public ChooseHuntActivityTest()
	{
		super(ChooseHuntActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		
		//Grabbing the classes
		mChooseHuntActivity = getActivity();
		mReturnHuntsTask = mChooseHuntActivity.new ReturnHuntsTask();
		
		//Setting up the mock
		jsonParserMock = Mockito.mock(JSONParser.class);
		mChooseHuntActivity.jsonParser = jsonParserMock;
		
		//Using reflection to access the JSON result variable
		//http://stackoverflow.com/questions/1555658/is-it-possible-in-java-to-access-private-fields-via-reflection
		mReturnHuntsResultField = ChooseHuntActivity.class.getDeclaredField("sTagResult");
		mReturnHuntsResultField.setAccessible(true);
		
	}
	
	public void testlistOfHuntsEmptyIfUnsuccessful() throws JSONException, IllegalArgumentException, IllegalAccessException {
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
		Object actualResult = mReturnHuntsResultField.get(mChooseHuntActivity);
		assertEquals(null, actualResult);	
	}
	
	public void testListOfHuntsGeneratedIfSuccessful()
			throws JSONException, IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully returned hunts!";
		JSONArray array = new JSONArray();
		array.put("Emma treasure hunt");
		 
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
		fakeObject.put("results", array);
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		try {
			mReturnHuntsTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		String actualResult = mReturnHuntsResultField.get(mChooseHuntActivity).toString();
		assertEquals(array.toString(), actualResult);	
	}
}
