package com.rizzi.rizzi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.print.PrintAttributes;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rizzi.rizzi.RidePostFragment.OnMatchedMeClickedListener;
import com.rizzi.rizzi.parseclasses.CustomGeoPoints;
import com.rizzi.rizzi.parseclasses.ParseUserHelper;
import com.rizzi.rizzi.parseclasses.TripPosts;
/**
 * This list fragment will list rides posted on the backend based on some criteria
 * 1. time (time of viewing the list should fall into depart time window)
 * 2. location (both start and end location within radius)
 * 3. social connections (# of friends in common)
 * @author David
 *
 */
public class RideListFragment extends ListFragment{
	/*
	 * constraint variables for query
	 */
	private Date departTimeConstraint = null;
	private int ridePrefConstraint = TripPosts.PREF_EITHER;
	private CustomGeoPoints originConstraint = null, destinationConstraint = null;
	
	/*
	 * the search radius for both origin and destination
	 */
	private float searchRadiusInMiles = 2;
	
	
	/* all posts that matches returned from backend */
	private List<Map<String, Object>> mPosts = new ArrayList<Map<String, Object>>();
	/* the data adapter that hosts all the data, it is directly associated with mPosts*/
	private RideListAdapter mPostAdapter = null;
	/* app-scope facebook id of the user of the app */
	private List<String> friendIds;
	/* app-scope facebook friends of the user of the app */
	private Map<String, Integer> friendMutulFriends = new HashMap<String, Integer>();
	
	/*
	 * the handler is used to generate a new thread
	 */
	private Handler mHandler = new Handler();
	
