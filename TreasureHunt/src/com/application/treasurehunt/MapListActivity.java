package com.application.treasurehunt;

import Utilities.SingleFragmentActivity;
import android.support.v4.app.Fragment;

public class MapListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new MapListFragment();
	}


}
