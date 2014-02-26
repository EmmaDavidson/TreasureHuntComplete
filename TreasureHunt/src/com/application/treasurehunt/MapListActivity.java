package com.application.treasurehunt;

import Utilities.SingleFragmentActivity;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.Menu;

public class MapListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new MapListFragment();
	}


}
