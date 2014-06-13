package com.rizzi.rizzi;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

/**
 * Login activity hosting only one login fragment, more fragments
 * can be added to this activity if login screen will change dynamically
 * according to current app state.
 * 
 * Currently using ParseFacebookUtils for easy log in
 *  
 * @author David
 *
 */
public class LoginActivity extends FragmentActivity {
	
	private static final String TAG = "LoginActivity";
	private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null){
        	loginFragment = new LoginFragment();
        	getSupportFragmentManager()
		        	.beginTransaction()
		        	.add(android.R.id.content, loginFragment)
		        	.commit();
        }else{
        	loginFragment = (LoginFragment) getSupportFragmentManager()
        					.findFragmentById(android.R.id.content);
        }
    }


    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/*
		 *  call the fragment's on activity result, because the ParseFacebookUtils.logIn
		 *  taks the activity class as input, it will return to the activity's onActivityResult
		 *  first after login, calling fragment's onActivityResult to trigger finishAuthentication 
		 */
		if (loginFragment != null){
			loginFragment.onActivityResult(requestCode, resultCode, data);
		}
	}


    /**
     * A fragment for login, this fragment is only used inside the LoginActivity class
     *
     */
    public static class LoginFragment extends Fragment {

    	private static final String TAG = "LoginFragment";
    	
    	private Button btnLogin;
    	private Dialog DlgProgress;
    	
    	Button btnLoad;
    	TextView tvUserInfo;

    	@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			// Check if there is a currently logged in user
    		// and they are linked to a Facebook account.
    		ParseUser currentUser = ParseUser.getCurrentUser();
    		if (currentUser!= null && ParseFacebookUtils.isLinked(currentUser)){
    			// Start a new Activity
    			showHomeActivity();
    		}
		}


		@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {    		
    		View view = inflater.inflate(R.layout.activity_login, container,false);		
    		
    		btnLogin = (Button) view.findViewById(R.id.activity_login_btnLogin);
    		btnLogin.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				onLoginButtonClicked();
    			}

    		});
    		
    		btnLoad = (Button) view.findViewById(R.id.activity_login_btnLoad);
    		//tvUserInfo = (TextView) view.findViewById(R.id.activity_login_tvUsrname);
    		
    		return view;
    	}
    	

    	protected void onLoginButtonClicked() {
    		DlgProgress = ProgressDialog.show(
    				getActivity(), "", "Please wait! Loggin in ...", true);
    		
    		List<String> fbPermissions = Arrays.asList("user_about_me",
    				"user_relationships", "user_birthday", "user_location");
    		ParseFacebookUtils.logIn(fbPermissions, getActivity(), new LogInCallback() {			
    			@Override
    			public void done(ParseUser user, ParseException err) {
    				DlgProgress.dismiss();
    				if (err != null){
    					Toast.makeText(getActivity(), 
    								   err.getMessage(), 
    								   Toast.LENGTH_LONG)
    								   .show();
    					return;
    				}
    				if (user== null){
    					// user cancelled, do something if necessary
    				}else{
    					if (user.isNew()){
    						// user signed up and logged in via FB
    					}
    					else{
    						// user logged in via FB
    					}
    					showHomeActivity();
    				}
    			}
    		});
    		
    	}

    	@Override
    	public void onViewCreated(View view, Bundle savedInstanceState) {
    		super.onViewCreated(view, savedInstanceState);
    		
    		btnLoad.setOnClickListener(new View.OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    			        Session session = Session.getActiveSession();
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
    				                public void onCompleted(Response response) {
    				                    Log.i(TAG, "Result: " + response.toString());
    				                }                  
    				        }); 
    				        //Request.executeBatchAsync(request);
    				        request.executeAsync();
    			        }
    			}
    		});
    	}

    	@Override
    	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    		super.onActivityResult(requestCode, resultCode, data);
    		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    	}
    	
    	private void showHomeActivity() {
    		Intent intent = new Intent(getActivity(), HomeActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    		startActivity(intent);
    	}
    	/*
    	private void showUserDetailsActivity() {
    		Intent intent = new Intent(getActivity(), UserProfileActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    		startActivity(intent);
    	}*/

    	/*
    	@Override
    	public void onDestroy() {
    		super.onDestroy();
    		uiHelper.onDestroy();
    	}

    	@Override
    	public void onPause() {
    		super.onPause();
    		uiHelper.onPause();
    	}

    	@Override
    	public void onResume() {
    		super.onResume();
    		// For scenarios where the main activity is launched and user
    	    // session is not null, the session state change notification
    	    // may not be triggered. Trigger it if it's open/closed.
    		Session session = Session.getActiveSession();
    		if (session != null && 
    				(session.isOpened()||session.isClosed()) ){
    			onSessionStateChange(session, session.getState(), null);
    		}
    		
    		uiHelper.onResume();
    	}

    	@Override
    	public void onSaveInstanceState(Bundle outState) {
    		super.onSaveInstanceState(outState);
    		uiHelper.onSaveInstanceState(outState);
    	}


    	private void onSessionStateChange(Session session, SessionState state, Exception exception){
    		
    		if (state.isOpened()){
    			// request for friends
    			Request.newMyFriendsRequest(session, 
    					new MyGraphUserListCallBack(session)
    					).executeAsync();	
    			// change to a new 
    		}else if (state.isClosed()){
    			tvUserInfo.setText("Please click the blue button to sign on to FB");
    		}
    	}
    	
    	private class SessionStatusCallback implements Session.StatusCallback{

    		@Override
    		public void call(Session session, SessionState state,
    				Exception exception) {
    			onSessionStateChange(session, state, exception);
    			
    		}
    	}

    	private String buildUserInfoDisplay(GraphUser user){
    		StringBuilder userInfo = new StringBuilder("");
    		
    		userInfo.append(String.format("Name: %s\n", user.getName()));
    		
    		userInfo.append(String.format("Birthday: %s\n", user.getBirthday()));
    		
    		userInfo.append(String.format("City: %s\n\n", user.getLocation()));
    		
    		JSONArray languages = (JSONArray) user.getProperty("languages");
    		if (languages != null && languages.length() > 0 ){
    			ArrayList<String> languageNames = new ArrayList<String>();
    			
    			GraphObjectList<MyGraphLanguage> graphMyLanguages = 
    					GraphObject.Factory.createList(languages, MyGraphLanguage.class);
    			for (MyGraphLanguage language : graphMyLanguages){
    				languageNames.add(language.getName());
    			}
    			userInfo.append(String.format("Languages: %s\n\n", languageNames.toString()));
    		}
    		
    		return userInfo.toString();
    	}
    	
    	private interface MyGraphLanguage extends GraphObject{
    		// Getter for the ID field
    		String getId();
    		
    		// Getter for the name field
    		String getName();
    	}
    	
    	private class MyGraphUserListCallBack implements GraphUserListCallback{
    		
    		private Session session;
    		
    		public MyGraphUserListCallBack(Session session){
    			this.session = session;
    		}

    		@Override
    		public void onCompleted(List<GraphUser> users, Response response) {
    			
    			Bundle params = new Bundle();
    			params.putString("fields", "picture.type(large)," +
    							 "context.fields(mutual_friends)");
    			RequestBatch requestBatch = new RequestBatch();					
    			tvUserInfo.setText("");
    			if (users.size() >0 ){
    				for (GraphUser user : users){
    					requestBatch
    					.add(new Request(
    						this.session,
    						user.getId(),
    						params,
    						HttpMethod.GET,
    						new Request.Callback() {
    							
    							@Override
    							public void onCompleted(Response response) {
    								Log.i(TAG, "Result: " + response.toString());
    								
    							}
    						}));
    					tvUserInfo.append(buildUserInfoDisplay(user));
    				}
    				requestBatch.executeAsync();
    			}
    		}
    		
    	}
    	*/
    }


}
