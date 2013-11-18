package com.wendesday.bippobippo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button mStartBtn, mStopBtn;
	private TextView[] mTextView = new TextView[4];
	private SensorDataReceiver mSensorDataReceiver = new SensorDataReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTextView[0] = (TextView)findViewById(R.id.textView1);
        mTextView[1] = (TextView)findViewById(R.id.textView2);
        mTextView[2] = (TextView)findViewById(R.id.textView3);
        mTextView[3] = (TextView)findViewById(R.id.textView4);
        
        mStartBtn = (Button)findViewById(R.id.button1);
        mStartBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    //Start Sensor Service
		        Intent intent = new Intent(getBaseContext(), SensorService.class);
		        intent.setAction(SensorService.ACTION_START);
		        startService(intent);
			}
		});
        mStopBtn = (Button)findViewById(R.id.button2);
        mStopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getBaseContext(), SensorService.class);
				intent.setAction(SensorService.ACTION_STOP);
				startService(intent);
			}
		});
        

        
        // Start Service
        Intent intent = new Intent(getBaseContext(), SensorService.class);
        intent.setAction(SensorService.ACTION_START);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }
    
    
    private class SensorDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent == null)
        		return;
        	
            String action = intent.getAction();
            double value;
            
            if (SensorService.ACTION_BROADCAST_UPDATE_HEAT.equals(action)) {
            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
 
        		mTextView[0].setText(String.valueOf(value));
            } else if (SensorService.ACTION_BROADCAST_UPDATE_WET.equals(action)) {
            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
 
        		mTextView[1].setText(String.valueOf(value));
            } else if (SensorService.ACTION_BROADCAST_UPDATE_BPM.equals(action)) {
            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
 
        		mTextView[2].setText(String.valueOf(value));
            } else if (SensorService.ACTION_BROADCAST_UPDATE_MIC.equals(action)) {
            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
 
        		mTextView[3].setText(String.valueOf(value));
            }
        }
    }


	@Override
	protected void onResume() {
		super.onResume();
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_HEAT);
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_WET);
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_BPM);
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_MIC);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mSensorDataReceiver, filter);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mSensorDataReceiver);
	}

}
