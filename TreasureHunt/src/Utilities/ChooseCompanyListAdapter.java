package Utilities;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import sqlLiteDatabase.Company;

import java.util.List;

import com.application.treasurehunt.R;

/* The purpose of this class is to help populate a custom list of Company data.
 *  Based on  http://www.ezzylearning.com/tutorial.aspx?tid=1763429.*/
public class ChooseCompanyListAdapter extends ArrayAdapter<Company> {

	/* Global variables for ChooseCompanyListAdapter */
	private Context mContext;
	
	/* Constructor */
	public ChooseCompanyListAdapter(Context thisContext, List<Company> companyResults) {
		super(thisContext, 0, companyResults);
		mContext = thisContext;
	}
	
	/* Method called 'for every item in the ListView (being populated)' i.e. it sets the text of the custom list item with the 
	 * Company data found at a defined position.*/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.choose_company_list_item, null);
		}
		
		Company companyResult = getItem(position);
		
		TextView nameText = (TextView) convertView.findViewById(R.id.company_name);
		nameText.setText(companyResult.getCompanyName());
		
		return convertView;
	}
}
