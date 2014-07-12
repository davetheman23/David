package com.rizzi.rizzi.parseclasses;

import android.location.Location;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * a wrapper class that wrap around a ParseGeoPoint object. 
 * This is a workaround for Parse restriction on the number of
 * ParseGeoPoint allowed in one class. (Currently is 1 as of
 * 07/05/2014). when this restriction is lifted, this wrapper
 * class may not be necessary
 */
@ParseClassName("CustomGeoPoints")
public class CustomGeoPoints extends ParseObject {
	
	/*
	 * constant sets for ride preferences
	 */
	public static final int TYPE_ORIGIN = 1;
	public static final int TYPE_DESTINATION = 2;
	
	/*
	 * Keys for all fields in the class, will be used
	 * as column names in the Parse DataStore
	 */
	public static final String KEY_PARSE_GEO_POINT = "parseGeoPoint";
	public static final String KEY_LOCATION_TYPE = "locationType";

	public ParseGeoPoint getGeoPoint(){
		return getParseGeoPoint(KEY_PARSE_GEO_POINT);
	}
	
	public void setGeoPoint(ParseGeoPoint point){
		put(KEY_PARSE_GEO_POINT, point);
	}
	
	public int getLocationType(){
		return getInt(KEY_LOCATION_TYPE);
	}
	/*
	 * the input value should be one of the TYPE_* constants
	 */
	public void setLocationType(int type){
		put(KEY_LOCATION_TYPE, type);
	}
	
	/**
	 * class method to obtain distance between two location objects
	 * @param start
	 * @param end
	 * @return the Euclidean distance between start and end
	 */
	public static float getDistanceBetweenInMeters(Location start, Location end){
		float[] results = new float[]{-1,0,0};
		Location.distanceBetween(start.getLatitude(), 
				 start.getLongitude(),
				 end.getLatitude(),
				 end.getLongitude(),
				 results);
		//Log.d(TAG, "distance between start and end locations are: " + results[0]);
		return results[0];
	}
	
	/**
	 * convert an Android native class "Location" to a Parse native class
	 * "ParseGeoPoint" 
	 * @param location
	 * @return
	 */
	public static ParseGeoPoint getParseGeoPointFromLocation(Location location){
		return new ParseGeoPoint(location.getLatitude(), 
								location.getLongitude());
	}
	
}
