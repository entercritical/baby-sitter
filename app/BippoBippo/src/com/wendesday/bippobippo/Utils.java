package com.wendesday.bippobippo;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Utils {
	private TelephonyManager mTelephonyManager = null;
	private static Utils sInstance;
	private static Context mContext;
	
	private Utils() {		
	}
	
	public static synchronized Utils getInstance(Context context) {
		if (sInstance == null) {
			mContext = context;
			return new Utils();
		}
		return sInstance;
	}
	
	public String getPhoneNumber() {
		// Phone number
		if (mTelephonyManager == null)
			mTelephonyManager=(TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		
		return mTelephonyManager.getLine1Number();
	}
}
