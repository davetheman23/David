package com.rizzi.rizzi.parseclasses;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("TripPosts")
public class TripPosts extends ParseObject {
	public static int PREF_RIDE = 0;
	public static int PREF_EITHER = 5;
	public static int PREF_DRIVE = 10;
	
	public static final String KEY_USER = "user";
	
	public static final String KEY_ORIGIN = "orig";
	
	public static final String KEY_DESTINATION = "dest";
	
	public static final String KEY_DESCRIPTION = "description";
	
	public static final String KEY_DEPART_AT = "depart_at";
	
	public static final String KEY_DEPART_RANGE_BEGIN = "depart_range_begin";
	
	public static final String KEY_DEPART_RANGE_END = "depart_range_end";
	
	public static final String KEY_RIDE_PREFERENCE = "ridePref";
	

	public ParseUser getUser(){
		return getParseUser(KEY_USER);
	}
	public void setUser(ParseUser user){
		put(KEY_USER, user);
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
		return getDate(KEY_DEPART_RANGE_BEGIN);
	}
	
	public void setDepartBegin(Date time){
		put(KEY_DEPART_RANGE_BEGIN, time);
	}
	
	public Date getDepartEnd(){
		return getDate(KEY_DEPART_RANGE_END);
	}
	
	public void setDepartEnd(Date time){
		put(KEY_DEPART_RANGE_END, time);
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
	 * the input value should be one of the Pref_* values
	 */
	public void setRidePreference(int preference){
		put(KEY_RIDE_PREFERENCE, preference);
	}
	
	public static ParseQuery<TripPosts> getQuery(){
		return ParseQuery.getQuery(TripPosts.class);
	}
}
