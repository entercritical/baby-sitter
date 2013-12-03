package com.wednesday.bippobippo;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;
import android.widget.TextView;

public class AlarmActivity extends Activity{
	private TextView[] mTextView = new TextView[4];
	private SensorDataReceiver mSensorDataReceiver = new SensorDataReceiver();

	
	private SoundPool mSoundPool;
	private int mSoundSiren;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.alarm_layout);
		
        mTextView[0] = (TextView)findViewById(R.id.heatText);
        mTextView[1] = (TextView)findViewById(R.id.wetText);
        mTextView[2] = (TextView)findViewById(R.id.bpmText);
        mTextView[3] = (TextView)findViewById(R.id.micText);
        
        Intent intent = getIntent();
        String action = intent.getAction();
        SensorDataModel sensor = intent.getParcelableExtra(SensorService.EXTRA_SENSOR_DATA);
        
        if (SensorService.ACTION_HEAT_ALARM.equals(action)) {
        	getActionBar().setTitle("Heat Alarm!");
        } else if (SensorService.ACTION_WET_ALARM.equals(action)) {
        	getActionBar().setTitle("Wet Alarm!");
        } else if (SensorService.ACTION_BPM_ALARM.equals(action)) {
        	getActionBar().setTitle("Bpm Alarm!");
        } else if (SensorService.ACTION_MIC_ALARM.equals(action)) {
        	getActionBar().setTitle("Mic Alarm!");
        }
        
        mTextView[0].setText(String.valueOf(sensor.getHeat()));
        mTextView[1].setText(String.valueOf(sensor.getWet()));
        mTextView[2].setText(String.valueOf(sensor.getBpm()));
        mTextView[3].setText(String.valueOf(sensor.getMic()));
		
//		IntentFilter filter = new IntentFilter();
//        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_SENSORDATA);
//        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mSensorDataReceiver, filter);
	
		mSoundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 0);
		mSoundSiren = mSoundPool.load(getBaseContext(), R.raw.siren, 1);
        
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				//TEMP mSoundPool.play(mSoundSiren, 0.5f, 0.5f, 0, -1, 1f);	
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mSensorDataReceiver);
		
		//mSoundPool.pause(mSoundSiren);
		mSoundPool.stop(mSoundSiren);
		mSoundPool.release();
	}
	
	
    private class SensorDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent == null)
        		return;
        	
            String action = intent.getAction();
            SensorDataModel sensorData;
            
            if (SensorService.ACTION_BROADCAST_UPDATE_SENSORDATA.equals(action)) {

            	sensorData = intent.getParcelableExtra(SensorService.EXTRA_SENSOR_DATA);
            	mTextView[0].setText(String.valueOf(sensorData.getHeat()));
            	mTextView[1].setText(String.valueOf(sensorData.getWet()));
            	mTextView[2].setText(String.valueOf(sensorData.getBpm()));
            	mTextView[3].setText(String.valueOf(sensorData.getMic()));            	
            }
        }
    }
}
