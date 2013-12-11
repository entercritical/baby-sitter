package com.wednesday.bippobippo;


import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
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
	private PersonModel mPerson;
	private ContentResolverHelper mContentResolverHelper;
	private Integer mStateIndex;
	private String mAction;
	private boolean mIsPaused = false;
	
	private Animation mAnimBlink;
	
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
	
	private static final int[] mImageRes = {
		R.drawable.baby_fever_icon,
		R.drawable.baby_peeing_icon,
		R.drawable.baby_crying_icon,
		R.drawable.baby_crying_icon
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
		
		mContentResolverHelper = new ContentResolverHelper(getBaseContext());
		mContentResolverHelper.open();
		
		mPerson = mContentResolverHelper.getPerson();
//		DebugUtils.Log("AlarmActivity: Person " + mPerson.getDisplayName() + " " 
//				+ mPerson.getPhone() + " "
//				+ mPerson.getBirthDay() + " "
//				+ mPerson.getDefaultTemprature() + " "
//				+ mPerson.getWetSensitivity() + " "
//				+ mPerson.getEmergency());
		
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
				if (mIsPaused == false) {
					mIsPaused = true;
					
					mSoundPool.pause(mSoundSiren);
					
					// Pause alarm
					Intent in = new Intent(SensorService.ACTION_PAUSE_ALARM);
					startService(in);
				} else {
					finish();
				}
			}
		});
        mFirstAidBtn = (ImageButton)findViewById(R.id.firstAid);
        mFirstAidBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				launchFirstAid();
			}
		});
        
        mEmergencyCallBtn = (ImageButton)findViewById(R.id.emergencyCall);
        mEmergencyCallBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPerson.getEmergency()));
				startActivity(in);
			}
		});

        // Siren Icon
		getActionBar().setIcon(R.drawable.alarm_icon);
		
		// Blink Animation
		mAnimBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);		
	}
	
	private void setBabyState(Intent intent) {
		mIsPaused = false;
		
        mAction = intent.getAction();
        SensorDataModel sensor = intent.getParcelableExtra(SensorService.EXTRA_SENSOR_DATA);
		
        mStateIndex = mAlarmIndexMap.get(mAction);
        if (mStateIndex == null) {
        	return;
        }
        
        mBabyStateImage.setImageResource(mImageRes[mStateIndex]);
        getActionBar().setTitle("Baby " + getResources().getString(mTitleRes[mStateIndex]));
        mTextView[mStateIndex].setTextColor(0xFFFF0000);
        mTextView[mStateIndex].startAnimation(mAnimBlink);
        
        mTextView[HEAT_INDEX].setText(sensor.getHeatString());
        mTextView[WET_INDEX].setText(sensor.getWetString());
        mTextView[BPM_INDEX].setText(String.valueOf(sensor.getBpm()));
        mTextView[MIC_INDEX].setText(sensor.getMicString());
        
        // Buttons
        if (SensorService.ACTION_BPM_ALARM.equals(mAction) || 
        		(SensorService.ACTION_MIC_ALARM.equals(mAction))) {
        	mFirstAidBtn.setEnabled(false);
        } else {
        	mFirstAidBtn.setEnabled(true);
        }
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
		
		if (mContentResolverHelper != null) {
			mContentResolverHelper.close();
			mContentResolverHelper = null;
		}
		
		if (mAction != null) {
			mAction = null;
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
            	mTextView[HEAT_INDEX].setText(sensorData.getHeatString());
            	mTextView[WET_INDEX].setText(sensorData.getWetString());
            	mTextView[BPM_INDEX].setText(String.valueOf(sensorData.getBpm()));
            	mTextView[MIC_INDEX].setText(sensorData.getMicString());            	
            }
        }
    }
    
    private void launchFirstAid() {
    	Intent in;
    	
    	if (SensorService.ACTION_HEAT_ALARM.equals(mAction)) {
    		in = new Intent(Constants.ACTION_VIEW_FEVER_DISCRIPTION);
    		startActivity(in);
    	} else if (SensorService.ACTION_WET_ALARM.equals(mAction)) {
    		in = new Intent(Constants.ACTION_VIEW_DIARRHEA_DISCRIPTION);
    		startActivity(in);    		
    	} else if (SensorService.ACTION_BPM_ALARM.equals(mAction)) {
    		// None
    	} else if (SensorService.ACTION_MIC_ALARM.equals(mAction)) {
    		// None
    	}
    }

	@Override
	protected void onNewIntent(Intent intent) {
		DebugUtils.Log("AlarmActivity: onNewIntent() " + intent.getAction());
		
		setBabyState(intent);
		
		if (mSoundPool != null) {
			mSoundPool.resume(mSoundSiren);
		}
		
		super.onNewIntent(intent);
	}
    
    
}
