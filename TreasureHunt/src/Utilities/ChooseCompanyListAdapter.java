package Utilities;

import java.util.List;
import sqlLiteDatabase.Company;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.application.treasurehunt.R;

public class ChooseCompanyListAdapter extends ArrayAdapter<Company> {

	private Context context;
	
	public ChooseCompanyListAdapter(Context thisContext, List<Company> companyResults)
	{
		super(thisContext, 0, companyResults);
		context = thisContext;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.choose_company_list_item, null);
		}
		
		Company companyResult = getItem(position);
		
		TextView nameText = (TextView) convertView.findViewById(R.id.company_name);
		nameText.setText(companyResult.getCompanyName());
		
		return convertView;
	}

}
