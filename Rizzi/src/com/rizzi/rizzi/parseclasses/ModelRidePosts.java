package com.rizzi.rizzi.parseclasses;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("ModelRidePosts")
public class ModelRidePosts extends ParseObject {
	public static int PREF_RIDE = 0;
	public static int PREF_EITHER = 5;
	public static int PREF_DRIVE = 10;
	
	public static final String KEY_USER = "user";
	
	public static final String KEY_RIDESHARER1 = "ridesharer1";
	
	public static final String KEY_RIDESHARER2 = "ridesharer2";
	
	public static final String KEY_ORIGIN = "orig";
	
	public static final String KEY_DESTINATION = "dest";
	
	public static final String KEY_START_TIME = "startTime";
	
	public static final String KEY_RIDE_PREFERENCE = "ridePref";
	
	public ParseUser getUser(){
		return getParseUser(KEY_USER);
	}
	public void setUser(ParseUser user){
		put(KEY_USER, user);
	}
	
	public ParseUser getRideSharer1(){
		return getParseUser(KEY_RIDESHARER1);
	}
	public void setRideSharer1(ParseUser user){
		put(KEY_RIDESHARER1, user);
	}
	public ParseUser getRideSharer2(){
		return getParseUser(KEY_RIDESHARER2);
	}
	public void setRideSharer2(ParseUser user){
		put(KEY_RIDESHARER2, user);
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
	
	public Date getStartTime(){
		return getDate(KEY_START_TIME);
	}
	
	public void setStartTime(Date time){
		put(KEY_START_TIME, time);
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
	
	public static ParseQuery<ModelRidePosts> getQuery(){
		return ParseQuery.getQuery(ModelRidePosts.class);
	}
}
