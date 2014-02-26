package com.application.treasurehunt;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ChooseTypeOfMyHuntActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_type_of_my_hunt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_type_of_my_hunt, menu);
		return true;
	}

}
