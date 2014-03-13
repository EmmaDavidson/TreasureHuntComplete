package com.application.treasurehunt;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.widget.TextView;

public class LicenceActivity extends Activity {

	
	TextView zbarScannerLicence; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_licence);
		
		//http://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
		zbarScannerLicence = (TextView) findViewById(R.id.zbar_scanner_link);
		zbarScannerLicence.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.licence, menu);
		return true;
	}

}
