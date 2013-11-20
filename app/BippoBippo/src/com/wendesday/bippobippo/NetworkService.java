package com.wendesday.bippobippo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

class NetworkService extends Thread {
	private boolean isRun = false;
	SensorDataModel testData = new SensorDataModel();
	
	public NetworkService(boolean isRun) {
		this.isRun = isRun;
		testData.setPhone("010-3951-3953");
		testData.setHeat(11);
		testData.setWet(22);
		testData.setBpm(33);
		testData.setMic(44);
		//testData.setTimeStamp(mTimeStamp);	
	}

	public void stopThread() {
		isRun = false;
	}

	public void run() {
		Log.d("ashton", "Thread start");

		while (isRun) {
			super.run();

			//POST("http://14.49.42.181:52273/user",testData);
			testData.setHeat(testData.getHeat()+1);
			testData.setWet(testData.getWet()+1);
			testData.setBpm(testData.getBpm()+1);
			testData.setMic(testData.getMic()+1);
			Log.d("ashton", "http post");
		    String url = "http://14.49.42.181:52273/healthinfo/010-3951-3953";
			POST(url,testData);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
	}

	public static String POST(String url, SensorDataModel person){
	    InputStream inputStream = null;
	    String result = "";

	    try {

	        // 1. create HttpClient
	        HttpClient httpclient = new DefaultHttpClient();

	        // 2. make POST request to the given URL
	        //HttpPost httpPost = new HttpPost("http://14.49.42.181:52273/healthinfo/010-3951-3953");
	        Log.d("ashton url", url);
	        HttpPost httpPost = new HttpPost(url);

	        String json = "";

	        // 3. build jsonObject
	        JSONObject jsonObject = new JSONObject();
	        jsonObject.accumulate("heat", person.getHeat());
	        jsonObject.accumulate("wet", person.getWet());
	        jsonObject.accumulate("mic", person.getMic());
	        jsonObject.accumulate("bpm", person.getBpm());

	        
        	        
	        //jsonObject.accumulate("pulse", person.getPulse());
	        

	        // 4. convert JSONObject to JSON to String
	        json = jsonObject.toString();

	        // ** Alternative way to convert Person object to JSON string usin Jackson Lib 
	        // ObjectMapper mapper = new ObjectMapper();
	        // json = mapper.writeValueAsString(person); 

	        // 5. set json to StringEntity
	        StringEntity se = new StringEntity(json);

	        // 6. set httpPost Entity
	        httpPost.setEntity(se);

	        // 7. Set some headers to inform server about the type of the content   
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");

	        // 8. Execute POST request to the given URL
	        HttpResponse httpResponse = httpclient.execute(httpPost);

	        // 9. receive response as inputStream
	        inputStream = httpResponse.getEntity().getContent();

	        // 10. convert inputstream to string
	        if(inputStream != null)
	        {
	            result = convertInputStreamToString(inputStream);
	            Log.d("InputStream", result);
	        }
	        else
	            result = "Did not work!";

	    } catch (Exception e) {
	        Log.d("InputStream", e.getLocalizedMessage());
	    }

	    // 11. return result
	    return result;
	}	
	
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }  	
	
}

