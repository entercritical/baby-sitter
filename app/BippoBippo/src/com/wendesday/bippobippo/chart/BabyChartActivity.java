package com.wendesday.bippobippo.chart;

import org.achartengine.GraphicalView;

import com.wendesday.bippobippo.DebugUtils;
import com.wendesday.bippobippo.R;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;


public class BabyChartActivity extends Activity {
	
	private GraphicalView mTempratureChart;	
	private GraphicalView mBpmChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_babychart);
		LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_temprature);
		LinearLayout bpmChartContainer = (LinearLayout) findViewById(R.id.chart_bpm);
		try{
		    mTempratureChart = (GraphicalView) new ShowBabyStatusChart().execute(this);
		    chartContainer.addView(mTempratureChart);
		    mBpmChart = (GraphicalView) new ShowBabyBpmChart().execute(this);
		    bpmChartContainer.addView(mBpmChart);
		}catch(NullPointerException e){
			Toast toast = Toast.makeText(this, "Data is empty !!! please turn on the app.", 3);
			toast.show();
			finish();
		}
		
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

}
