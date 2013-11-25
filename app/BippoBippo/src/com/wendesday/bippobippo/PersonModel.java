package com.wendesday.bippobippo;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonModel implements Parcelable {
	private String mPhone;
	private String mDisplayName;
	private String mBirthDay;

	public PersonModel() {

	}

	public PersonModel(Parcel in) {
		mPhone = in.readString();
		mDisplayName = in.readString();
		mBirthDay = in.readString();
	}

	public PersonModel(Builder builder) {
		mPhone = builder.mPhone;
		mDisplayName = builder.mDisplayName;
		mBirthDay = builder.mBirthDay;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String phone) {
		this.mPhone = phone;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String displayName) {
		this.mDisplayName = displayName;
	}

	public String getBirthDay() {
		return mBirthDay;
	}

	public void setBirthDay(String birthDay) {
		this.mBirthDay = birthDay;
	}

	public static class Builder {
		public String mPhone;
		public String mDisplayName;
		public String mBirthDay;

		public Builder() {
		}

		public Builder phone(String phone) {
			this.mPhone = phone;
			return this;
		}

		public Builder displayName(String displayName) {
			this.mDisplayName = displayName;
			return this;
		}

		public Builder birthDay(String birthDay) {
			this.mBirthDay = birthDay;
			return this;
		}

		public PersonModel build() {
			return new PersonModel(this);
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
		dest.writeString(mDisplayName);
		dest.writeString(mBirthDay);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public PersonModel createFromParcel(Parcel in) {
			return new PersonModel(in);
		}

		public PersonModel[] newArray(int size) {
			return new PersonModel[size];
		}
	};
	
	
	 /**
	   * Convert the RawContact object into a JSON string.  From the
	   * JSONString interface.
	   * @return a JSON string representation of the object
	   */
	public JSONObject toJSONObject() {
	    JSONObject json = new JSONObject();

	    try {	      
	        json.put(Constants.PHONE, mPhone);
	        json.put(Constants.DISPLAYNAME, mDisplayName);
	        json.put(Constants.BIRTHDAY, mBirthDay);
	    } catch (final Exception ex) {
	       DebugUtils.ErrorLog("Error converting RawContact to JSONObject" + ex.toString());
	    }
	    
	    return json;
	}
}
