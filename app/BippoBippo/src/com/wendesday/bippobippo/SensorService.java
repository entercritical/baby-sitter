package com.wendesday.bippobippo;

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
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class SensorService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private ArduinoReceiver mArduinoReceiver = new ArduinoReceiver();
	private String mDeviceAddress;
	private SharedPreferences mPref;
	
	public static final String SHARED_PREF_NAME = "SensorServicePref";
	public static final String DEVICE_ADDRESS_KEY = "deviceAddress";
	public static final String ACTION_START = "com.wendesday.bippobippo.ACTION_START";
	public static final String ACTION_STOP = "com.wendesday.bippobippo.ACTION_STOP";

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
				
				//Amarino.connect(getBaseContext(), DEVICE_ADDRESS);
				//Intent i = new Intent(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES);
				//sendBroadcast(i);
				if (mPref != null) {
					mDeviceAddress = mPref.getString(DEVICE_ADDRESS_KEY, null);
					if (mDeviceAddress != null) {
						Amarino.connect(getBaseContext(), mDeviceAddress);
					} else {
						DebugUtils.ErrorLog("SensorService: mDeviceAddress is null");
						Intent amarino = getApplicationContext().getPackageManager().getLaunchIntentForPackage("at.abraxas.amarino");
						DebugUtils.Log("SensorService: " + amarino);
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
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
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
		
		mPref = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		mDeviceAddress = mPref.getString(DEVICE_ADDRESS_KEY, null);
				
		DebugUtils.Log("SensorService: Service Started");
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
	
				// the device address from which the data was sent, we don't need it
				// here but to demonstrate how you retrieve it
				final String address = intent
						.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
	
				// the type of data which is added to the intent
				final int dataType = intent.getIntExtra(
						AmarinoIntent.EXTRA_DATA_TYPE, -1);
	
				// we only expect String data though, but it is better to check if
				// really string was sent
				// later Amarino will support differnt data types, so far data comes
				// always as string and
				// you have to parse the data to the type you have sent from
				// Arduino, like it is shown below
				if (dataType == AmarinoIntent.STRING_EXTRA) {
					data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
	
					if (data != null) {
						DebugUtils.Log("SensorService: " + data);
						String []output = data.split("\\s");
						//for (int x=0; x<output.length; x++)
						//	DebugUtils.Log(output[x]);
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
			} else if (AmarinoIntent.ACTION_DISCONNECTED.equals(action)) {
				DebugUtils.Log("SensorService: DISCONNECTED");
			} else if (AmarinoIntent.ACTION_CONNECTION_FAILED.equals(action)) {
				DebugUtils.Log("SensorService: CONNECTION_FAILED");
			} else if (AmarinoIntent.ACTION_PAIRING_REQUESTED.equals(action)) {
				DebugUtils.Log("SensorService: REQUESTED");
			} else if (AmarinoIntent.ACTION_CONNECTED_DEVICES.equals(action)) {
//				String[] data = intent.getStringArrayExtra(AmarinoIntent.EXTRA_CONNECTED_DEVICE_ADDRESSES);
//				
//				if (data == null || data.length == 0) {
//					DebugUtils.Log("SensorService: CONNECTED_DEVICES " + "none");
//					return;
//				}
//				
//				for (int i = 0; i < data.length; i++) {
//					DebugUtils.Log("SensorService: CONNECTED_DEVICES " + data[i]);
//				}
			}
		}
	}
}
