package com.wednesday.bippobippo;

import java.util.ArrayList;
import java.util.Date;

import com.wednesday.bippobippo.network.NetworkCommunicator;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class SensorService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private ArduinoReceiver mArduinoReceiver = new ArduinoReceiver();
	private String mDeviceAddress;
	private SharedPreferences mPref;
	private int mState = STATE_DISCONNECTED;
	private ContentResolverHelper mContentResolverHelper;
	private Resources mResources;
	private long mStartTimestamp;
	private int mAlarmWetValue;
	private boolean mIsAlarmPause = false;
	private long mAlarmPauseTime;
	private String mAlarmAction;
	private PersonModel mPerson;

	private static final long WET_IGNORE_TIME = 60000; // for humidity sensor, ignore 1 minute
	private static final double WET_ALARM_CONSTANT = 0.184615385; // result by experiment
	private static final long ALARM_PAUSE_TIME = 300000; // alarm pause 5 minute 
	
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_DISCONNECTED = 2;
	
	public static final String SHARED_PREF_NAME = "SensorServicePref";
	public static final String DEVICE_ADDRESS_KEY = "deviceAddress";
	public static final String ACTION_START = "com.wednesday.bippobippo.ACTION_START";
	public static final String ACTION_STOP = "com.wednesday.bippobippo.ACTION_STOP";
	public static final String ACTION_REFRESH_DATA = "com.wednesday.bippobippo.ACTION_REFRESH_DATA";
	public static final String ACTION_PAUSE_ALARM = "com.wednesday.bippobippo.ACTION_PAUSE_ALARM";
	
	public static final String ACTION_ALARM = "com.wednesday.bippobippo.ACTION_ALARM";
	public static final String ACTION_HEAT_ALARM = "com.wednesday.bippobippo.ACTION_HEAT_ALARM";
	public static final String ACTION_WET_ALARM = "com.wednesday.bippobippo.ACTION_WET_ALARM";
	public static final String ACTION_BPM_ALARM = "com.wednesday.bippobippo.ACTION_BPM_ALARM";
	public static final String ACTION_MIC_ALARM = "com.wednesday.bippobippo.ACTION_MIC_ALARM";
	
	public static final String ACTION_BROADCAST_UPDATE_SENSORDATA = "com.wednesday.bippobippo.ACTION_BROADCAST_UPDATE_SENSORDATA";
	public static final String ACTION_BROADCAST_UPDATE_HEAT = "com.wednesday.bippobippo.ACTION_BROADCAST_UPDATE_HEAT";
	public static final String ACTION_BROADCAST_UPDATE_WET = "com.wednesday.bippobippo.ACTION_BROADCAST_UPDATE_WET";
	public static final String ACTION_BROADCAST_UPDATE_BPM = "com.wednesday.bippobippo.ACTION_BROADCAST_UPDATE_BPM";
	public static final String ACTION_BROADCAST_UPDATE_MIC = "com.wednesday.bippobippo.ACTION_BROADCAST_UPDATE_MIC";
	public static final String EXTRA_SENSOR_DATA = "com.wednesday.bippobippo.EXTRA_SENSOR_DATA";
	public static final String EXTRA_DOUBLE_DATA = "com.wednesday.bippobippo.EXTRA_DOUBLE_DATA";

	public static final int SENSORDATA_ARRAY_SIZE = 10;
	
	public static final char FLAG_GET_CURRENT_DATA = 'C';
	
	private String mPhone;
	private ArrayList<SensorDataModel> mSensorDataList = new ArrayList<SensorDataModel>();
	
    // for synchronize service instance
 //   private static final Object[] sWait = new Object[0];
