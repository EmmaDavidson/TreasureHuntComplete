package com.application.treasurehunt.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import android.test.ActivityInstrumentationTestCase2;

import com.application.treasurehunt.LoginActivity;
import com.application.treasurehunt.ScanQRCodeActivity;

/*
 * There is not much to test on this activity apart from the UI, so no functionality tested here
 */

public class ScanQRCodeActivityTest extends ActivityInstrumentationTestCase2<ScanQRCodeActivity> {

	public ScanQRCodeActivityTest(Class<ScanQRCodeActivity> name) {
		super(name);
	}
	
	public ScanQRCodeActivityTest()
	{
		super(ScanQRCodeActivity.class);
	}

	@Before
	public void setUp() throws Exception {
	}

}
