package com.wednesday.bippobippo;

public class Constants {
    
	// URL information 
	public static final String BASE_URL = "http://14.49.42.181:52273";
	public static final String USER_DATA_URL = BASE_URL+ "/user"; 
	public static final String HEALTH_DATA_URL = BASE_URL + "/healthinfo";
	
	public static final String FEVER_OVERVIEW_URL = BASE_URL + "/guide/fever/Fever_Outline";
	public static final String FEVER_SOLUTION_URL = BASE_URL + "/guide/fever/Fever_Solution";
	public static final String FEVER_NOTICE_URL = BASE_URL + "/guide/fever/Fever_Attention";
	
	public static final String DIARRHEA_OVERVIEW_URL = BASE_URL + "/guide/diarrhea/Diarrhea_Outline";
	public static final String DIARRHEA_SOLUTION_URL = BASE_URL + "/guide/diarrhea/Diarrhea_Solution";
	public static final String DIARRHEA_NOTICE_URL = BASE_URL + "/guide/diarrhea/Diarrhea_Attention";	
	
	public static final String HEAT_STATISTICS_URL = BASE_URL + "/statistics";
	
	// INTENT action for network service
	public static final String ACTION_SEND_USER_DATA = "com.wednesday.bippobippo.user.SEND";
	public static final String ACTION_SEND_HEALTH_DATA= "com.wednesday.bippobippo.health.SEND";
	public static final String ACTION_UPDATE_USER_DATA = "com.wednesday.bippobippo.user.UPDATE";
	
	// INTENT action for chart & settings activity
	public static final String ACTION_VIEW_CHART = "com.wednesday.bippobippo.VIEW_CHART";
	public static final String ACTION_VIEW_SETTINGS = "com.wednesday.bippobippo.VIEW_SETTING";
	public static final String ACTION_INIT_SETTINGS = "com.wednesday.bippobippo.INIT_SETTING";
	
	public static final String ACTION_VIEW_FEVER_DISCRIPTION = "com.wednesday.bippobippo.VIEW_FEVER_DISCRIPTION";
	public static final String ACTION_VIEW_FEVER_SOLUTION = "com.wednesday.bippobippo.VIEW_FEVER_SOLUTION";
	public static final String ACTION_VIEW_FEVER_NOTICE = "com.wednesday.bippobippo.VIEW_FEVER_NOTICE";
	
	public static final String ACTION_VIEW_DIARRHEA_DISCRIPTION = "com.wednesday.bippobippo.VIEW_DIARRHEA_DISCRIPTION";
	public static final String ACTION_VIEW_DIARRHEA_SOLUTION = "com.wednesday.bippobippo.VIEW_DIARRHEA_SOLUTION";
	public static final String ACTION_VIEW_DIARRHEA_NOTICE = "com.wednesday.bippobippo.VIEW_DIARRHEA_NOTICE";
	
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
	
    /** Timeout (in ms) we specify for each http request */
    public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
    public static final int ONE_DAY = 6 * 60 * 24 ;
    
    // statistics json key
    public static final String MY_AVG_HEAT = "myAvgHeat";
    public static final String MY_MIN_HEAT = "myMinHeat";
    public static final String MY_MAX_HEAT = "myMaxHeat";
    public static final String AVG_HEAT = "avgHeat";
    public static final String MIN_HEAT ="minHeat";
    public static final String MAX_HEAT = "maxHeat";
	
}