	/*
	 * this is triggered whenever the underlying data for the listadpater
	 * has been modified
	 */
	// the interface
	public interface onPostListLoadedListener{
		public void onPostListLoaded(List<TripPosts> posts);
	}
	// the listener callback
	onPostListLoadedListener loadedCallback;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
		try{
			loadedCallback = (onPostListLoadedListener) activity;
		}catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onPostListLoadedListener");
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.frag_ridepost_list, container, false);
		
		Button btnRefresh = (Button)rootView
									.findViewById(R.id.frag_ridepost_list_btn_refresh);
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updatePostList();
			}
		});
		
		return rootView;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (mPostAdapter == null){
			// setup the adapter, and initially, it is empty
			mPostAdapter = new RideListAdapter(getActivity(), 
					R.layout.list_post_item, mPosts);
			getListView().setAdapter(mPostAdapter);
		}
	}
	
	public void setQueryConstraints(TripPosts constraints){
		departTimeConstraint = constraints.getDepartAt();
		ridePrefConstraint = constraints.getRidePreference();
		originConstraint = constraints.getOrigin();
		destinationConstraint = constraints.getDestination();
	}

	/*
	 * setup a background thread to load the ride posts from both 
	 * the facebook and parse server
	 */
	public void updatePostList() {
		//creates a background thread to execute two steps
		new Thread(new Runnable() {
			@Override
			public void run() {
				// step 1: find app friends from Facebook 
				findAppFriendsOnFacebook();
				// step 1b: find app friends from other sources
				// step 2: fetch more info about the target users, 
				// at least mutual friend list
            	getMutualFriendCountAsync(friendIds);
				// step 3: find the posts by these friends
				if (friendIds != null){
					queryRideLists();
				}
			}
		}).start();
		
	}

	/*
	 * this makes a query to Parse to obtain relevant ride posts
	 * filters are added here to find appropriate posts. This will not
	 * create a new thread, so make sure the caller thread is a background 
	 * thread.
	 * Note: friendList should be available before executing this method
	 */
	private void queryRideLists(){
		// Create query for objects of type "TripPosts"
		ParseQuery<TripPosts> postQuery = TripPosts.getQuery();
	  
		/*
		 * Add constraints to the query: 
		 * 1. time, 2. location, 3.social connectivity, 4. ride preference
		 */	  
		// 1. adding time constraints
		Date departtime = Calendar.getInstance().getTime();
		if (departTimeConstraint!=null && departTimeConstraint.after(departtime)){
			departtime = departTimeConstraint;
		}
		postQuery.whereLessThanOrEqualTo(TripPosts.KEY_DEPART_TIMERANGE_BEGIN, departtime);
		postQuery.whereGreaterThanOrEqualTo(TripPosts.KEY_DEPART_TIMERANGE_END, departtime);

		// 2. location constraints
		ParseQuery<CustomGeoPoints> originQuery = ParseQuery.getQuery(CustomGeoPoints.class);
		originQuery.whereWithinKilometers(CustomGeoPoints.KEY_PARSE_GEO_POINT, 
										  originConstraint.getGeoPoint(), 
										  searchRadiusInMiles);
		ParseQuery<CustomGeoPoints> destQuery = ParseQuery.getQuery(CustomGeoPoints.class);
		destQuery.whereWithinKilometers(CustomGeoPoints.KEY_PARSE_GEO_POINT, 
										  destinationConstraint.getGeoPoint(), 
										  searchRadiusInMiles);
		
		// 3. social connectivity constraints
		// add additional constraint that is from another query
		//ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
		//userQuery.whereContainedIn(ParseUserHelper.KEY_FB_ID, friendIds);
		
		// 4. ride preference
		switch (ridePrefConstraint){
		case TripPosts.PREF_DRIVE:
			postQuery.whereEqualTo(TripPosts.KEY_RIDE_PREFERENCE,TripPosts.PREF_RIDE);
			break;
		case TripPosts.PREF_RIDE:
			postQuery.whereEqualTo(TripPosts.KEY_RIDE_PREFERENCE,TripPosts.PREF_DRIVE);
			break;
		case TripPosts.PREF_EITHER:
			//no constraints need to be added
		}
		
		/* add query constraints */
		postQuery.whereMatchesQuery(TripPosts.KEY_DESTINATION, destQuery);
		postQuery.whereMatchesQuery(TripPosts.KEY_ORIGIN, originQuery);
		//postQuery.whereMatchesQuery(TripPosts.KEY_OWNER, userQuery);
		
		
		/* join the other classes to this using include, 
		 * further joins can use the dot notation, e.g. "include("class1.class2") */
		postQuery.include(TripPosts.KEY_OWNER);
		postQuery.include(TripPosts.KEY_ORIGIN);
		postQuery.include(TripPosts.KEY_DESTINATION);
		/*
		 * run the query
		 */
		try {
			List<TripPosts> postList = postQuery.find();
			// If there are results, update the list of posts
	        // and notify the adapter
	    	mPosts.clear();
	        for (TripPosts post : postList) {
	        	// get the facebook id for displaying facebook profile pic later
	        	ParseUser postOwner = post.getOwner();
	        	String facebookId = ParseUserHelper.getFacebookId(postOwner);
	        	
	        	// collect the data to be display in the list of ride posts
	        	Map<String, Object> postItem = new HashMap<String, Object>();
	        	postItem.put(TripPosts.KEY_OWNER, post.getOwner());
	        	postItem.put(TripPosts.KEY_ORIGIN, post.getOrigin());
	        	postItem.put(TripPosts.KEY_DESTINATION, post.getDestination());
	        	postItem.put(TripPosts.KEY_DESCRIPTION, post.getDescription());
	        	postItem.put(TripPosts.KEY_DEPART_AT, post.getDepartAt());
	        	postItem.put(ParseUserHelper.KEY_FB_ID, facebookId);
	        	mPosts.add(postItem);
	        }
	        // make changes to the list in the background
	        mHandler.post(new Runnable() {				
				@Override
				public void run() {
					// only notify data change is needed, using 
					// addAll will have duplicate list items
					mPostAdapter.notifyDataSetChanged();
				}
			});
	        // also shows the posts on the map
	        loadedCallback.onPostListLoaded(postList);
	        
		} catch (ParseException e1) {
			Log.d("Post retrieval", "Error: " + e1.getMessage());
		}
	}
	
	/*
	 *  this will execute to fetch friend lists. This will not create
	 *  a background thread to execute the Facebook API call, so need 
	 *  to make sure the thread call this method is a background thread
	 */
	private void findAppFriendsOnFacebook(){
		Session session = ParseFacebookUtils.getSession();
        if (session!=null && session.isOpened()){
        	Request request = new Request(
        		session,
	        	"/me/friends",
	            null,
	            HttpMethod.GET,                 
	            new Request.Callback(){   
	        		// getting the response from Facebook graph API call
	                public void onCompleted(Response response) {
	                    //Log.i(TAG, "Result: " + response.toString());
	                	friendIds = new ArrayList<String>();
						try {
							JSONObject json = new JSONObject(response.getRawResponse());
							JSONArray jarray = json.getJSONArray("data");
		                    for(int i = 0; i < jarray.length(); i++){
		                    	JSONObject friendObject = jarray.getJSONObject(i);
		                    	String friendId = friendObject.getString("id");
		                    	friendIds.add(friendId);
		                    	
		                    }
						} catch (JSONException e) {
							e.printStackTrace();
						}
	                }                  
	        }); 
        	// this makes sure the queryRideLists method will wait for this to finish
	        request.executeAndWait();
	        
	        
        }
	}
	
	/*
	 * get mutual friends in background 
	 */
	private void getMutualFriendCountAsync(List<String> targetUserIds){
		Session session = ParseFacebookUtils.getSession();
        if (session!=null && session.isOpened() && targetUserIds.size() > 0){
        	Bundle params = new Bundle();
    		params.putString("fields", "context.fields(mutual_friends)");
    		List<Request> requests = new ArrayList<Request>(); 
    		/* make the API call */
    		for (final String targetUserId: targetUserIds){
    			requests.add( 
    				new Request(
		    		    session,
		    		    "/" + targetUserId,
		    		    params,
		    		    HttpMethod.GET,
		    		    new Request.Callback() {
		    		        public void onCompleted(Response response) {
		    		        	try {
									JSONObject json = new JSONObject(response.getRawResponse());
									JSONObject contextObject = json.getJSONObject("context");
									JSONObject mutualfrdObject = contextObject.getJSONObject("mutual_friends");
									JSONObject summaryObject = mutualfrdObject.getJSONObject("summary");
									int count = summaryObject.getInt("total_count");
									friendMutulFriends.put(targetUserId, count);
								} catch (JSONException e) {
									e.printStackTrace();
								}
		    		        }
		    		    }
		    		));
    			}
    		//Request.executeBatchAsync(requests);
    		Request.executeBatchAndWait(requests);
    		
        }
		
	}
	

}
