package com.application.treasurehunt;

import java.util.List;

import com.application.treasurehunt.R;
import com.application.treasurehunt.R.id;
import com.application.treasurehunt.R.layout;
import com.application.treasurehunt.R.menu;

import sqlLiteDatabase.Leaderboard;
import sqlLiteDatabase.MapData;
import sqlLiteDatabase.MapDataDAO;
import Utilities.LeaderboardListAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MapListFragment extends Fragment {

	private List<MapData> mRuns;
	private ListView mMapDataListView;
	MapDataListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map_list, container, false);
		mMapDataListView = (ListView) view.findViewById(R.id.map_list);
		mRuns = MapManager.get(getActivity()).queryMapData();
		
		adapter = new MapDataListAdapter(getActivity(), mRuns);
		mMapDataListView.setAdapter(adapter);
		
		mMapDataListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MapData run = adapter.getItem((int)id);
				
				Intent i = new Intent(getActivity(), MapActivity.class);
				i.putExtra("userParticipantIdForMap",(int) run.getParticipantId());
				startActivity(i);

			}
		});
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.run_list_items, menu);
	} 
	
	//http://mobileorchard.com/android-app-development-menus-part-1-options-menu/
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_item_new_map:
			Intent i = new Intent(getActivity(), MapActivity.class);
			startActivityForResult(i, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if( requestCode == 0)
		{
			//this is where mCursor.requery() should have been 
			mRuns = MapManager.get(getActivity()).queryMapData();
			adapter = new MapDataListAdapter(getActivity(), mRuns);
			mMapDataListView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}
}
