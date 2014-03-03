package Utilities;

import com.application.treasurehunt.RegisterWithHuntActivity;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//THIS IS A SINGLETON ... this may not actually be relevant as a singleton...
//ONLY WANT ONE INTERNETUTILITY OBJECT TO BE CREATED

public class InternetUtility {
	
	private static InternetUtility instance;
	
	public synchronized static InternetUtility getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new InternetUtility(context.getApplicationContext());
		}
		return instance;
	}
	
	Context currentContext;
	
	private InternetUtility(Context context)
	{
		currentContext = context;
	}

	public synchronized boolean isInternetConnected()
	{	
		//http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
		ConnectivityManager manager = (ConnectivityManager) currentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		return info != null && info.isConnected();
	}
	
	//Not sure if this should be here... 
	public synchronized void showNoConnectionMessage()
	{
		Builder noConnectionAlert;
		noConnectionAlert = new Builder(currentContext);
		noConnectionAlert.setTitle("No connection");
		noConnectionAlert.setMessage("There is no internet connection. Connection required.");
		noConnectionAlert.setCancelable(false);
		noConnectionAlert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();	
			}
		});
		
		noConnectionAlert.create();
	}
}
