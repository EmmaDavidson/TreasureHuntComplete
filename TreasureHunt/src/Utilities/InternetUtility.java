/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */

package Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
 * The purpose of this singleton is to check whether or not a participant's device is connected to the Internet. 
 * It must be connected to the Internet for the majority of the application to work as expected.
 * */

public class InternetUtility {
	
	/*
	 * Global variables used within InternetUtility.
	 */
	private static InternetUtility sInstance;
	public static final String INTERNET_DISCONNECTED = "Internet is required.";
	private Context mCurrentContext;
	
	/*Returns an instance of the singleton i.e. if it has already been created by another class,
	 * grab an instance of it, else, create a new instance. */
	public synchronized static InternetUtility getInstance(Context context) {
		
		if(sInstance == null) {
			sInstance = new InternetUtility(context.getApplicationContext());
		}
		return sInstance;
	}
	
	/*Private constructor for this class.*/
	private InternetUtility(Context context) {
		mCurrentContext = context;
	}

	/*Method that checks whether or not the device is connected to the Internet and returns the result.*/
	public synchronized boolean isInternetConnected()
	{	
		//http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
		ConnectivityManager manager = (ConnectivityManager) mCurrentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		return info != null && info.isConnected();
	}
	
}
