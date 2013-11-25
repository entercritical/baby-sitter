package com.wendesday.bippobippo.chart;

import org.achartengine.GraphicalView;

import com.wendesday.bippobippo.DebugUtils;
import com.wendesday.bippobippo.R;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;


public class BabyChartActivity extends Activity {
	
	private GraphicalView mChart;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_babychart);
		LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_container);
		mChart = (GraphicalView) new ShowBabyStatusChart().execute(this);
		chartContainer.addView(mChart);
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
