package com.wednesday.bippobippo;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorDataModel implements Parcelable {
	private String mPhone;
	private long mTimeStamp;
	private float mHeat;
	private int mWet;
	private int mBpm;
	private int mMic;
	private String mWetString;
	private String mMicString;

	public SensorDataModel() {

	}

	public SensorDataModel(Builder builder) {
		mPhone = builder.mPhone;
		mTimeStamp = builder.mTimeStamp;
		mHeat = builder.mHeat;
		mWet = builder.mWet;
		mBpm = builder.mBpm;
		mMic = builder.mMic;
		mWetString = builder.mWetString;
		mMicString = builder.mMicString;
	}

	public SensorDataModel(Parcel in) {
		mPhone = in.readString();
		mTimeStamp = in.readLong();
		mHeat = in.readFloat();
		mWet = in.readInt();
		mBpm = in.readInt();
		mMic = in.readInt();
		mWetString = in.readString();
		mMicString = in.readString();
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String mPhone) {
		this.mPhone = mPhone;
	}

	public long getTimeStamp() {
		return mTimeStamp;
	}

	public void setTimeStamp(long mTimeStamp) {
		this.mTimeStamp = mTimeStamp;
	}

	public float getHeat() {
		return mHeat;
	}

	public void setHeat(float mHeat) {
		this.mHeat = mHeat;
	}

	public int getWet() {
		return mWet;
	}

	public void setWet(int mWet) {
		this.mWet = mWet;
	}

	public int getBpm() {
		return mBpm;
	}

	public void setBpm(int mBpm) {
		this.mBpm = mBpm;
	}

	public int getMic() {
		return mMic;
	}

	public void setMic(int mMic) {
		this.mMic = mMic;
	}

	public String getWetString() {
		return mWetString;
	}

	public void setWetString(String mWetString) {
		this.mWetString = mWetString;
	}

	public String getMicString() {
		return mMicString;
	}

	public void setMicString(String mMicString) {
		this.mMicString = mMicString;
	}
	
	public String getHeatString() {
		return String.valueOf(mHeat) + " ��";
	}

	public static class Builder {
		private String mPhone;
		private long mTimeStamp;
		private float mHeat;
		private int mWet;
		private int mBpm;
		private int mMic;
		private String mWetString;
		private String mMicString;

		public Builder() {
		}

		public Builder phone(String phone) {
			mPhone = phone;
			return this;
		}

		public Builder timestamp(long timestamp) {
			mTimeStamp = timestamp;
			return this;
		}

		public Builder heat(float heat) {
			mHeat = heat;
			return this;
		}

		public Builder wet(int wet) {
			mWet = wet;
			return this;
		}

		public Builder bpm(int bpm) {
			mBpm = bpm;
			return this;
		}

		public Builder mic(int mic) {
			mMic = mic;
			return this;
		}
		
		public Builder wetString(String wetString) {
			mWetString = wetString;
			return this;
		}
		
		public Builder micString(String micString) {
			mMicString = micString;
			return this;
		}

		public SensorDataModel build() {
			return new SensorDataModel(this);
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPhone);
		dest.writeLong(mTimeStamp);
		dest.writeFloat(mHeat);
		dest.writeInt(mWet);
		dest.writeInt(mBpm);
		dest.writeInt(mMic);
		dest.writeString(mWetString);
		dest.writeString(mMicString);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public SensorDataModel createFromParcel(Parcel in) {
			return new SensorDataModel(in);
		}

		public SensorDataModel[] newArray(int size) {
			return new SensorDataModel[size];
		}
	};

	public void clear() {
		mPhone = "";
		mTimeStamp = 0;
		mHeat = 0;
		mWet = 0;
		mBpm = 0;
		mMic = 0;
		mWetString = "";
		mMicString = "";
	}
	
	 /**
	   * Convert the RawContact object into a JSON string.  From the
	   * JSONString interface.
	   * @return a JSON string representation of the object
	   */
	public JSONObject toJSONObject() {
	    JSONObject json = new JSONObject();

	    try {	      
	        json.put(Constants.HEAT, mHeat);
	        json.put(Constants.WET, mWet);
	        json.put(Constants.BPM, mBpm);
	        json.put(Constants.MIC, mMic);
	        json.put(Constants.TIMESTAMP, mTimeStamp);
	    } catch (final Exception ex) {
	       DebugUtils.ErrorLog("Error converting RawContact to JSONObject" + ex.toString());
	    }
	    
	    return json;
	}	
}
