package com.wendesday.bippobippo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
		DebugUtils.Log("ContentProviderHelper.insertSensorData() " + uri.toString());
		return uri;
	}
	
	public int deleteSensorData() {
		
		return 0;
	}
	
	public int deleteSensorDataAll() {
		return 0;
	}
	
	public void printSensorData() {
		//mContentResolver.query(BippoBippo.SensorData.CONTENT_URI, projection, selection, selectionArgs, sortOrder)
	}
	public Uri insertPerson(PersonModel person) {
		if (mContentResolver == null || person == null) {
			return null;
		}

		ContentValues values = new ContentValues();
		values.put(BippoBippo.Person.PHONE_NUMBER, person.getPhone());
		values.put(BippoBippo.Person.DISPLAY_NAME, person.getDisplayName());
		values.put(BippoBippo.Person.BIRTHDAY, person.getBirthDay());
		
		return mContentResolver.insert(BippoBippo.Person.CONTENT_URI, values);
	}
	
	public int deletePerson() {
		return 0;
	}
	
	public int deletePersonALl() {
		return 0;
	}
}
