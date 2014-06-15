package com.rizzi.rizzi;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.CameraPositionCreator;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rizzi.rizzi.utils.LocationUtils;
import com.rizzi.rizzi.utils.ModelRidePosts;
import com.rizzi.rizzi.utils.RizziApplication;

public class HomeActivity extends Activity implements 
	com.google.android.gms.location.LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener{
	
	private static final String TAG = "HomeActivity";
	
	/*
	 * Define a request code to send to Google Play services This code is returned in
	 * Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	/*
	 * a google map object 
	 */
	private GoogleMap mGoogleMap;
	
	/*
	 * manage the Location Services connection and callbacks
	 */
	private LocationClient mLocationClient;
	
	/*
	 * hold the location update parameters used by the LocationClient instance
	 */
	private LocationRequest mLocationRequest;
	
	/*
	 *	Location variables that hold current and last location respectively 
	 */
	private Location mCurrentLocation;
	private Location mLastLocation;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		/*
		 *  get a reference to the google map object
		 */
		mGoogleMap = ((MapFragment) getFragmentManager()
						.findFragmentById(R.id.activity_home_mappane))
						.getMap();
		
		
		// define actions for camera change
		mGoogleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition arg0) {
				// TODO e.g. Run the map query, or other actions
				
			}
		});
		
		// set a map long click listener
		mGoogleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			private PostPopupFragment postDialog = null;
			
			@Override
			public void onMapLongClick(LatLng point) {
				
				Location destination = new Location(RizziApplication.APPTAG);
				destination.setLatitude(point.latitude);
				destination.setLongitude(point.longitude);
				destination.setTime(new Date().getTime());
				
				Location origin = (mCurrentLocation == null) ? 
											mLastLocation : mCurrentLocation;
				if (origin == null){
					Toast.makeText(HomeActivity.this,
				            "Please try again after your location appears on the map.", 
				            Toast.LENGTH_LONG)
				            .show();
				        return;
				}
				
				postDialog = new PostPopupFragment();
				
				postDialog.setParameters(origin, destination);
				
				postDialog.show(getFragmentManager(), TAG);

				
				final ParseGeoPoint orig = getParseGeoPointFromLocation(origin);
				
				final ParseGeoPoint dest = getParseGeoPointFromLocation(destination);

				ModelRidePosts ridePosts = new ModelRidePosts();
				ridePosts.setUser(ParseUser.getCurrentUser());
				ridePosts.setRideSharer1(ParseUser.getCurrentUser());
				ridePosts.setOrigin(orig);
				//ridePosts.setDestination(dest);
				
				ParseACL acl = new ParseACL();
				 acl.setPublicReadAccess(true);
				 ridePosts.setACL(acl);
				ridePosts.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e != null){
							Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
						}
					}
				});;
			}
		});
		
		/*
		 *  Create a new global location parameters object, and set request params
		 */
		mLocationRequest = LocationRequest.create();
		
		mLocationRequest.setSmallestDisplacement(LocationUtils
										.LOCATION_UPDATE_DISTANCE_METERS);
		mLocationRequest.setInterval(LocationUtils
										.UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(LocationUtils
										.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		/*
		 *  Create a new location client, using the enclosing class to handle callbacks
		 */
		mLocationClient = new LocationClient(this, this, this);
		
	}
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.home_menu_log_out:
				logoutUser();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}



	@Override
	protected void onResume() {
		super.onResume();
		ParseUser user = ParseUser.getCurrentUser();
		if (user == null || !ParseFacebookUtils.isLinked(user)){
			logoutUser();
		}
		
	}
	

	@Override
	protected void onStop() {
		super.onStop();
		if (mLocationClient.isConnected()){
			stopLocationUpdates();
		}
		mLocationClient.disconnect();
	}
	

	
	/**
	   * Handle results returned to this Activity by other Activities started with
	   * startActivityForResult(). In particular, the method onConnectionFailed() in
	   * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to start
	   * an Activity that handles Google Play services problems. The result of this call returns here,
	   * to onActivityResult.
	   */
    @Override
	protected void onActivityResult(int requestCode, 
									int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Decide what to do based on the original request code
		switch (requestCode){
		/*
         * In the case the request was by google play service connection
         */
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			switch (resultCode){
			case Activity.RESULT_OK:
				
			}
		}
	}

    /**
     * Logout User and go back to the log in activity
     */
    private void logoutUser(){
    	ParseUser.logOut();
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected(){
    	//check if Google Play services is available
    	int resultCode = GooglePlayServicesUtil
    							.isGooglePlayServicesAvailable(this);
    	
    	// if it is available
    	if (resultCode == ConnectionResult.SUCCESS){
    		return true;
    	}else{
    	// if it isn't available, then error 
    		showErrorDialog(resultCode);
    		return false;
    	}
    }

    /**
     * get last known location of the device,  
     * @param animateToLocationWhenFound	
     */
    public void findMyLocation(boolean animateToLocationWhenFound){
    	
    	// If Google Play services is available
    	if (servicesConnected() && mLocationClient.isConnected()){
    		
    		// enable current location layer
    		if (mGoogleMap != null){
    			mGoogleMap.setMyLocationEnabled(true);
    		}
    		
    		// Get the current location
            Location currentLocation = mLocationClient.getLastLocation();
            
            if (currentLocation != null ){
            final LatLng CIU = new LatLng(currentLocation.getLatitude(),
            							currentLocation.getLongitude());

            // add a marker to the location
            //mMyLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
            //						.position(CIU).title(CIU.toString()));
            CameraPosition cameraPosition 
	    		= new Builder()
	    			.target(CIU)
	    			.zoom(15)
	    			.tilt(45)
	    			.build();
            CameraUpdate camUpdate = CameraUpdateFactory
            				.newCameraPosition(cameraPosition);
            	//move map camera to my location, either animate or non-animate 
	            if(animateToLocationWhenFound){
	            	mGoogleMap.animateCamera(camUpdate);
	            }else{
	                mGoogleMap.moveCamera(camUpdate);
	            }
            }
    		
    		// -- Get an address from the current location,
    		// step 2: get the address in the background 
    		//(new GetAddressTask(this)).execute(currentLocation);
    		// step 3: set the address text to mMyLocationMarker, 
            // which is done after execute defined in the GetAddressTask class
    	}
    }
    
    private void startLocationUpdates() {
    	mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}
	 
	private void stopLocationUpdates() {
		mLocationClient.removeLocationUpdates(this);
	}
	 
	/**
     * get the last known location
     *
     * @return the location object
     */
    public Location getLocation() {

        // If Google Play Services is available
    	if (servicesConnected()) {
    	    return mLocationClient.getLastLocation();
		} else {
			return null;
		}
    }

    /**
     * Get the address of the current location, using reverse geocoding. This only works if
     * a geocoding service is available.
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getAddress(View v) {

        // In Gingerbread (API-9) and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (!Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, "R.string.no_geocoder_available", Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {
            // Get the current location
            //Location currentLocation = mLocationClient.getLastLocation();
        	Location currentLocation = mCurrentLocation;

            // Turn the indefinite activity indicator on
            //mActivityIndicator.setVisibility(View.VISIBLE);

            // Start the background task
            (new LocationUtils.GetAddressTask(this, getString(R.string.address_output_string)){
				@Override
				protected void onPostExecute(String formmatedAddress) {
					// do something here once the address is returned
				}
            }).execute(currentLocation);
        }
    }
    
    
	/*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()){
			/*
	         * Google Play services can resolve some errors it detects.
	         * If the error has a resolution, try sending an Intent to
	         * start a Google Play services activity that can resolve
	         * error.
	         */
			try {
				result.startResolutionForResult(this, 
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
			
		}else{
			//option 1: show dialog fragment
			showErrorDialog(result.getErrorCode());
			// option 2: just show a toast
			Toast.makeText(
					this, 
					LocationUtils.ErrorMessages.getErrorString(this, result.getErrorCode()), 
					Toast.LENGTH_LONG)
					.show();
		}
	}

	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
	@Override
	public void onConnected(Bundle bundle) {
		mCurrentLocation = getLocation();
		startLocationUpdates();
		findMyLocation(false);
	}

	/*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
	@Override
	public void onDisconnected() {	
	}

	/*
     * Called by LocationListener, trigger events when system location 
     * has changed. 
     */
	@Override
	public void onLocationChanged(Location location) {
		mCurrentLocation = location;
		if (mLastLocation != null){
			// if last distance between the two locations 
			if (getDistanceBetweenInMeters(mCurrentLocation, mLastLocation)<10){
				return;
			}
		}
		mLastLocation = location;
	}	
	
	private float getDistanceBetweenInMeters(Location start, Location end){
		float[] results = new float[]{-1,0,0};
		Location.distanceBetween(mCurrentLocation.getLatitude(), 
				 mCurrentLocation.getLongitude(),
				 mLastLocation.getLatitude(),
				 mLastLocation.getLongitude(),
				 results);
		Log.d(TAG, "distance between start and end locations are: " + results[0]);
		return results[0];
	}
	
	private ParseGeoPoint getParseGeoPointFromLocation(Location location){
		return new ParseGeoPoint(mCurrentLocation.getLatitude(), 
								mCurrentLocation.getLongitude());
	}
	
	/**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
	  private void showErrorDialog(int errorCode) {
	    // Get the error dialog from Google Play services
	    Dialog errorDialog =
	        GooglePlayServicesUtil.getErrorDialog(errorCode, this,
	            CONNECTION_FAILURE_RESOLUTION_REQUEST);

	    // If Google Play services can provide an error dialog
	    if (errorDialog != null) {

	      // Create a new DialogFragment in which to show the error dialog
	      ErrorDialogFragment errorFragment = new ErrorDialogFragment();

	      // Set the dialog in the DialogFragment
	      errorFragment.setDialog(errorDialog);

	      // Show the error dialog in the DialogFragment
	      errorFragment.show(getFragmentManager(), "Location Updates");
	    }
	  }

	/**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
	public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;
     
        public ErrorDialogFragment() {
          super();
          mDialog = null;
        }
     
        public void setDialog(Dialog dialog) {
          mDialog = dialog;
        }
     
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
          return mDialog;
        }
    }
	
	/**
	 * define a pop-up dialog box for user to confirm ride posting. 
	 * 
	 */
	public static class PostPopupFragment extends DialogFragment{
		private Location mFromLocation, mToLocation;
		private TextView tv_FromAddress = null;
		private TextView tv_ToAddress = null;
		private ProfilePictureView userProfilePictureView = null;
		
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

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			/*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder
				.setPositiveButton("Post", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				});
			
			AlertDialog alertDialog = builder.create();
			
			final Dialog dialog = (Dialog)alertDialog;*/
			
			final Dialog dialog = new Dialog(getActivity());
			
			dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
										WindowManager.LayoutParams.FLAG_FULLSCREEN);
			dialog.getWindow().setContentView(R.layout.frag_ridepost_p1);
			
			
			dialog.show();
			
			
			
			userProfilePictureView = (ProfilePictureView) dialog
										.findViewById(R.id.frag_ridepost_user_pic);
			tv_FromAddress = (TextView) dialog.findViewById(
												R.id.frag_ridepsot_tv_from_address); 
			tv_ToAddress = (TextView) dialog.findViewById(
												R.id.frag_ridepsot_tv_to_address);
			tv_FromAddress.setText("Loading Address ...");
			tv_ToAddress.setText("Loading Address ...");
			
			new LocationUtils.GetAddressTask(getActivity(), addressFormat) {
				@Override
				protected void onPostExecute(String formmatedAddress) {
					tv_ToAddress.setText(formmatedAddress);
					
				}
			}.execute(mToLocation);
			
			new LocationUtils.GetAddressTask(getActivity(), addressFormat) {
				@Override
				protected void onPostExecute(String formmatedAddress) {
					tv_FromAddress.setText(formmatedAddress);
				}
			}.execute(mFromLocation);
			
			
			ParseUser currentUser = ParseUser.getCurrentUser();
			if (currentUser.get("profile") != null) {
				JSONObject userProfile = currentUser.getJSONObject("profile");
				try {
					if (userProfile.getString("facebookId") != null) {
						String facebookId = userProfile.get("facebookId")
								.toString();
						userProfilePictureView.setProfileId(facebookId);
					}else{
						userProfilePictureView.setProfileId(null);
					}
				} catch (JSONException e) {
					Log.d(TAG,"Error parsing saved user data.");
				}
			}
			
			return dialog;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		
		
	}

}
