package Utilities;
//http://www.vogella.com/articles/AndroidSQLite/article.html
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelperForLeaderboard extends SQLiteOpenHelper {
  
  public static final String TABLE_HUNT_LEADERBOARD = "Leaderboard";
  public static final String COLUMN_LEADERBOARD_USERNAME = "UserName";
  public static final String COLUMN_LEADERBOARD_TALLY = "Tally";
  public static final String COLUMN_LEADERBOARD_ELAPSED_TIME = "ElapsedTime";
  

  private static final String DATABASE_NAME = "TreasureHunt.db";
  private static final int DATABASE_VERSION = 1;
  
  //http://stackoverflow.com/questions/15424382/sqlite-database-android-create-table
  //http://stackoverflow.com/questions/19619418/android-sqlite-sqliteexception-near-syntax-error
  private static final String DATABASE_CREATE_LEADERBOARD =  "create table " 
		  + TABLE_HUNT_LEADERBOARD + " (" + COLUMN_LEADERBOARD_USERNAME + " name not null, " +
	        COLUMN_LEADERBOARD_TALLY + " INTEGER NOT NULL, " + COLUMN_LEADERBOARD_ELAPSED_TIME + " FLOAT NOT NULL); ";

  public MySQLiteHelperForLeaderboard(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE_LEADERBOARD);

  }

  //WE REALLY WANT A HELPER FOR EACH DIFFERENT TABLE - UPDATE LATER 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_HUNT_LEADERBOARD);
    onCreate(db);
  }
  

} 