//    private static SensorService sInstance;
//    /**
//     * return the SensorService instance
//     */
//    public static SensorService getInstance(Context context) {
//        if (sInstance == null) {
//            context.startService(new Intent(context, SensorService.class));
//
//            while (sInstance == null) {
//                try {
//                    synchronized (sWait) {
//                        sWait.wait();
//                    }
//                } catch (InterruptedException ignored) {
//                }
//            }
//        }
//
//        return sInstance;
//    }
    
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Intent intent = (Intent)msg.obj;
			if (intent == null) {
				return;
			}
			
			String action = intent.getAction();
			
			if (ACTION_START.equals(action)) {
				//Start
				DebugUtils.Log("SensorService: ACTION_START");
				
				//Get Person
				mPerson = mContentResolverHelper.getPerson();
				
				if (mPref != null) {
					mDeviceAddress = mPref.getString(DEVICE_ADDRESS_KEY, null);
					if (mDeviceAddress != null) {
						Amarino.connect(getBaseContext(), mDeviceAddress);

						// check connected devices
						Intent in = new Intent(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES);
						sendBroadcast(in);
					} else {
						DebugUtils.ErrorLog("SensorService: mDeviceAddress is null");
						Intent amarino = getApplicationContext().getPackageManager().getLaunchIntentForPackage("at.abraxas.amarino");
						DebugUtils.Log("SensorService: startActivity()" + amarino);
						if (amarino != null)
							startActivity(amarino);
					}
				} else {
					DebugUtils.ErrorLog("SensorService: mPref is null");
				}
				
			} else if (ACTION_STOP.equals(action)) {
				//Stop
				DebugUtils.Log("SensorService: ACTION_STOP");
				Amarino.disconnect(getBaseContext(), mDeviceAddress);
				
				stopSelf();
			} else if (ACTION_REFRESH_DATA.equals(action)) {
				DebugUtils.Log("SensorService: ACTION_REFRESH_DATA");
				Amarino.sendDataToArduino(getBaseContext(), mDeviceAddress, FLAG_GET_CURRENT_DATA, "");
			} else if (ACTION_PAUSE_ALARM.equals(action)) {
				DebugUtils.Log("SensorService: ACTION_PAUSE_ALARM");
				mAlarmPauseTime = System.currentTimeMillis();
				mIsAlarmPause = true;
				Toast.makeText(getBaseContext(), R.string.alarm_paused_5_minutes, Toast.LENGTH_LONG).show();;
			}
		}
	}

	@Override
	public void onCreate() {
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("SensorServiceThread",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(AmarinoIntent.ACTION_RECEIVED);
		filter.addAction(AmarinoIntent.ACTION_CONNECTED);
		filter.addAction(AmarinoIntent.ACTION_DISCONNECTED);
		filter.addAction(AmarinoIntent.ACTION_CONNECTION_FAILED);
		filter.addAction(AmarinoIntent.ACTION_PAIRING_REQUESTED);
		filter.addAction(AmarinoIntent.ACTION_CONNECTED_DEVICES);
		
		registerReceiver(mArduinoReceiver, filter);
		
		// Preference for device address
		mPref = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		mDeviceAddress = mPref.getString(DEVICE_ADDRESS_KEY, null);
		
		// Phone number
		mPhone = Utils.getInstance(this).getPhoneNumber();
		DebugUtils.Log("SensorService: PhoneNumber = " + mPhone);
		
		// Sensor Data List
		for (int i = 0; i < SENSORDATA_ARRAY_SIZE; i++)
			mSensorDataList.add(new SensorDataModel());
		
		// Content Resolver
		mContentResolverHelper = new ContentResolverHelper(getBaseContext());
		mContentResolverHelper.open();
		
		mResources = getBaseContext().getResources();
		
		// for Humidity Sensor
		mStartTimestamp = System.currentTimeMillis();
		mAlarmWetValue = 0;
		
		DebugUtils.Log("SensorService: Service Started");
	
// test
//		sendBroadcastSensorData(
//				new SensorDataModel.Builder()
//				.phone(mPhone)
//				.timestamp(System.currentTimeMillis())
//				.heat(36)
//				.wet(50)
//				.bpm(80)
//				.mic(200)
//				.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = (Object)intent;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		DebugUtils.Log("SensorService: Service Stopped");
		
		unregisterReceiver(mArduinoReceiver);
		
		mPref = null;
		
		mContentResolverHelper.close();
		mContentResolverHelper = null;
		
		mStartTimestamp = 0;
		mAlarmWetValue = 0;
		
		mPerson = null;
	}

	/**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino events.
	 * 
	 * It extracts data from the intent and updates the graph accordingly.
	 */
	public class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (AmarinoIntent.ACTION_RECEIVED.equals(action)) {
				String data = null;
				
				// the type of data which is added to the intent
				final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
	
				if (dataType == AmarinoIntent.STRING_EXTRA) {
					data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
	
					if (data != null) {
						DebugUtils.Log("SensorService: " + data);
						String []output = data.split("\\s");
						
						setSensorDataList(output);
						
						if (BippoBippo.SensorData.MIC.equals(output[0])) { // End of Data stream (Heat -> Wet -> Bpm -> Mic)
							SensorDataModel sensorData;
							
							if (output.length == 2) { // Flag 'C' : get current data
								sensorData = mSensorDataList.get(0);
							} else {
								sensorData = getAverageSensorData(mSensorDataList);
							}
							sensorData.setPhone(mPhone);
							sensorData.setTimeStamp(System.currentTimeMillis());
							String wetString;
							if (mAlarmWetValue == 0 || sensorData.getWet() <= mAlarmWetValue) {
								wetString = mResources.getString(R.string.dry);
							} else {
								wetString = mResources.getString(R.string.wet);
							}
							sensorData.setWetString(wetString);
							sensorData.setMicString(sensorData.getMic() > mResources.getInteger(R.integer.mic_alarm_value) ? 
									mResources.getString(R.string.loud) : mResources.getString(R.string.quiet));
							
							sendUISensorData(sensorData);
							sendServerSensorData(sensorData);
							mContentResolverHelper.insertSensorData(sensorData);
							//mContentResolverHelper.printLastSensorData();
							
							checkBabyStatus(sensorData);
						}
					}
				}
			} else if (AmarinoIntent.ACTION_CONNECTED.equals(action)) {
				mDeviceAddress = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
				 
				DebugUtils.Log("SensorService: CONNECTED " + mDeviceAddress);
				
				// add to pref
				if (mPref != null) {
					Editor editor = mPref.edit();
					editor.putString(DEVICE_ADDRESS_KEY, mDeviceAddress);
					editor.commit();
				}
				mState = STATE_CONNECTED;
			} else if (AmarinoIntent.ACTION_DISCONNECTED.equals(action)) {
				DebugUtils.Log("SensorService: DISCONNECTED");
				mState = STATE_DISCONNECTED;
				
			} else if (AmarinoIntent.ACTION_CONNECTION_FAILED.equals(action)) {
				DebugUtils.Log("SensorService: CONNECTION_FAILED");
			} else if (AmarinoIntent.ACTION_PAIRING_REQUESTED.equals(action)) {
				DebugUtils.Log("SensorService: REQUESTED");
			} else if (AmarinoIntent.ACTION_CONNECTED_DEVICES.equals(action)) {
				String[] data = intent.getStringArrayExtra(AmarinoIntent.EXTRA_CONNECTED_DEVICE_ADDRESSES);
				
				if (data == null || data.length == 0) {
					DebugUtils.Log("SensorService: CONNECTED_DEVICES " + "none");
					return;
				}
				
				for (int i = 0; i < data.length; i++) {
					DebugUtils.Log("SensorService: CONNECTED_DEVICES " + data[i]);
				}
				mState = STATE_CONNECTED;
				
				// get Current Sensor Data
				Amarino.sendDataToArduino(getBaseContext(), mDeviceAddress, FLAG_GET_CURRENT_DATA, "");
			}
		}
	}
	
	private void setSensorDataList(String[] data) {
		if (data == null || data.length == 0)
			return;
		
		if (BippoBippo.SensorData.HEAT.equals(data[0])) {
			for (int i = 1; i < data.length; i++) {
				mSensorDataList.get(i - 1).setHeat(Float.valueOf(data[i]) / 10);	// HEAT value is x10 (245 -> 24.5) 
			}
		} else if (BippoBippo.SensorData.WET.equals(data[0])) {
			for (int i = 1; i < data.length; i++) {
				mSensorDataList.get(i - 1).setWet(Integer.valueOf(data[i]));
			}			
		} else if (BippoBippo.SensorData.BPM.equals(data[0])) {
			for (int i = 1; i < data.length; i++) {
				mSensorDataList.get(i - 1).setBpm(Integer.valueOf(data[i]));
			}			
		} else if (BippoBippo.SensorData.MIC.equals(data[0])) {
			for (int i = 1; i < data.length; i++) {
				mSensorDataList.get(i - 1).setMic(Integer.valueOf(data[i]));
			}			
		}
	}
	
