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
import com.application.treasurehunt.LoginActivity;
import com.application.treasurehunt.ScanQRCodeActivity;
import com.application.treasurehunt.ChooseCompanyActivity.ReturnCompaniesTask;

/*
 * There is not much to test on this activity apart from the UI, so not a lot of functionality tested here
 */

public class ScanQRCodeActivityTest extends ActivityInstrumentationTestCase2<ScanQRCodeActivity> {
	
	ScanQRCodeActivity mScanQRCodeActivity;
	ScanQRCodeActivity.SaveScanResultTask mSaveScanResultTask;
	
	//The JSON parser to be mocked
	@Mock private JSONParser jsonParserMock;

	public ScanQRCodeActivityTest(Class<ScanQRCodeActivity> name) {
		super(name);
	}
	
	public ScanQRCodeActivityTest()
	{
		super(ScanQRCodeActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		
		System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		
		//Grabbing the classes
		mScanQRCodeActivity = getActivity();
		mSaveScanResultTask = mScanQRCodeActivity.new SaveScanResultTask();
		
		//Setting up the mock
		jsonParserMock = Mockito.mock(JSONParser.class);
		mScanQRCodeActivity.jsonParser = jsonParserMock;
	}
	
	public void testDatabaseCallMadeOnSaveDetails() throws JSONException {
		
		//Set up the JSONObject to be returned by the mock
		String SUCCESSFUL_MESSAGE = "Sucessfully saved details!";
		 
		JSONObject fakeObject = new JSONObject();	
		fakeObject.put("message", SUCCESSFUL_MESSAGE);
		fakeObject.put("success", "1");
	
		//Set up the mock
		Mockito.when(jsonParserMock.makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList())).thenReturn(fakeObject);
		
		//Call the method
		//Want to use reflection here! Not happy about making 'doInBackground' public.
		try {
			mSaveScanResultTask.doInBackground("");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		//Assert that the database call was made
		Mockito.verify(jsonParserMock, Mockito.times(1)).makeHttpRequest(Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyList());		
	}

}
