package com.rizzi.rizzi.parseclasses;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * This class is logs the request one user sent to the 
 * owner of a particular trip post
 */
@ParseClassName("RideRequests")
public class RideRequests extends ParseObject{
	
	/*
	 * constant sets for status of the post
	 */
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_CLOSED = 1;
	
	/*
	 * Keys for all fields in the class, will be used
	 * as column names in the Parse DataStore
	 */
	public static final String KEY_TRIP_POST = "tripPost";
	public static final String KEY_SENDER = "sender";	
	public static final String KEY_STATUS = "status";
	public static final String KEY_SEND_AT = "createdAt";
	public static final String KEY_RESPONDED_AT = "respondedAt";
	
	public TripPosts getTripPosts(){
		return (TripPosts) getParseObject(KEY_TRIP_POST);
	}
	
	public void setTripPosts(TripPosts post){
		put(KEY_TRIP_POST, post);
	}
	
	public ParseUser getReceiver(){
		return (ParseUser) getParseObject(KEY_SENDER);
	}
	
	public void setReceiver(ParseUser receiver){
		put(KEY_SENDER, receiver);
	}
	
	public Date getSendTime(){
		return getDate(KEY_RESPONDED_AT);
	}
	
	public Date getResponseTime(){
		return getDate(KEY_RESPONDED_AT);
	}
	
	public void setResponseTime(Date responseT){
		put(KEY_RESPONDED_AT, responseT);
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
}
