/*
 * Copyright (C) 2013 The Android Open Source Project 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.application.treasurehunt;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.LinkMovementMethod;
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
}
