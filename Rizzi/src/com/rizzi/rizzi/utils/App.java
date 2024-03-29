package com.rizzi.rizzi.utils;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.rizzi.rizzi.R;
import com.rizzi.rizzi.parseclasses.CustomGeoPoints;
import com.rizzi.rizzi.parseclasses.TripPosts;

public class App extends Application {
	
	public static final String APPTAG = "Rizzi App";
	
	public static final String SHARED_PREF = "com.rizzi.rizzi.shared_preference";
	
	public static final String APP_PACKAGE_PREFIX = "com.rizzi.rizzi.shared_preference";

	public static Context appContext;
	@Override
	public void onCreate() {
		super.onCreate();
		
		ParseObject.registerSubclass(TripPosts.class);
		ParseObject.registerSubclass(CustomGeoPoints.class);
		Parse.initialize(this, "6DTl0J025zmS0ep12aDS0rv5CXVacV6r4BrIs45A", 
								"VIbs2Nfn6OrpePcz3hUH6dLleeFP13A0eNbUOndu");
		
		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.fb_app_id));
		
		appContext = getApplicationContext();
		
	}
	
	
}
