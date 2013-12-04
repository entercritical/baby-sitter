package com.wednesday.bippobippo;

import com.wednesday.bippobippo.BippoBippo.Person;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ContentResolverHelper {
	Context mContext;
	ContentResolver mContentResolver;
	
	public ContentResolverHelper(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
	}
	
	public void open() {
		
	}
	
	public void close() {
		mContext = null;
		mContentResolver = null;
	}
	
	public Uri insertSensorData(SensorDataModel sensor) {
		if (mContentResolver == null || sensor == null) {
			return null;
		}
		
		ContentValues values = new ContentValues();
		values.put(BippoBippo.SensorData.HEAT, sensor.getHeat());
		values.put(BippoBippo.SensorData.WET, sensor.getWet());
		values.put(BippoBippo.SensorData.BPM, sensor.getBpm());
		values.put(BippoBippo.SensorData.MIC, sensor.getMic());
		values.put(BippoBippo.SensorData.TIMESTAMP, sensor.getTimeStamp());
		
		Uri uri = mContentResolver.insert(BippoBippo.SensorData.CONTENT_URI, values);
		//DebugUtils.Log("ContentProviderHelper.insertSensorData() " + uri.toString());
		return uri;
	}
	
	public int deleteSensorData() {
		
		return 0;
	}
	
	public int deleteSensorDataAll() {
		return 0;
	}
	
	public void printLastSensorData() {
		
		Cursor cursor = mContentResolver.query(BippoBippo.SensorData.CONTENT_URI, null, null, null, null);
		
		if (cursor != null) {
			cursor.moveToLast();
			DebugUtils.Log("ContentProviderHelper.printLastSensorData() " 
			+ cursor.getColumnName(0) + ":" +cursor.getString(0) 
					+ " " + cursor.getColumnName(1) + ":" + cursor.getString(1)
					+ " " + cursor.getColumnName(2) + ":" + cursor.getString(2)
					+ " " + cursor.getColumnName(3) + ":" + cursor.getString(3)
					+ " " + cursor.getColumnName(4) + ":" + cursor.getString(4)
					+ " " + cursor.getColumnName(5) + ":" + cursor.getString(5));

			cursor.close();
		}
	}
	public Uri insertPerson(PersonModel person) {
		if (mContentResolver == null || person == null) {
			return null;
		}

		ContentValues values = new ContentValues();
		values.put(BippoBippo.Person.PHONE_NUMBER, person.getPhone());
		values.put(BippoBippo.Person.DISPLAY_NAME, person.getDisplayName());
		values.put(BippoBippo.Person.BIRTHDAY, person.getBirthDay());
		values.put(BippoBippo.Person.DEFAULT_TEMPRATURE, person.getDefaultTemprature());
		values.put(BippoBippo.Person.WET_SENSITIVITY, person.getWetSensitivity());
		
		return mContentResolver.insert(BippoBippo.Person.CONTENT_URI, values);
	}
	
	public int deletePerson() {
		return 0;
	}
	
	public int deletePersonALl() {
		return 0;
	}
	
	public int updatePerson(PersonModel person) {
		if (mContentResolver == null || person == null) {
			return 0;
		}
		ContentValues values = new ContentValues();
		values.put(BippoBippo.Person.PHONE_NUMBER, person.getPhone());
		values.put(BippoBippo.Person.DISPLAY_NAME, person.getDisplayName());
		values.put(BippoBippo.Person.BIRTHDAY, person.getBirthDay());
		values.put(BippoBippo.Person.DEFAULT_TEMPRATURE, person.getDefaultTemprature());
		values.put(BippoBippo.Person.WET_SENSITIVITY, person.getWetSensitivity());
		
		return mContentResolver.update(Person.CONTENT_URI, values, null, null);
	}
}
