package com.wendesday.bippobippo;
import android.util.Log;

public class DebugUtils {
	private static boolean mDebug = true;
	
	public static void Log(String msg) {
		if (mDebug)
			Log.d("BippoBippo", msg);
	}

}
