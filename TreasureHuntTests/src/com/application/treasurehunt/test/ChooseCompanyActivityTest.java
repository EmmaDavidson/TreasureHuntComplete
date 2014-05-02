/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */

package com.application.treasurehunt.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

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

import com.application.treasurehunt.ChooseCompanyActivity;
import com.application.treasurehunt.ChooseHuntActivity;
import com.application.treasurehunt.ChooseHuntActivity.ReturnHuntsTask;

public class ChooseCompanyActivityTest extends ActivityInstrumentationTestCase2<ChooseCompanyActivity> {

	//testListOfCompaniesGeneratedIfSuccessful runs fine.
	//testlistOfCompaniesEmptyIfUnsuccessful has a problem where 'actualResult' is not null because as the 
	//test begins the actual db call is made and it brings down all of the data. This is because the json returns '0',
	//but the value of sTagResult is not changed in the ChooseCompanyActivity class and still retains 
	//the value of the initial call when the test is being loaded. Therefore, it will not equal null.
	
	//Have not tested the check for the password as the it only decides whether or not to move onto the next activity
	//(else it throws up an error message) and this is to be acceptance tested. 
	
	//The classes to be tested
	ChooseCompanyActivity mChooseCompanyActivity;
	ChooseCompanyActivity.ReturnCompaniesTask mReturnCompaniesTask;
	
	//Field within class to access using reflection
	Field mReturnedCompanyResultField;
	
	//The JSON parser to be mocked
	@Mock private JSONParser jsonParserMock;
	
	public ChooseCompanyActivityTest() {
		super(ChooseCompanyActivity.class);
	}
	
	public ChooseCompanyActivityTest(Class<ChooseCompanyActivity> name) {
		super(name);
	}

	@Before
	public void setUp() throws Exception {
		
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		
		//Grabbing the classes
		mChooseCompanyActivity = getActivity();
		mReturnCompaniesTask = mChooseCompanyActivity.new ReturnCompaniesTask();
		
		//Setting up the mock
		jsonParserMock = Mockito.mock(JSONParser.class);
		mChooseCompanyActivity.jsonParser = jsonParserMock;
		
		//Using reflection to access the JSON result variable
		//http://stackoverflow.com/questions/1555658/is-it-possible-in-java-to-access-private-fields-via-reflection
		mReturnedCompanyResultField = ChooseCompanyActivity.class.getDeclaredField("sTagResult");
		mReturnedCompanyResultField.setAccessible(true);
		
	}

	public void testlistOfCompaniesEmptyIfUnsuccessful() throws JSONException, 
										IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		mChooseCompanyActivity = getActivity();
		//Set up the JSONObject to be returned by the mock
		String UNSUCCESSFUL_MESSAGE = "No companies were returned.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_MESSAGE);
		fakeObject.put("success", "0");
		fakeObject.put("results", null);
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), 
				Matchers.anyList())).thenReturn(fakeObject);
	
		//Call the method
		try {
			mReturnCompaniesTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());	
		
		//Having problems here asserting that the result should be empty
		Object actualResult = mReturnedCompanyResultField.get(mChooseCompanyActivity);
		assertEquals(null, actualResult);	
	}
	
	public void testListOfCompaniesGeneratedIfSuccessful()
			throws JSONException, IllegalArgumentException, IllegalAccessException {
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully returned companies!";
		
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
		//Want to use reflection here! Not happy about making 'doInBackground' public.
		try {
			mReturnCompaniesTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array passed back the correct data
		Object actualResult = mReturnedCompanyResultField.get(mChooseCompanyActivity);
		assertEquals(array, actualResult);	
	}
}
