package Utilities;
//http://www.vogella.com/articles/AndroidSQLite/article.html
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelperForHunts extends SQLiteOpenHelper {

  public static final String TABLE_HUNTS = "Hunt";
  public static final String COLUMN_HUNTNAME = "HuntName";
  

  private static final String DATABASE_NAME = "TreasureHuntHunts.db";
  private static final int DATABASE_VERSION = 1;

  // Database creation sql statement
  //http://stackoverflow.com/questions/14137622/confused-regarding-sqliteopenhelper-and-creating-multiple-tables
  private static final String DATABASE_CREATE = "create table "
      + TABLE_HUNTS + "("  + COLUMN_HUNTNAME
      + " text not null); " ;
  
  public MySQLiteHelperForHunts(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  //WE REALLY WANT A HELPER FOR EACH DIFFERENT TABLE - UPDATE LATER 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_HUNTS);

    onCreate(db);
  }
  

} 
