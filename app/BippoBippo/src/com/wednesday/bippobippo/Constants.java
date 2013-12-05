package com.wednesday.bippobippo;

public class Constants {
    
	// URL information 
	public static final  String USER_DATA_URL = "http://14.49.42.181:52273/user"; 
	public static final  String HEALTH_DATA_URL = "http://14.49.42.181:52273/healthinfo";
	
	// INTENT action for network service
	public static final String ACTION_SEND_USER_DATA = "com.wednesday.bippobippo.user.SEND";
	public static final String ACTION_SEND_HEALTH_DATA= "com.wednesday.bippobippo.health.SEND";
	public static final String ACTION_UPDATE_USER_DATA = "com.wednesday.bippobippo.user.UPDATE";
	
	// INTENT action for chart & settings activity
	public static final String ACTION_VIEW_CHART = "com.wednesday.bippobippo.VIEW_CHART";
	public static final String ACTION_VIEW_SETTINGS = "com.wednesday.bippobippo.VIEW_SETTING";
	public static final String ACTION_INIT_SETTINGS = "com.wednesday.bippobippo.INIT_SETTING";
	
	// JSON KEY
	public static final String HEAT = "heat";
	public static final String WET = "wet";
	public static final String BPM = "bpm";
	public static final String MIC = "mic";
	public static final String TIMESTAMP = "timestamp";
	
	//PERSON JSON KEY
	public static final String PHONE = "phone";
	public static final String DISPLAYNAME = "name";
	public static final String BIRTHDAY = "birth";
	
	// Preference KEY
	public static final String PREF_SETTINGS = "pref_settings";
	public static final String PREF_KEY = "init_settings";
	
	public static final String EXTRA_USER_DATA = "com.wednesday.bippobippo.EXTRA_USER_DATA";
	
}
