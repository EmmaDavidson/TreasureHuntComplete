package sqlLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import Utilities.MySQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CompanyDAO {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_COMPANY_ID, MySQLiteHelper.COLUMN_COMPANY_NAME, MySQLiteHelper.COLUMN_COMPANY_PASSWORD };
	  
	  public CompanyDAO(Context context) {
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
	  
	  public Company addCompany(int companyId, String companyName, String companyPassword) {
		    ContentValues values = new ContentValues();
		    values.put(MySQLiteHelper.COLUMN_COMPANY_ID, companyId);
		    values.put(MySQLiteHelper.COLUMN_COMPANY_NAME, companyName);
		    values.put(MySQLiteHelper.COLUMN_COMPANY_PASSWORD, companyPassword);

		    long insertId = database.insert(MySQLiteHelper.TABLE_COMPANIES, null, values);
		    Cursor cursor = database.query(MySQLiteHelper.TABLE_COMPANIES,
		        allColumns, null, null,
		        null, null, null);
		    cursor.moveToFirst();
		    Company newCompany = cursorToCompany(cursor);
		    cursor.close();
		    return newCompany;
		  }

	  public List<Company> getAllCompanies() {
	    List<Company> listOfCompanies = new ArrayList<Company>();

	    Cursor cursor = database.query(MySQLiteHelper.TABLE_COMPANIES,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Company hunt = cursorToCompany(cursor);
	      listOfCompanies.add(hunt);
	      cursor.moveToNext();
	    }
	    
	    cursor.close();
	    return listOfCompanies;
	  }

	  //http://stackoverflow.com/questions/6781954/android-3-0-couldnt-read-row-column-from-cursor-window
	  private Company cursorToCompany(Cursor cursor) {
	    Company company = new Company();
	    company.setCompanyId(cursor.getInt(0));
	    company.setCompanyName(cursor.getString(1));
	    company.setCompanyPassword(cursor.getString(2));
	    return company;
	  }
}
