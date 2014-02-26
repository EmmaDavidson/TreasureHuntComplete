package Utilities;

import java.util.List;

import sqlLiteDatabase.Leaderboard;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.application.treasurehunt.LeaderboardActivity;
import com.application.treasurehunt.R;

//The big nerd ranch guide
//Also http://www.ezzylearning.com/tutorial.aspx?tid=1763429
	public class LeaderboardListAdapter extends ArrayAdapter<Leaderboard>
	{
		private Context context;
		private List<Leaderboard> listOfLeaderboardResults;
		
		public LeaderboardListAdapter(Context thisContext, List<Leaderboard> leaderboardResults)
		{
			super(thisContext, 0, leaderboardResults);
			context = thisContext;

		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null)
			{
				convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.leaderboard_list_item, null);
			}
			
			Leaderboard leaderBoardResult = getItem(position);
			
			TextView nameText = (TextView) convertView.findViewById(R.id.leaderboard_name);
			nameText.setText(leaderBoardResult.getUserName());
			
			TextView tallyText = (TextView) convertView.findViewById(R.id.leaderboard_tally_score);
			tallyText.setText("Current Score:" +leaderBoardResult.getUserTally());
			
			return convertView;
		}
	}
