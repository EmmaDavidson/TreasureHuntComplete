package Utilities;

import java.util.List;
import sqlLiteDatabase.Leaderboard;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.application.treasurehunt.R;

/* The purpose of this class is to help populate a custom list of Leaderboard data.
 *  Based on  http://www.ezzylearning.com/tutorial.aspx?tid=1763429.*/
public class LeaderboardListAdapter extends ArrayAdapter<Leaderboard> {
	
	/* Global variables for LeaderboardListAdapter */
	private Context mContext;
	
	/* Constructor */
	public LeaderboardListAdapter(Context thisContext, List<Leaderboard> leaderboardResults) {
		super(thisContext, 0, leaderboardResults);
		mContext = thisContext;
	}
	
	/* Method called 'for every item in the ListView (being populated)' i.e. it sets the text of the custom list item with the 
	 * Leaderboard data found at a defined position.*/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.leaderboard_list_item, null);
		}
		
		Leaderboard leaderBoardResult = getItem(position);
		
		TextView nameText = (TextView) convertView.findViewById(R.id.leaderboard_name);
		nameText.setText(leaderBoardResult.getUserName());
		
		TextView tallyText = (TextView) convertView.findViewById(R.id.leaderboard_tally_score);
		tallyText.setText("Current Score:" +leaderBoardResult.getUserTally());
		
		return convertView;
	}
}
