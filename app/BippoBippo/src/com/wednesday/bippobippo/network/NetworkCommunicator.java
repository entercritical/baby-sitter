package com.wednesday.bippobippo.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.wednesday.bippobippo.Constants;
import com.wednesday.bippobippo.DebugUtils;
import com.wednesday.bippobippo.PersonModel;
import com.wednesday.bippobippo.SensorDataModel;
import com.wednesday.bippobippo.SensorService;
import com.wednesday.bippobippo.Utils;

public class NetworkCommunicator extends Service {
	
	private Looper mServiceLooper;
	private NetworkHandler mNetworkHandler;
	private String mUserNumber;
	
	public final int SEND_USER_DATA = 1;
	public final int SEND_HEALTH_DATA = 2 ;
	public final int GET_HEALTH_DATA = 3;
	public final int UPDATE_USER_DATA = 4;
	
    /** Timeout (in ms) we specify for each http request */
    public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;

	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}

	@Override
	public void onCreate() {
		DebugUtils.Log("NetworkCommunicator service created");		
		mUserNumber = Utils.getInstance(getApplicationContext()).
				getPhoneNumber();
		
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
			sendMessageToHandler(intent, SEND_USER_DATA);
		}else if(Constants.ACTION_SEND_HEALTH_DATA.equals(action)){			
			sendMessageToHandler(intent, SEND_HEALTH_DATA);
		}else if(Constants.ACTION_UPDATE_USER_DATA.equals(action)){
			sendMessageToHandler(intent, UPDATE_USER_DATA);
		}
		
		return START_REDELIVER_INTENT;
	}
	
	private void sendMessageToHandler(Intent intent, int action) {
		Message msg = mNetworkHandler.obtainMessage();
		msg.obj = (Object)intent;
		msg.arg1 = action;
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
			    default :
			        break;
			    }
		
		}	
	}

	@Override
	public void onDestroy() {			
		super.onDestroy();
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
        HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        return httpClient;
    }
    

}