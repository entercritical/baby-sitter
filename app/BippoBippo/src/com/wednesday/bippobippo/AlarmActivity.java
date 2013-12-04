package com.wednesday.bippobippo;


import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AlarmActivity extends Activity{
	private TextView[] mTextView = new TextView[4];
	private ImageView mBabyStateImage;
	private ImageButton mStopBtn, mFirstAidBtn, mEmergencyCallBtn;
//	private SensorDataReceiver mSensorDataReceiver = new SensorDataReceiver();
	private SoundPool mSoundPool;
	private int mSoundSiren;
	
	Animation mAnimBlink;
	
	private static final int HEAT_INDEX = 0;
	private static final int WET_INDEX = 1;
	private static final int BPM_INDEX = 2;
	private static final int MIC_INDEX = 3;
	
	private HashMap<String, Integer> mAlarmIndexMap = new HashMap<String, Integer>() {
		{
			put(SensorService.ACTION_HEAT_ALARM, HEAT_INDEX);
			put(SensorService.ACTION_WET_ALARM, WET_INDEX);
			put(SensorService.ACTION_BPM_ALARM, BPM_INDEX);
			put(SensorService.ACTION_MIC_ALARM, MIC_INDEX);
		}
	};
	
	private static final int[] mTitleRes = {
		R.string.heat_alarm,
		R.string.wet_alarm,
		R.string.bpm_alarm,
		R.string.mic_alarm
	};
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		initViews();
		setBabyState(getIntent());
	
        // Siren Sound Play
		mSoundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 0);
		mSoundSiren = mSoundPool.load(getBaseContext(), R.raw.siren, 1);        
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				mSoundPool.play(mSoundSiren, 0.5f, 0.5f, 0, -1, 1f);	
			}
		});
		
		
//		IntentFilter filter = new IntentFilter();
//        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_SENSORDATA);
//        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mSensorDataReceiver, filter);
	}
	
	private void initViews() {
		setContentView(R.layout.alarm_layout);

        mTextView[HEAT_INDEX] = (TextView)findViewById(R.id.heatText);
        mTextView[WET_INDEX] = (TextView)findViewById(R.id.wetText);
        mTextView[BPM_INDEX] = (TextView)findViewById(R.id.bpmText);
        mTextView[MIC_INDEX] = (TextView)findViewById(R.id.micText);        
        mBabyStateImage = (ImageView)findViewById(R.id.babyStateImage);
        mStopBtn = (ImageButton)findViewById(R.id.stop);
        mStopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//finish();
				if (mSoundPool != null) {
					mSoundPool.stop(mSoundSiren);
					mSoundPool.release();
					mSoundPool = null;
				} else {
					finish();
				}
			}
		});
        mFirstAidBtn = (ImageButton)findViewById(R.id.firstAid);
        mFirstAidBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        mEmergencyCallBtn = (ImageButton)findViewById(R.id.emergencyCall);
        mEmergencyCallBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});

        // Siren Icon
		getActionBar().setIcon(R.drawable.alarm_icon);
		
		// Blink Animation
		mAnimBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);		
	}
	
	private void setBabyState(Intent intent) {
		Integer stateIndex; 
        String action = intent.getAction();
        SensorDataModel sensor = intent.getParcelableExtra(SensorService.EXTRA_SENSOR_DATA);
		
        stateIndex = mAlarmIndexMap.get(action);
        if (stateIndex == null) {
        	return;
        }
        
        mBabyStateImage.setImageResource(R.drawable.baby_crying_icon);
        getActionBar().setTitle(mTitleRes[stateIndex]);
        mTextView[stateIndex].setTextColor(0xFFFF0000);
        mTextView[stateIndex].startAnimation(mAnimBlink);
        
        mTextView[HEAT_INDEX].setText(String.valueOf(sensor.getHeat()));
        mTextView[WET_INDEX].setText(String.valueOf(sensor.getWet()));
        mTextView[BPM_INDEX].setText(String.valueOf(sensor.getBpm()));
        mTextView[MIC_INDEX].setText(String.valueOf(sensor.getMic()));		
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
		
		if (mAnimBlink != null) {
			mAnimBlink.cancel();
			mAnimBlink = null;
		}
		
		if (mSoundPool != null) {
			mSoundPool.stop(mSoundSiren);
			mSoundPool.release();
			mSoundPool = null;
		}
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
            	mTextView[HEAT_INDEX].setText(String.valueOf(sensorData.getHeat()));
            	mTextView[WET_INDEX].setText(String.valueOf(sensorData.getWet()));
            	mTextView[BPM_INDEX].setText(String.valueOf(sensorData.getBpm()));
            	mTextView[MIC_INDEX].setText(String.valueOf(sensorData.getMic()));            	
            }
        }
    }
}
