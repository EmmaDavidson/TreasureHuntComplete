/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package sqlLiteDatabase;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/* The purpose of this class is to provide a subclass so those extending it will be able to 'indirectly execute (a) 
 * query on another thread' - and has been taken entirely from Nerd Ranch Guide Page 1425. I do not own this class.
 * It will 'load and hold a Cursor' (Page 1428) i.e. it will load a given
 * cursor on a thread other than the main UI thread. Entire class taken from here. */
public abstract class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {

	private Cursor mCursor;

	
	public SQLiteCursorLoader(Context context) {
		super(context);
	}
	
	protected abstract Cursor loadCursor();
	
	@Override
	public Cursor loadInBackground() {
		Cursor cursor = loadCursor();
		if(cursor != null)
		{
			cursor.getCount();
		}
		return cursor;
	}
	
	@Override
	public void deliverResult(Cursor data)
	{
		Cursor oldCursor = mCursor;
		mCursor = data;
		if(isStarted()){
			super.deliverResult(data);
		}
		
		if(oldCursor != null && oldCursor != data && !oldCursor.isClosed())
		{
			oldCursor.close();
		}
	}
	
	@Override
	protected void onStartLoading()
	{
		if(mCursor != null)
		{
			deliverResult(mCursor);
		}
		if(takeContentChanged() || mCursor == null)
		{
			forceLoad();
		}
	}
	
	@Override
	protected void onStopLoading()
	{
		cancelLoad();
	}
	
	@Override
	public void onCanceled(Cursor cursor)
	{
		if(cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}
	}
	
	@Override
	protected void onReset()
	{
		super.onReset();
		onStopLoading();
		
		if(mCursor != null && !mCursor.isClosed())
		{
			mCursor.close();
		}
		
		mCursor = null;
	}

}
