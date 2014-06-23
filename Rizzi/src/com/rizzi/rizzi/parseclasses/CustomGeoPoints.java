package com.rizzi.rizzi.parseclasses;

import android.location.Location;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("CustomGeoPoints")
public class CustomGeoPoints extends ParseObject {
	
	public static final int TYPE_ORIGIN = 1;
	public static final int TYPE_DESTINATION = 2;
	
	
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
	/*please use only the types defined in the class */
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
	 * 
	 * @param location
	 * @return
	 */
	public static ParseGeoPoint getParseGeoPointFromLocation(Location location){
		return new ParseGeoPoint(location.getLatitude(), 
								location.getLongitude());
	}
	
}
