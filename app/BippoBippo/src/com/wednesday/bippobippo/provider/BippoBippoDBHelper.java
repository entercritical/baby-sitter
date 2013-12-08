package com.wednesday.bippobippo.provider;

import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wednesday.bippobippo.BippoBippo;


/**
 * 
 * This class helps open, create, and upgrade the database file.
 */
public class BippoBippoDBHelper extends SQLiteOpenHelper {

	private static final String TAG = "BippoBippoDBHelper";
	private static String DATABASE_NAME = "bippobippo.db";
	private static int DATABASE_VERSION = 1;

	BippoBippoDBHelper(Context context) {        
		// calls the super constructor, requesting the default cursor factory.
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.i(TAG, " called BippoBippoDBHelper");
	}



	/**
	 * 
	 * Creates the underlying database with table name and column names taken
	 * from the NotePad class.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		Log.i(TAG, "Bootstrapping database version: " + DATABASE_VERSION);
		
		// person table
		db.execSQL("CREATE TABLE " + BippoBippo.Person.TABLE_NAME + " ("
				+ BippoBippo.Person._ID + " INTEGER PRIMARY KEY, "
				+ BippoBippo.Person.DISPLAY_NAME + " TEXT, "
				+ BippoBippo.Person.PHONE_NUMBER + " TEXT, "
				+ BippoBippo.Person.EMERGENCY_NUMBER + " TEXT, "
				+ BippoBippo.Person.BIRTHDAY + " TEXT, " 
				+ BippoBippo.Person.DEFAULT_TEMPRATURE + " TEXT, "
				+ BippoBippo.Person.WET_SENSITIVITY + " TEXT " + " );");

		// sensor data table
		db.execSQL("CREATE TABLE " + BippoBippo.SensorData.TABLE_NAME + " ("
				+ BippoBippo.SensorData._ID + " INTEGER PRIMARY KEY, "
				+ BippoBippo.SensorData.HEAT + " TEXT, "
				+ BippoBippo.SensorData.WET + " TEXT, "
				+ BippoBippo.SensorData.BPM + " INTEGER DEFAULT 0, "
				+ BippoBippo.SensorData.MIC + " TEXT, "
				+ BippoBippo.SensorData.TIMESTAMP + " LONG " + ");");
//		// create view
//		createBuddyViews(db);
	}

	/**
	 * 
	 * Demonstrates that the provider must consider what happens when the
	 * underlying datastore is changed. In this sample, the database is upgraded
	 * the database by destroying the existing data. A real application should
	 * upgrade the database in place.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// Logs that the database is being upgraded
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		Log.i(TAG, " open the bippobippo db ");
		super.onOpen(db);
		
		/////////////////////////
		// test
//		createDummyPersonData(db);
//		createDummySensorData(db);

		
		/////////////////////////
		
	}

	private void createDummyPersonData(SQLiteDatabase db) {
		Cursor c = db.rawQuery(" SELECT _id FROM person;", null);
		if (c!=null && c.getCount() > 0) {
			c.close();
			return;
		}

		ContentValues values = new ContentValues();
        values.put(BippoBippo.Person.DISPLAY_NAME, "JOY");
		values.put(BippoBippo.Person.PHONE_NUMBER, "01022223333");
		values.put(BippoBippo.Person.BIRTHDAY, "2013/11/01");
		db.insert(BippoBippo.Person.TABLE_NAME,
				null, values);
	}

	private void createDummySensorData(SQLiteDatabase db) {
		Cursor c = db.rawQuery(" SELECT _id FROM sensordata;", null);
		if (c!= null && c.getCount() > 0) {
			c.close();
			return;
		}

    	Random random = new Random();
    	long time = System.currentTimeMillis();
    	int heat = 34 + random.nextInt(6);
    	int bpm = 70 + random.nextInt(80);
    	int mic = random.nextInt(10);
    	int wet = random.nextInt(1);


		ContentValues values = new ContentValues();
        
		for(int i=0; i<5000; i++){
			values.put(BippoBippo.SensorData.HEAT, heat);
			values.put(BippoBippo.SensorData.WET, wet);
			values.put(BippoBippo.SensorData.BPM, bpm);
			values.put(BippoBippo.SensorData.MIC, mic);
			values.put(BippoBippo.SensorData.TIMESTAMP, time);
		    db.insert(BippoBippo.SensorData.TABLE_NAME,
					null, values);
		    time = time + (10*1000);
	        heat = 34 + random.nextInt(6);
	    	bpm = 70 + random.nextInt(80);
	    	mic = random.nextInt(10);
	    	wet = random.nextInt(1);
			
		}
//
//
//		values.clear();
//		time = System.currentTimeMillis();
//		values.put(BippoBippo.SensorData.HEAT, "35");
//		values.put(BippoBippo.SensorData.WET, "0");
//		values.put(BippoBippo.SensorData.BPM, "100");
//		values.put(BippoBippo.SensorData.MIC, "10");
//		values.put(BippoBippo.SensorData.TIMESTAMP, time);
//	    db.insert(BippoBippo.SensorData.TABLE_NAME,
//				null, values);
//
//		values.clear();
//		time = System.currentTimeMillis();
//		values.put(BippoBippo.SensorData.HEAT, "36.5");
//		values.put(BippoBippo.SensorData.WET, "0");
//		values.put(BippoBippo.SensorData.BPM, "150");
//		values.put(BippoBippo.SensorData.MIC, "10");
//		values.put(BippoBippo.SensorData.TIMESTAMP, time);
//	    db.insert(BippoBippo.SensorData.TABLE_NAME,
//				null, values);
//
//		values.clear();
//		time = System.currentTimeMillis();
//		values.put(BippoBippo.SensorData.HEAT, "38");
//		values.put(BippoBippo.SensorData.WET, "1");
//		values.put(BippoBippo.SensorData.BPM, "150");
//		values.put(BippoBippo.SensorData.MIC, "11");
//		values.put(BippoBippo.SensorData.TIMESTAMP, time);
//	    db.insert(BippoBippo.SensorData.TABLE_NAME,
//				null, values);
//
//		values.clear();
//		time = System.currentTimeMillis();
//		values.put(BippoBippo.SensorData.HEAT, "33");
//		values.put(BippoBippo.SensorData.WET, "1");
//		values.put(BippoBippo.SensorData.BPM, "180");
//		values.put(BippoBippo.SensorData.MIC, "10");
//		values.put(BippoBippo.SensorData.TIMESTAMP, time);
//	    db.insert(BippoBippo.SensorData.TABLE_NAME,
//				null, values);
//	    
//		values.clear();
//		time = System.currentTimeMillis();
//		values.put(BippoBippo.SensorData.HEAT, "35");
//		values.put(BippoBippo.SensorData.WET, "1");
//		values.put(BippoBippo.SensorData.BPM, "90");
//		values.put(BippoBippo.SensorData.MIC, "100");
//		values.put(BippoBippo.SensorData.TIMESTAMP, time);
//	    db.insert(BippoBippo.SensorData.TABLE_NAME,
//				null, values);

	}

	
}