/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package com.application.treasurehunt;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/*
 * This activity is to display the licences and their details for third party work that I have used inside my 
 * final year project.
 * */

public class LicenceActivity extends Activity {

	private TextView mZbarScannerLicence; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_licence);
		
		//http://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
		mZbarScannerLicence = (TextView) findViewById(R.id.zbar_scanner_link);
		mZbarScannerLicence.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	/* Methods to set up the on screen menu. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		//http://mobileorchard.com/android-app-development-menus-part-1-options-menu/
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		menu.add(Menu.NONE, 1, Menu.NONE, "Login");
		return true;
	} 
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		
		switch(item.getItemId()) {
			case 1: {
					
				Intent homepageActivityIntent = new Intent(LicenceActivity.this, LoginActivity.class);
				startActivity(homepageActivityIntent);
				
				return true;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
}
