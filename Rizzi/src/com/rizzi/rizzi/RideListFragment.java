package com.rizzi.rizzi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rizzi.rizzi.parseclasses.ParseUserHelper;
import com.rizzi.rizzi.parseclasses.TripPosts;

public class RideListFragment extends ListFragment {
	
	RideListAdapter mAdapter = null;
	
	View rootView = null;
	
	List<Map<String, Object>> mPosts = new ArrayList<Map<String, Object>>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (rootView == null){
			// to avoid the error "The specified child already has a parent", make sure
			// to set the attachToRoot to false
			rootView = inflater.inflate(R.layout.frag_ridepost_list, container, false);
		}else{
			((ViewGroup) rootView.getParent()).removeView(rootView);
		}
		
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
	
	private void updatePostList() {
	  // Create query for objects of type "TripPosts"
	  ParseQuery<TripPosts> query = TripPosts.getQuery();
	  
	  // adding owner constraint
	  //query.whereEqualTo(TripPosts.KEY_OWNER, ParseUser.getCurrentUser());
	  
	  // adding time constraints
	  Date now = Calendar.getInstance().getTime();
	  query.whereLessThanOrEqualTo(TripPosts.KEY_DEPART_TIMERANGE_BEGIN, now);
	  query.whereGreaterThanOrEqualTo(TripPosts.KEY_DEPART_TIMERANGE_END, now);
	  
	  /* join the other classes to this using include, 
	   * further joins can use the dot notation, e.g. "include("class1.class2") */
	  query.include(TripPosts.KEY_OWNER);
	  
	  // Run the query  
	  query.findInBackground(new FindCallback<TripPosts>() {
	    @Override
	    public void done(List<TripPosts> postList,
	        ParseException e) {
	      if (e == null) {
	        // If there are results, update the list of posts
	        // and notify the adapter
	    	mPosts.clear();
	        for (TripPosts post : postList) {
	        	ParseUser postOwner = post.getOwner();
	        	String facebookId = ParseUserHelper.getFacebookId(postOwner);
	        	Map<String, Object> postItem = new HashMap<String, Object>();
	        	postItem.put(TripPosts.KEY_OWNER, post.getOwner());
	        	postItem.put(TripPosts.KEY_ORIGIN, post.getOrigin());
	        	postItem.put(TripPosts.KEY_DESCRIPTION, post.getDescription());
	        	postItem.put(TripPosts.KEY_DEPART_AT, post.getDepartAt());
	        	postItem.put(ParseUserHelper.KEY_FB_ID, facebookId);
	        	mPosts.add(postItem);
	        }
	 
	        RideListAdapter adapter = new RideListAdapter(getActivity(), 
					R.layout.list_post_item, mPosts);
	        getListView().setAdapter(adapter);
	        //((ArrayAdapter<Map<String,Object>>)getListAdapter()).notifyDataSetChanged();
	      } else {
	        Log.d("Post retrieval", "Error: " + e.getMessage());
	      }
	    }
	                     
	  });
	             
	}
	
	@Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rootView != null) {
            ViewGroup parentViewGroup = (ViewGroup) rootView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
    }

}
