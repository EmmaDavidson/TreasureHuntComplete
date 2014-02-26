package com.application.treasurehunt;

import java.util.List;

import com.application.treasurehunt.R;

import sqlLiteDatabase.MapData;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//The big nerd ranch guide
//Also http://www.ezzylearning.com/tutorial.aspx?tid=1763429
	public class MapDataListAdapter extends ArrayAdapter<MapData>
	{
		private Context context;
		private List<MapData> listOfLeaderboardResults;
		
		TextView mParticipantId;
		
		List<MapData> listOfMapResults;
		
		public MapDataListAdapter(Context mapFragment, List<MapData> mapResults)
		{
			super(mapFragment, 0, mapResults);
			context = mapFragment;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null)
			{
				convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.map_data_list_item, null);
			}
			
			MapData mapDataResult = getItem(position);
			
			mParticipantId = (TextView) convertView.findViewById(R.id.map_data_participantId);
			mParticipantId.setText(mapDataResult.getParticipantId()+"");
			
			return convertView;
		}
	}
