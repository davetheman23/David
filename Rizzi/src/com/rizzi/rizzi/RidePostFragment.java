package com.rizzi.rizzi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rizzi.rizzi.parseclasses.CustomGeoPoints;
import com.rizzi.rizzi.parseclasses.ParseUserHelper;
import com.rizzi.rizzi.parseclasses.TripPosts;
import com.rizzi.rizzi.utils.LocationUtils;
import com.rizzi.rizzi.utils.App;

/**
 * define a pop-up dialog box for user to confirm ride posting. 
 * 
 */
public class RidePostFragment extends Fragment{
	private static final String TAG = "PostRide";
	
	private Location mFromLocation, mToLocation;
	private List<ParseObject> mPostObjects = null;
	private RadioButton rb_ride, rb_either, rb_drive;
	private TextView tv_FromAddress = null;
	private TextView tv_ToAddress = null;
	private ProfilePictureView userProfilePictureView = null;
	private Spinner sp_StartTime = null;
	
	private static final long MILLISECONDS_IN_SECOND = 1000;
	private static final long SECONDS_IN_MINUTE = 60;
	
	// the default value for depart time flexibility
	private static final int DEFAULT_FLEXIBILITY_IN_MIN = 15;
	
	//private final String addressFormat = Resources.getSystem()
	//							.getString(R.string.address_output_string);
	private final String addressFormat = "%1$s, %2$s, %3$s";

