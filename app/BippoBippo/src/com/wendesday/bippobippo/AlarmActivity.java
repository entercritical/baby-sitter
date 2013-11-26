package com.wendesday.bippobippo;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class AlarmActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
