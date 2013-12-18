package com.wednesday.bippobippo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wednesday.bippobippo.BippoBippo.Person;

public class SettingsActivity extends Activity {
	
	private EditText mNameText;
	private EditText mNumberText;
	private EditText mBirthday;
	private EditText mTemprature;
	private EditText mEmergency;
	private SeekBar mWetSensitivity;
	private TextView mSensitivityValue;
	private Button mConfirmButton;
	private String mIntentAction;
	
	private Calendar mCalendar;
	private ContentResolverHelper mContentResolverHelper;
	
	public static String[] PERSON_PROJECTION = new String[] {
		Person.DISPLAY_NAME,
		Person.PHONE_NUMBER,
		Person.BIRTHDAY,
		Person.DEFAULT_TEMPRATURE,
		Person.WET_SENSITIVITY,
        Person.EMERGENCY_NUMBER
	};
	
	private final int NAME = 0;
	private final int NUMBER =  1;
	private final int BIRTHDAY = 2;
	private final int TEMPRATURE = 3;
	private final int SENSITIVITY = 4;
	private final int EMERGENCY = 5;
	
	public static final String mFormat = "yyyy-MM-dd";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);
		
		mNameText = (EditText)findViewById(R.id.name_input);
		mNumberText = (EditText)findViewById(R.id.number_input);
		mBirthday = (EditText)findViewById(R.id.birth_input);
		mTemprature = (EditText)findViewById(R.id.temp_input);
		mWetSensitivity = (SeekBar)findViewById(R.id.wet_seek);
		mSensitivityValue = (TextView)findViewById(R.id.wet_value);
		mConfirmButton = (Button)findViewById(R.id.setting_confirm);
		mEmergency = (EditText)findViewById(R.id.emergencynumber_input);
		
		Intent intent = getIntent();
		mIntentAction = intent.getAction();
		
		mCalendar= Calendar.getInstance();
		
		if(Constants.ACTION_VIEW_SETTINGS.equals(mIntentAction)){
			getActionBar().setTitle(R.string.menu_settings);
			initSettingDisplay();			
		}else{
			getActionBar().setTitle(R.string.setting_title);
			initDefaultSettingValueDisplay();			
		}
		
		mConfirmButton.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				verifyAndSaveOrUpdateValues();				
			}
		});

		mContentResolverHelper = new ContentResolverHelper(getApplicationContext());
	}
	
	private void initDefaultSettingValueDisplay() {
		initNumberDisplay();
		initBirthdayDisplay();
		initSensitivityDisplay();		
	}

	private void initSettingDisplay() {
		initSensitivityDisplay();
		// Data comes from database
		Cursor cur = getContentResolver().query(Person.CONTENT_URI, PERSON_PROJECTION , null, null, null);
		if(cur != null && cur.moveToFirst()){
			mNameText.setText(cur.getString(NAME));
			mNumberText.setText(cur.getString(NUMBER));	
			mEmergency.setText(cur.getString(EMERGENCY));
			
			String birthday = cur.getString(BIRTHDAY);
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(mFormat, Locale.KOREA);
				Date date = sdf.parse(birthday);
				Log.d("setting", date.getTime()+"");
                mCalendar.setTime(date);
				setBirthdayTouchListener(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), 
		        		mCalendar.get(Calendar.DAY_OF_MONTH));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			mBirthday.setText(birthday);
			
			mTemprature.setText(cur.getString(TEMPRATURE));			
			String sensitivity = cur.getString(SENSITIVITY);
			if(!TextUtils.isEmpty(sensitivity)){
				mWetSensitivity.setProgress(Integer.valueOf(sensitivity)+5);
				mSensitivityValue.setText(sensitivity);				
			}			
			cur.close();
		}		
	}

	protected void verifyAndSaveOrUpdateValues() {
		String name = mNameText.getText().toString();
		String number = mNumberText.getText().toString();
		String birth = mBirthday.getText().toString();
		String temprature = mTemprature.getText().toString();
		String sensitivity = (String) mSensitivityValue.getText();
		String emergency = mEmergency.getText().toString();
		
		if(TextUtils.isEmpty(name) || TextUtils.isEmpty(number)
				|| TextUtils.isEmpty(temprature)){
			Toast toast = Toast.makeText(this, "Please fill in the blanks !!!", 3);
			toast.show();
			return;
		}
		
		PersonModel person = new PersonModel.Builder().displayName(name).phone(number)
				.birthDay(birth).defaultTemprature(temprature).wetSensitivity(sensitivity)
				.emgergency(emergency).build();
		if(Constants.ACTION_VIEW_SETTINGS.equals(mIntentAction)){		
			mContentResolverHelper.updatePerson(person);			
			Intent intent = new Intent(Constants.ACTION_UPDATE_USER_DATA);
			intent.putExtra(Constants.EXTRA_USER_DATA, person);
			startService(intent);
		}else{
			Uri uri = mContentResolverHelper.insertPerson(person);
			if(uri != null){
				savePreferences();
			}
			Intent intent = new Intent(Constants.ACTION_SEND_USER_DATA);
			intent.putExtra(Constants.EXTRA_USER_DATA, person);
			startService(intent);
		}
		
		finish();
		
	}
	
	private void savePreferences() {
		SharedPreferences pref = getSharedPreferences(Constants.PREF_SETTINGS, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(Constants.PREF_KEY, "1");
		editor.commit();
	}

	private void initSensitivityDisplay() {		
		mWetSensitivity.setOnSeekBarChangeListener(new SeekBarChangeListener());	
	}

	private void initNumberDisplay() {
		String number = Utils.getInstance(getApplicationContext()).getPhoneNumber();
		mNumberText.setText(number);
		
	}

    // ********************************************************
	// Birthday setting
	private void initBirthdayDisplay() {
        
        updateBirthdayDisplay();
        
        setBirthdayTouchListener(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), 
        		mCalendar.get(Calendar.DAY_OF_MONTH));

	}
	
	private void setBirthdayTouchListener(final int year, final int month, final int day){
        mBirthday.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					new DatePickerDialog(SettingsActivity.this, mDateSetListener, year , month , 
			    			day ).show();
				}
				return true;
			}
		});
		
	}

	private void updateBirthdayDisplay() {	
		SimpleDateFormat sdf = new SimpleDateFormat(mFormat, Locale.KOREA);
		mBirthday.setText(sdf.format(mCalendar.getTime()));		
	}	
	
	// Date picker dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mCalendar.set(Calendar.YEAR, year);
			mCalendar.set(Calendar.MONTH, monthOfYear);
			mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateBirthdayDisplay();
		}
	};	
	// Birthday
	// *********************************************************

	private final class SeekBarChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
			int displayValue = progress - 5; // -5 <= sensitivity <= 5
			mSensitivityValue.setText(String.valueOf(displayValue));			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
		
	}

}
