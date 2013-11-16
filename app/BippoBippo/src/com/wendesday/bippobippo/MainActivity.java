package com.wendesday.bippobippo;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button mStartBtn, mStopBtn;
	private TextView mTextView;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTextView = (TextView)findViewById(R.id.textView1);
        mStartBtn = (Button)findViewById(R.id.button1);
        mStartBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    //Start Sensor Service
		        Intent intent = new Intent(getBaseContext(), SensorService.class);
		        startService(intent);
			}
		});
        mStopBtn = (Button)findViewById(R.id.button2);
        mStopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getBaseContext(), SensorService.class);
				stopService(intent);
			}
		});
        
        
 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

}
