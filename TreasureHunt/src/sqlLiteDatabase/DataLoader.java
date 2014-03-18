package sqlLiteDatabase;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/* Entire class taken from Nerd Ranch Guide Page 1436. */

/* The purpose of this abstract class is to be a 'simple loader' to 'handle arbitrary data' and to be a 'generic loader' 
 * Page 1435. Used by the LastLocationLoader class.  */
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {

	/*
	 * Global variables used within DataLoader.
	 */
	private D mData;
	
	/* Constructor */ 
	public DataLoader(Context context) {
		super(context);
	}
	
	/* Method 'checks for the presence of... whatever data it is loading' else it 'calls the superclass's forceLoad()
	 * method to go and fetch it' Page 1437. */
	@Override
	protected void onStartLoading() {
		if(mData != null) {
			deliverResult(mData);
		}
		else {
			forceLoad();
		}
	}
	
	/*Method to 'stash away the new data object' Page 1437. */
	@Override
	public void deliverResult(D data) {
		mData = data;
		if(isStarted())	{
			super.deliverResult(data);
		}
	}
}
