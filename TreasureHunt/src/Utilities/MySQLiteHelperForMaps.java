package Utilities;
//http://www.vogella.com/articles/AndroidSQLite/article.html
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelperForMaps extends SQLiteOpenHelper {

  public static final String TABLE_LOCATIONS = "Locations";
  public static final String COLUMN_MAPS_LATITUDE = "Latitude";
  public static final String COLUMN_MAPS_LONGTITUDE = "Longtitude";
  public static final String COLUMN_MAPS_ALTITUDE = "Altitude";
  public static final String COLUMN_MAPS_TIME_STAMP= "TimeStamp";
  
  public static final String TABLE_MAPS = "Map";
  public static final String COLUMN_MAPS_START_TIME= "StartTime";
  public static final String COLUMM_MAPS_PARTICIPANT_ID = "HuntParticipantId";
  
  public static final String TABLE_MARKERS = "Markers";
  
  private static final String DATABASE_NAME = "TreasureHuntMaps.db";
  private static final int DATABASE_VERSION = 1;

  //MIGHT NEED TO CHANGE START DATE TO DOUBLE
  private static final String DATABASE_CREATE_LOCATIONS = "create table " + TABLE_LOCATIONS + " (" + COLUMM_MAPS_PARTICIPANT_ID + " INTEGER NOT NULL, " 
		  									+ COLUMN_MAPS_LATITUDE + " real, " + COLUMN_MAPS_LONGTITUDE + " real, " 
		  									 + COLUMN_MAPS_ALTITUDE + " real, " + COLUMN_MAPS_TIME_STAMP + " INTEGER NOT NULL); " ;

  private static final String DATABASE_CREATE_MAPS = "create table " + TABLE_MAPS + " (" + COLUMM_MAPS_PARTICIPANT_ID + " INTEGER NOT NULL, " 
		  								+ COLUMN_MAPS_START_TIME + " LONG); " ;
  
  private static final String DATABASE_CREATE_MARKERS = "create table " + TABLE_MARKERS + " (" + COLUMM_MAPS_PARTICIPANT_ID + " INTEGER NOT NULL, " 
		  + COLUMN_MAPS_LATITUDE + " real, " + COLUMN_MAPS_LONGTITUDE + " real); " ;
  
  public MySQLiteHelperForMaps(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE_MAPS);
    database.execSQL(DATABASE_CREATE_LOCATIONS);
    database.execSQL(DATABASE_CREATE_MARKERS);
  }

  //WE REALLY WANT A HELPER FOR EACH DIFFERENT TABLE - UPDATE LATER 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS);
    onCreate(db);
  }
  

} 
