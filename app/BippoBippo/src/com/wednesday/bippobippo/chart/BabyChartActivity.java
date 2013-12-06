package com.wednesday.bippobippo.chart;

import org.achartengine.GraphicalView;

import com.wednesday.bippobippo.BippoBippo;
import com.wednesday.bippobippo.BippoBippo.Person;
import com.wednesday.bippobippo.BippoBippo.SensorData;
import com.wednesday.bippobippo.Utils;
import com.wednesday.bippobippo.network.NetworkCommunicator;
import com.wednesday.bippobippo.DebugUtils;
import com.wednesday.bippobippo.R;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
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
	private boolean mIsBound=false;
	private NetworkCommunicator mBoundService;
	//private GraphicalView mBpmChart;
	private TextView[] mTextView = new TextView[3];
	private int mPeriod=30;
	
	public static String[] SUMMARY_PROJECTION = new String[] {BippoBippo.SensorData.HIGHEST,
                                                        BippoBippo.SensorData.AVERAGE,
                                                        BippoBippo.SensorData.LOWEST};
	
	public ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {	
			DebugUtils.Log("<<<<<<<<<<< Network Service DisConnected");
			mBoundService = null;			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			DebugUtils.Log("<<<<<<<<<<< Network Service Connected");
			mBoundService = ((NetworkCommunicator.MyBinder) service).getService();
	
			long time = System.currentTimeMillis();
			mBoundService.getHealthData(mPeriod);
			long time2 = System.currentTimeMillis();
            DebugUtils.Log("Server response time : " + (time2-time));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_babychart);
		final LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_temprature);
		
		mPeriodSpinner = (Spinner) findViewById(R.id.chart_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 
				R.array.period, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPeriodSpinner.setAdapter(adapter);
		
		mPeriodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				 chartContainer.invalidate();
				 mTempratureChart = (GraphicalView) new ShowBabyBpmChart().execute(BabyChartActivity.this);
				 chartContainer.addView(mTempratureChart);
				 
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});

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
		if(mIsBound){
			unbindService(mConnection);
			mIsBound = false;
		}
			
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		
		super.onPause();
	}

	@Override
	protected void onResume() {		
		if(!mIsBound){			
			Intent intent = new Intent(BabyChartActivity.this, NetworkCommunicator.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			mIsBound = true;
		}
		super.onResume();
		
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

}
