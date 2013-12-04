package com.wednesday.bippobippo.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.wednesday.bippobippo.BippoBippo;


public class BippoBippoProvider extends ContentProvider {
	
	// Used for debugging and logging
    private static final String TAG = "BippoBippoProvider";
    
    /**
     * A projection map used to select columns from the database
     */
    
    private static HashMap<String, String> sPersonProjectionMap;
    
    private static HashMap<String, String> sSensorDataProjectionMap;
    
    private static HashMap<String, String> sSensorDataSummaryProjectionMap;
    
    
    /*
     * Constants used by the Uri matcher to choose an action based on the pattern
     * of the incoming URI
     */
    // The incoming URI matches the Notes URI pattern
    
    private static final int PERSON = 1;

    private static final int SENSOR_DATA = 10;
    private static final int SENSOR_DATA_SUMMARY = 11;
    
    /**
     * A UriMatcher instance
     */
    private static final UriMatcher sUriMatcher;

    // Handle to a new DatabaseHelper.
    private BippoBippoDBHelper mOpenHelper;
    
    /**
     * A block that instantiates and sets static objects
     */
    static {

        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
        sUriMatcher.addURI(BippoBippo.AUTHORITY, "person", PERSON);        
        sUriMatcher.addURI(BippoBippo.AUTHORITY, "sensordata", SENSOR_DATA);
        sUriMatcher.addURI(BippoBippo.AUTHORITY, "sensordata/summary", SENSOR_DATA_SUMMARY);

        /*
         * Creates and initializes a projection map that returns all columns
         */

        // Creates a new projection map instance. The map returns a column name
        // given a string. The two are usually equal.
        sPersonProjectionMap = new HashMap<String, String>();
        sPersonProjectionMap.put(BippoBippo.Person._ID, BippoBippo.Person._ID);
        sPersonProjectionMap.put(BippoBippo.Person.DISPLAY_NAME, BippoBippo.Person.DISPLAY_NAME);
        sPersonProjectionMap.put(BippoBippo.Person.PHONE_NUMBER, BippoBippo.Person.PHONE_NUMBER);
        sPersonProjectionMap.put(BippoBippo.Person.BIRTHDAY, BippoBippo.Person.BIRTHDAY);
        sPersonProjectionMap.put(BippoBippo.Person.DEFAULT_TEMPRATURE, BippoBippo.Person.DEFAULT_TEMPRATURE);
        sPersonProjectionMap.put(BippoBippo.Person.WET_SENSITIVITY, BippoBippo.Person.WET_SENSITIVITY);
        
        sSensorDataProjectionMap = new HashMap<String, String>();
        // Maps the string "_ID" to the column name "_ID"
        sSensorDataProjectionMap.put(BippoBippo.SensorData._ID, BippoBippo.SensorData._ID);
        sSensorDataProjectionMap.put(BippoBippo.SensorData.HEAT, BippoBippo.SensorData.HEAT);
        sSensorDataProjectionMap.put(BippoBippo.SensorData.BPM, BippoBippo.SensorData.BPM);        
        sSensorDataProjectionMap.put(BippoBippo.SensorData.WET, BippoBippo.SensorData.WET);
        sSensorDataProjectionMap.put(BippoBippo.SensorData.MIC, BippoBippo.SensorData.MIC);
        sSensorDataProjectionMap.put(BippoBippo.SensorData.TIMESTAMP, BippoBippo.SensorData.TIMESTAMP);
        
        sSensorDataSummaryProjectionMap = new HashMap<String, String>();
        sSensorDataSummaryProjectionMap.put(BippoBippo.SensorData._ID, BippoBippo.SensorData._ID);
        sSensorDataSummaryProjectionMap.put(BippoBippo.SensorData.HIGHEST, "MAX(heat)");
        sSensorDataSummaryProjectionMap.put(BippoBippo.SensorData.AVERAGE, "AVG(heat)");
        sSensorDataSummaryProjectionMap.put(BippoBippo.SensorData.LOWEST, "MIN(heat)");
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		// Opens the database object in "write" mode.
	    SQLiteDatabase db = mOpenHelper.getWritableDatabase();	
	    int intUri = sUriMatcher.match(uri);
	    
	    switch(intUri){
	    case PERSON : {
	    	db.delete(BippoBippo.Person.TABLE_NAME, selection, selectionArgs);
	    	break;
	    }
	    case SENSOR_DATA : {
	    	db.delete(BippoBippo.SensorData.TABLE_NAME, selection, selectionArgs);
	    	break;
	    }
	    default :
	    	throw new IllegalArgumentException("Unknown URI " + uri);
	    }
	    
		return count;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		// Opens the database object in "write" mode.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();			
		ContentValues localValues = new ContentValues();
		localValues.putAll(values);
		
		int intUri = sUriMatcher.match(uri);
		long retId = 0;
		
		switch(intUri){
		case PERSON : {
			retId = db.insert(BippoBippo.Person.TABLE_NAME, null, localValues);
			break;
		}
		case SENSOR_DATA : {
			retId = insertSensorData(db,localValues);
			break;
		}

		default :
			throw new IllegalArgumentException("Unknown URI " + uri);
			
		}
		
		Uri retUri = uri.buildUpon().appendPath(String.valueOf(retId)).build();
		return retUri;
	}


