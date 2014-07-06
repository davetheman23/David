package com.rizzi.rizzi.parseclasses;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

/**
 * This helper class mainly helps dealing with the custom fields,
 * in particular those related to facebook,  
 * saved within the built-in User class in the parse backend *
 */
public class ParseUserHelper {
	private static final String TAG = "ParseUserHelper";
	
	/*
	 * Keys for custom fields in the Parse user class
	 */
	public static final String KEY_FB_ID = "facebookId";
	public static final String KEY_PROFILE = "profile";
	public static final String KEY_NAME = "name";
	public static final String KEY_CITY = "city";
	public static final String KEY_GENDER = "gender";
	
	//URLs facebook graph calls
	public static final String URL_FB_GRAPH = "https://graph.facebook.com/";
	
	// facebook permission definition
	public static final List<String> fbPermissions = Arrays.asList(
			"user_about_me", "user_birthday", "user_location");
	
	/**
	 * Use this method to get the facebook Id saved in the Parse backend
	 * this requires the Parseuser has a profile field that is saved once 
	 * when the user is first register
	 * @param user
	 * @return app-scoped Facebook Id
	 */
	public static String getFacebookId(ParseUser user){
		if (user.get(KEY_PROFILE) != null) {
			JSONObject userProfile = user.getJSONObject(KEY_PROFILE);
			try {
				if (userProfile.getString(KEY_FB_ID) != null) {
					return userProfile.get(KEY_FB_ID).toString();
				}
			} catch (JSONException err) {
				Log.d(TAG,"Error parsing saved user data.");
			}
		}
		return null;
	}
	
	/**
	 * this method makes a graph api call to facebook server,
	 * it takes the facebook profile information and saves them to the
	 * parse backend using ParseObject.saveInBackground()
	 */
	public static void fetchAndSaveUserProfile() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							// Create a JSON object to hold the profile info
							JSONObject userProfile = new JSONObject();
							try {
								// Populate the JSON object
								userProfile.put(KEY_FB_ID, user.getId());
								userProfile.put(KEY_NAME, user.getName());
								if (user.getLocation().getProperty(KEY_NAME) != null) {
									userProfile.put(KEY_CITY, (String) user
											.getLocation().getProperty(KEY_NAME));
								}
								
								if (user.getProperty(KEY_GENDER) != null) {
									userProfile.put(KEY_GENDER,
											(String) user.getProperty(KEY_GENDER));
								}
								/* if more data are needed from the facebook 
								 * get them here. 
								if (user.getBirthday() != null) {
									userProfile.put("birthday",
											user.getBirthday());
								}
								if (user.getProperty("relationship_status") != null) {
									userProfile
											.put("relationship_status",
													(String) user
															.getProperty("relationship_status"));
								}
								 */
								// Save the user profile info in a user property
								ParseUser currentUser = ParseUser.getCurrentUser();
								currentUser.put(KEY_PROFILE, userProfile);
								currentUser.saveInBackground();

							} catch (JSONException e) {
								Log.d(TAG, "Error parsing returned user data.");
							}

						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d(TAG, "The facebook session was invalidated.");
								//onLogoutButtonClicked();
							} else {
								Log.d(TAG, "Some other error: "
												+ response.getError()
														.getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();

	}
	
	/**
	 * This just shows an example on how to make a facebook graph api
	 * using the the ParseFacebookUtils from Parse
	 * Use this method to test the API calls 
	 */
	public static void makeGraphApiCallExample(){
		Session session = ParseFacebookUtils.getSession();
        if (session!=null && session.isOpened()){
	        Bundle params = new Bundle();
	        //params.putString("fields", "context.fields(mutual_friends),name,picture.type(large)");
	        //params.putString("fields", "permissions,email,name,picture.type(large)");
	        //params.putString("fields", "email");
	        params.putString("fields", "email,name,picture.type(large)");
	        Request request = new Request(session,
	            //"/1381039532185729",
	        	"/me",
	            params,
	            HttpMethod.GET,                 
	            new Request.Callback(){   
	        		// getting the response from Facebook graph API call
	                public void onCompleted(Response response) {
	                    Log.i(TAG, "Result: " + response.toString());
	                }                  
	        }); 
	        //Request.executeBatchAsync(request);
	        request.executeAsync();
        }
	}
}
