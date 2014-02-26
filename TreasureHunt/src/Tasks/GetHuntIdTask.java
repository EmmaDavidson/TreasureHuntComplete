package Tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import Utilities.JSONParser;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.application.treasurehunt.RegisterWithHuntActivity;

public class GetHuntIdTask extends AsyncTask<String, String, String> {

	String currentHunt;
	JSONObject huntIdResult;
	
	private static final String getHuntIdUrl =  "http://192.168.1.74:80/webservice/returnCurrentHuntId.php";
	public JSONParser jsonParserForRegister = new JSONParser();
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	private static final String tagResult = "result";
	
	//http://stackoverflow.com/questions/3075009/android-how-can-i-pass-parameters-to-asynctasks-onpreexecute
	public GetHuntIdTask(String CurrentHunt) 
	{
		super();
		currentHunt = CurrentHunt;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		int huntIdSuccess;		
		
		try {
					
			//GETTING THE HUNT ID
			List<NameValuePair> parametersForHuntId = new ArrayList<NameValuePair>();
			//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
			parametersForHuntId.add(new BasicNameValuePair("hunt", currentHunt));
			
			Log.d("request", "starting");
			JSONObject jsonFindHuntId = jsonParserForRegister.makeHttpRequest(getHuntIdUrl, "POST", parametersForHuntId);
			Log.d("Register With Hunt Attempt", jsonFindHuntId.toString());
			huntIdSuccess = jsonFindHuntId.getInt(tagSuccess);
			if(huntIdSuccess == 1)
			{
				return jsonFindHuntId.getString(jsonFindHuntId.getJSONObject("result").toString());			
			}
			else
			{
				Log.d("Getting Hunt Id failed!", jsonFindHuntId.getString(tagMessage));
				return jsonFindHuntId.getString(tagMessage);
			}
		}catch (JSONException e) {
			
			}
		return null;	
	}
	

	
	public static interface iGetHuntIdTask
	{
		
	}
	
	//on cancelled?
}