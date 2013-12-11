package com.wednesday.bippobippo;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.wednesday.bippobippo.network.NetworkUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class GeneralDescriptionActivity extends Activity {
	
	private TextView mTitleView;
	private TextView mDescriptionView;
	public int mDisplayMode = 0;
	public final int mFeverSolutionMode = 1;
	public final int mFeverNoticeMode = 2;
	public final int mDiarrheaSolutionMode = 3;
	public final int mDiarrheaNoticeMode = 4;
	public final int mCryingSolutionMode = 5;
	public final int mCryingNoticeMode = 6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		final String action = intent.getAction();
		setDisplayMode(action);

		setContentView(R.layout.activity_general_description);
		
		mTitleView = (TextView)findViewById(R.id.general_description_title);
		mDescriptionView = (TextView)findViewById(R.id.description_text);
		
		setTitleViewAndGetDescriptionData();
	}

	private void setDisplayMode(String action) {
		if(TextUtils.isEmpty(action)){
			finish();
		}else if(Constants.ACTION_VIEW_FEVER_SOLUTION.equals(action)){
			mDisplayMode = mFeverSolutionMode;			
		}else if(Constants.ACTION_VIEW_FEVER_NOTICE.equals(action)){
			mDisplayMode = mFeverNoticeMode;
		}else if(Constants.ACTION_VIEW_DIARRHEA_SOLUTION.equals(action)){
			mDisplayMode = mDiarrheaSolutionMode;
		}else if(Constants.ACTION_VIEW_DIARRHEA_NOTICE.equals(action)){
			mDisplayMode = mDiarrheaNoticeMode;
		}else if(Constants.ACTION_VIEW_CRYING_SOLUTION.equals(action)){
			mDisplayMode = mCryingSolutionMode;
		}else if(Constants.ACTION_VIEW_CRYING_NOTICE.equals(action)){
			mDisplayMode = mCryingNoticeMode;
		}		
		
	}

	private void setTitleViewAndGetDescriptionData() {
		String title = null;
		String[] params = new String[1];
		switch(mDisplayMode){
		case mFeverSolutionMode: {
            title = getString(R.string.solution_title);
            params[0] = Constants.FEVER_SOLUTION_URL;       
			break;
		}
		case mDiarrheaSolutionMode: {
			title = getString(R.string.solution_title);			
			params[0] = Constants.DIARRHEA_SOLUTION_URL;
			break;
		}
		case mCryingSolutionMode: {
			title = getString(R.string.solution_title);
			params[0] = Constants.CRYING_SOLUTION_URL;
			break;
		}
		case mFeverNoticeMode: {
			title = getString(R.string.notice_title);
			params[0] = Constants.FEVER_NOTICE_URL;
			break;
		}
		case mDiarrheaNoticeMode:{
			title = getString(R.string.notice_title);
			params[0] = Constants.DIARRHEA_NOTICE_URL;
			break;
		}
		case mCryingNoticeMode: {
			title = getString(R.string.notice_title);
			params[0] = Constants.CRYING_NOTICE_URL;			
			break;
		}

		}
		mTitleView.setText(title);
	    new DownloadDescriptionTask().execute(params);
	}
	
	private class DownloadDescriptionTask extends AsyncTask<String, Void, String>{
		@Override
		protected void onPostExecute(String result) {
			//super.onPostExecute(result);
			mDescriptionView.setText(result);
		}

		@Override
		protected String doInBackground(String... params) {
			final String descriptionUri = params[0];
			String resultStr = null;
			try {
				resultStr =  downloadServerData(descriptionUri);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return resultStr;

		}
		
		protected String downloadServerData(String descriptionUri) 
				throws ClientProtocolException, IOException{
			
			DebugUtils.Log(" @@ Get description uri : " + descriptionUri);
			
	        HttpGet httpget = new HttpGet(descriptionUri);
	        httpget.setHeader("Content-type", "text");
	        // Execute the request
	        HttpResponse response;
	        
	        response = NetworkUtils.getHttpClient().execute(httpget);;
	        String resp = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);
	        String resultStr = resp.replace("\\r\\n", System.getProperty("line.separator"))
	        		.replace("\\n", System.getProperty("line.separator"))
	        		.replace("\"", "");
	        DebugUtils.Log(" @@ Server description response " + resultStr);            
	        
	        return resultStr;
		}
		
	}
	

}
