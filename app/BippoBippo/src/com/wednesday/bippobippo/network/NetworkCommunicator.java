package com.wednesday.bippobippo.network;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.wednesday.bippobippo.Constants;
import com.wednesday.bippobippo.DebugUtils;
import com.wednesday.bippobippo.PersonModel;
import com.wednesday.bippobippo.SensorDataModel;
import com.wednesday.bippobippo.SensorService;
import com.wednesday.bippobippo.Utils;
import com.wednesday.bippobippo.BippoBippo.Person;
import com.wednesday.bippobippo.chart.BabyChartActivity;

public class NetworkCommunicator extends Service {
	
	private Looper mServiceLooper;
	private NetworkHandler mNetworkHandler;
	private String mUserNumber;
	
	public final int SEND_USER_DATA = 1;
	public final int SEND_HEALTH_DATA = 2;
	public final int GET_HEALTH_DATA = 3;
	public final int UPDATE_USER_DATA = 4;	
	
    /** Timeout (in ms) we specify for each http request */
    public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;

    private final IBinder mBinder = new MyBinder();
    public static HttpClient mHttpClient;
    
	@Override
	public IBinder onBind(Intent arg0) {		
		return mBinder;
	}

	@Override
	public void onCreate() {
		DebugUtils.Log("NetworkCommunicator service created");		
		mUserNumber = getUserNumber();
		
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("NetworkClient",
						Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		
		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mNetworkHandler= new NetworkHandler(mServiceLooper);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(intent == null){
			return START_STICKY;
		}
		
		String action = intent.getAction();
		DebugUtils.Log("NetworkCommunicator action : " + action);
		
		if(Constants.ACTION_SEND_USER_DATA.equals(action)){
			sendMessageToHandler(intent, SEND_USER_DATA, 0);
		}else if(Constants.ACTION_SEND_HEALTH_DATA.equals(action)){			
			sendMessageToHandler(intent, SEND_HEALTH_DATA, 0);
		}else if(Constants.ACTION_UPDATE_USER_DATA.equals(action)){
			sendMessageToHandler(intent, UPDATE_USER_DATA, 0);
		}
		
		return START_REDELIVER_INTENT;
	}
	
	private void sendMessageToHandler(Intent intent, int action, int extra) {
		Message msg = mNetworkHandler.obtainMessage();
		msg.obj = (Object)intent;
		msg.arg1 = action;
		msg.arg2 = extra;
		mNetworkHandler.sendMessage(msg);		
	}

	private final class NetworkHandler extends Handler {
		public NetworkHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			int action = msg.arg1;		
			
			switch(action){
			    case SEND_USER_DATA:
			    {
			    	Intent intent  = (Intent)msg.obj;
			    	PersonModel personModel = intent.getParcelableExtra(Constants.EXTRA_USER_DATA);
			    	try {
						sendUserDataToServer(personModel);	
					} catch (AuthenticationException e) {
						e.printStackTrace();
					} catch (ClientProtocolException e) {						
						e.printStackTrace();
					} catch (JSONException e) {						
						e.printStackTrace();
					} catch (IOException e) {						
						e.printStackTrace();
					}
				    break;
			    }
			    case SEND_HEALTH_DATA:
			    {
			    	Intent intent  = (Intent)msg.obj;
			    	SensorDataModel sensorData = intent.getParcelableExtra(SensorService.EXTRA_SENSOR_DATA);
			    	try {
						sendHealthDataToServer(sensorData);
					} catch (AuthenticationException e) {						
						e.printStackTrace();
					} catch (ClientProtocolException e) {						
						e.printStackTrace();
					} catch (IOException e) {						
						e.printStackTrace();
					}
				    break;
			    }
			    case UPDATE_USER_DATA:
			    {
			    	Intent intent  = (Intent)msg.obj;
			    	PersonModel personModel = intent.getParcelableExtra(Constants.EXTRA_USER_DATA);
			    	try {
			    		updateUserDataToServer(personModel);	
					} catch (AuthenticationException e) {
						e.printStackTrace();
					} catch (ClientProtocolException e) {						
						e.printStackTrace();
					} catch (JSONException e) {						
						e.printStackTrace();
					} catch (IOException e) {						
						e.printStackTrace();
					}
				    break;
			    	
			    }
			    case GET_HEALTH_DATA:{
			    	int period = msg.arg2;
			    	try {
						getHealthDataFromServer(period);
					} catch (ClientProtocolException e) {						
						e.printStackTrace();
					} catch (IOException e) {						
						e.printStackTrace();
					} catch (JSONException e) {						
						e.printStackTrace();
					}
			    	break;
			    }
			    default :
			        break;
			    }
		
		}	
	}

	@Override
	public void onDestroy() {			
		super.onDestroy();
	}


