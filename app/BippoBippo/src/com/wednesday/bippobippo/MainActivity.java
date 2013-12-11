package com.wednesday.bippobippo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView[] mTextView = new TextView[4];
	private SensorDataReceiver mSensorDataReceiver = new SensorDataReceiver();
	private Switch mActionBarSwitch;
	private ProgressDialog mProgressDialog;
	private LinearLayout mButtonsLayout;
	private ImageView mBabyStateImage;
	private SensorDataModel mSensorData;
	private Resources mResources;
	//private TextView mTimeStamp;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Application initiation
        if(!"1".equals(getPreference())){
        	// User ought to set the default values
        	Intent intent = new Intent(Constants.ACTION_INIT_SETTINGS);
			startActivity(intent);        	
        }
        
        mResources = getResources();
        
        initViews();
        
        // Start Service
        Intent intent = new Intent(getBaseContext(), SensorService.class);
        intent.setAction(SensorService.ACTION_START);
        startService(intent);

        //NetworkService networkThread = new NetworkService(true);
        //networkThread.start();      
    }
    
    private void initViews() {
    	mBabyStateImage = (ImageView)findViewById(R.id.babyStateImage);
    	
        mTextView[0] = (TextView)findViewById(R.id.heatText);
        mTextView[1] = (TextView)findViewById(R.id.wetText);
        mTextView[2] = (TextView)findViewById(R.id.bpmText);
        mTextView[3] = (TextView)findViewById(R.id.micText);
        //mTimeStamp = (TextView)findViewById(R.id.timestampText);
        
        mButtonsLayout = (LinearLayout)findViewById(R.id.buttonsLayout);
        mButtonsLayout.setVisibility(View.INVISIBLE);
                
        ActionBar actionbar = getActionBar();
		mActionBarSwitch = new Switch(this);

		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setCustomView(mActionBarSwitch, new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
						| Gravity.RIGHT));
		mActionBarSwitch.setChecked(true);
		mActionBarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked == true) {
				    //Start Sensor Service
			        Intent intent = new Intent(getBaseContext(), SensorService.class);
			        intent.setAction(SensorService.ACTION_START);
			        startService(intent);
			        
			        // Progress Popup
			        if (mProgressDialog != null)
			        	mProgressDialog.show();
				} else {
					//Stop Sensor Service
					Intent intent = new Intent(getBaseContext(), SensorService.class);
					intent.setAction(SensorService.ACTION_STOP);
					startService(intent);					
				}
			}
		});
		
        // Progress Popup
        mProgressDialog = ProgressDialog.show(this, getString(R.string.connecting), getString(R.string.please_wait), false, true, new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				mActionBarSwitch.setChecked(false);
			}
		});
    }

    private String getPreference() {
    	SharedPreferences pref = getSharedPreferences(Constants.PREF_SETTINGS, MODE_PRIVATE);
    	return pref.getString(Constants.PREF_KEY, "0");
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
            
//            if (SensorService.ACTION_BROADCAST_UPDATE_HEAT.equals(action)) {
//            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
// 
//        		mTextView[0].setText(String.valueOf(value));
//            } else if (SensorService.ACTION_BROADCAST_UPDATE_WET.equals(action)) {
//            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
// 
//        		mTextView[1].setText(String.valueOf(value));
//            } else if (SensorService.ACTION_BROADCAST_UPDATE_BPM.equals(action)) {
//            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
// 
//        		mTextView[2].setText(String.valueOf(value));
//            } else if (SensorService.ACTION_BROADCAST_UPDATE_MIC.equals(action)) {
//            	value = intent.getDoubleExtra(SensorService.EXTRA_DOUBLE_DATA, -1);
// 
//        		mTextView[3].setText(String.valueOf(value));
//            } else 
            if (SensorService.ACTION_BROADCAST_UPDATE_SENSORDATA.equals(action)) {
            	if (mProgressDialog != null && mProgressDialog.isShowing()) {
            		mProgressDialog.dismiss();
            	}
            	mSensorData = intent.getParcelableExtra(SensorService.EXTRA_SENSOR_DATA);
            	mTextView[0].setText(mSensorData.getHeatString());
            	mTextView[1].setText(mSensorData.getWetString());
            	mTextView[2].setText(String.valueOf(mSensorData.getBpm()));
            	mTextView[3].setText(mSensorData.getMicString());     
            	
            	//mTimeStamp.setText(String.valueOf(sensorData.getTimeStamp()));
            	
            	setBabyState();
            }
        }
    }


	@Override
	protected void onResume() {
		super.onResume();
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_SENSORDATA);        
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_HEAT);
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_WET);
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_BPM);
        filter.addAction(SensorService.ACTION_BROADCAST_UPDATE_MIC);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mSensorDataReceiver, filter);
        
        // for refresh sensor data
        Intent intent = new Intent(getBaseContext(), SensorService.class);
        intent.setAction(SensorService.ACTION_REFRESH_DATA);
        startService(intent);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mSensorDataReceiver);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.action_chart :{
				// disaplay a chart view
				Intent intent = new Intent(Constants.ACTION_VIEW_CHART);
				startActivity(intent);
				break;
			}
			case R.id.action_settings:{
				// disaplay a settings view
				Intent intent = new Intent(Constants.ACTION_VIEW_SETTINGS);
				startActivity(intent);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void setBabyState() {
		if (mSensorData == null)
			return;
		
		if (mSensorData.getHeat() > mResources.getInteger(R.integer.heat_alarm1_value)) {
			mBabyStateImage.setImageResource(R.drawable.baby_fever_icon);
		} else if (mSensorData.getWetString().equals(mResources.getString(R.string.wet))) {
			mBabyStateImage.setImageResource(R.drawable.baby_peeing_icon);
		} else if (mSensorData.getMicString().equals(mResources.getString(R.string.loud))) {
			mBabyStateImage.setImageResource(R.drawable.baby_crying_icon);
		} else {
			mBabyStateImage.setImageResource(R.drawable.baby_sleeping_icon);
		}
			
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mResources = null;
	}	
}
