package com.wednesday.bippobippo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.wednesday.bippobippo.network.NetworkUtils;

public class FirstAidActivity extends Activity {
	
	private TextView mOverView;
	private Button mSolutionButton;
	private Button mNoticeButton;
	public int mDisplayMode = 0;
	public final int mFeverMode = 1;
	public final int mDiarrheaMode = 2;
	public final int mCryingMode = 3;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		final String action = intent.getAction();
        setDisplayMode(action);
		
		setContentView(R.layout.activity_firstaid);
		
		mOverView = (TextView)findViewById(R.id.overview_text);
		mSolutionButton = (Button)findViewById(R.id.solution_button);
		mSolutionButton.setOnClickListener(new OnClickListener() {

			Intent intent = new Intent();

			@Override
			public void onClick(View v) {
				switch (mDisplayMode) {
				case mFeverMode: {
					intent.setAction(Constants.ACTION_VIEW_FEVER_SOLUTION);
					startActivity(intent);
					break;
				}
				case mDiarrheaMode: {
					intent.setAction(Constants.ACTION_VIEW_DIARRHEA_SOLUTION);
					startActivity(intent);
					break;
				}
				case mCryingMode: {
					intent.setAction(Constants.ACTION_VIEW_CRYING_SOLUTION);
					startActivity(intent);
					break;
				}
				}
			}
		});

		mNoticeButton = (Button) findViewById(R.id.notice_button);
		mNoticeButton.setOnClickListener(new OnClickListener() {

			Intent intent = new Intent();

			@Override
			public void onClick(View v) {
				switch (mDisplayMode) {
				case mFeverMode: {
					intent.setAction(Constants.ACTION_VIEW_FEVER_NOTICE);
					startActivity(intent);
					break;
				}
				case mDiarrheaMode: {
					intent.setAction(Constants.ACTION_VIEW_DIARRHEA_NOTICE);
					startActivity(intent);
					break;
				}
				case mCryingMode: {
					intent.setAction(Constants.ACTION_VIEW_CRYING_NOTICE);
					startActivity(intent);
					break;
				}
				}
			}
		});

		// Getting a text from server according to intent action
		getServerOverviewText();
	}


	private void setDisplayMode(String action) {
		if(TextUtils.isEmpty(action)){
			finish();
		}else if(Constants.ACTION_VIEW_FEVER_DISCRIPTION.equals(action)){
			getActionBar().setTitle("Fever");
			mDisplayMode = mFeverMode;			
		}else if(Constants.ACTION_VIEW_DIARRHEA_DISCRIPTION.equals(action)){
			getActionBar().setTitle("Diarrhoea");
			mDisplayMode = mDiarrheaMode;
		}else if(Constants.ACTION_VIEW_CRYING_DISCRIPTION.equals(action)){
			getActionBar().setTitle("Crying");
			mDisplayMode = mCryingMode;
		}		
	}


	private void getServerOverviewText() {
		String[] params = new String[1];
		switch(mDisplayMode){
		case mFeverMode:{
			params[0] = Constants.FEVER_OVERVIEW_URL;
			new DownloadOverViewTask().execute(params);
			break;
		}
		case mDiarrheaMode:{
			params[0] = Constants.DIARRHEA_OVERVIEW_URL;
			new DownloadOverViewTask().execute(params);
			break;
		}
		case mCryingMode:{
			params[0] = Constants.CRYING_OVERVIEW_URL;
			new DownloadOverViewTask().execute(params);
			break;
		}
		}
		
	}


	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private class DownloadOverViewTask extends AsyncTask<String, Void, String>{
		@Override
		protected void onPostExecute(String result) {
			//super.onPostExecute(result);
			result = result + System.getProperty("line.separator") 
					+ System.getProperty("line.separator");
			mOverView.setText(result);
		}

		@Override
		protected String doInBackground(String... params) {
			final String overViewUri = params[0];
			String resultStr = null;
			try {
				resultStr =  downloadServerData(overViewUri);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return resultStr;

		}
		
		protected String downloadServerData(String overViewUri) 
				throws ClientProtocolException, IOException{


			DebugUtils.Log(" @@ Get outline uri : " + overViewUri);
			
	        HttpGet httpget = new HttpGet(overViewUri);
	        httpget.setHeader("Content-type", "text");
	        // Execute the request
	        HttpResponse response;
	        
	        response = NetworkUtils.getHttpClient().execute(httpget);;
	        String resp = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);
	        String resultStr = resp.replace("\\r\\n", System.getProperty("line.separator"))
	        		.replace("\\n", System.getProperty("line.separator"))
	        		.replace("\"", "");

	        DebugUtils.Log(" @@ Server overview response " + resultStr);            
	        
	        return resultStr;
		}
		
	}

}
