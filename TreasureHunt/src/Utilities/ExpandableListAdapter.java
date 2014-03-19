package Utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.application.treasurehunt.R;

import java.util.HashMap;
import java.util.List;

/* The purpose of this class is to aid the creation of a generic expandable list to 'group data' and that has
 *  the 'capability of expanding and collapsing' its groups. 
 * Entire class Taken from the website below. */
//
//http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
//http://stackoverflow.com/questions/19494572/cannot-instantiate-the-type-expandablelistadapter
public class ExpandableListAdapter extends BaseExpandableListAdapter{

	/* Global variables for ExpandableListAdapter */
	private Context mContext;
    private List<String> mListDataHeader;
    private HashMap<String, List<String>> mListDataChild;
 
    /* Constructor */
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
    }
 
    /* Method returning the child item identified by parameters supplied*/
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                .get(childPosititon);
    }
 
    /* Method returning the child item's position identified by the parameters supplied*/
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    /* Method called for every (child) item in the ExpandableListView (being populated) i.e. it sets the text of the 
     * custom child list item with the data found at a defined position.*/
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = (String) getChild(groupPosition, childPosition);
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.hunts_group_list_item, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.hunts_list_item_label);
 
        txtListChild.setText(childText);
        return convertView;
    }
 
    /* Method returning the number of child items for a given group.*/
    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                .size();
    }
 
    /* Method returning a group at a giving position.*/
    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }
 
    /* Method returning the number of groups.*/
    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }
 
    /* Method returning the group item's position identified by the parameters supplied*/
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    /* Method called for every (group) item in the ExpandableListView (being populated) i.e. it sets the text of the 
     * custom group list item with the data found at a defined position.*/
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.hunts_group_list, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.hunts_list_header_label);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
        return convertView;
    }
 
    /* Method returning whether or not StableIds are present. Not used but required by abstract class.*/
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    /* Method returning whether or not a child is selectable. Not used but required by abstract class. */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