//	private void clearSensorDataList() {
//		for (int i = 0; i < mSensorDataList.size(); i++) {
//			mSensorDataList.get(i).clear();
//		}
//	}
	
	private SensorDataModel getAverageSensorData(ArrayList<SensorDataModel> list) {
		SensorDataModel avg = new SensorDataModel();
		int heat, wet, bpm, mic;
		
		//avg.setPhone(mPhone);
		//avg.setTimeStamp(System.currentTimeMillis());
		heat = wet = bpm = mic = 0;
		
		for (int i = 0; i < list.size(); i++) {
			SensorDataModel d = list.get(i);
			heat += d.getHeat();
			wet += d.getWet();
			bpm += d.getBpm();
			mic += d.getMic();
		}
		avg.setHeat(heat/list.size());
		avg.setWet(wet/list.size());
		avg.setBpm(bpm/list.size());
		avg.setMic(mic/list.size());
		
		return avg;
	}
	
//	private double getAverage(String[] data) {
//		double average = 0;
//		if (data == null || data.length == 0)
//			return -1;
//		
//		for (int x = 1; x< data.length; x++)
//			average += Integer.valueOf(data[x]);
//		average /= data.length - 1;
//		
//		return average;
//	}
//	
//	private void sendBroadcastDoubleData(String action, double value) {
//		Intent localIntent = new Intent(action);
//		localIntent.putExtra(EXTRA_DOUBLE_DATA, value);
//		LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(localIntent);
//	}
	
	private void sendUISensorData(SensorDataModel sensorData) {
		//Status View
		Intent localIntent = new Intent(ACTION_BROADCAST_UPDATE_SENSORDATA);
		localIntent.putExtra(EXTRA_SENSOR_DATA, sensorData);
		LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(localIntent);
	}
	
	private void sendServerSensorData(SensorDataModel sensorData) {
		//Web Server
		Intent serverIntent = new Intent(Constants.ACTION_SEND_HEALTH_DATA);
		serverIntent.putExtra(EXTRA_SENSOR_DATA, sensorData);
		startService(serverIntent);
	}
	
	// Check older than 6 month baby
	// for heat alarm 
	// less 6 month : 
	private boolean isOlderThan6month() {
		if (mPerson == null) {
			return true;
		}
		Date d = mPerson.getBirthDayDate();
		Date now = new Date();
		
		if (d == null || now == null) {
			return true;
		}
		
		int y = now.getYear() - d.getYear();
		int m = now.getMonth() - d.getMonth();
		
		if (y == 0 && m <= 6) {
			DebugUtils.Log("SensorService: Less than 6 month old");
			return false;
		} else {
			DebugUtils.Log("SensorService: More than 6 month old");
			return true;
		}
	}
	
	private void checkBabyStatus(SensorDataModel sensorData) {
		if (sensorData == null) {
			return;
		}
		
		//for wet
		if (mAlarmWetValue == 0 && mStartTimestamp + WET_IGNORE_TIME < System.currentTimeMillis()) {
			int baseWet = sensorData.getWet();
			mAlarmWetValue = baseWet + (int)((double)(100 - baseWet) * WET_ALARM_CONSTANT);
			DebugUtils.Log("SensorService: Set Wet base = " + baseWet + " alarm = " + mAlarmWetValue);
		}
		
		// check pause
		if (mIsAlarmPause == true 
				&& System.currentTimeMillis() > mAlarmPauseTime + ALARM_PAUSE_TIME) { //pause after ALARM_PAUSE_TIME
			mIsAlarmPause = false;
		}
		
		if (sensorData.getHeat() > (isOlderThan6month() ?
				mResources.getInteger(R.integer.heat_alarm2_value) :
					mResources.getInteger(R.integer.heat_alarm1_value))) {
			DebugUtils.Log("SensorService: HEAT Alarm");
			startAlarmActivity(ACTION_HEAT_ALARM, sensorData);
		} else if (mAlarmWetValue != 0 && sensorData.getWet() > mAlarmWetValue) {
			DebugUtils.Log("SensorService: WET Alarm");
			startAlarmActivity(ACTION_WET_ALARM, sensorData);
		} else if (sensorData.getBpm() > mResources.getInteger(R.integer.bpm_alarm_high_value)) {
			DebugUtils.Log("SensorService: BPM Alarm");
			startAlarmActivity(ACTION_BPM_ALARM, sensorData);
		} else if (sensorData.getMic() > mResources.getInteger(R.integer.mic_alarm_value)) {
			DebugUtils.Log("SensorService: MIC Alarm");
			startAlarmActivity(ACTION_MIC_ALARM, sensorData);
		}
	}
	
	private void startAlarmActivity(String action, SensorDataModel sensorData) {
		// check pause
		if (mIsAlarmPause == true 
				&& System.currentTimeMillis() < mAlarmPauseTime + ALARM_PAUSE_TIME
				&& action.equals(mAlarmAction)) {
			DebugUtils.Log("SensorService: startAlarmActivity alarm paused");
			return;
		}
		
		mIsAlarmPause = false;
		mAlarmAction = action;
		
		Intent intent = new Intent(action);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(EXTRA_SENSOR_DATA, sensorData);
		startActivity(intent);		
	}
}
