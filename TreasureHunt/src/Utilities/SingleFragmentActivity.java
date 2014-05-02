/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */

package Utilities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.application.treasurehunt.R;

/*Entire class taken from the Nerd Ranch Guide Page 459.*/

/* The purpose of this abstract class is to create an instance of a basic Fragment that can be created and
 *  used many times 'to avoid typing again and again' - Nerd Ranch Guide Page 457 
 *  */
public abstract class SingleFragmentActivity extends FragmentActivity {
	
	/*Abstract method for overriding by instance class.*/
	protected abstract Fragment createFragment();
	
	/* Method called when the Fragment Activity is created (as part of the Android Life Cycle), 
	 * generating a new instance of a Fragment. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragment_container);
		
		if(fragment == null) {
			fragment = createFragment();
			fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
		}
	}
}
