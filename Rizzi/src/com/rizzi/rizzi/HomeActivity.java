package com.rizzi.rizzi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.rizzi.rizzi.parseclasses.CustomGeoPoints;
import com.rizzi.rizzi.parseclasses.TripPosts;
import com.rizzi.rizzi.utils.App;
import com.rizzi.rizzi.utils.LocationUtils;

public class HomeActivity extends FragmentActivity implements 
	com.google.android.gms.location.LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener,
	RideListFragment.onPostListLoadedListener{
	
	private static final String TAG = "HomeActivity";
	
	/*
	 * Define a request code to send to Google Play services This code is returned in
	 * Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	/*
	 * google map objects 
	 */
	// the map itself
	private GoogleMap mGoogleMap;
	// the destination marker for the current user
	private Marker mMyDestMarker = null;
	// the origin and destination markers from other users
	private List<Marker> mDestMarkers = new ArrayList<Marker>();
	private List<Marker> mOriginMarkers = new ArrayList<Marker>();
	
	private final static int MARKER_TYPE_ORIGIN = 1;
	private final static int MARKER_TYPE_DESTINATION = 2;
	
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
		mGoogleMap.setOnMapLongClickListener(longClickListener);
		
		MapsInitializer.initialize(this);
		
		
		//  Create a new global location parameters object, and set request params
		 
		mLocationRequest = LocationRequest.create();
		
		mLocationRequest.setSmallestDisplacement(LocationUtils
										.LOCATION_UPDATE_DISTANCE_METERS);
		mLocationRequest.setInterval(LocationUtils
										.UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(LocationUtils
										.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		
		//Create a new location client, using the enclosing class to handle callbacks
		 
		mLocationClient = new LocationClient(this, this, this);
		
	}
	
	OnMapLongClickListener longClickListener = new OnMapLongClickListener() {
		private PopupDialogFragment popupDialog = null;
		
		@Override
		public void onMapLongClick(LatLng point) {
			
			// the the current user location as the origin
			Location origin = (mCurrentLocation == null) ? 
										mLastLocation : mCurrentLocation;
			if (origin == null){
				Toast.makeText(HomeActivity.this,
			            "Please try again after your location appears on the map.", 
			            Toast.LENGTH_LONG)
			            .show();
			        return;
			}
			
			// obtain a location object where the user clicked as destination
			Location destination = new Location(App.APPTAG);
			destination.setLatitude(point.latitude);
			destination.setLongitude(point.longitude);
			destination.setTime(new Date().getTime());
			
			// add or move the marker on the map 
			if (mMyDestMarker == null){
				mMyDestMarker = mGoogleMap.addMarker(
						new MarkerOptions()
    						.position(point)
    						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_pin))
    						.title(point.toString())
    						);
				mMyDestMarker.setDraggable(true);
			}else{
				mMyDestMarker.setPosition(point);
			}
			
			// create a new post list fragment
			RideListFragment postListFragment = new RideListFragment();
			
			// create a new ride post fragment
			RidePostFragment postFragment = new RidePostFragment();
			postFragment.setParameters(origin, destination);
			
			// create a dialog fragment to host both fragments
			popupDialog = new PopupDialogFragment();
			popupDialog.setFragments(postFragment, postListFragment);
			popupDialog.show(getSupportFragmentManager(), TAG);
		}
	};

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
			//stopLocationUpdates();
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
    		
    		// Get the current location
            Location currentLocation = mLocationClient.getLastLocation();
            
            if (currentLocation != null ){
	            final LatLng CIU = new LatLng(currentLocation.getLatitude(),
	            							currentLocation.getLongitude());
	            
	            // enable current location layer
	    		if (mGoogleMap != null){
	    			mGoogleMap.setMyLocationEnabled(true);
	    		}
	
	            // add a marker to the location
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
		//startLocationUpdates();
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
			if (CustomGeoPoints.getDistanceBetweenInMeters(
					mCurrentLocation, mLastLocation)<10){
				return;
			}
		}
		mLastLocation = location;
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
	 * This method will be called when the post list is loaded 
	 * in the ride list fragment, and markers will be shown in the map.
	 */
	@Override
	public void onPostListLoaded(final List<TripPosts> posts) {
		// markers need to be added in the UI threads only
		HomeActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				clearMarkers(MARKER_TYPE_DESTINATION);
				for (TripPosts post: posts){
					// add pins on the map, the pins are the destinations
					mDestMarkers.add(mGoogleMap.addMarker(
							new MarkerOptions()
								.position((post.getDestination().getLatlng()))
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_pin_dest_matched))
								.title(post.get(TripPosts.KEY_DESCRIPTION).toString())
								)
					);
					
				}
			}
		});
		
	}
	
	/*
	 * clear markers on the map by its types
	 */
	private void clearMarkers(int type){
		switch (type){
		case MARKER_TYPE_ORIGIN:
			for (Marker marker : mOriginMarkers){
				marker.remove();
			}
			break;
		case MARKER_TYPE_DESTINATION:
			for (Marker marker : mDestMarkers){
				marker.remove();
			}
			break;
		}
	}
	
	

}
