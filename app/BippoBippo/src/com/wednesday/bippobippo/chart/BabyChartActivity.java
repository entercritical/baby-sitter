package com.wednesday.bippobippo.chart;

import java.io.IOException;

import org.achartengine.GraphicalView;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wednesday.bippobippo.BippoBippo;
import com.wednesday.bippobippo.BippoBippo.Person;
import com.wednesday.bippobippo.BippoBippo.SensorData;
import com.wednesday.bippobippo.Constants;
import com.wednesday.bippobippo.Utils;
import com.wednesday.bippobippo.network.NetworkCommunicator;
import com.wednesday.bippobippo.network.NetworkUtils;
import com.wednesday.bippobippo.DebugUtils;
import com.wednesday.bippobippo.R;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class BabyChartActivity extends Activity {
	
	private GraphicalView mTempratureChart;	
	private Spinner mPeriodSpinner;
    private LinearLayout mChartContainer;
	//private GraphicalView mBpmChart;
	private TextView[] mTextView = new TextView[6];
	//private int mPeriod=30;
	private String mUserNumber ;
	public static String[] SUMMARY_PROJECTION = new String[] {BippoBippo.SensorData.HIGHEST,
                                                        BippoBippo.SensorData.AVERAGE,
                                                        BippoBippo.SensorData.LOWEST};
	public final String oneDay = "1 day";
	public final String oneWeek = "1 week";
	public final String oneMonth = "1 month";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_babychart);
		mChartContainer = (LinearLayout) findViewById(R.id.chart_temprature); 
		
        mTextView[0] = (TextView) findViewById(R.id.my_baby_highest);
        mTextView[1] = (TextView) findViewById(R.id.my_baby_average);
        mTextView[2] = (TextView) findViewById(R.id.my_baby_lowest);
        mTextView[3] = (TextView) findViewById(R.id.other_baby_highest);
        mTextView[4] = (TextView) findViewById(R.id.other_baby_average);
        mTextView[5] = (TextView) findViewById(R.id.other_baby_lowest); 
        
		mUserNumber = getUserNumber();
		
		getActionBar().setTitle("Chart");
		
		mPeriodSpinner = (Spinner) findViewById(R.id.chart_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 
				R.array.period, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPeriodSpinner.setAdapter(adapter);
		
		mPeriodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {				
				String periodValue = (String) parent.getItemAtPosition(pos);
				DebugUtils.Log(" pos value : " + periodValue);
				String period = convertPeriod(periodValue);
				mChartContainer.removeAllViews();				
				String[] params = new String[]{period};
				new DownloadChartValueTask().execute(params);
				new DownloadStatisticValueTask().execute(params);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});

	}
	
	protected String convertPeriod(String periodValue) {
		String period = null;
		if(oneDay.equals(periodValue)){
			period = "1";
		}else if(oneWeek.equals(periodValue)){
			period = "7";
		}else if(oneMonth.equals(periodValue)){
			period = "30";
		}
		return period;
	}


	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		
		super.onPause();
	}

	@Override
	protected void onResume() {		
		super.onResume();
		
	}
	
	private class DownloadChartValueTask extends AsyncTask<String, Void, double[]>{

		@Override
		protected void onPostExecute(double[] result) {
			if(result != null && result.length > 0){
				mTempratureChart = (GraphicalView) new ShowBabyStatusChart().execute(BabyChartActivity.this, result);
				mChartContainer.addView(mTempratureChart);
			}else{
				Toast toast = Toast.makeText(BabyChartActivity.this, "Data is empty !!! please turn on the app.", 3);
				toast.show();
				//finish();
			}

		}

		@Override
		protected double[] doInBackground(String... params) {
			final String period = params[0];
			try {
				return downloadServerData(period);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		protected double[] downloadServerData(String period) 
				throws ClientProtocolException, IOException, JSONException{

            final String chartUri = Constants.HEALTH_DATA_URL + "/" + mUserNumber + "/" + period ;
			DebugUtils.Log(" @@ Get chart uri : " + chartUri);
			
	        HttpGet httpget = new HttpGet(chartUri);
	        httpget.setHeader("Content-type", "application/json");;
	        // Execute the request
	        HttpResponse response;
	        
	        response = NetworkUtils.getHttpClient().execute(httpget);;
	        String resp = EntityUtils.toString(response.getEntity());
	        DebugUtils.Log(" @@ Server chart response " + resp);    
	        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){	        	
	        	final JSONArray serverData = new JSONArray(resp);
	        	int length = serverData.length();
	        	double[] heatValues = new double[length];
	        	for(int i=0; i< length ; i++ ){
	        		JSONObject jsonValue = serverData.getJSONObject(i);
	        		double heat = jsonValue.getDouble(Constants.HEAT);
	        		heatValues[i] = heat;        		
	        	}
	        	return heatValues;
	        }
	        
	        return null;
		}
		
	}
	
	private class DownloadStatisticValueTask extends AsyncTask<String, Void, String[]>{

		@Override
		protected void onPostExecute(String[] result) {
			setSummaryData(result);

		}

		@Override
		protected String[] doInBackground(String... params) {
			final String period = params[0];

			try {
				return downloadServerData(period);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		protected String[] downloadServerData(String period) 
				throws ClientProtocolException, IOException, JSONException{
            final String chartUri = Constants.HEAT_STATISTICS_URL + "/" + mUserNumber + "/" + period ;
			DebugUtils.Log(" @@ statistics uri : " + chartUri);
			
	        HttpGet httpget = new HttpGet(chartUri);
	        httpget.setHeader("Content-type", "application/json");;
	        // Execute the request
	        HttpResponse response;
	        
	        response = NetworkUtils.getHttpClient().execute(httpget);;
	        String resp = EntityUtils.toString(response.getEntity());
	        DebugUtils.Log(" @@ Server statistics response " + resp);   
        	//String[] heatStatisticValues = new String[length];
        	
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				//final JSONArray serverData = new JSONArray(resp);
				final JSONObject jsonValue = new JSONObject(resp);			
				String heatStatisticValues[] = new String[6];
				//jsonValue = serverData.getJSONObject(0);
				//double myMaxHeat = ;
				heatStatisticValues[0] = jsonValue.getString(Constants.MY_MAX_HEAT);
				//double myAvgHeat = jsonValue.getDouble(Constants.MY_AVG_HEAT);
				heatStatisticValues[1] = jsonValue.getString(Constants.MY_AVG_HEAT);;
				//double myMinHeat = jsonValue.getDouble(Constants.MY_MIN_HEAT);
				heatStatisticValues[2] = jsonValue.getString(Constants.MY_MIN_HEAT);;
				//double maxHeat = jsonValue.getDouble(Constants.MAX_HEAT);
				heatStatisticValues[3] = jsonValue.getString(Constants.MAX_HEAT);
				//double avgHeat = jsonValue.getDouble(Constants.AVG_HEAT);
				heatStatisticValues[4] = jsonValue.getString(Constants.AVG_HEAT);
				heatStatisticValues[5] = jsonValue.getString(Constants.MIN_HEAT);

				return heatStatisticValues;
			}
	        
	        return null;
		}
	}
	
	protected String getUserNumber() {
		String number = null;
		Cursor cursor = getContentResolver().query(Person.CONTENT_URI, new String[]{Person.PHONE_NUMBER},
				null, null, null);
		if(cursor !=null && cursor.moveToFirst()){
			number = cursor.getString(0);
			cursor.close();
		}
		if(TextUtils.isEmpty(number)){
			number = Utils.getInstance(getApplicationContext()).getPhoneNumber();
		}
		return number;
	}

	private void getSummaryData() {
		
		Cursor cursor = getContentResolver().query(SensorData.SUMMARY_URI, SUMMARY_PROJECTION, 
				null, null, null);
		if(cursor != null && cursor.moveToFirst()){
			mTextView[0].setText(getString(R.string.highest_temprature)  + " : " +  cursor.getString(0));
			String avg = cursor.getString(1);
			if(!TextUtils.isEmpty(avg)){
				avg = String.format("%.1f", Double.valueOf(avg));
			}
			mTextView[1].setText(getString(R.string.average_temprature)  + " : " + avg);
			mTextView[2].setText(getString(R.string.lowest_temprature)  + " : " + cursor.getString(2));
			cursor.close();			
		}
		
	}

	private void setSummaryData(String[] values) {
		
		String myMaxHeat = values[0];
		String myAvgHeat = values[1];
		String myMinHeat = values[2];
		String otherMaxHeat = values[3];
		String otherAvgHeat = values[4];
		String otherMinHeat = values[5];
		

//		mTextView[0].setText(getString(R.string.highest_temprature) + " : "
//				+ convertHeatString(myMaxHeat));
//		mTextView[1].setText(getString(R.string.average_temprature) + " : "
//				+ convertHeatString(myAvgHeat));
//		mTextView[2].setText(getString(R.string.lowest_temprature) + " : "
//				+ convertHeatString(myMinHeat));
		mTextView[0].setText(convertHeatString(myMaxHeat));
		mTextView[1].setText(convertHeatString(myAvgHeat));
		mTextView[2].setText(convertHeatString(myMinHeat));
		mTextView[3].setText(convertHeatString(otherMaxHeat));
		mTextView[4].setText(convertHeatString(otherAvgHeat));
		mTextView[5].setText(convertHeatString(otherMinHeat));

	}

	private String convertHeatString(String heat){
		
	    String nonValue = "NA";
	    String retValue = null;
	    if(!TextUtils.isEmpty(heat) && !TextUtils.equals("null", heat) 
	    		&& !TextUtils.equals("0", heat)){	    	
	    	retValue = String.format("%.1f", Double.valueOf(heat));   	
	    }else {
	    	retValue = nonValue;
	    }
	    return retValue;
	}

}
