package com.wednesday.bippobippo.chart;

import org.achartengine.GraphicalView;

import com.wednesday.bippobippo.BippoBippo;
import com.wednesday.bippobippo.BippoBippo.SensorData;
import com.wednesday.bippobippo.DebugUtils;
import com.wednesday.bippobippo.R;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class BabyChartActivity extends Activity {
	
	private GraphicalView mTempratureChart;	
	//private GraphicalView mBpmChart;
	private TextView[] mTextView = new TextView[3];
	
	public static String[] SUMMARY_PROJECTION = new String[] {BippoBippo.SensorData.HIGHEST,
                                                        BippoBippo.SensorData.AVERAGE,
                                                        BippoBippo.SensorData.LOWEST};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_babychart);
		LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_temprature);
		// LinearLayout bpmChartContainer = (LinearLayout) findViewById(R.id.chart_bpm);
		try{
		    mTempratureChart = (GraphicalView) new ShowBabyStatusChart().execute(this);
		    chartContainer.addView(mTempratureChart);
		   // mBpmChart = (GraphicalView) new ShowBabyBpmChart().execute(this);
		   // bpmChartContainer.addView(mBpmChart);
		}catch(NullPointerException e){
			Toast toast = Toast.makeText(this, "Data is empty !!! please turn on the app.", 3);
			toast.show();
			finish();
		}
        mTextView[0] = (TextView) findViewById(R.id.highestTemp);
        mTextView[1] = (TextView) findViewById(R.id.averageTemp);
        mTextView[2] = (TextView) findViewById(R.id.lowestTemp); 
        getSummaryData();

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

	private void getSummaryData() {
		
		Cursor cursor = getContentResolver().query(SensorData.SUMMARY_URI, SUMMARY_PROJECTION, 
				null, null, null);
		if(cursor != null && cursor.moveToFirst()){
			mTextView[0].setText(getString(R.string.highest_temprature)  + " : " +  cursor.getString(0));
			String avg = cursor.getString(1);			
			mTextView[1].setText(getString(R.string.average_temprature)  + " : " + String.format("%.1f", Double.valueOf(avg)));
			mTextView[2].setText(getString(R.string.lowest_temprature)  + " : " + cursor.getString(2));
			cursor.close();			
		}
		
	}

}
