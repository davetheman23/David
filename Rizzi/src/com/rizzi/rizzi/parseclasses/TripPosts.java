package com.rizzi.rizzi.parseclasses;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * This class defines all the constants, setters and getters
 * methods for a TripPost class 
 */
@ParseClassName("TripPosts")
public class TripPosts extends ParseObject {
	/*
	 * constant sets for ride preferences
	 */
	public static final int PREF_RIDE = 0;
	public static final int PREF_EITHER = 5;
	public static final int PREF_DRIVE = 10;
	
	/*
	 * constant sets for status of the post
	 */
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_CLOSED = 1;
	
	/*
	 * Keys for all fields in the class, will be used
	 * as column names in the Parse DataStore
	 */
	public static final String KEY_OWNER = "owner";
	public static final String KEY_ORIGIN = "orig";	
	public static final String KEY_DESTINATION = "dest";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_DEPART_AT = "departAt";
	public static final String KEY_DEPART_TIMERANGE_BEGIN = "departRangeBegin";
	public static final String KEY_DEPART_TIMERANGE_END = "departRangeEnd";
	public static final String KEY_RIDE_PREFERENCE = "ridePref";
	public static final String KEY_STATUS = "status";

	public ParseUser getOwner(){
		return getParseUser(KEY_OWNER);
	}
	public void setOwner(ParseUser user){
		put(KEY_OWNER, user);
	}
	
	public CustomGeoPoints getOrigin(){
		return (CustomGeoPoints) getParseObject(KEY_ORIGIN);
	}
	
	public void setOrigin(CustomGeoPoints origin){
		put(KEY_ORIGIN, origin);
	}
	public CustomGeoPoints getDestination(){
		return (CustomGeoPoints) getParseObject(KEY_DESTINATION);
	}
	
	public void setDestination(CustomGeoPoints destination){
		put(KEY_DESTINATION, destination);
	}
	
	public Date getDepartAt(){
		return getDate(KEY_DEPART_AT);
	}
	
	public void setDepartAt(Date time){
		put(KEY_DEPART_AT, time);
	}
	
	public Date getDepartBegin(){
		return getDate(KEY_DEPART_TIMERANGE_BEGIN);
	}
	
	public void setDepartBegin(Date time){
		put(KEY_DEPART_TIMERANGE_BEGIN, time);
	}
	
	public Date getDepartEnd(){
		return getDate(KEY_DEPART_TIMERANGE_END);
	}
	
	public void setDepartEnd(Date time){
		put(KEY_DEPART_TIMERANGE_END, time);
	}
	
	public String getDescription(){
		return getString(KEY_DESCRIPTION);
	}
	
	public void setDescription(String description){
		put(KEY_DESCRIPTION, description);
	}
	
	public int getRidePreference(){
		return getInt(KEY_RIDE_PREFERENCE);
	}
	
	/*
	 * the input value should be one of the Pref_* constants
	 */
	public void setRidePreference(int preference){
		put(KEY_RIDE_PREFERENCE, preference);
	}
	
	public int getStatus(){
		return getInt(KEY_STATUS);
	}
	/*
	 * the input value should be one of the STATUS_* constants
	 */
	public void setStatus(int status){
		put(KEY_STATUS, status);
	}
	
	public static ParseQuery<TripPosts> getQuery(){
		return ParseQuery.getQuery(TripPosts.class);
	}
}
