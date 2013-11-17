package com.wendesday.bippobippo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
		        intent.setAction(SensorService.ACTION_START);
		        startService(intent);
			}
		});
        mStopBtn = (Button)findViewById(R.id.button2);
        mStopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getBaseContext(), SensorService.class);
				intent.setAction(SensorService.ACTION_STOP);
				startService(intent);
			}
		});
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
            String action = intent.getAction();
 
            
//            if (SensorService.ACTION_BROADCAST_PREPARED.equals(action)) {
//                // get onPrepared() & set duration of SeekBar
//                int duration = intent.getExtras().getInt("duration", 0);
//                if (mSeekBar != null) {
//                    mSeekBar.setMax(duration);
//                }
//                
//                // set play timer for update time & seekbar
//                setPlayTimer(1000);
//            }
        }
    }

}
