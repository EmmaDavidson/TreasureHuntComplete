package sqlLiteDatabase;
//http://www.vogella.com/articles/AndroidSQLite/article.html
import java.util.ArrayList;
import java.util.List;

import Utilities.MySQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HuntDAO {

  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { MySQLiteHelper.COLUMN_HUNTNAME };
  private String[] allColumnsUserHunt  = { MySQLiteHelper.COLUMN_USER_HUNTS_ID, MySQLiteHelper.COLUMN_USER_HUNTS_HUNTNAME };

  
  public HuntDAO(Context context) {
    dbHelper = new MySQLiteHelper(context);
  }

  public void open() throws SQLException {
	  database = dbHelper.getWritableDatabase();
	  //Unclean - drops the database each time and re-adds the table.
	  updateDatabaseLocally();
  }
  
  public void updateDatabaseLocally()
  {
	  dbHelper.onUpgrade(database, database.getVersion(), database.getVersion());
  }

  public void close() {
    dbHelper.close();
  }
  
  public Hunt addHunt(String comment) {
	    ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_HUNTNAME, comment);
	    long insertId = database.insert(MySQLiteHelper.TABLE_HUNTS, null,
	        values);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_HUNTS,
	        allColumns, null, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Hunt newComment = cursorToHunt(cursor);
	    cursor.close();
	    return newComment;
	  }

  public List<Hunt> getAllHunts() {
    List<Hunt> listOfHunts = new ArrayList<Hunt>();

    Cursor cursor = database.query(MySQLiteHelper.TABLE_HUNTS,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Hunt hunt = cursorToHunt(cursor);
      listOfHunts.add(hunt);
      cursor.moveToNext();
    }
    
    cursor.close();
    return listOfHunts;
  }
  
  public Hunt addUserHunt(int huntId, String comment)
  {
	  ContentValues values = new ContentValues();
	  	values.put(MySQLiteHelper.COLUMN_USER_HUNTS_ID, huntId);
	    values.put(MySQLiteHelper.COLUMN_USER_HUNTS_HUNTNAME, comment);
	    long insertId = database.insert(MySQLiteHelper.TABLE_USER_HUNTS, null,
	        values);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_USER_HUNTS,
	    		allColumnsUserHunt, null, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Hunt newComment = cursorToUserHunt(cursor);
	    cursor.close();
	    return newComment;
  }
  
  public List<Hunt> getAllUserHunts() {
	    List<Hunt> listOfHunts = new ArrayList<Hunt>();

	    Cursor cursor = database.query(MySQLiteHelper.TABLE_USER_HUNTS,
	    		allColumnsUserHunt, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Hunt hunt = cursorToUserHunt(cursor);
	      listOfHunts.add(hunt);
	      cursor.moveToNext();
	    }
	    
	    cursor.close();
	    return listOfHunts;
	  }
  
  public Hunt getParticularHunt(String huntName)
  {
	  String arguments =  "HuntName = '"+huntName + "'";
	  Cursor cursor = database.query(MySQLiteHelper.TABLE_USER_HUNTS, allColumnsUserHunt , arguments, null , null , null, null);
	  cursor.moveToFirst();
	  if(!cursor.isAfterLast())
	  {
		  Hunt hunt = cursorToUserHunt(cursor);
		  return hunt;
	  }
	  return null;
	  
  }

  //http://stackoverflow.com/questions/6781954/android-3-0-couldnt-read-row-column-from-cursor-window
  private Hunt cursorToHunt(Cursor cursor) {
    Hunt hunt = new Hunt();
    hunt.setHuntName(cursor.getString(0));
    return hunt;
  }
  
  private Hunt cursorToUserHunt(Cursor cursor) {
	    Hunt hunt = new Hunt();
	    hunt.setHuntId(cursor.getInt(0));
	    hunt.setHuntName(cursor.getString(1));
	    return hunt;
	  }
} 
