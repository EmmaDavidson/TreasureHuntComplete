package Utilities;

import com.application.treasurehunt.R;

import android.content.Context;
import android.text.TextUtils;

public class ValidationHelper {

	/*
	 * Global variables used within ValidationHelper.
	 */
	private static ValidationHelper mValidationHelper;
	private Context mCurrentContext;
	
	/*Private constructor for this class.*/
	private ValidationHelper(Context context) {
		mCurrentContext = context;
	}
	
	/*Returns an instance of the singleton i.e. if it has already been created by another class,
	 * grab an instance of it, else, create a new instance. */
	public synchronized static ValidationHelper getInstance(Context context) {
		
		if(mValidationHelper == null) {
			mValidationHelper = new ValidationHelper(context.getApplicationContext());
		}
		return mValidationHelper;
	}
	
	public boolean isEmpty(String string) {
		if (!TextUtils.isEmpty(string)) {
			return false;			
		}
		return true;
	}
	
	public boolean isValidLength(String string, int minLength, int maxLength) {
		if(string.length() < minLength	
				|| string.length() >= maxLength) {
			return false;
		}
		return true;
	}
	
	public boolean isValidEmailFormat(String string) {
		if(!android.util.Patterns.EMAIL_ADDRESS.matcher(string).matches()) {
			return false;
		}
		return true;
	}
	
	public boolean isStringMatching(String stringOne, String stringTwo) {
		if(!stringOne.equals(stringTwo)){
			return false;
		}
		return true;
	}
	
	//http://stackoverflow.com/questions/11241690/regex-for-checking-if-a-string-is-strictly-alphanumeric
		public boolean isValidCharacters(String stringOne) {
			String regex = "^[a-zA-Z]*$";
			if(!stringOne.matches(regex)) {
				return false;
			}
			return true;
		}
	
	//http://stackoverflow.com/questions/11241690/regex-for-checking-if-a-string-is-strictly-alphanumeric
	public boolean isValidPassword(String stringOne) {
		String regex = "^[a-zA-Z0-9]*$";
		if(!stringOne.matches(regex)) {
			return false;
		}
		return true;
	}
	
	
}