	private long insertSensorData(SQLiteDatabase db, ContentValues localValues) {
		long retId = db.insert(BippoBippo.SensorData.TABLE_NAME, null, localValues);
		return retId;
	}

/**
    *
    * Initializes the provider by creating a new DatabaseHelper. onCreate() is called
    * automatically when Android creates the provider in response to a resolver request from a
    * client.
    */
	@Override
	public boolean onCreate() {
		Log.i(TAG, "onCreate");
	    // Creates a new helper object. Note that the database itself isn't opened until
	    // something tries to access it, and it's only created if it doesn't already exist.
	    mOpenHelper = new BippoBippoDBHelper(getContext());

	    // Assumes that any failures will be reported by a thrown exception.
	    return true;
	}

	/**
	 * This method is called when a client calls
	 * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}.
	 * Queries the database and returns a cursor containing the results.
	 *
	 * @return A cursor containing the results of the query. The cursor exists but is empty if
	 * the query returns no results or an exception occurs.
	 * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
	           String sortOrder) {
		
		// Opens the database object in "read" mode, since no writes need to be done.
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();		
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = sortOrder;
        
        int intUri = sUriMatcher.match(uri);

		switch (intUri) {
		
		case PERSON : {
			qb.setTables(BippoBippo.Person.TABLE_NAME);
			qb.setProjectionMap(sPersonProjectionMap);
			break;
		}

		case SENSOR_DATA: {
			qb.setTables(BippoBippo.SensorData.TABLE_NAME);
			qb.setProjectionMap(sSensorDataProjectionMap);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = BippoBippo.SensorData.DEFAULT_SORT_ORDER;
			} 
			break;
		}
		
		case SENSOR_DATA_SUMMARY:{
			//StringBuilder sb = new StringBuilder();
			//sb.append(" ( SELECT _id, MAX(heat) AS highest, AVG(heat) AS average, MIN(heat) AS lowest" +
			//           " FROM " + BippoBippo.SensorData.TABLE_NAME + ") as " );
			//qb.setTables(sb.toString());
			qb.setTables(BippoBippo.SensorData.TABLE_NAME);
			qb.setProjectionMap(sSensorDataSummaryProjectionMap);		
			break;
		}

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

        /*
         * Performs the query. If no problems occur trying to read the database, then a Cursor
         * object is returned; otherwise, the cursor variable contains null. If no records were
         * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
         */
        Cursor c = qb.query(
            db,            // The database to query
            projection,    // The columns to return from the query
            selection,     // The columns for the where clause
            selectionArgs, // The values for the where clause
            null,          // don't group the rows
            null,          // don't filter by row groups
            orderBy        // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
        
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		
		int count = 0;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		int intUri = sUriMatcher.match(uri);
		
		switch(intUri){
		case PERSON: {
			count = db.update(BippoBippo.Person.TABLE_NAME, values, selection, selectionArgs);			
			break;
		}
		default :
			throw new IllegalArgumentException("Unknown URI " + uri);			
		}
		
		return count;
	}
	
	private void convertToStringValues (ContentValues values, String key, String value){
		
		if(TextUtils.isEmpty(value)){
			values.putNull(key);
		} else{
			values.put(key, value);
		}
		
	}
    
    

}