	public void getHealthDataFromServer(int period) 
			throws ClientProtocolException, IOException, JSONException {
		 // Prepare a request object
		final String uri = Constants.HEALTH_DATA_URL
				+ "/" + Uri.encode(mUserNumber) + "/" + period + "";
		DebugUtils.Log(" @@ Get health data uri : " + uri);
        HttpGet httpget = new HttpGet(uri);
        httpget.setHeader("Content-type", "application/json");
        // Execute the request
        HttpResponse response;
        
        response = getHttpClient().execute(httpget);
        final String resp = EntityUtils.toString(response.getEntity());
        DebugUtils.Log(" @@ Server healthdata response " + resp);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
        	final JSONArray serverData = new JSONArray(resp);
        	for(int i=0; i< serverData.length(); i++ ){
        		JSONObject jsonValue = serverData.getJSONObject(i);
        		double heat = jsonValue.getDouble(Constants.HEAT);
        		DebugUtils.Log(" Server healthdata heat : " + heat);
        		long timeStamp = jsonValue.getLong(Constants.TIMESTAMP);
        		DebugUtils.Log(" Server healthdata timestamp : " + timeStamp);
        		
        	}
        	
        }
		
	}

	public void sendUserDataToServer(PersonModel personModel) 
			throws JSONException, ClientProtocolException, IOException, AuthenticationException {
		
		final String uri = Constants.USER_DATA_URL;    	
        DebugUtils.Log("Syncing to: " + uri);
        
        final String userData = personModel.toJSONObject().toString();
        DebugUtils.Log("json data : " + userData);
        StringEntity se = new StringEntity(userData);
        
        // Send a sensor data to the server        
        final HttpPost post = new HttpPost(uri);
        post.setEntity(se);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
  
        final HttpResponse resp = getHttpClient().execute(post);
        final String response = EntityUtils.toString(resp.getEntity());
        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	DebugUtils.Log(response);        	
        }else{
        	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                DebugUtils.ErrorLog("Authentication exception in sending user data");
                throw new AuthenticationException();
            } else {
                DebugUtils.ErrorLog("Server error : " + resp.getStatusLine());
                throw new IOException();
            }        	
        }  	
	}
	public void updateUserDataToServer(PersonModel personModel) 
			throws JSONException, ClientProtocolException, IOException, AuthenticationException {
		
		final String uri = Constants.USER_DATA_URL;    	
        DebugUtils.Log("Syncing to: " + uri);
        
        final String userData = personModel.toJSONObject().toString();
        DebugUtils.Log("json data : " + userData);
        StringEntity se = new StringEntity(userData);
        
        // Send a sensor data to the server        
        final HttpPut put = new HttpPut(uri);
        put.setEntity(se);
        put.setHeader("Accept", "application/json");
        put.setHeader("Content-type", "application/json");
  
        final HttpResponse resp = getHttpClient().execute(put);
        final String response = EntityUtils.toString(resp.getEntity());
        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	DebugUtils.Log(response);        	
        }else{
        	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                DebugUtils.ErrorLog("Authentication exception in sending user data");
                throw new AuthenticationException();
            } else {
                DebugUtils.ErrorLog("Server error in sending dirty : " + resp.getStatusLine());
                throw new IOException();
            }        	
        }  	
	}

	public void sendHealthDataToServer(SensorDataModel sensorData) 
			throws ClientProtocolException, IOException, AuthenticationException {
		
        final String uri = Constants.HEALTH_DATA_URL + "/" + Uri.encode(mUserNumber);    	
        DebugUtils.Log("Syncing to: " + uri);
        
        final String healthData = sensorData.toJSONObject().toString();
        DebugUtils.Log("json data : " + healthData);
        StringEntity se = new StringEntity(healthData);
        
        // Send a sensor data to the server        
        final HttpPost post = new HttpPost(uri);
        post.setEntity(se);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
  
        final HttpResponse resp = getHttpClient().execute(post);
        final String response = EntityUtils.toString(resp.getEntity());
        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	DebugUtils.Log(response);        	
        }else{
        	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                DebugUtils.ErrorLog("Authentication exception in sending dirty contacts");
                throw new AuthenticationException();
            } else {
                DebugUtils.ErrorLog("Server error in sending dirty contacts: " + resp.getStatusLine());
                throw new IOException();
            }        	
        }   	
	}
    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static HttpClient getHttpClient() {
    	if(mHttpClient == null){
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
            HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
            ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
    	}
        return mHttpClient;
    }

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	public class MyBinder extends Binder{
		public NetworkCommunicator getService(){
			return NetworkCommunicator.this;
		}
	}
    
	public void getHealthData(int period){
		sendMessageToHandler(null, GET_HEALTH_DATA, period);
	}
	
	protected String getUserNumber() {
		String number = null;
		Cursor cursor = getContentResolver().query(Person.CONTENT_URI, new String[]{Person.PHONE_NUMBER},
				null, null, null);
		if(cursor !=null && cursor.moveToFirst()){
			number = cursor.getString(0);
			cursor.close();
		}
		if(TextUtils.isEmpty(number)){
			number = Utils.getInstance(getApplicationContext()).getPhoneNumber();
		}
		return number;
	}

}