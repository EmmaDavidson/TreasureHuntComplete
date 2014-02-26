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

import Utilities.JSONParser;
import android.test.ActivityInstrumentationTestCase2;

import com.application.treasurehunt.ChooseHuntActivity;
import com.application.treasurehunt.ChooseHuntActivity.ReturnHuntsTask;
import com.application.treasurehunt.LoginActivity;
import com.application.treasurehunt.RegisterActivity;
import com.application.treasurehunt.LoginActivity.UserLoginTask;

public class ChooseHuntActivityTest extends ActivityInstrumentationTestCase2<ChooseHuntActivity> {

	ChooseHuntActivity mChooseHuntActivity;
	
	Class mChooseHuntActivityClass;
	Class mReturnHuntsTaskClass;
	
	Method doInBackgroundMethod;
	
	Field tagResultField;
	
	Object doInBackgroundMethodObject;
	@Mock private JSONParser jsonParserMock;
	
	ChooseHuntActivity.ReturnHuntsTask returnHuntsTask;
	
	public ChooseHuntActivityTest(Class<ChooseHuntActivity> name) {
		super(name);
	}
	
	public ChooseHuntActivityTest()
	{
		super(ChooseHuntActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		
		mChooseHuntActivity = getActivity();
		returnHuntsTask = mChooseHuntActivity.new ReturnHuntsTask();
		
		jsonParserMock = Mockito.mock(JSONParser.class);
		mChooseHuntActivity.jsonParser = jsonParserMock;
		
		mChooseHuntActivityClass = ChooseHuntActivity.class;
		mReturnHuntsTaskClass = ReturnHuntsTask.class;
		
		doInBackgroundMethod = mReturnHuntsTaskClass.getDeclaredMethod("doInBackground", null);
		doInBackgroundMethod.setAccessible(true);
		
		//http://stackoverflow.com/questions/1555658/is-it-possible-in-java-to-access-private-fields-via-reflection
		tagResultField = ChooseHuntActivity.class.getDeclaredField("tagResult");
		tagResultField.setAccessible(true);
		
	}

	public void listOfHuntsGeneratedIfSuccessful() throws JSONException, IllegalArgumentException, IllegalAccessException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String SUCCESSFUL_MESSAGE = "Sucessfully returned hunts!";
		String RESULTS = "Emma Treasure Hunt";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
		fakeObject.put("results", RESULTS);
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			doInBackgroundMethodObject = doInBackgroundMethod.invoke(returnHuntsTask, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}	
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array is equal to what is expected
		//Acceptance test what is actually shown on screen
		String actualResult = (String) tagResultField.get(mChooseHuntActivity);
		assertEquals(RESULTS, actualResult);
	}
	
	public void listEmptyIfUnsuccessful() throws JSONException, IllegalArgumentException, IllegalAccessException
	{
		//http://stackoverflow.com/questions/3559063/how-to-enter-quotes-in-a-string
		String UNSUCCESSFUL_MESSAGE = "No hunts were returned.";
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", UNSUCCESSFUL_MESSAGE);
		fakeObject.put("success", "0");
		fakeObject.put("results", null);
	
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		try {
			doInBackgroundMethodObject = doInBackgroundMethod.invoke(returnHuntsTask, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}	
		
		//Assert that the call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyList());		
		
		//Assert that the array is equal to what is expected
		//Acceptance test what is actually shown on screen
		String actualResult = (String) tagResultField.get(mChooseHuntActivity);
		assertEquals(null, actualResult);
	}

	

}
