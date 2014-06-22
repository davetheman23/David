package com.rizzi.rizzi.parseclasses;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("CustomGeoPoints")
public class CustomGeoPoints extends ParseObject {
	
	
	public static final String KEY_PARSE_GEO_POINT = "parseGeoPoint";

	public ParseGeoPoint getGeoPoint(){
		return getParseGeoPoint(KEY_PARSE_GEO_POINT);
	}
	
	public void setGeoPoint(ParseGeoPoint point){
		put(KEY_PARSE_GEO_POINT, point);
	}
	
	
	
}
