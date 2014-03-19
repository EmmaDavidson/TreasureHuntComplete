package Utilities;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.application.treasurehunt.R;

import sqlLiteDatabase.Hunt;

import java.util.List;

/* The purpose of this class is to help populate a custom list of Hunt data.
 *  Based on  http://www.ezzylearning.com/tutorial.aspx?tid=1763429.*/
public class ChooseHuntListAdapter extends ArrayAdapter<Hunt> {
	
	/* Global variables for ChooseHuntListAdapter */
	private Context mContext;
	
	/* Constructor */
	public ChooseHuntListAdapter(Context thisContext, List<Hunt> huntResults) {
		super(thisContext, 0, huntResults);
		mContext = thisContext;
	}
	
	/* Method called 'for every item in the ListView (being populated)' i.e. it sets the text of the custom list item with the 
	 * Hunt data found at a defined position.*/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.choose_hunt_list_item, null);
		}
		
		Hunt huntResult = getItem(position);
		
		TextView nameText = (TextView) convertView.findViewById(R.id.choose_hunt_item);
		nameText.setText(huntResult.getHuntName());
		
		return convertView;
	}
}
