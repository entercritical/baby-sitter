package com.wendesday.bippobippo;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorDataModel implements Parcelable {
	private String mPhone;
	private long mTimeStamp;
	private int mHeat;
	private int mWet;
	private int mBpm;
	private int mMic;

	public SensorDataModel() {

	}

	public SensorDataModel(Builder builder) {
		mPhone = builder.mPhone;
		mTimeStamp = builder.mTimeStamp;
		mHeat = builder.mHeat;
		mWet = builder.mWet;
		mBpm = builder.mBpm;
		mMic = builder.mMic;
	}

	public SensorDataModel(Parcel in) {
		mPhone = in.readString();
		mTimeStamp = in.readLong();
		mHeat = in.readInt();
		mWet = in.readInt();
		mBpm = in.readInt();
		mMic = in.readInt();
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

	public int getHeat() {
		return mHeat;
	}

	public void setHeat(int mHeat) {
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

	public static class Builder {
		private String mPhone;
		private long mTimeStamp;
		private int mHeat;
		private int mWet;
		private int mBpm;
		private int mMic;

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

		public Builder heat(int heat) {
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
		dest.writeInt(mHeat);
		dest.writeInt(mWet);
		dest.writeInt(mBpm);
		dest.writeInt(mMic);
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
	}
}
