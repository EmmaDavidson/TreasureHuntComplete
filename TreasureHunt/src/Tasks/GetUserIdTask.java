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

public class GetUserIdTask extends AsyncTask<String, String, String> {

	String currentUser;
	JSONObject userIdResult;
	
	private static final String getUserIdUrl =  "http://192.168.1.74:80/webservice/returnCurrentUserId.php";
	public JSONParser jsonParserForRegister = new JSONParser();
	
	private static final String tagSuccess = "success";
	private static final String tagMessage = "message";
	private static final String tagResult = "result";

	public GetUserIdTask(String CurrentUser)
	{
		super();
		currentUser = CurrentUser;
		
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		
		int success;
		//GETTING THE USER ID
		List<NameValuePair> parametersForUserId = new ArrayList<NameValuePair>();
		//http://stackoverflow.com/questions/8603583/sending-integer-to-http-server-using-namevaluepair
		parametersForUserId.add(new BasicNameValuePair("email", currentUser));
		
		try{
			Log.d("request", "starting");
			JSONObject jsonFindUserId = jsonParserForRegister.makeHttpRequest(getUserIdUrl, "POST", parametersForUserId);
			Log.d("Get User Id Attempt", jsonFindUserId.toString());
			success = jsonFindUserId.getInt(tagSuccess);
			
			if(success == 1)
			{
				userIdResult = jsonFindUserId.getJSONObject("result");
				return jsonFindUserId.getString(jsonFindUserId.getJSONObject("result").toString());
				
			}
			else
			{
				Log.d("Getting User Id failed!", jsonFindUserId.getString(tagMessage));
				return jsonFindUserId.getString(tagMessage);
			}
		}
		catch (JSONException e) {
			
		}
		
		return null;
	}
	
//Should have on cancelled?
}

