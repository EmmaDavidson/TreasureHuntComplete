package Utilities;
//http://www.vogella.com/articles/AndroidSQLite/article.html
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelperForUserHunts extends SQLiteOpenHelper {

  public static final String TABLE_USER_HUNTS = "UserHunt";
  public static final String COLUMN_USER_HUNTS_HUNTNAME = "HuntName";
  public static final String COLUMN_USER_HUNTS_ID = "HuntId";
 

  private static final String DATABASE_NAME = "TreasureHuntUserHunts.db";
  private static final int DATABASE_VERSION = 1;

  
  private static final String DATABASE_CREATE_USER_HUNTS =  "create table " 
		  + TABLE_USER_HUNTS + " ("  + COLUMN_USER_HUNTS_ID + " INTEGER NOT NULL, " + COLUMN_USER_HUNTS_HUNTNAME
	      + " text not null); " ;
  
  public MySQLiteHelperForUserHunts(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE_USER_HUNTS);
  }

  //WE REALLY WANT A HELPER FOR EACH DIFFERENT TABLE - UPDATE LATER 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_HUNTS);

    onCreate(db);
  }
  

} 
