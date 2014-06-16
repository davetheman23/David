package com.rizzi.rizzi.parseclasses;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("ModelRidePosts")
public class ModelRidePosts extends ParseObject {
	public static final String KEY_USER = "user";
	
	public static final String KEY_RIDESHARER1 = "ridesharer1";
	
	public static final String KEY_RIDESHARER2 = "ridesharer2";
	
	public static final String KEY_ORIGIN = "orig";
	
	public static final String KEY_DESTINATION = "dest";
	
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
	/*
	public ParseGeoPoint getOrigin(){
		return getParseGeoPoint(KEY_ORIGIN);
	}
	public void setOrigin(ParseGeoPoint origin){
		put(KEY_ORIGIN, origin);
	}*/
	/*public ParseGeoPoint getDestination(){
		return getParseGeoPoint(KEY_DESTINATION);
	}
	public void setDestination(ParseGeoPoint destination){
		put(KEY_DESTINATION, destination);
	}*/
	
	public static ParseQuery<ModelRidePosts> getQuery(){
		return ParseQuery.getQuery(ModelRidePosts.class);
	}
}
