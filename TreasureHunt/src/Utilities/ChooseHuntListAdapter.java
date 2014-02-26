package Utilities;

import java.util.List;

import sqlLiteDatabase.Hunt;
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
	public class ChooseHuntListAdapter extends ArrayAdapter<Hunt>
	{
		private Context context;
		
		public ChooseHuntListAdapter(Context thisContext, List<Hunt> huntResults)
		{
			super(thisContext, 0, huntResults);
			context = thisContext;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null)
			{
				convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.choose_hunt_list_item, null);
			}
			
			Hunt huntResult = getItem(position);
			
			TextView nameText = (TextView) convertView.findViewById(R.id.choose_hunt_item);
			nameText.setText(huntResult.getHuntName());
			
			return convertView;
		}
	}
