package com.wendesday.bippobippo;

import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class SensorService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private ArduinoReceiver mArduinoReceiver = new ArduinoReceiver();
	private String mDeviceAddress;
	private SharedPreferences mPref;
	private int mState = STATE_DISCONNECTED;
	
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_DISCONNECTED = 2;
	
	public static final String SHARED_PREF_NAME = "SensorServicePref";
	public static final String DEVICE_ADDRESS_KEY = "deviceAddress";
	public static final String ACTION_START = "com.wendesday.bippobippo.ACTION_START";
	public static final String ACTION_STOP = "com.wendesday.bippobippo.ACTION_STOP";
	
	public static final String ACTION_BROADCAST_UPDATE_SENSORDATA = "com.wendesday.bippobippo.ACTION_BROADCAST_UPDATE_SENSORDATA";
	public static final String ACTION_BROADCAST_UPDATE_HEAT = "com.wendesday.bippobippo.ACTION_BROADCAST_UPDATE_HEAT";
	public static final String ACTION_BROADCAST_UPDATE_WET = "com.wendesday.bippobippo.ACTION_BROADCAST_UPDATE_WET";
	public static final String ACTION_BROADCAST_UPDATE_BPM = "com.wendesday.bippobippo.ACTION_BROADCAST_UPDATE_BPM";
	public static final String ACTION_BROADCAST_UPDATE_MIC = "com.wendesday.bippobippo.ACTION_BROADCAST_UPDATE_MIC";
	public static final String EXTRA_SENSOR_DATA = "com.wendesday.bippobippo.EXTRA_SENSOR_DATA";
	public static final String EXTRA_DOUBLE_DATA = "com.wendesday.bippobippo.EXTRA_DOUBLE_DATA";

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
						
						if (BippoBippo.SensorData.MIC.equals(output[0])) {
							sendBroadcastSensorData((output.length == 2) ? mSensorDataList.get(0) : getAverageSensorData(mSensorDataList));
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
				mSensorDataList.get(i - 1).setHeat(Integer.valueOf(data[i]));
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
		
		avg.setPhone(mPhone);
		avg.setTimeStamp(System.currentTimeMillis());
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
	
	private void sendBroadcastSensorData(SensorDataModel sensorData) {
		Intent localIntent = new Intent(ACTION_BROADCAST_UPDATE_SENSORDATA);
		localIntent.putExtra(EXTRA_SENSOR_DATA, sensorData);
		LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(localIntent);
	}
}
