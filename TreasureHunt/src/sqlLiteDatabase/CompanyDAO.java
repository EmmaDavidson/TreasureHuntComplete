/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
package sqlLiteDatabase;

import Mapping.MapManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/* This singleton class handles the interaction between the application and SQLite (local) 
 * database for the Company table.*/

public class CompanyDAO {

	  /* Global variables for CompanyDAO*/
      private static CompanyDAO mCompanyDAO;
	  private SQLiteDatabase mDatabase;
	  private MySQLiteHelper mDbHelper;
	  private String[] mAllColumns = { MySQLiteHelper.COLUMN_COMPANY_ID, MySQLiteHelper.COLUMN_COMPANY_NAME, MySQLiteHelper.COLUMN_COMPANY_PASSWORD };
	  
	  /* Constructor*/
	  private CompanyDAO(Context context) {
	    mDbHelper = new MySQLiteHelper(context);
	  }
	  
	  /* Method that returns an instance of MapManager.*/
	  public static CompanyDAO getInstance(Context c) {
			
			if(mCompanyDAO == null) {
				mCompanyDAO = new CompanyDAO(c.getApplicationContext());
			}
			return mCompanyDAO;
	   }
  

	  /* Method handling what happens when the helper is first opened. It retrieves access to the SQLite (local) database. */
	  public void open() throws SQLException {
		  
		  mDatabase = mDbHelper.getWritableDatabase();
		  mDatabase = mDbHelper.getWritableDatabase();

		  //refreshCompanies();
	  }
	  
	  /* Method handling what happens when the helper is closed. It closes access to the SQLite (local) database.*/
	  public void close() {
	      mDbHelper.close();
	  }
	  
	  /* Method that removes all of the current data stored in the Company table.*/
	  public void refreshCompanies() {
		  
		  mDbHelper.refreshTable(mDatabase, MySQLiteHelper.TABLE_COMPANIES);
	  }
	  
	  /* Method that inserts a new row into the Company table with the data supplied. It returns the company newly added. */
	  public Company addCompany(int companyId, String companyName, String companyPassword) {
		  
		    ContentValues values = new ContentValues();
		    values.put(MySQLiteHelper.COLUMN_COMPANY_ID, companyId);
		    values.put(MySQLiteHelper.COLUMN_COMPANY_NAME, companyName);
		    values.put(MySQLiteHelper.COLUMN_COMPANY_PASSWORD, companyPassword);

		    long insertId = mDatabase.insert(MySQLiteHelper.TABLE_COMPANIES, null, values);
		    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_COMPANIES,
		        mAllColumns, null, null,
		        null, null, null);
		    cursor.moveToFirst();
		    Company newCompany = cursorToCompany(cursor);
		    cursor.close();
		    return newCompany;
		  }

	  /* Method returning a list of all companies that are currently found in the SQLite (local) database. */
	  public List<Company> getAllCompanies() {
		  
	    List<Company> listOfCompanies = new ArrayList<Company>();

	    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_COMPANIES,
	        mAllColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Company hunt = cursorToCompany(cursor);
	      listOfCompanies.add(hunt);
	      cursor.moveToNext();
	    }
	    
	    cursor.close();
	    return listOfCompanies;
	  }

	  /* A cursor that creates a Company instance from a given Cursor i.e. retrieves the data found in a single row
	   * returned by a query for the Company table.*/
	  //http://stackoverflow.com/questions/6781954/android-3-0-couldnt-read-row-column-from-cursor-window
	  private Company cursorToCompany(Cursor cursor) {
		  
	    Company company = new Company();
	    company.setAdministratorId(cursor.getInt(0));
	    company.setCompanyName(cursor.getString(1));
	    company.setCompanyPassword(cursor.getString(2));
	    return company;
	  }
}
