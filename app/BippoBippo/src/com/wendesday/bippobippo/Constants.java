package com.wendesday.bippobippo;

public class Constants {
    
	// URL information 
	public static final  String USER_DATA_URL = "http://14.49.42.181:52273/user"; 
	public static final  String HEALTH_DATA_URL = "http://14.49.42.181:52273/healthinfo";
	
	// INTENT action for network service
	public static final String ACTION_SEND_USER_DATA = "com.wendesday.bippobippo.user.SEND";
	public static final String ACTION_SEND_HEALTH_DATA= "com.wnedesday.bippobippo.health.SEND";
	
	// INTENT action for chart activity
	public static final String ACTION_VIEW_CHART = "com.wendesday.bippobippo.VIEW_CHART";	
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
	
}
