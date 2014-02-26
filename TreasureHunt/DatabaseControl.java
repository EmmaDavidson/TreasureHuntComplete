package com.example.databaseproject;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;

//Control SQL Calls
//WHere SQL functions are
//CRUD here
//ACCESSING THE DATABASE
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class DatabaseControl {

	//Column names for the inventory table
	private static final String KEY_ROWID = "_id";
	private static final String KEY_ITEMTYPE = "itemType";
	private static final String KEY_QUANTITY = "quantity";
	
	//table name
	private static final String DATABASE_TABLE = "inventory";
	
	private Context context;
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	
	public DatabaseControl(Context context)
	{
		this.context = context;
	}
	
	//When opening up the db for making calls
	
	public DatabaseControl open() throws SQLiteException
	{
		
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase(); //gives us access to the db
		return this;
	}
	
	//close db when finished with it
	public void close()
	{
		dbHelper.close();
	}
	
	//Add an item to the db
	public long addItem(String type, int quantity)
	{	//Need list of key value pairs to send to the database
		ContentValues setUpVals = createContentValues(type, quantity);
		return database.insert(DATABASE_TABLE, null, setUpVals);
	}
	
	public boolean updateItem(long id, String type, int quantity)
	{
		//will return how many arguments were updated
		ContentValues updateVals = createContentValues(type, quantity);
		return database.update(DATABASE_TABLE, updateVals, KEY_ROWID+"=" + id, null ) > 0;
	}
	
	//Will allow users to input name of object and search for actual row id for that item
	public long fetchItemIdByType(String type)
	{
		//Grab data in the form of a cursors
		//Like a file pointer it points to the data
		//Access data returned from the call
		Cursor dbCursor;
		long id = 0;
		
		//will return the db cursor
		//grabs the data 
		try {
			dbCursor = database.query(true,DATABASE_TABLE,new String[]{KEY_ROWID}, KEY_ITEMTYPE + "= '" + type + "'", null, null, null, null, null, null);
			dbCursor.moveToFirst(); //at beginnning
			id = dbCursor.getLong(dbCursor.getColumnIndex(KEY_ROWID));
		} catch (SQLiteException e) {
			//i.e no item found
			id = -1;
		}
		
		return id;
	
	}
	
	public String fetchAllItems()
	{
		String allData = "";
		Cursor dbCursor;
		try {
			dbCursor = database.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_ITEMTYPE, KEY_QUANTITY}, null, null, null, null, null);
			//Will give the index of the given rows
			int iRow = dbCursor.getColumnIndex(KEY_ROWID);
			int iType = dbCursor.getColumnIndex(KEY_ITEMTYPE);
			int iQuantity = dbCursor.getColumnIndex(KEY_QUANTITY);
			
			for(dbCursor.moveToFirst(); !dbCursor.isAfterLast(); dbCursor.moveToNext())
			{
				//Will print out a table
				allData = allData + " " + dbCursor.getString(iRow) + "\t" + dbCursor.getString(iType) + "\t\t\t" + dbCursor.getString(iQuantity) + "\n";
				
			}
		} catch (Exception e) {
			allData = "";
		}
		return allData;
	}
	
	public boolean deleteItem(long id)
	{
		return database.delete(DATABASE_TABLE, KEY_ROWID + " = " + id, null) > 0;
	}
	
	//Going to set up key value pair list
	public ContentValues createContentValues(String type, int quantity)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_ITEMTYPE, type);
		values.put(KEY_QUANTITY, quantity);
		return values;
		
	}
}