	public void setParameters(Location fromLocation, Location toLocation){
		mFromLocation = fromLocation;
		mToLocation = toLocation;
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	

	View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (rootView == null) {
			// to avoid the error "The specified child already has a parent", make sure
			// to set the attachToRoot to false
			rootView = inflater.inflate(R.layout.frag_ridepost_post, container,false);
		} else {
		    ((ViewGroup) rootView.getParent()).removeView(rootView);
		}
		
		/*
		 * get references for all the widgets on the dialog box
		 */
		userProfilePictureView = (ProfilePictureView) rootView
									.findViewById(R.id.frag_ridepost_user_pic);
		tv_FromAddress = (TextView) rootView.findViewById(
											R.id.frag_ridepost_tv_from_address); 
		tv_ToAddress = (TextView) rootView.findViewById(
											R.id.frag_ridepost_tv_to_address);
		sp_StartTime = (Spinner) rootView.findViewById(R.id.frag_ridepost_start_time);
		rb_ride = (RadioButton)rootView.findViewById(R.id.frag_ridepost_radio_ride);
		rb_either = (RadioButton)rootView.findViewById(R.id.frag_ridepost_radio_either);
		rb_drive = (RadioButton)rootView.findViewById(R.id.frag_ridepost_radio_drive);
		
		// initialize address boxes
		tv_FromAddress.setText("Loading Address ...");
		tv_ToAddress.setText("Loading Address ...");
		// get address of origin in a background thread, display once done
		new LocationUtils.GetAddressTask(App.appContext, addressFormat) {
			@Override
			protected void onPostExecute(String formmatedAddress) {
				tv_ToAddress.setText(formmatedAddress);
				
			}
		}.execute(mToLocation);
		
		// get address of destination in background thread, display once done
		new LocationUtils.GetAddressTask(getActivity(), addressFormat) {
			@Override
			protected void onPostExecute(String formmatedAddress) {
				tv_FromAddress.setText(formmatedAddress);
			}
		}.execute(mFromLocation);
		
		// get the the current Parseuser, 
		ParseUser currentUser = ParseUser.getCurrentUser();
		// here require the Parseuser has a profile field that is saved once
		// the user first logged in
		if (currentUser.get(ParseUserHelper.KEY_PROFILE) != null) {
			JSONObject userProfile = currentUser.getJSONObject(
											ParseUserHelper.KEY_PROFILE);
			try {
				if (userProfile.getString(ParseUserHelper.KEY_FB_ID) != null) {
					String facebookId = userProfile.get(ParseUserHelper.KEY_FB_ID)
							.toString();
					userProfilePictureView.setProfileId(facebookId);
				}else{
					userProfilePictureView.setProfileId(null);
				}
			} catch (JSONException e) {
				Log.d(TAG,"Error parsing saved user data.");
			}
		}
		
		// set a listener for the post button
		Button postButton = (Button) rootView.findViewById(
											R.id.frag_ridepost_post);
		postButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				mPostObjects = getPostData();

				if (mPostObjects == null){
					return;
				}
				/* 
				 * Note: saveAllinBAckground will limit the number of requests made
				 * to the server, 
				 */
				ParseObject.saveAllInBackground(mPostObjects, new SaveCallback(){
					@Override
					public void done(ParseException e) {
						String message ="Post Succeeded";
						if (e != null){
							message = e.getMessage();
						}
						Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
					}
				});
			}
		});
		return rootView;
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

	/**
	 * fetch from the ui the user inputs for a ride post
	 * @return a list object that can be used in 
	 *         {@link ParseObject#saveAllInBackground(List)}
	 */
	private List<ParseObject> getPostData(){
		// get the trip planned start time
		Date departTime = null;
		int startTimeSelPos = sp_StartTime.getSelectedItemPosition();
		if (startTimeSelPos == Spinner.INVALID_POSITION){
			// TODO alert user to select a start time
		}else{
			departTime = getStartTimeinDate(startTimeSelPos);
		}
		
		// get the depart time flexibility
		// TODO this should be obtained from user input
		Long felxibility_before = DEFAULT_FLEXIBILITY_IN_MIN * SECONDS_IN_MINUTE 
														* MILLISECONDS_IN_SECOND;
		Long felxibility_after = felxibility_before;
		Date departRangeBegin = new Date(departTime.getTime()-felxibility_before);
		Date departRangeEnd = new Date(departTime.getTime()+felxibility_after);
		
		// get the ride preference
		int ridePreference = TripPosts.PREF_EITHER;
		if (rb_drive.isChecked()){
			ridePreference = TripPosts.PREF_DRIVE;
		}else if (rb_ride.isChecked()){
			ridePreference = TripPosts.PREF_RIDE;
		}
		
		// set a geopoint object to and its wrapper class
		final ParseGeoPoint orig = CustomGeoPoints
							.getParseGeoPointFromLocation(mFromLocation);
		CustomGeoPoints _orig_t = new CustomGeoPoints();
		_orig_t.setGeoPoint(orig);
		_orig_t.setLocationType(CustomGeoPoints.TYPE_ORIGIN);
		final ParseGeoPoint dest = CustomGeoPoints
							.getParseGeoPointFromLocation(mToLocation);
		CustomGeoPoints _dest_t = new CustomGeoPoints();
		_dest_t.setGeoPoint(dest);
		_dest_t.setLocationType(CustomGeoPoints.TYPE_DESTINATION);
		
		/*
		 * create a ride post class, the class needs to be defined in its own
		 * java file and needs to be registered in RizziApplication.java 
		 */
		TripPosts ridePosts = new TripPosts();
		ridePosts.setOwner(ParseUser.getCurrentUser());
		ridePosts.setOrigin(_orig_t);
		ridePosts.setDestination(_dest_t);
		ridePosts.setDepartAt(departTime);
		ridePosts.setDepartBegin(departRangeBegin);
		ridePosts.setDepartEnd(departRangeEnd);
		ridePosts.setDescription(" Hello, I just want to go shopping! ");
		ridePosts.setRidePreference(ridePreference);
		// set read/write permission for this ride post record
		ParseACL acl = new ParseACL();
		 acl.setPublicReadAccess(true);
		ridePosts.setACL(acl);
		
		// create list to save all Parse objects at the same time
		List<ParseObject> postObjects = new ArrayList<ParseObject>();
		//postObjects.add(_orig_t);
		//postObjects.add(_dest_t);
		postObjects.add(ridePosts);
		
		return postObjects;
	}
	
	/**
	 * get a start time based on the spinner selection 
	 * @param position the item selection (starting from 0)
	 * @return a date of start time/leave time from origin location
	 */
	private Date getStartTimeinDate(int position){
		int[] options = getResources().getIntArray(
								R.array.depart_time_options_in_mins);
		long millisecFromNow = options[position] * SECONDS_IN_MINUTE 
											* MILLISECONDS_IN_SECOND;
		Date now = Calendar.getInstance().getTime();
		Date starttime = new Date(now.getTime() + millisecFromNow);
		return starttime;
	}		
	
}
